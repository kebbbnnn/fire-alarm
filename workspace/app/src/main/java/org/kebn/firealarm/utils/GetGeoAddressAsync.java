package org.kebn.firealarm.utils;

import android.location.Address;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kebn.firealarm.handlers.AsyncTaskHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kevin on 6/13/2015.
 *
 * Hardcoded option when reactivelocation fails to get geo addresses
 */
public class GetGeoAddressAsync extends AsyncTask<Double, Integer, AsyncTaskHandler<Object>> {

  @Override
  protected AsyncTaskHandler<Object> doInBackground(Double... doubles) {
    try {
      String address = String
          .format(Locale.ENGLISH,
              "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f," +
                  "%2$f&sensor=true&language="
                  + Locale.getDefault().getCountry(), doubles[0], doubles[1]);
      HttpGet httpGet = new HttpGet(address);
      HttpClient client = new DefaultHttpClient();
      HttpResponse response;
      StringBuilder stringBuilder = new StringBuilder();

      List<Address> retList = null;

      response = client.execute(httpGet);
      HttpEntity entity = response.getEntity();
      InputStream stream = entity.getContent();
      int b;
      while ((b = stream.read()) != -1) {
        stringBuilder.append((char) b);
      }

      JSONObject jsonObject = new JSONObject(stringBuilder.toString());
      retList = new ArrayList<Address>();
      if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
        JSONArray results = jsonObject.getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
          JSONObject result = results.getJSONObject(i);
          String indiStr = result.getString("formatted_address");
          Address addr = new Address(Locale.getDefault());
          addr.setAddressLine(0, indiStr);
          //getting the location
          JSONObject geometry = result.getJSONObject("geometry");
          JSONObject location = geometry.getJSONObject("location");
          addr.setLatitude(location.getDouble("lng"));
          addr.setLongitude(location.getDouble("lat"));

          retList.add(addr);
        }
      }
      return new AsyncTaskHandler<Object>(retList);
    } catch (ClientProtocolException e1) {
      return new AsyncTaskHandler<>(e1);
    } catch (IOException e2) {
      return new AsyncTaskHandler<>(e2);
    } catch (JSONException e3) {
      return new AsyncTaskHandler<>(e3);
    }
  }

  @Override
  protected void onPostExecute(AsyncTaskHandler<Object> result) {
    if (result.getError() != null) {
      LogUtil.e("Error", result.getError());
    }
  }
}