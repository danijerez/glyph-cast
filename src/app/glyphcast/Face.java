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
  public static final int SHOWERS = 7;
  public static final int HAIL = 8;
  public static final int SLEET = 9;
  public static final int SOLAR_ECLIPSE = 10;
  public static final int LUNAR_ECLIPSE = 11;
  public static final int KINDS = 12;
  public static final int WEATHER_ON_TOP = 0;
  public static final int TIME_ON_TOP = 1;
  public static final long FRAME = 500;
  public static final int MOON_PHASES = 8;
  private static final int[] NIGHT_SAMPLES = {CLEAR, PARTLY, SHOWERS};
  public static final int SAMPLES = KINDS + NIGHT_SAMPLES.length * MOON_PHASES; // day icons, then every moon phase per night sky

  // NASA eclipse tables (eclipse.gsfc.nasa.gov), greatest eclipse in TD (~1 min off UTC). Penumbral lunar eclipses left out.
  private static final long SOLAR_HALF = 2 * 60 * 60 * 1000L; // ponytail: global window, not checked against the local path
  private static final String[] SOLAR = {
    "2027-02-06T16:00:47Z", "2027-08-02T10:07:49Z", "2028-01-26T15:08:58Z", "2028-07-22T02:56:39Z", "2029-01-14T17:13:47Z",
    "2029-06-12T04:06:13Z", "2029-07-11T15:37:18Z", "2029-12-05T15:03:57Z", "2030-06-01T06:29:13Z", "2030-11-25T06:51:37Z",
    "2031-05-21T07:16:04Z", "2031-11-14T21:07:30Z", "2032-05-09T13:26:42Z", "2032-11-03T05:34:12Z", "2033-03-30T18:02:35Z",
    "2033-09-23T13:54:31Z", "2034-03-20T10:18:45Z", "2034-09-12T16:19:27Z", "2035-03-09T23:05:53Z", "2035-09-02T01:56:46Z",
    "2036-02-27T04:46:49Z", "2036-07-23T10:32:06Z", "2036-08-21T17:25:45Z", "2037-01-16T09:48:55Z", "2037-07-13T02:40:35Z",
    "2038-01-05T13:47:10Z", "2038-07-02T13:32:54Z", "2038-12-26T01:00:10Z", "2039-06-21T17:12:53Z", "2039-12-15T16:23:46Z",
    "2040-05-11T03:43:02Z", "2040-11-04T19:09:01Z"
  };
  // greatest eclipse and umbral (partial phase) duration in minutes
  private static final String[] LUNAR = {
    "2028-01-12T04:14:13Z 56", "2028-07-06T18:20:57Z 141", "2028-12-31T16:53:15Z 209", "2029-06-26T03:23:22Z 220",
    "2029-12-20T22:43:12Z 213", "2030-06-15T18:34:34Z 144", "2032-04-25T15:14:51Z 211", "2032-10-18T19:03:40Z 196",
    "2033-04-14T19:13:51Z 215", "2033-10-08T10:56:23Z 202", "2034-09-28T02:47:37Z 27", "2035-08-19T01:12:15Z 77",
    "2036-02-11T22:13:06Z 202", "2036-08-07T02:52:32Z 231", "2037-01-31T14:01:38Z 197", "2037-07-27T04:09:53Z 192",
    "2039-06-06T18:54:25Z 179", "2039-11-30T16:56:28Z 206", "2040-05-26T11:46:22Z 211", "2040-11-18T19:04:41Z 220"
  };

  private static final int[] HALF_WIDTHS = {3, 5, 7, 8, 9, 10, 10, 11, 11, 12, 12, 12, 12, 12, 12, 12, 11, 11, 10, 10, 9, 8, 7, 5, 3};
  private static final int TIME = 255;
  private static final int TEMP = 170;
  private static final int ICON = 210;
  private static final int SHADE = 140;
  private static final int BATTERY = 110;
  private static final int TRACK = 30;
  private static final int FAINT = 50;
  private static final int MOON_SIZE = 5;
  private static final double MOON_RADIUS = 2.4;
  private static final double NEW_MOON_DAY = 10962.76;
  private static final double SYNODIC_MONTH = 29.530588853;
  private static final int[][] BATTERY_ROW = {{14, 9}, {14, 10}}; // [zigzag, dotted][layout], between the weather and the time
  private static final int BATTERY_ALONE_ROW = 22;
  private static final int BATTERY_HALF = 8; // 17 LEDs; battery() also keeps it one LED clear of the rim
  // [zigzag battery, dotted battery, no battery][layout]: without the battery the weather and the time close up around the centre
  private static final int[][] BAND_TOP = {{4, 12}, {4, 12}, {5, 11}};
  private static final int[][] TIME_TOP = {{17, 3}, {16, 4}, {15, 5}};
  private static final int GAP = 2;
  private static final int SWEEP = 65;
  private static final int LOW_BATTERY = 15;
  private static final int BLINK = 4;
  private static final int[] DROP_TOP = {0, 22}; // next to the weather: above it, or below it when the time is on top
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
  private static final String[] DROP = {".#.", "###", "###"};
  private static final String[] UP = {".#.", "###", ".#.", ".#.", ".#."};
  private static final String[] DOWN = {".#.", ".#.", ".#.", "###", ".#."};
  private static final String[] NIGHT_CLOUD = {
    ".........",
    ".........",
    ".........",
    ".........",
    "....##...",
    "...#--##.",
    "..#-----#",
    ".#------#",
    ".#######."
  };
  private static final String[] SHOWER_CLOUD = {
    ".........",
    ".........",
    "....##...",
    "...#--##.",
    "..#-----#",
    ".#------#",
    ".#######.",
    "..+..+..+",
    ".+..+..+."
  };
  private static final String[] SMALL_SUN = {"+.+.+", ".+++.", "+++++", ".+++.", "+.+.+"};

  private static final String[][] ICONS = {
    {
      "....+....",
      ".+.....+.",
      "...###...",
      "..#+++#..",
      "+.#+++#.+",
      "..#+++#..",
      "...###...",
      ".+.....+.",
      "....+...."
    },
    {
      ".+..+....",
      "..+++....",
      ".+++++...",
      "..++##...",
      ".+.#--##.",
      "..#-----#",
      ".#------#",
      ".#######.",
      "........."
    },
    {
      ".........",
      ".........",
      "....###..",
      ".##.#--#.",
      "#--#----#",
      "#-------#",
      ".#######.",
      ".........",
      "........."
    },
    {
      ".........",
      ".#######.",
      ".........",
      "..+++++++",
      ".........",
      "#######..",
      ".........",
      ".+++++++.",
      "........."
    },
    {
      "....###..",
      ".##.#--#.",
      "#--#----#",
      "#-------#",
      ".#######.",
      ".........",
      "..+..+..+",
      ".+..+..+.",
      "........."
    },
    {
      "....###..",
      ".##.#--#.",
      "#--#----#",
      "#-------#",
      ".#######.",
      ".........",
      "..+...+..",
      ".+#+.+#+.",
      "..+...+.."
    },
    {
      "....###..",
      ".##.#--#.",
      "#--#----#",
      "#-------#",
      ".#######.",
      "....##...",
      "...##....",
      "....##...",
      "...#....."
    },
    behind(SHOWER_CLOUD, SMALL_SUN),
    {
      "....###..",
      ".##.#--#.",
      "#--#----#",
      "#-------#",
      ".#######.",
      ".........",
      ".+..+..+.",
      ".........",
      "...+..+.."
    },
    {
      "....###..",
      ".##.#--#.",
      "#--#----#",
      "#-------#",
      ".#######.",
      ".........",
      "..+...+..",
      ".+...+#+.",
      "......+.."
    },
    {
      "....#....",
      ".#.+++.#.",
      "..+---+..",
      ".+-----+.",
      "#+-----+#",
      ".+-----+.",
      "..+---+..",
      ".#.+++.#.",
      "....#...."
    },
    {
      "...###...",
      ".###++++.",
      ".##+++++.",
      "##+++++++",
      "#++++++++",
      "#++++++++",
      ".+++++++.",
      ".+++++++.",
      "...+++..."
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
    if (code >= 96) return HAIL;
    if (code == 95) return STORM;
    if (code == 56 || code == 57 || code == 66 || code == 67) return SLEET;
    if (code >= 80 && code <= 82) return SHOWERS;
    if ((code >= 71 && code <= 77) || code == 85 || code == 86) return SNOW;
    return RAIN;
  }

  // Solar eclipses show by day, lunar ones by night (the moon is up when the sun is down); -1 when none.
  public static int eclipse(long millis, boolean day) {
    for (String entry : day ? SOLAR : LUNAR) {
      String[] parts = entry.split(" ");
      long half = day ? SOLAR_HALF : Long.parseLong(parts[1]) * 30000L;
      if (Math.abs(millis - java.time.Instant.parse(parts[0]).toEpochMilli()) <= half) return day ? SOLAR_ECLIPSE : LUNAR_ECLIPSE;
    }
    return -1;
  }

  public static double moonPhase(long millis) {
    double cycles = (millis / 86400000.0 - NEW_MOON_DAY) / SYNODIC_MONTH;
    return cycles - Math.floor(cycles);
  }

  public record Scene(int hour, int minute, Integer temperature, int kind, boolean day, double moon,
      int battery, boolean charging, boolean rainSoon, long tick, int layout, boolean twelve, boolean clock, boolean animate,
      boolean showTemperature, boolean zigzag) {}

  public static long interval(Scene scene) {
    if (scene.animate() && scene.temperature() != null && (moves(scene.kind(), scene.day()) || drop(scene))) return FRAME;
    if (scene.battery() < 0) return 0;
    if (scene.charging()) return FRAME;
    return scene.battery() <= LOW_BATTERY ? FRAME * BLINK : 0;
  }

  public static int[] draw(Scene scene) {
    int[] pixels = new int[SIZE * SIZE];
    int hour = scene.hour();
    int minute = scene.minute();
    int[] digits = {scene.twelve() && hour < 10 ? -1 : hour / 10, hour % 10, minute / 10, minute % 10}; // 12 h drops the leading zero
    Integer temperature = scene.temperature();
    boolean alone = temperature == null || !scene.clock();
    if (temperature == null) { // the time alone, the battery right below it
      int rows = scene.zigzag() ? 2 : 1;
      int top = (SIZE + 1 - BIG[0].length - (scene.battery() < 0 ? 0 : 1 + rows)) / 2; // centre the time, with the battery if shown
      clock(pixels, digits, BIG, top);
      battery(pixels, scene, top + BIG[0].length + 1, rows);
      return pixels;
    }
    if (alone) battery(pixels, scene, BATTERY_ALONE_ROW, 1); // one dotted row: no room for the zigzag under the big temperature
    else battery(pixels, scene, BATTERY_ROW[scene.zigzag() ? 0 : 1][scene.layout()], scene.zigzag() ? 2 : 1);
    int kind = scene.kind();
    boolean drop = drop(scene);
    int dropLevel = scene.animate() && scene.tick() / 2 % 2 == 1 ? SHADE : ICON; // the rain alert pulses
    String[] icon = icon(kind, scene.day(), scene.moon());
    if (scene.animate()) icon = animate(icon, kind, scene.day(), scene.tick());
    String text = Integer.toString(Math.abs(temperature));
    int sign = temperature < 0 ? MINUS[0].length() + 1 : 0;
    if (alone) {
      if (drop) stamp(pixels, DROP, (SIZE - DROP[0].length()) / 2, 0, dropLevel);
      if (!scene.showTemperature()) { // the icon alone, in the middle of the matrix
        stamp(pixels, icon, (SIZE - icon[0].length()) / 2, (SIZE - icon.length) / 2, ICON);
        return pixels;
      }
      stamp(pixels, icon, (SIZE - icon[0].length()) / 2, ALONE_ICON_TOP, ICON);
      int width = sign + text.length() * (BIG[0][0].length() + 1) - 1;
      number(pixels, (SIZE - width) / 2, ALONE_TEMP_TOP, temperature, TEMP, BIG);
      return pixels;
    }
    int mode = scene.battery() < 0 ? 2 : scene.zigzag() ? 0 : 1; // zigzag battery, dotted battery, no battery
    int band = BAND_TOP[mode][scene.layout()];
    clock(pixels, digits, SMALL, TIME_TOP[mode][scene.layout()]);
    if (drop) stamp(pixels, DROP, (SIZE - DROP[0].length()) / 2, DROP_TOP[scene.layout()], dropLevel);
    if (!scene.showTemperature()) { // the icon alone, centred in its band
      stamp(pixels, icon, (SIZE - icon[0].length()) / 2, band, ICON);
      return pixels;
    }
    int width = icon[0].length() + GAP + sign + text.length() * 4 - 1; // no degree sign: the minus always fits
    int x = (SIZE - width) / 2;
    stamp(pixels, icon, x, band, ICON);
    number(pixels, x + icon[0].length() + GAP, band + (icon.length - SMALL[0].length) / 2, temperature, TEMP, SMALL);
    return pixels;
  }

  // one gallery icon, centred; tick < 0 draws it still
  public static int[] sample(int index, long tick) {
    int night = index - KINDS;
    boolean day = night < 0;
    int kind = day ? index : NIGHT_SAMPLES[night / MOON_PHASES];
    String[] icon = icon(kind, day, day ? 0 : night % MOON_PHASES / (double) MOON_PHASES);
    if (tick >= 0) icon = animate(icon, kind, day, tick);
    int[] pixels = new int[SIZE * SIZE];
    stamp(pixels, icon, (SIZE - icon[0].length()) / 2, (SIZE - icon.length) / 2, ICON);
    return pixels;
  }

  private static String[] icon(int kind, boolean day, double moon) {
    if (day) return ICONS[kind];
    if (kind == CLEAR) return centred(moon(moon, MOON_SIZE, MOON_RADIUS, '#'));
    String[] small = moon(moon, MOON_SIZE, MOON_RADIUS, '+');
    if (kind == PARTLY) return behind(NIGHT_CLOUD, small);
    return kind == SHOWERS ? behind(SHOWER_CLOUD, small) : ICONS[kind];
  }

  // the moon disc is as big as the sun's, centred in the 9 × 9 icon box
  private static String[] centred(String[] disc) {
    int size = ICONS[CLEAR].length;
    String pad = ".".repeat((size - disc.length) / 2);
    String[] rows = new String[size];
    java.util.Arrays.fill(rows, ".".repeat(size));
    for (int y = 0; y < disc.length; y++) rows[pad.length() + y] = pad + disc[y] + pad;
    return rows;
  }

  public static int[] detail(int max, int min) {
    int[] pixels = new int[SIZE * SIZE];
    extreme(pixels, UP, max, DETAIL_TOP, TIME);
    extreme(pixels, DOWN, min, DETAIL_TOP + DETAIL_STEP, TEMP);
    return pixels;
  }

  private static void extreme(int[] pixels, String[] arrow, int value, int top, int level) {
    int sign = value < 0 ? MINUS[0].length() + 1 : 0;
    int width = arrow[0].length() + 2 + sign + Integer.toString(Math.abs(value)).length() * 4 - 1;
    int x = (SIZE - width) / 2;
    stamp(pixels, arrow, x, top, level);
    number(pixels, x + arrow[0].length() + 2, top, value, level, SMALL);
  }

  private static void number(int[] pixels, int x, int top, int value, int level, String[][] font) {
    if (value < 0) {
      stamp(pixels, MINUS, x, top + (font[0].length - MINUS.length) / 2, level);
      x += MINUS[0].length() + 1;
    }
    for (char c : Integer.toString(Math.abs(value)).toCharArray()) {
      stamp(pixels, font[c - '0'], x, top, level);
      x += font[0][0].length() + 1;
    }
  }

  private static String[] moon(double phase, int size, double radius, char light) {
    String[] rows = new String[size];
    double edge = Math.cos(2 * Math.PI * phase);
    for (int y = 0; y < size; y++) {
      StringBuilder row = new StringBuilder();
      double dy = y - size / 2;
      double reach = Math.sqrt(Math.max(0, radius * radius - dy * dy));
      for (int x = 0; x < size; x++) {
        double dx = x - size / 2;
        boolean lit = phase < 0.5 ? dx > reach * edge : dx < -reach * edge;
        row.append(Math.abs(dx) > reach ? '.' : lit ? light : '-');
      }
      rows[y] = row.toString();
    }
    return rows;
  }

  private static boolean drop(Scene scene) {
    return scene.rainSoon() && (scene.kind() < RAIN || scene.kind() > SLEET);
  }

  public static boolean sampleMoves(int index) {
    return index < KINDS ? moves(index, true) : moves(NIGHT_SAMPLES[(index - KINDS) / MOON_PHASES], false);
  }

  private static boolean moves(int kind, boolean day) {
    return (kind >= FOG && kind <= SLEET) || (kind == CLEAR && day);
  }

  // One small step per frame: rays twinkle, fog drifts, the bolt flickers, rain, hail and sleet fall, snow falls slower.
  private static String[] animate(String[] icon, int kind, boolean day, long tick) {
    if (!moves(kind, day)) return icon;
    String[] rows = icon.clone();
    boolean odd = tick / 2 % 2 == 1;
    if (kind == CLEAR) {
      int[][] rays = odd ? new int[][] {{1, 1}, {7, 1}, {1, 7}, {7, 7}} : new int[][] {{4, 0}, {0, 4}, {8, 4}, {4, 8}};
      for (int[] ray : rays) {
        char[] row = rows[ray[1]].toCharArray();
        row[ray[0]] = '-';
        rows[ray[1]] = new String(row);
      }
      return rows;
    }
    if (kind == FOG) {
      if (!odd) return rows;
      for (int y = 1; y < rows.length; y += 2) {
        String row = rows[y];
        rows[y] = y % 4 == 1 ? "." + row.substring(0, row.length() - 1) : row.substring(1) + ".";
      }
      return rows;
    }
    int base = 0;
    for (int y = 0; y < rows.length; y++) if (rows[y].contains("#######")) base = y;
    if (kind == STORM) {
      if (tick % 4 == 3) for (int y = base + 1; y < rows.length; y++) rows[y] = rows[y].replace('#', '.');
      return rows;
    }
    int fall = rows.length - base - 1;
    int step = (int) ((kind == SNOW ? tick / 2 : tick) % fall);
    for (int y = 0; y < fall; y++) rows[base + 1 + (y + step) % fall] = icon[base + 1 + y];
    return rows;
  }

  // the sun or moon peeks out from the top-left corner; the cloud's faint fill hides what is behind it
  private static String[] behind(String[] cloud, String[] back) {
    String[] rows = new String[cloud.length];
    for (int y = 0; y < cloud.length; y++) {
      char[] row = cloud[y].toCharArray();
      for (int x = 0; y < back.length && x < back[y].length(); x++) if (row[x] == '.') row[x] = back[y].charAt(x);
      rows[y] = new String(row);
    }
    return rows;
  }

  // rows 2: a zigzag, even LEDs on the first row and odd ones on the second; rows 1: every other LED
  private static void battery(int[] pixels, Scene scene, int row, int rows) {
    if (scene.battery() < 0) return;
    int half = BATTERY_HALF;
    for (int y = row; y < row + rows; y++) half = Math.min(half, HALF_WIDTHS[y] - 1); // keep one LED clear of the rim
    int length = half * 2 + 1;
    int lit = Math.max(1, Math.round(Math.min(100, scene.battery()) * length / 100f));
    int sweep = scene.charging() ? (int) (scene.tick() % (length - lit + 1)) : 0;
    boolean blink = !scene.charging() && scene.battery() <= LOW_BATTERY && scene.tick() / BLINK % 2 == 1;
    for (int i = 0; i < length; i += 3 - rows) {
      int level = i < lit ? (blink ? TRACK : BATTERY) : i < lit + sweep ? SWEEP : TRACK;
      pixels[(row + i % rows) * SIZE + SIZE / 2 - half + i] = level;
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
