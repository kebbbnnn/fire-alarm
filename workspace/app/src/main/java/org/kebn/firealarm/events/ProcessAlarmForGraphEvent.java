package org.kebn.firealarm.events;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Kevin on 7/28/2015.
 */
public class ProcessAlarmForGraphEvent {
  public List<ParseObject> parseObjects;

  public ProcessAlarmForGraphEvent(List<ParseObject> parseObjects) {
    this.parseObjects = parseObjects;
  }
}
