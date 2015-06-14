package org.kebn.firealarm.utils;

import android.location.Address;
import android.text.TextUtils;

/**
 * Created by Kevin on 6/13/2015.
 */
public class AddressUtil {
  /**
   * Gets a string formatted version of the address
   *
   * @param address
   * @return
   */
  public static String getCompleteAddress(Address address) {
    if (address.getMaxAddressLineIndex() == 0) {
      return address.getAddressLine(0);
    }
    StringBuilder str = new StringBuilder();
    if (!TextUtils.isEmpty(address.getAddressLine(0))) {
      str.append(address.getAddressLine(0) + ",");
    }
    if (!TextUtils.isEmpty(address.getLocality())) {
      str.append(address.getLocality() + ",");
    }
    if (!TextUtils.isEmpty(address.getAdminArea())) {
      str.append(address.getSubAdminArea() + ",");
    }
    if (!TextUtils.isEmpty(address.getCountryName())) {
      str.append(address.getCountryName());
    }
    return str.toString();
  }
}
