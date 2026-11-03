package app.glyphcast;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class MatrixView extends View {
  private static final int OFF = 34;
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private int[] pixels = new int[Face.SIZE * Face.SIZE];

  public MatrixView(Context context) {
    super(context);
  }

  void show(int[] next) {
    pixels = next;
    invalidate();
  }

  @Override
  protected void onMeasure(int width, int height) {
    int size = MeasureSpec.getSize(width);
    setMeasuredDimension(size, size);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    float half = getWidth() / 2f;
    float cell = getWidth() / (float) Face.SIZE;
    paint.setColor(Color.BLACK);
    canvas.drawCircle(half, half, half, paint);
    for (int y = 0; y < Face.SIZE; y++) {
      for (int x = 0; x < Face.SIZE; x++) {
        if (!Face.led(x, y)) continue;
        int pixel = pixels[y * Face.SIZE + x];
        int level = pixel == 0 ? OFF : OFF + pixel * (255 - OFF) / 255;
        paint.setColor(Color.rgb(level, level, level));
        canvas.drawCircle((x + 0.5f) * cell, (y + 0.5f) * cell, cell * 0.38f, paint);
      }
    }
  }
}
