package org.kebn.firealarm.events;

import com.parse.ParseException;

/**
 * Created by Kevin on 6/14/2015.
 */
public class AlarmSentEvent {
  public ParseException exception;

  public AlarmSentEvent(ParseException exception) {
    this.exception = exception;
  }
}
