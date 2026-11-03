package app.glyphcast;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.text.format.DateFormat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import org.json.JSONArray;
import org.json.JSONObject;

final class Weather {
  private static final long REFRESH = 30 * 60 * 1000L;
  private static final long RETRY = 5 * 60 * 1000L;
  private static final long EXPIRE = 3 * 60 * 60 * 1000L;
  private static final int TIMEOUT = 10000;
  static final String FAHRENHEIT = "fahrenheit";
  static final String CLOCK = "clock";
  static final String SHOW_TIME = "show_time";
  static final String SHOW_WEATHER = "show_weather";
  static final String SHOW_BATTERY = "show_battery";
  static final String BRIGHTNESS = "brightness";
  static final String SHOW_RAIN = "show_rain";
  static final String NIGHT = "night";
  static final String LAYOUT = "layout";
  static final String POCKET = "pocket";
  static final String NIGHT_FROM = "night_from";
  static final String NIGHT_TO = "night_to";
  static final int CLOCK_AUTO = 0;
  static final int CLOCK_24 = 1;
  static final int CLOCK_12 = 2;
  static final int FULL_BRIGHTNESS = 100;
  static final int NIGHT_START = 23 * 60;
  static final int NIGHT_END = 7 * 60;
  private static final int NIGHT_BRIGHTNESS = 35;
  // ponytail: guessed lowest level the LEDs still show; calibrate on the phone
  private static final int MIN_LEVEL = 16;
  private static final int RESULTS = 5;
  private static final double RAIN_THRESHOLD = 0.1;

  private Weather() {}

  record Place(String name, String detail, double latitude, double longitude) {}

  static SharedPreferences prefs(Context context) {
    return context.getSharedPreferences("weather", Context.MODE_PRIVATE);
  }

  static List<Place> places(String name) throws Exception {
    JSONArray results = get("https://geocoding-api.open-meteo.com/v1/search?count=" + RESULTS + "&language="
        + Locale.getDefault().getLanguage() + "&name=" + URLEncoder.encode(name, "UTF-8")).optJSONArray("results");
    List<Place> places = new ArrayList<>();
    for (int i = 0; results != null && i < results.length(); i++) {
      JSONObject result = results.getJSONObject(i);
      String city = result.getString("name");
      String country = result.optString("country");
      places.add(new Place(label(city, country), label(city, result.optString("admin1"), country),
          result.getDouble("latitude"), result.getDouble("longitude")));
    }
    return places;
  }

  static void choose(Context context, Place place) {
    prefs(context).edit()
        .putFloat("lat", (float) place.latitude())
        .putFloat("lon", (float) place.longitude())
        .putString("place", place.name())
        .remove("updated")
        .commit();
    refresh(context);
  }

  private static String label(String... parts) {
    StringJoiner label = new StringJoiner(", ");
    for (String part : parts) if (!part.isEmpty()) label.add(part);
    return label.toString();
  }

  static boolean stale(Context context) {
    SharedPreferences prefs = prefs(context);
    long now = System.currentTimeMillis();
    return prefs.contains("lat") && now - prefs.getLong("tried", 0) > RETRY
        && (!prefs.contains("max") || now - prefs.getLong("updated", 0) > REFRESH);
  }

  static boolean refresh(Context context) {
    SharedPreferences prefs = prefs(context);
    if (!prefs.contains("lat")) return false;
    try {
      JSONObject forecast = get("https://api.open-meteo.com/v1/forecast?current=temperature_2m,weather_code,is_day"
          + "&minutely_15=precipitation&forecast_minutely_15=4&daily=temperature_2m_max,temperature_2m_min&forecast_days=1&timezone=auto"
          + "&latitude=" + prefs.getFloat("lat", 0) + "&longitude=" + prefs.getFloat("lon", 0));
      JSONObject current = forecast.getJSONObject("current");
      JSONArray rain = forecast.getJSONObject("minutely_15").getJSONArray("precipitation");
      boolean rainSoon = false;
      for (int i = 0; i < rain.length(); i++) rainSoon |= rain.optDouble(i, 0) >= RAIN_THRESHOLD;
      JSONObject daily = forecast.getJSONObject("daily");
      prefs.edit()
          .putBoolean("rain_soon", rainSoon)
          .putFloat("max", (float) daily.getJSONArray("temperature_2m_max").getDouble(0))
          .putFloat("min", (float) daily.getJSONArray("temperature_2m_min").getDouble(0))
          .putFloat("celsius", (float) current.getDouble("temperature_2m"))
          .putInt("code", current.getInt("weather_code"))
          .putBoolean("day", current.getInt("is_day") == 1)
          .putLong("updated", System.currentTimeMillis())
          .commit();
      return true;
    } catch (Exception offline) {
      prefs.edit().putLong("tried", System.currentTimeMillis()).apply();
      return false;
    }
  }

  static boolean fresh(Context context) {
    SharedPreferences prefs = prefs(context);
    return prefs.contains("max") && System.currentTimeMillis() - prefs.getLong("updated", 0) < EXPIRE;
  }

  static Face.Scene scene(Context context) {
    SharedPreferences prefs = prefs(context);
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    int clock = prefs.getInt(CLOCK, CLOCK_AUTO);
    boolean twelve = clock == CLOCK_12 || (clock == CLOCK_AUTO && !DateFormat.is24HourFormat(context));
    if (twelve) hour = hour % 12 == 0 ? 12 : hour % 12;
    boolean weather = fresh(context) && prefs.getBoolean(SHOW_WEATHER, true);
    BatteryManager manager = context.getSystemService(BatteryManager.class);
    int battery = prefs.getBoolean(SHOW_BATTERY, true) ? manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) : -1;
    return new Face.Scene(hour, now.get(Calendar.MINUTE), weather ? degrees(prefs, "celsius") : null,
        Face.kind(prefs.getInt("code", 0)), prefs.getBoolean("day", true), Face.moonPhase(now.getTimeInMillis()),
        battery, manager.isCharging(), weather && prefs.getBoolean(SHOW_RAIN, true) && prefs.getBoolean("rain_soon", false),
        System.currentTimeMillis() / Face.FRAME, prefs.getInt(LAYOUT, Face.WEATHER_ON_TOP), twelve,
        !weather || prefs.getBoolean(SHOW_TIME, true));
  }

  static int[] frame(Context context) {
    return dim(context, Face.draw(scene(context)));
  }

  static int[] detail(Context context) {
    SharedPreferences prefs = prefs(context);
    return dim(context, Face.detail(degrees(prefs, "max"), degrees(prefs, "min")));
  }

  private static int degrees(SharedPreferences prefs, String key) {
    float celsius = prefs.getFloat(key, 0);
    return Math.round(prefs.getBoolean(FAHRENHEIT, false) ? celsius * 9 / 5 + 32 : celsius);
  }

  private static int[] dim(Context context, int[] pixels) {
    SharedPreferences prefs = prefs(context);
    Calendar now = Calendar.getInstance();
    int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
    int from = prefs.getInt(NIGHT_FROM, NIGHT_START);
    int to = prefs.getInt(NIGHT_TO, NIGHT_END);
    boolean night = prefs.getBoolean(NIGHT, true) && (from <= to ? minute >= from && minute < to : minute >= from || minute < to);
    int brightness = prefs.getInt(BRIGHTNESS, FULL_BRIGHTNESS) * (night ? NIGHT_BRIGHTNESS : FULL_BRIGHTNESS) / FULL_BRIGHTNESS;
    int top = 255 * brightness / FULL_BRIGHTNESS;
    for (int i = 0; i < pixels.length; i++) {
      if (pixels[i] > 0) pixels[i] = MIN_LEVEL + pixels[i] * (top - MIN_LEVEL) / 255;
    }
    return pixels;
  }

  private static JSONObject get(String url) throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setConnectTimeout(TIMEOUT);
    connection.setReadTimeout(TIMEOUT);
    try (InputStream in = connection.getInputStream()) {
      return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    } finally {
      connection.disconnect();
    }
  }
}
