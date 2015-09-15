package org.kebn.firealarm.events;

import com.parse.ParseObject;

/**
 * Created by Kevin on 8/23/2015.
 */
public class SendNotifToClientEvent {
  public ParseObject parseObject;

  public SendNotifToClientEvent(ParseObject parseObject) {
    this.parseObject = parseObject;
  }
}
