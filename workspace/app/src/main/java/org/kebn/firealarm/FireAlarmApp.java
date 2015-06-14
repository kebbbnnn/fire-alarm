package org.kebn.firealarm;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;

/**
 * Created by Kevin on 6/12/2015.
 */
public class FireAlarmApp extends Application {
  private static Context context;

  @Override
  public void onCreate() {
    super.onCreate();
    /** Initialize Parse or this app**/
    Parse.initialize(this, getString(R.string.applicationId), getString(R.string.clientId));
    /** Initialize static context**/
    FireAlarmApp.context = getApplicationContext();
  }

  /**
   * Gets context for this app
   *
   * @return context
   */
  public static Context getAppContext() {
    return FireAlarmApp.context;
  }
}
