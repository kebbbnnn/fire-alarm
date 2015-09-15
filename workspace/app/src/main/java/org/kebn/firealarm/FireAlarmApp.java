package org.kebn.firealarm;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

import org.kebn.firealarm.utils.LogUtil;

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
    /** Enable Crash Reporting **/
    ParseCrashReporting.enable(this);
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
    LogUtil.e("Point A");
    ParseInstallation.getCurrentInstallation().saveInBackground();
    ParsePush.subscribeInBackground(config.getAppType().equals(Config.Type.HOST.toString())
        ? "host-channel" : "client-channel");
  }

}
