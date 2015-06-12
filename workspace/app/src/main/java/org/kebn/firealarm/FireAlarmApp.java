package org.kebn.firealarm;

import android.app.Application;
import android.content.Context;

/**
 * Created by Kevin on 6/12/2015.
 */
public class FireAlarmApp extends Application {
  private static Context context;

  @Override
  public void onCreate() {
    super.onCreate();
    FireAlarmApp.context = getApplicationContext();
  }

  public static Context getAppContext() {
    return FireAlarmApp.context;
  }
}
