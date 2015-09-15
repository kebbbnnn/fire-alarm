package org.kebn.firealarm.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Kevin on 9/5/2015.
 */
public class LocationUtil {

  public static Location createLocation(LatLng latLng) {
    Location location = new Location("FireLocation" + String.valueOf(latLng.hashCode()));
    location.setLatitude(latLng.latitude);
    location.setLongitude(latLng.longitude);
    location.setAccuracy(3333);
    location.setBearing(333);
    return location;
  }
}
