package app.glyphcast;

// Plain-Java checks for the renderer; build.mjs runs them before packaging.
public final class FaceCheck {
  private static final long FULL_MOON = 1706205240000L; // 2024-01-25 17:54 UTC

  public static void main(String[] args) {
    int leds = 0;
    for (int y = 0; y < Face.SIZE; y++) for (int x = 0; x < Face.SIZE; x++) if (Face.led(x, y)) leds++;
    check(leds == 489, "LED count " + leds);

    check(Face.kind(0) == Face.CLEAR && Face.kind(2) == Face.PARTLY && Face.kind(3) == Face.CLOUD && Face.kind(45) == Face.FOG
        && Face.kind(61) == Face.RAIN && Face.kind(73) == Face.SNOW && Face.kind(95) == Face.STORM, "weather codes");

    double phase = Face.moonPhase(FULL_MOON);
    check(Math.abs(phase - 0.5) < 0.03, "moon phase " + phase);

    for (int layout : new int[] {Face.WEATHER_ON_TOP, Face.TIME_ON_TOP}) {
      for (Integer temperature : new Integer[] {null, -40, -5, 0, 21, 104}) {
        for (int kind = Face.CLEAR; kind <= Face.STORM; kind++) {
          for (int battery : new int[] {-1, 0, 10, 100}) {
            for (boolean clock : new boolean[] {true, false}) {
              inside(Face.draw(new Face.Scene(23, 59, temperature, kind, kind % 2 == 0, 0.3, battery, battery == 10, true, 3, layout, false, clock)));
            }
          }
        }
      }
    }
    inside(Face.detail(-12, 35));

    int[] twelve = Face.draw(new Face.Scene(8, 8, null, Face.CLEAR, true, 0, -1, false, false, 0, Face.WEATHER_ON_TOP, true, true));
    int left = Face.SIZE;
    int right = 0;
    for (int i = 0; i < twelve.length; i++) {
      if (twelve[i] == 0) continue;
      left = Math.min(left, i % Face.SIZE);
      right = Math.max(right, i % Face.SIZE);
    }
    check(Math.abs(left - (Face.SIZE - 1 - right)) <= 1, "12 h clock off centre: " + left + ".." + right);

    int[] untimed = Face.draw(new Face.Scene(12, 0, 21, Face.CLEAR, true, 0, -1, false, false, 0, Face.WEATHER_ON_TOP, false, false));
    int[] timed = Face.draw(new Face.Scene(12, 0, 21, Face.CLEAR, true, 0, -1, false, false, 0, Face.WEATHER_ON_TOP, false, true));
    check(!java.util.Arrays.equals(untimed, timed), "hiding the time changes the face");

    Face.Scene low = new Face.Scene(12, 0, null, Face.CLEAR, true, 0, 10, false, false, 0, Face.WEATHER_ON_TOP, false, true);
    check(Face.interval(low) == Face.FRAME * 4, "low battery blinks slowly");
    System.out.println("Face checks passed");
  }

  private static void inside(int[] pixels) {
    for (int i = 0; i < pixels.length; i++) check(pixels[i] == 0 || Face.led(i % Face.SIZE, i / Face.SIZE), "pixel outside the matrix at " + i);
  }

  private static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }
}
