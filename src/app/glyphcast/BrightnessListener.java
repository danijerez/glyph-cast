package app.glyphcast;

import android.content.SharedPreferences;
import android.widget.SeekBar;
import android.widget.TextView;

public class BrightnessListener implements SeekBar.OnSeekBarChangeListener {
  private final TextView value;
  private final int minimum;
  private final SharedPreferences prefs;

  BrightnessListener(TextView value, int minimum, SharedPreferences prefs) {
    this.value = value;
    this.minimum = minimum;
    this.prefs = prefs;
  }

  @Override
  public void onProgressChanged(SeekBar bar, int progress, boolean user) {
    value.setText((progress + minimum) + " %");
    if (user) prefs.edit().putInt(Weather.BRIGHTNESS, progress + minimum).apply();
  }

  @Override
  public void onStartTrackingTouch(SeekBar bar) {}

  @Override
  public void onStopTrackingTouch(SeekBar bar) {}
}
