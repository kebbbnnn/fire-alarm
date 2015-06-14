package org.kebn.firealarm.events;

import android.location.Address;
import android.location.Location;

/**
 * Created by Kevin on 6/13/2015.
 */
public class SendAlarmEvent {
  public Address  address;
  public Location location;

  public SendAlarmEvent(Address address, Location location) {
    this.address = address;
    this.location = location;
  }
}
