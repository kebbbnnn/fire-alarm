package org.kebn.firealarm.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.kebn.firealarm.FireAlarmApp;
import org.kebn.firealarm.events.AlarmSentEvent;
import org.kebn.firealarm.events.InvokePushNotifEvent;
import org.kebn.firealarm.events.RequestActiveAlarmEvent;
import org.kebn.firealarm.events.SendAlarmEvent;
import org.kebn.firealarm.events.UpdateMapMarkers;
import org.kebn.firealarm.utils.AddressUtil;
import org.kebn.firealarm.utils.LogUtil;

import java.util.List;

import de.greenrobot.event.EventBus;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;


/**
 * Created by Kevin on 6/13/2015.
 */
public class BaseActivity extends ActionBarActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  /**
   * Gets the location provider for this app
   *
   * @return the location provider
   */
  public ReactiveLocationProvider getLocationProvider() {
    return new ReactiveLocationProvider(
        FireAlarmApp.getAppContext());
  }

  /**
   * Receives event that will send data to Parse API
   *
   * @param event
   */
  public void onEventMainThread(SendAlarmEvent event) {
    if (event.address == null) { return; }
    ParseObject alarmData = new ParseObject("AlarmData");
    alarmData.put("latitude", event.location.getLatitude());
    alarmData.put("longitude", event.location.getLongitude());
    alarmData.put("address", AddressUtil.getCompleteAddress(event.address));
    alarmData.put("active", true);
    alarmData.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        EventBus.getDefault().post(new AlarmSentEvent(e));
        EventBus.getDefault().post(new InvokePushNotifEvent(e));
      }
    });
  }

  /**
   * Send data request to Parse API
   *
   * @param request
   */
  public void onEventMainThread(RequestActiveAlarmEvent request) {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("AlarmData");
    query.whereEqualTo("active", true);
    query.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> parseObjects, ParseException e) {
        if (e == null) {
          EventBus.getDefault().post(new UpdateMapMarkers(parseObjects));
        }
      }
    });
  }

  public void onEventMainThread(InvokePushNotifEvent event) {
    if (event.exception != null) { return; }
    ParsePush push = new ParsePush();
    push.setChannel("host-channel");
    push.setMessage("New fire alert receive!");
    push.sendInBackground(new SendCallback() {
      @Override
      public void done(ParseException e) {
        LogUtil.e("push notification sent!");
      }
    });
  }
}
