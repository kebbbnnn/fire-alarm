package org.kebn.firealarm.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import org.kebn.firealarm.FireAlarmApp;

import java.util.UUID;

/**
 * Created by Kevin on 6/20/2015.
 */
public class DeviceUtil {
  public static String getDeviceUUID() {
    final TelephonyManager tm = (TelephonyManager) FireAlarmApp.getAppContext().getSystemService(
        Context.TELEPHONY_SERVICE);

    final String tmDevice, tmSerial, androidId;
    tmDevice = "" + tm.getDeviceId();
    tmSerial = "" + tm.getSimSerialNumber();
    androidId = "" + android.provider.Settings.Secure.getString(
        FireAlarmApp.getAppContext().getContentResolver(),
        android.provider.Settings.Secure.ANDROID_ID);

    UUID deviceUuid = new UUID(androidId.hashCode(),
        ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
    return deviceUuid.toString();
  }
}
