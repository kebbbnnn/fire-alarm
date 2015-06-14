package org.kebn.firealarm.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Kevin on 6/13/2015.
 */
public class MarkerUtil {
  public static Bitmap scaleImage(Resources res, int id, int lessSideSize) {
    Bitmap b = null;
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inJustDecodeBounds = true;

    BitmapFactory.decodeResource(res, id, o);

    float sc = 0.0f;
    int scale = 1;
    // if image height is greater than width
    if (o.outHeight > o.outWidth) {
      sc = o.outHeight / lessSideSize;
      scale = Math.round(sc);
    }
    // if image width is greater than height
    else {
      sc = o.outWidth / lessSideSize;
      scale = Math.round(sc);
    }

    // Decode with inSampleSize
    BitmapFactory.Options o2 = new BitmapFactory.Options();
    o2.inSampleSize = scale;
    b = BitmapFactory.decodeResource(res, id, o2);
    return b;
  }

}
