package app.glyphcast;

public final class Face {
  public static final int SIZE = 25;
  public static final int CLEAR = 0;
  public static final int PARTLY = 1;
  public static final int CLOUD = 2;
  public static final int FOG = 3;
  public static final int RAIN = 4;
  public static final int SNOW = 5;
  public static final int STORM = 6;
  public static final int WEATHER_ON_TOP = 0;
  public static final int TIME_ON_TOP = 1;
  public static final long FRAME = 500;

  private static final int[] HALF_WIDTHS = {3, 5, 7, 8, 9, 10, 10, 11, 11, 12, 12, 12, 12, 12, 12, 12, 11, 11, 10, 10, 9, 8, 7, 5, 3};
  private static final int TIME = 255;
  private static final int TEMP = 170;
  private static final int ICON = 210;
  private static final int SHADE = 140;
  private static final int BATTERY = 200;
  private static final int TRACK = 60;
  private static final int FAINT = 50;
  private static final int MOON_WIDTH = 9;
  private static final int MOON_HEIGHT = 9;
  private static final double MOON_RADIUS = 4.4;
  private static final double NEW_MOON_DAY = 10962.76;
  private static final double SYNODIC_MONTH = 29.530588853;
  private static final int[] BATTERY_ROW = {14, 10};
  private static final int BATTERY_ALONE_ROW = 22;
  private static final int BATTERY_HALF = 7;
  private static final int[] BAND_TOP = {4, 12};
  private static final int[] TEMP_TOP = {6, 14};
  private static final int[] TIME_TOP = {16, 4};
  private static final int GAP = 2;
  private static final int BAND_WIDTH = 21;
  private static final int SWEEP = 110;
  private static final int LOW_BATTERY = 15;
  private static final int BLINK = 4;
  private static final int DROP_TOP = 22;
  private static final int ALONE_ICON_TOP = 4;
  private static final int ALONE_TEMP_TOP = 14;
  private static final int DETAIL_TOP = 6;
  private static final int DETAIL_STEP = 8;

  private static final String[][] BIG = {
    {".##.", "#..#", "#..#", "#..#", "#..#", "#..#", ".##."},
    {"..#.", ".##.", "..#.", "..#.", "..#.", "..#.", ".###"},
    {".##.", "#..#", "...#", "..#.", ".#..", "#...", "####"},
    {"###.", "...#", "...#", ".##.", "...#", "...#", "###."},
    {"..#.", ".##.", "#.#.", "#.#.", "####", "..#.", "..#."},
    {"####", "#...", "###.", "...#", "...#", "#..#", ".##."},
    {".##.", "#...", "#...", "###.", "#..#", "#..#", ".##."},
    {"####", "...#", "..#.", "..#.", ".#..", ".#..", ".#.."},
    {".##.", "#..#", "#..#", ".##.", "#..#", "#..#", ".##."},
    {".##.", "#..#", "#..#", ".###", "...#", "...#", ".##."}
  };

  private static final String[][] SMALL = {
    {"###", "#.#", "#.#", "#.#", "###"},
    {".#.", "##.", ".#.", ".#.", "###"},
    {"###", "..#", "###", "#..", "###"},
    {"###", "..#", ".##", "..#", "###"},
    {"#.#", "#.#", "###", "..#", "..#"},
    {"###", "#..", "###", "..#", "###"},
    {"###", "#..", "###", "#.#", "###"},
    {"###", "..#", ".#.", ".#.", ".#."},
    {"###", "#.#", "###", "#.#", "###"},
    {"###", "#.#", "###", "..#", "###"}
  };

  private static final String[] MINUS = {"..", "..", "##", "..", ".."};
  private static final String[] DEGREE = {"##", "##"};
  private static final String[] DROP = {".#.", "###", "###"};
  private static final String[] UP = {".#.", "###", ".#.", ".#.", ".#."};
  private static final String[] DOWN = {".#.", ".#.", ".#.", "###", ".#."};
  private static final String[] PARTLY_NIGHT = {
    "..++.....",
    ".++......",
    ".+.......",
    ".+..##...",
    ".+.#..##.",
    "..#.....#",
    ".#......#",
    ".#######.",
    "........."
  };

  private static final String[][] ICONS = {
    {
      "....#....",
      ".#.....#.",
      "...###...",
      "..#...#..",
      "#.#...#.#",
      "..#...#..",
      "...###...",
      ".#.....#.",
      "....#...."
    },
    {
      ".+..+....",
      "..+++....",
      ".+++++...",
      "..++##...",
      ".+.#..##.",
      "..#.....#",
      ".#......#",
      ".#######.",
      "........."
    },
    {
      ".........",
      ".........",
      "....###..",
      ".##.#..#.",
      "#..#....#",
      "#.......#",
      ".#######.",
      ".........",
      "........."
    },
    {
      ".........",
      ".#######.",
      ".........",
      "..#######",
      ".........",
      "#######..",
      ".........",
      ".#######.",
      "........."
    },
    {
      "....###..",
      ".##.#..#.",
      "#..#....#",
      "#.......#",
      ".#######.",
      ".........",
      "..#..#..#",
      ".#..#..#.",
      "........."
    },
    {
      "....###..",
      ".##.#..#.",
      "#..#....#",
      "#.......#",
      ".#######.",
      ".........",
      "..#...#..",
      ".###.###.",
      "..#...#.."
    },
    {
      "....###..",
      ".##.#..#.",
      "#..#....#",
      "#.......#",
      ".#######.",
      "....##...",
      "...##....",
      "....##...",
      "...#....."
    }
  };

  private Face() {}

  public static boolean led(int x, int y) {
    return y >= 0 && y < SIZE && Math.abs(x - SIZE / 2) <= HALF_WIDTHS[y];
  }

  public static int kind(int code) {
    if (code <= 1) return CLEAR;
    if (code == 2) return PARTLY;
    if (code == 3) return CLOUD;
    if (code == 45 || code == 48) return FOG;
    if (code >= 95) return STORM;
    if ((code >= 71 && code <= 77) || code == 85 || code == 86) return SNOW;
    return RAIN;
  }

  public static double moonPhase(long millis) {
    double cycles = (millis / 86400000.0 - NEW_MOON_DAY) / SYNODIC_MONTH;
    return cycles - Math.floor(cycles);
  }

  public record Scene(int hour, int minute, Integer temperature, int kind, boolean day, double moon,
      int battery, boolean charging, boolean rainSoon, long tick, int layout, boolean twelve, boolean clock) {}

  public static long interval(Scene scene) {
    if (scene.battery() < 0) return 0;
    if (scene.charging()) return FRAME;
    return scene.battery() <= LOW_BATTERY ? FRAME * BLINK : 0;
  }

  public static int[] draw(Scene scene) {
    int[] pixels = new int[SIZE * SIZE];
    int hour = scene.hour();
    int minute = scene.minute();
    int[] digits = {scene.twelve() && hour < 10 ? -1 : hour / 10, hour % 10, minute / 10, minute % 10};
    Integer temperature = scene.temperature();
    boolean alone = temperature == null || !scene.clock();
    battery(pixels, scene, alone ? BATTERY_ALONE_ROW : BATTERY_ROW[scene.layout()]);
    if (temperature == null) {
      clock(pixels, digits, BIG, 9);
      return pixels;
    }
    int kind = scene.kind();
    boolean drop = scene.rainSoon() && kind != RAIN && kind != STORM && kind != SNOW;
    String[] icon = scene.day() ? ICONS[kind] : kind == CLEAR ? moon(scene.moon()) : kind == PARTLY ? PARTLY_NIGHT : ICONS[kind];
    String text = Integer.toString(Math.abs(temperature));
    int sign = temperature < 0 ? MINUS[0].length() + 1 : 0;
    if (alone) {
      if (drop) stamp(pixels, DROP, (SIZE - DROP[0].length()) / 2, 0, ICON);
      stamp(pixels, icon, (SIZE - icon[0].length()) / 2, ALONE_ICON_TOP, ICON);
      int width = sign + text.length() * (BIG[0][0].length() + 1) + DEGREE[0].length();
      number(pixels, (SIZE - width) / 2, ALONE_TEMP_TOP, temperature, true, TEMP, BIG);
      return pixels;
    }
    clock(pixels, digits, SMALL, TIME_TOP[scene.layout()]);
    if (drop) stamp(pixels, DROP, (SIZE - DROP[0].length()) / 2, DROP_TOP, ICON);
    boolean degree = icon[0].length() + GAP + sign + text.length() * 4 + DEGREE[0].length() <= BAND_WIDTH;
    int width = icon[0].length() + GAP + sign + text.length() * 4 + (degree ? DEGREE[0].length() : -1);
    int x = (SIZE - width) / 2;
    stamp(pixels, icon, x, BAND_TOP[scene.layout()], ICON);
    number(pixels, x + icon[0].length() + GAP, TEMP_TOP[scene.layout()], temperature, degree, TEMP, SMALL);
    return pixels;
  }

  public static int[] detail(int max, int min) {
    int[] pixels = new int[SIZE * SIZE];
    extreme(pixels, UP, max, DETAIL_TOP, TIME);
    extreme(pixels, DOWN, min, DETAIL_TOP + DETAIL_STEP, TEMP);
    return pixels;
  }

  private static void extreme(int[] pixels, String[] arrow, int value, int top, int level) {
    int sign = value < 0 ? MINUS[0].length() + 1 : 0;
    int width = arrow[0].length() + 2 + sign + Integer.toString(Math.abs(value)).length() * 4 + DEGREE[0].length();
    int x = (SIZE - width) / 2;
    stamp(pixels, arrow, x, top, level);
    number(pixels, x + arrow[0].length() + 2, top, value, true, level, SMALL);
  }

  private static void number(int[] pixels, int x, int top, int value, boolean degree, int level, String[][] font) {
    if (value < 0) {
      stamp(pixels, MINUS, x, top + (font[0].length - MINUS.length) / 2, level);
      x += MINUS[0].length() + 1;
    }
    for (char c : Integer.toString(Math.abs(value)).toCharArray()) {
      stamp(pixels, font[c - '0'], x, top, level);
      x += font[0][0].length() + 1;
    }
    if (degree) stamp(pixels, DEGREE, x, top, level);
  }

  private static String[] moon(double phase) {
    String[] rows = new String[MOON_HEIGHT];
    double edge = Math.cos(2 * Math.PI * phase);
    for (int y = 0; y < MOON_HEIGHT; y++) {
      StringBuilder row = new StringBuilder();
      double dy = y - MOON_HEIGHT / 2;
      double reach = Math.sqrt(Math.max(0, MOON_RADIUS * MOON_RADIUS - dy * dy));
      for (int x = 0; x < MOON_WIDTH; x++) {
        double dx = x - MOON_WIDTH / 2;
        boolean lit = phase < 0.5 ? dx > reach * edge : dx < -reach * edge;
        row.append(Math.abs(dx) > reach ? '.' : lit ? '#' : '-');
      }
      rows[y] = row.toString();
    }
    return rows;
  }

  private static void battery(int[] pixels, Scene scene, int row) {
    if (scene.battery() < 0) return;
    int half = Math.min(BATTERY_HALF, HALF_WIDTHS[row]);
    int length = half * 2 + 1;
    int lit = Math.max(1, Math.round(Math.min(100, scene.battery()) * length / 100f));
    int sweep = scene.charging() ? (int) (scene.tick() % (length - lit + 1)) : 0;
    boolean blink = !scene.charging() && scene.battery() <= LOW_BATTERY && scene.tick() / BLINK % 2 == 1;
    for (int i = 0; i < length; i++) {
      int level = i < lit ? (blink ? TRACK : BATTERY) : i < lit + sweep ? SWEEP : TRACK;
      pixels[row * SIZE + SIZE / 2 - half + i] = level;
    }
  }

  private static void clock(int[] pixels, int[] digits, String[][] font, int top) {
    int width = font[0][0].length();
    int height = font[0].length;
    int first = digits[0] < 0 ? 1 : 0;
    int x = (SIZE - ((4 - first) * (width + 1) + 1)) / 2;
    for (int i = first; i < 4; i++) {
      if (i == 2) {
        pixels[(top + height / 2 - 1) * SIZE + x] = TIME;
        pixels[(top + height / 2 + 1) * SIZE + x] = TIME;
        x += 2;
      }
      stamp(pixels, font[digits[i]], x, top, TIME);
      x += width + 1;
    }
  }

  private static void stamp(int[] pixels, String[] rows, int left, int top, int level) {
    for (int y = 0; y < rows.length; y++) {
      for (int x = 0; x < rows[y].length(); x++) {
        char c = rows[y].charAt(x);
        if (c == '.' || !led(left + x, top + y)) continue;
        pixels[(top + y) * SIZE + left + x] = c == '+' ? SHADE : c == '-' ? FAINT : level;
      }
    }
  }
}
