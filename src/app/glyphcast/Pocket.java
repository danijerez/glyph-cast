package app.glyphcast;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Pocket implements SensorEventListener {
  private static final float FLAT = 7f;
  private final SensorManager sensors;
  private final Sensor proximity;
  private final Sensor gravity;
  private final Runnable changed;
  private boolean near;
  private boolean inside;

  Pocket(SensorManager sensors, Runnable changed) {
    this.sensors = sensors;
    this.proximity = sensors.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    this.gravity = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    this.changed = changed;
  }

  void start() {
    if (proximity != null && gravity != null) sensors.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
  }

  void stop() {
    sensors.unregisterListener(this);
    near = false;
    inside = false;
  }

  void check() {
    if (near) sensors.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
  }

  boolean inside() {
    return inside;
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (event.sensor == proximity) {
      near = event.values[0] < proximity.getMaximumRange();
      if (near) sensors.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
      else update(false);
      return;
    }
    sensors.unregisterListener(this, gravity);
    update(near && Math.abs(event.values[2]) < FLAT);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  private void update(boolean value) {
    if (value == inside) return;
    inside = value;
    changed.run();
  }
}
