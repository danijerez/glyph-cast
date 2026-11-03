package app.glyphcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Ticker extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    ((ToyService) context).tick();
  }
}
