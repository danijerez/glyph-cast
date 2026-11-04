package app.glyphcast;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.nothing.ketchum.Glyph;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphMatrixFrame;
import com.nothing.ketchum.GlyphMatrixManager;
import com.nothing.ketchum.GlyphMatrixObject;
import com.nothing.ketchum.GlyphToy;

public class ToyService extends Service implements GlyphMatrixManager.Callback {
  private final Handler handler = new Handler(Looper.getMainLooper(), this::onMessage);
  private final Messenger messenger = new Messenger(handler);
  private final Ticker ticker = new Ticker();
  private final SharedPreferences.OnSharedPreferenceChangeListener changed = (prefs, key) -> render();
  private GlyphMatrixManager manager;
  private boolean connected;
  private static final long DETAIL = 5000;
  private final Runnable animate = this::render;
  private final Runnable hideDetail = () -> {
    detail = false;
    render();
  };
  private Pocket pocket;
  private boolean fetching;
  private boolean detail;

  @Override
  public IBinder onBind(Intent intent) {
    IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
    filter.addAction(Intent.ACTION_TIME_CHANGED);
    filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    registerReceiver(ticker, filter);
    Weather.prefs(this).registerOnSharedPreferenceChangeListener(changed);
    pocket = new Pocket(getSystemService(SensorManager.class), this::render);
    pocket.start();
    manager = GlyphMatrixManager.getInstance(getApplicationContext());
    manager.init(this);
    return messenger.getBinder();
  }

  @Override
  public boolean onUnbind(Intent intent) {
    unregisterReceiver(ticker);
    handler.removeCallbacks(animate);
    handler.removeCallbacks(hideDetail);
    pocket.stop();
    Weather.prefs(this).unregisterOnSharedPreferenceChangeListener(changed);
    connected = false;
    manager.turnOff();
    manager.unInit();
    manager = null;
    return false;
  }

  @Override
  public void onServiceConnected(ComponentName name) {
    handler.post(() -> {
      if (manager == null) return;
      manager.register(Glyph.DEVICE_23112);
      connected = true;
      tick();
    });
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    connected = false;
  }

  void tick() {
    pocket.check();
    render();
    if (fetching || !Weather.stale(this)) return;
    fetching = true;
    new Thread(() -> {
      Weather.refresh(this);
      handler.post(() -> {
        fetching = false;
        render();
      });
    }).start();
  }

  private boolean onMessage(Message message) {
    if (message.what != GlyphToy.MSG_GLYPH_TOY) return false;
    String event = message.getData().getString(GlyphToy.MSG_GLYPH_TOY_DATA);
    if (GlyphToy.EVENT_AOD.equals(event)) tick();
    if (GlyphToy.EVENT_CHANGE.equals(event) && Weather.fresh(this)) {
      detail = true;
      render();
      handler.removeCallbacks(hideDetail);
      handler.postDelayed(hideDetail, DETAIL);
    }
    return true;
  }

  private void render() {
    handler.removeCallbacks(animate);
    if (!connected) return;
    boolean hidden = pocket.inside() && Weather.prefs(this).getBoolean(Weather.POCKET, true);
    long interval = hidden || detail ? 0 : Face.interval(Weather.scene(this));
    if (interval > 0) handler.postDelayed(animate, interval - System.currentTimeMillis() % interval);
    else if (!hidden && !detail && Weather.sampleLeft(this) > 0) handler.postDelayed(animate, Weather.sampleLeft(this));
    int[] levels = hidden ? new int[Face.SIZE * Face.SIZE] : detail ? Weather.detail(this) : Weather.frame(this);
    Bitmap bitmap = Bitmap.createBitmap(Face.SIZE, Face.SIZE, Bitmap.Config.ARGB_8888);
    for (int i = 0; i < levels.length; i++) {
      bitmap.setPixel(i % Face.SIZE, i / Face.SIZE, Color.rgb(levels[i], levels[i], levels[i]));
    }
    try {
      GlyphMatrixObject object = new GlyphMatrixObject.Builder().setImageSource(bitmap).build();
      manager.setMatrixFrame(new GlyphMatrixFrame.Builder().addTop(object).build(this).render());
    } catch (GlyphException | RuntimeException rejected) {
      Log.w("GlyphCast", "Frame rejected", rejected);
    }
  }
}
