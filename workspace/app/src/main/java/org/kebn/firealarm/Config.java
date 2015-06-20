package org.kebn.firealarm;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Kevin on 6/20/2015.
 */
public class Config {
  public enum Type {
    HOST {
      @Override
      public String toString() {
        return "host";
      }
    }, CLIENT {
      @Override
      public String toString() {
        return "client";
      }
    };
  }

  private Type appType;

  public Config() {
  }

  public Config(Type type) {
    setAppType(type);
  }

  public void setAppType(Type type) {
    appType = type;
  }

  public String getAppType() {
    return appType.toString();
  }

  public static class Device {
    public static final String DEVICE = "device";

    public static void save() {
      SharedPreferences.Editor editor = FireAlarmApp.getAppContext().getSharedPreferences(DEVICE,
          Context.MODE_PRIVATE).edit();
      editor.putBoolean("saved", true);
      editor.commit();
    }


    public static boolean restore() {
      SharedPreferences preferences = FireAlarmApp.getAppContext().getSharedPreferences(DEVICE,
          Context.MODE_PRIVATE);
      return preferences.getBoolean("saved", false);
    }

    public static void clear() {
      SharedPreferences.Editor editor = FireAlarmApp.getAppContext().getSharedPreferences(DEVICE,
          Context.MODE_PRIVATE).edit();
      editor.clear();
      editor.commit();
    }
  }
}
