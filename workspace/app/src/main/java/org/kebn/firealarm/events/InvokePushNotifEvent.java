package org.kebn.firealarm.events;


import com.parse.ParseException;

/**
 * Created by Kevin on 6/20/2015.
 */
public class InvokePushNotifEvent {
  public ParseException exception;

  public InvokePushNotifEvent(ParseException e) {
    this.exception = e;
  }
}
