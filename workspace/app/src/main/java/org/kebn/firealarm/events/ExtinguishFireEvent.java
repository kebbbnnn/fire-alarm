package org.kebn.firealarm.events;

import org.kebn.firealarm.models.LocationObject;

/**
 * Created by Kevin on 7/12/2015.
 */
public class ExtinguishFireEvent {
  public LocationObject parseObject;

  public ExtinguishFireEvent(LocationObject object) {
    parseObject = object;
  }
}
