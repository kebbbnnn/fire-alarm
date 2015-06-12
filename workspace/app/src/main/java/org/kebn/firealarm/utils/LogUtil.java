package org.kebn.firealarm.utils;

import android.util.Log;

/**
 * Wraps android.util.Log methods for convenience.
 *
 * @author null
 */
public class LogUtil {

  /**
   * Determines if printing data in LogCat is enabled.
   *
   * true - DEBUG MODE false - RELEASE MODE
   *
   * Set this to true to enable log output. Remember to turn this back off before releasing. Sending
   * sensitive data to log is a security risk.
   */
  public static final boolean ENABLE_LOG = true;
  public static final String  TAG        = "FireAlarm";

  public static void d(String msg) {
    if (ENABLE_LOG) {
      Log.d(TAG, msg);
    }
  }

  public static void d(String tag, String msg) {
    if (ENABLE_LOG) {
      Log.d(tag, msg);
    }
  }

  public static void d(String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.d(TAG, msg, tr);
    }
  }

  public static void d(String tag, String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.d(tag, msg, tr);
    }
  }

  public static void e(String msg) {
    if (ENABLE_LOG) {
      Log.e(TAG, msg);
    }
  }

  public static void e(String tag, String msg) {
    if (ENABLE_LOG) {
      Log.e(tag, msg);
    }
  }

  public static void e(String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.e(TAG, msg, tr);
    }
  }

  public static void e(String tag, String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.e(tag, msg, tr);
    }
  }

  public static void i(String msg) {
    if (ENABLE_LOG) {
      Log.i(TAG, msg);
    }
  }

  public static void i(String tag, String msg) {
    if (ENABLE_LOG) {
      Log.i(tag, msg);
    }
  }

  public static void i(String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.i(TAG, msg, tr);
    }
  }

  public static void i(String tag, String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.i(tag, msg, tr);
    }
  }

  public static void v(String msg) {
    if (ENABLE_LOG) {
      Log.v(TAG, msg);
    }
  }

  public static void v(String tag, String msg) {
    if (ENABLE_LOG) {
      Log.v(tag, msg);
    }
  }

  public static void v(String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.v(TAG, msg, tr);
    }
  }

  public static void v(String tag, String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.v(tag, msg, tr);
    }
  }

  public static void w(String msg) {
    if (ENABLE_LOG) {
      Log.w(TAG, msg);
    }
  }

  public static void w(String tag, String msg) {
    if (ENABLE_LOG) {
      Log.w(tag, msg);
    }
  }

  public static void w(String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.w(TAG, msg, tr);
    }
  }

  public static void w(String tag, String msg, Throwable tr) {
    if (ENABLE_LOG) {
      Log.w(tag, msg, tr);
    }
  }
}
