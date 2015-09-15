package org.kebn.firealarm.events;

import com.parse.ParseObject;

/**
 * Created by Kevin on 7/12/2015.
 */
public class UpdateMapEvent {
  public ParseObject parseObject;

  public UpdateMapEvent(ParseObject object) {
    parseObject = object;
  }
}
