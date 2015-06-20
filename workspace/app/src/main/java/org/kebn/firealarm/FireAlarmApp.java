package org.kebn.firealarm;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

/**
 * Created by Kevin on 6/12/2015.
 */
public class FireAlarmApp extends Application {
  private static Context context;
  public static  Config  config;

  @Override
  public void onCreate() {
    super.onCreate();
    /** Initialize static context**/
    FireAlarmApp.context = getApplicationContext();
    /**init app type**/
    config = new Config(Config.Type.CLIENT);
    /** Initialize Parse or this app**/
    Parse.initialize(this, getString(R.string.applicationId), getString(R.string.clientId));
    /**register device type**/
    registerDevice();
  }

  /**
   * Gets context for this app
   *
   * @return context
   */
  public static Context getAppContext() {
    return FireAlarmApp.context;
  }

  private void registerDevice() {
    ParseInstallation.getCurrentInstallation().saveInBackground();
    ParsePush.subscribeInBackground(config.getAppType().equals(Config.Type.HOST.toString())
        ? "host-channel" : "client-channel");
  }

}
