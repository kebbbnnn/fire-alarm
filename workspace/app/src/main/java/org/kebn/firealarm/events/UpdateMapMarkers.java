package org.kebn.firealarm.events;

import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Kevin on 6/13/2015.
 */
public class UpdateMapMarkers {
  public List<ParseObject> parseObjects;

  public UpdateMapMarkers(List<ParseObject> parseObjects) {
    this.parseObjects = parseObjects;
  }
}
