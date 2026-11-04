package app.glyphcast;

import android.app.Activity;
import android.app.LocaleManager;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
  private static final int BACKGROUND = Color.BLACK;
  private static final int CARD = 0xFF141414;
  private static final int FIELD = 0xFF222222;
  private static final int TEXT = Color.WHITE;
  private static final int MUTED = 0xFF8C8C8C;
  private static final int ACCENT = 0xFFD71921;
  private static final int GAP = 16;
  private static final int RADIUS = 20;
  private static final int CONTROL = 48;
  private static final int PREVIEW = 260;
  private static final int MIN_BRIGHTNESS = 20;
  private static final long REDRAW = 15000;
  private static final int ICON = 24;
  private static final int PILL_ICON = 18;
  private static final int SAMPLE_COLUMNS = 4;
  private static final String[] LANGUAGES = {"", "es", "en", "zh-CN"};

  private final Handler handler = new Handler(Looper.getMainLooper());
  private final Runnable redraw = this::redraw;
  private final SharedPreferences.OnSharedPreferenceChangeListener changed = (shared, key) -> redraw();
  private SharedPreferences prefs;
  private MatrixView preview;
  private EditText city;
  private LinearLayout results;
  private TextView status;
  private TextView updated;
  private boolean resumed;

  @Override
  protected void onCreate(Bundle state) {
    super.onCreate(state);
    prefs = Weather.prefs(this);
    getWindow().setStatusBarColor(BACKGROUND);
    getWindow().setNavigationBarColor(BACKGROUND);

    LinearLayout page = new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setPadding(dp(GAP + 4), dp(GAP * 2), dp(GAP + 4), dp(GAP * 2));

    page.addView(text(getString(R.string.app_name), 30, TEXT, true));

    preview = new MatrixView(this);
    LinearLayout.LayoutParams previewSize = new LinearLayout.LayoutParams(dp(PREVIEW), dp(PREVIEW));
    previewSize.gravity = Gravity.CENTER_HORIZONTAL;
    previewSize.topMargin = dp(GAP * 2);
    previewSize.bottomMargin = dp(GAP);
    page.addView(preview, previewSize);

    LinearLayout search = row();
    city = new EditText(this);
    city.setHint(R.string.city);
    city.setSingleLine();
    city.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
    city.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
    city.setTextColor(TEXT);
    city.setHintTextColor(MUTED);
    city.setBackground(round(FIELD, CONTROL / 2));
    city.setPadding(dp(GAP), 0, dp(GAP), 0);
    city.setText(prefs.getString("place", ""));
    city.setOnEditorActionListener((view, action, event) -> {
      search();
      return true;
    });
    search.addView(city, new LinearLayout.LayoutParams(0, dp(CONTROL), 1));
    ImageView find = button(R.drawable.ic_search, R.string.search, ACCENT);
    find.setOnClickListener(view -> search());
    search.addView(find, square(GAP / 2));
    page.addView(search);

    results = new LinearLayout(this);
    results.setOrientation(LinearLayout.VERTICAL);
    results.setBackground(round(CARD, RADIUS));
    page.addView(results, margins(match(), GAP / 2));

    status = text("", 13, MUTED, false);
    status.setVisibility(View.GONE);
    page.addView(status, margins(match(), GAP / 2));

    LinearLayout weather = row();
    ImageView refresh = button(R.drawable.ic_refresh, R.string.refresh, FIELD);
    refresh.setOnClickListener(view -> refresh(refresh));
    weather.addView(refresh, square(0));
    updated = text("", 13, MUTED, false);
    LinearLayout.LayoutParams updatedSize = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    updatedSize.leftMargin = dp(GAP / 2 + 4);
    weather.addView(updated, updatedSize);
    page.addView(weather, margins(match(), GAP / 2));

    LinearLayout toggles = card();
    toggles.setPadding(dp(GAP / 2), dp(GAP / 2 - 3), dp(GAP / 2), dp(GAP / 2 - 3));
    LinearLayout shown = row();
    shown.addView(tile(R.drawable.ic_clock, R.string.time, R.string.show_time, Weather.SHOW_TIME), tileSize());
    shown.addView(tile(R.drawable.ic_cloud, R.string.show_weather, R.string.show_weather, Weather.SHOW_WEATHER), tileSize());
    shown.addView(tile(R.drawable.ic_battery, R.string.show_battery, R.string.show_battery, Weather.SHOW_BATTERY), tileSize());
    toggles.addView(shown);
    LinearLayout behaviour = row();
    behaviour.addView(tile(R.drawable.ic_drop, R.string.rain_short, R.string.show_rain, Weather.SHOW_RAIN), tileSize());
    behaviour.addView(tile(R.drawable.ic_moon, R.string.night_short, R.string.night, Weather.NIGHT), tileSize());
    behaviour.addView(tile(R.drawable.ic_pocket, R.string.pocket_short, R.string.pocket, Weather.POCKET), tileSize());
    toggles.addView(behaviour);
    page.addView(toggles, margins(match(), GAP * 3 / 2));

    LinearLayout display = card();
    display.addView(setting(R.drawable.ic_language, R.string.language, choice(
        new int[] {R.string.clock_auto, R.string.lang_es, R.string.lang_en, R.string.lang_zh}, null, language(),
        index -> getSystemService(LocaleManager.class).setApplicationLocales(
            index == 0 ? LocaleList.getEmptyLocaleList() : LocaleList.forLanguageTags(LANGUAGES[index]))), false));
    display.addView(setting(R.drawable.ic_top, R.string.on_top, choice(
        new int[] {R.string.show_weather, R.string.time}, new int[] {R.drawable.ic_cloud, R.drawable.ic_clock},
        prefs.getInt(Weather.LAYOUT, Face.WEATHER_ON_TOP),
        index -> prefs.edit().putInt(Weather.LAYOUT, index).apply()), false));
    display.addView(setting(R.drawable.ic_thermometer, R.string.unit, choice(
        new int[] {R.string.celsius, R.string.fahrenheit}, null, prefs.getBoolean(Weather.FAHRENHEIT, false) ? 1 : 0,
        index -> prefs.edit().putBoolean(Weather.FAHRENHEIT, index == 1).apply()), false));
    display.addView(setting(R.drawable.ic_clock, R.string.clock, choice(
        new int[] {R.string.clock_auto, R.string.clock_24, R.string.clock_12}, null, prefs.getInt(Weather.CLOCK, Weather.CLOCK_AUTO),
        index -> prefs.edit().putInt(Weather.CLOCK, index).apply()), false));
    display.addView(setting(R.drawable.ic_sun, R.string.brightness, brightness(), true));
    LinearLayout hours = row();
    hours.addView(hour(Weather.NIGHT_FROM, Weather.NIGHT_START));
    TextView dash = text("–", 14, MUTED, false);
    dash.setGravity(Gravity.CENTER);
    hours.addView(dash, new LinearLayout.LayoutParams(dp(GAP), LinearLayout.LayoutParams.WRAP_CONTENT));
    hours.addView(hour(Weather.NIGHT_TO, Weather.NIGHT_END));
    display.addView(setting(R.drawable.ic_moon, R.string.night_hours, hours, false));
    LinearLayout gallery = gallery();
    TextView icons = chip(getString(R.string.preview_icons));
    icons.setOnClickListener(view -> gallery.setVisibility(gallery.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
    display.addView(setting(R.drawable.ic_cloud, R.string.weather_icons, icons, false));
    page.addView(display, margins(match(), GAP));
    page.addView(gallery, margins(match(), GAP / 2));

    TextView activate = pill(R.string.activate, R.drawable.ic_add, TEXT, BACKGROUND);
    activate.setOnClickListener(view -> openToys());
    page.addView(activate, margins(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(CONTROL + 4)), GAP * 2));

    LinearLayout hint = row();
    hint.setGravity(Gravity.CENTER);
    ImageView hold = new ImageView(this);
    hold.setImageResource(R.drawable.ic_hold);
    hold.setImageTintList(ColorStateList.valueOf(MUTED));
    hint.addView(hold, new LinearLayout.LayoutParams(dp(PILL_ICON), dp(PILL_ICON)));
    TextView press = text(getString(R.string.long_press), 13, MUTED, false);
    press.setPadding(dp(GAP / 2), 0, 0, 0);
    hint.addView(press);
    page.addView(hint, margins(match(), GAP));

    TextView credit = text(getString(R.string.credit), 12, MUTED, false);
    credit.setGravity(Gravity.CENTER);
    page.addView(credit, margins(match(), GAP));

    ScrollView scroll = new ScrollView(this);
    scroll.setBackgroundColor(BACKGROUND);
    scroll.addView(page);
    setContentView(scroll);
    prefs.registerOnSharedPreferenceChangeListener(changed);
  }

  @Override
  protected void onDestroy() {
    prefs.unregisterOnSharedPreferenceChangeListener(changed);
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    resumed = true;
    redraw();
    if (Weather.stale(this)) new Thread(() -> Weather.refresh(this)).start();
  }

  @Override
  protected void onPause() {
    super.onPause();
    resumed = false;
    handler.removeCallbacks(redraw);
  }

  private void redraw() {
    handler.removeCallbacks(redraw);
    if (!resumed) return;
    preview.show(Weather.frame(this));
    long time = prefs.getLong("updated", 0);
    updated.setText(time == 0 ? "" : getString(R.string.updated,
        DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)));
    long interval = Face.interval(Weather.scene(this));
    long next = interval > 0 ? interval - System.currentTimeMillis() % interval : REDRAW;
    long sample = Weather.sampleLeft(this);
    handler.postDelayed(redraw, sample > 0 ? Math.min(next, sample) : next);
  }

  private String label(String[] names, int index) {
    if (index < Face.KINDS) return names[index];
    int night = index - Face.KINDS;
    String moon = names[Face.KINDS + night % Face.MOON_PHASES];
    int sky = night / Face.MOON_PHASES;
    return sky == 0 ? moon : getString(sky == 1 ? R.string.cloudy_moon : R.string.showers_moon, moon);
  }

  private LinearLayout gallery() {
    LinearLayout gallery = card();
    gallery.setVisibility(View.GONE);
    String[] names = getResources().getStringArray(R.array.icon_names);
    LinearLayout line = null;
    for (int i = 0; i < Face.SAMPLES + (SAMPLE_COLUMNS - Face.SAMPLES % SAMPLE_COLUMNS) % SAMPLE_COLUMNS; i++) {
      if (i % SAMPLE_COLUMNS == 0) {
        line = row();
        line.setGravity(Gravity.TOP);
        gallery.addView(line);
      }
      LinearLayout cell = new LinearLayout(this);
      cell.setOrientation(LinearLayout.VERTICAL);
      if (i < Face.SAMPLES) {
        int index = i;
        MatrixView icon = new MatrixView(this);
        icon.show(Face.sample(index));
        cell.addView(icon, match());
        String label = label(names, index);
        TextView name = text(label, 11, MUTED, false);
        name.setGravity(Gravity.CENTER);
        cell.addView(name, margins(match(), 4));
        cell.setContentDescription(label);
        cell.setOnClickListener(view -> {
          prefs.edit().putInt(Weather.SAMPLE, index)
              .putLong(Weather.SAMPLE_UNTIL, System.currentTimeMillis() + Weather.SAMPLE_TIME).apply();
        });
      }
      line.addView(cell, tileSize());
    }
    return gallery;
  }

  private void search() {
    String name = city.getText().toString().trim();
    if (name.isEmpty()) return;
    getSystemService(InputMethodManager.class).hideSoftInputFromWindow(city.getWindowToken(), 0);
    results.removeAllViews();
    say(R.string.searching, MUTED);
    new Thread(() -> {
      List<Weather.Place> found;
      try {
        found = Weather.places(name);
      } catch (Exception offline) {
        found = null;
      }
      List<Weather.Place> places = found;
      handler.post(() -> {
        if (places == null) say(R.string.offline, ACCENT);
        else if (places.isEmpty()) say(R.string.not_found, ACCENT);
        else if (places.size() == 1) choose(places.get(0));
        else {
          say(0, MUTED);
          for (Weather.Place place : places) results.addView(option(place));
        }
      });
    }).start();
  }

  private TextView option(Weather.Place place) {
    TextView option = text(place.detail(), 15, TEXT, false);
    option.setGravity(Gravity.CENTER_VERTICAL);
    option.setMinHeight(dp(CONTROL));
    option.setPadding(dp(GAP), 0, dp(GAP), 0);
    option.setOnClickListener(view -> choose(place));
    return option;
  }

  private void choose(Weather.Place place) {
    results.removeAllViews();
    city.setText(place.name());
    say(0, MUTED);
    new Thread(() -> Weather.choose(this, place)).start();
  }

  private void refresh(View button) {
    if (!prefs.contains("lat")) {
      city.requestFocus();
      return;
    }
    button.setEnabled(false);
    button.setAlpha(0.4f);
    new Thread(() -> {
      boolean done = Weather.refresh(this);
      handler.post(() -> {
        button.setEnabled(true);
        button.setAlpha(1);
        say(done ? 0 : R.string.offline, ACCENT);
      });
    }).start();
  }

  private void say(int message, int color) {
    status.setVisibility(message == 0 ? View.GONE : View.VISIBLE);
    status.setTextColor(color);
    if (message != 0) status.setText(message);
  }

  private void openToys() {
    Intent intent = new Intent();
    intent.setComponent(new ComponentName("com.nothing.thirdparty", "com.nothing.thirdparty.matrix.toys.manager.ToysManagerActivity"));
    try {
      startActivity(intent);
    } catch (ActivityNotFoundException missing) {
      say(R.string.no_toys, ACCENT);
    }
  }

  private int language() {
    LocaleList chosen = getSystemService(LocaleManager.class).getApplicationLocales();
    if (chosen.isEmpty()) return 0;
    String code = chosen.get(0).getLanguage();
    for (int i = 1; i < LANGUAGES.length; i++) if (LANGUAGES[i].startsWith(code)) return i;
    return 0;
  }

  private interface Choice {
    void picked(int index);
  }

  private View choice(int[] labels, int[] icons, int selected, Choice choice) {
    RadioGroup group = new RadioGroup(this);
    group.setOrientation(LinearLayout.HORIZONTAL);
    group.setBackground(round(FIELD, CONTROL / 2));
    group.setPadding(dp(3), dp(3), dp(3), dp(3));
    for (int i = 0; i < labels.length; i++) {
      RadioButton option = new RadioButton(this);
      option.setId(i + 1);
      option.setSaveEnabled(false); // ids repeat across groups; prefs are the source of truth, so never restore them
      option.setButtonDrawable(null);
      option.setGravity(Gravity.CENTER);
      option.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
      option.setTypeface(Typeface.DEFAULT_BOLD);
      option.setTextColor(checked(BACKGROUND, MUTED));
      option.setBackground(states(round(TEXT, CONTROL / 2), round(Color.TRANSPARENT, CONTROL / 2)));
      if (icons == null) {
        option.setText(labels[i]);
        option.setPadding(dp(14), 0, dp(14), 0);
      } else {
        option.setContentDescription(getString(labels[i]));
        option.setTooltipText(getString(labels[i]));
        option.setCompoundDrawablesRelativeWithIntrinsicBounds(icons[i], 0, 0, 0);
        option.setCompoundDrawableTintList(checked(BACKGROUND, MUTED));
        option.setPadding(dp(11), 0, dp(11), 0);
      }
      group.addView(option, new RadioGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(CONTROL - 6)));
    }
    group.check(selected + 1);
    group.setOnCheckedChangeListener((view, id) -> choice.picked(id - 1));
    return group;
  }

  private CheckBox tile(int icon, int label, int description, String key) {
    CheckBox tile = new CheckBox(this);
    tile.setButtonDrawable(null);
    tile.setGravity(Gravity.CENTER);
    tile.setText(label);
    tile.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
    tile.setSingleLine();
    tile.setTextColor(checked(TEXT, MUTED));
    tile.setCompoundDrawablesRelativeWithIntrinsicBounds(0, icon, 0, 0);
    tile.setCompoundDrawableTintList(checked(TEXT, MUTED));
    tile.setCompoundDrawablePadding(dp(4));
    tile.setPadding(0, dp(10), 0, dp(8));
    tile.setBackground(states(round(ACCENT, RADIUS - 6), round(FIELD, RADIUS - 6)));
    tile.setTooltipText(getString(description));
    tile.setChecked(prefs.getBoolean(key, true));
    tile.setOnCheckedChangeListener((view, on) -> prefs.edit().putBoolean(key, on).apply());
    return tile;
  }

  private LinearLayout.LayoutParams tileSize() {
    LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
    size.setMargins(dp(3), dp(3), dp(3), dp(3));
    return size;
  }

  private TextView hour(String key, int fallback) {
    TextView chip = chip(clock(prefs.getInt(key, fallback)));
    chip.setOnClickListener(view -> {
      int minutes = prefs.getInt(key, fallback);
      new TimePickerDialog(this, (picker, hour, minute) -> {
        prefs.edit().putInt(key, hour * 60 + minute).apply();
        chip.setText(clock(hour * 60 + minute));
      }, minutes / 60, minutes % 60, DateFormat.is24HourFormat(this)).show();
    });
    return chip;
  }

  private TextView chip(String label) {
    TextView chip = text(label, 14, TEXT, true);
    chip.setGravity(Gravity.CENTER);
    chip.setPadding(dp(14), 0, dp(14), 0);
    chip.setMinHeight(dp(CONTROL - 6));
    chip.setBackground(round(FIELD, CONTROL / 2));
    return chip;
  }

  private String clock(int minutes) {
    Calendar time = Calendar.getInstance();
    time.set(Calendar.HOUR_OF_DAY, minutes / 60);
    time.set(Calendar.MINUTE, minutes % 60);
    return DateFormat.getTimeFormat(this).format(time.getTime());
  }

  private View brightness() {
    LinearLayout box = row();
    SeekBar slider = new SeekBar(this);
    slider.setMax(Weather.FULL_BRIGHTNESS - MIN_BRIGHTNESS);
    slider.setProgress(prefs.getInt(Weather.BRIGHTNESS, Weather.FULL_BRIGHTNESS) - MIN_BRIGHTNESS);
    slider.setProgressTintList(ColorStateList.valueOf(ACCENT));
    slider.setThumbTintList(ColorStateList.valueOf(TEXT));
    slider.setProgressBackgroundTintList(ColorStateList.valueOf(MUTED));
    slider.setContentDescription(getString(R.string.brightness));
    box.addView(slider, new LinearLayout.LayoutParams(0, dp(CONTROL), 1));
    TextView value = text((slider.getProgress() + MIN_BRIGHTNESS) + " %", 14, MUTED, false);
    value.setGravity(Gravity.END);
    box.addView(value, new LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT));
    slider.setOnSeekBarChangeListener(new BrightnessListener(value, MIN_BRIGHTNESS, prefs));
    return box;
  }

  private LinearLayout setting(int icon, int label, View control, boolean stretch) {
    LinearLayout line = row();
    line.setPadding(0, dp(GAP / 2), 0, dp(GAP / 2));
    ImageView symbol = new ImageView(this);
    symbol.setImageResource(icon);
    symbol.setImageTintList(ColorStateList.valueOf(MUTED));
    symbol.setContentDescription(getString(label));
    symbol.setTooltipText(getString(label));
    LinearLayout.LayoutParams symbolSize = new LinearLayout.LayoutParams(dp(ICON), dp(ICON));
    symbolSize.rightMargin = dp(GAP);
    line.addView(symbol, symbolSize);
    if (stretch) {
      line.addView(control, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
    } else {
      line.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1));
      line.addView(control);
    }
    return line;
  }

  private LinearLayout card() {
    LinearLayout card = new LinearLayout(this);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setBackground(round(CARD, RADIUS));
    card.setPadding(dp(GAP), dp(GAP - 4), dp(GAP), dp(GAP - 4));
    return card;
  }

  private LinearLayout row() {
    LinearLayout row = new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    return row;
  }

  private ImageView button(int icon, int label, int fill) {
    ImageView button = new ImageView(this);
    button.setImageResource(icon);
    button.setImageTintList(ColorStateList.valueOf(TEXT));
    button.setScaleType(ImageView.ScaleType.CENTER);
    button.setBackground(round(fill, CONTROL / 2));
    button.setContentDescription(getString(label));
    button.setTooltipText(getString(label));
    return button;
  }

  private LinearLayout.LayoutParams square(int left) {
    LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(CONTROL), dp(CONTROL));
    size.leftMargin = dp(left);
    return size;
  }

  private TextView pill(int label, int icon, int fill, int color) {
    TextView pill = text("", 15, color, true);
    pill.setGravity(Gravity.CENTER);
    pill.setPadding(dp(GAP + 4), 0, dp(GAP + 4), 0);
    pill.setBackground(round(fill, CONTROL / 2));
    Drawable glyph = getDrawable(icon).mutate();
    glyph.setTint(color);
    glyph.setBounds(0, 0, dp(PILL_ICON), dp(PILL_ICON));
    SpannableString text = new SpannableString("  " + getString(label));
    text.setSpan(new ImageSpan(glyph, ImageSpan.ALIGN_CENTER), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    pill.setText(text);
    return pill;
  }

  private ColorStateList checked(int on, int off) {
    return new ColorStateList(new int[][] {{android.R.attr.state_checked}, {}}, new int[] {on, off});
  }

  private StateListDrawable states(Drawable on, Drawable off) {
    StateListDrawable states = new StateListDrawable();
    states.addState(new int[] {android.R.attr.state_checked}, on);
    states.addState(new int[0], off);
    return states;
  }

  private TextView text(String value, int size, int color, boolean bold) {
    TextView view = new TextView(this);
    view.setText(value);
    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    view.setTextColor(color);
    if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
    return view;
  }

  private GradientDrawable round(int color, int radius) {
    GradientDrawable shape = new GradientDrawable();
    shape.setColor(color);
    shape.setCornerRadius(dp(radius));
    return shape;
  }

  private LinearLayout.LayoutParams match() {
    return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
  }

  private LinearLayout.LayoutParams margins(LinearLayout.LayoutParams params, int top) {
    params.topMargin = dp(top);
    return params;
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }
}
