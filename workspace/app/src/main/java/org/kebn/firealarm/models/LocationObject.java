package org.kebn.firealarm.models;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 9/6/2015.
 */
public class LocationObject {
  public List<ParseObject> objects;

  public LocationObject() {
    this.objects = new ArrayList<>();
  }

  public LocationObject add(ParseObject parseObject) {
    if (this.objects == null) {
      this.objects = new ArrayList<>();
    }
    this.objects.add(parseObject);
    return this;
  }
}
