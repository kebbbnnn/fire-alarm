package org.kebn.firealarm.utils;

import android.support.v4.graphics.ColorUtils;

/**
 * Created by Kevin on 7/11/2015.
 */
public class ColorUtil {
  private static final float[] TEMP_HSL = new float[]{0, 0, 0};

  public static int randomColor() {
    float[] hsl = TEMP_HSL;
    hsl[0] = (float) (Math.random() * 360);
    hsl[1] = (float) (40 + (Math.random() * 60));
    hsl[2] = (float) (40 + (Math.random() * 60));
    return ColorUtils.HSLToColor(hsl);
  }
}
