package org.kebn.firealarm.activities;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.kebn.firealarm.FireAlarmApp;
import org.kebn.firealarm.R;
import org.kebn.firealarm.handlers.ErrorHandler;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;


public class MainActivity extends ActionBarActivity {
  private GoogleMap    googleMap;
  private Subscription updatableLocationSubscription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    LocationRequest request = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(3).setInterval(100);
    ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(
        FireAlarmApp.getAppContext());
    updatableLocationSubscription = locationProvider.getUpdatedLocation(request).subscribe(
        new Action1<Location>() {
          @Override
          public void call(Location location) {
            updateCamToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            addMarker(new LatLng(location.getLatitude(), location.getLongitude()));
            addCircle(new LatLng(location.getLatitude(), location.getLongitude()));
          }
        }, new ErrorHandler());

  }

  private void updateCamToLocation(LatLng latLng) {
    if (googleMap != null) {
      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
  }

  private void addMarker(LatLng latLng) {
    if (googleMap != null) {
      Bitmap pin = scaleImage(R.mipmap.fire, 50);
      googleMap.addMarker(new MarkerOptions().position(latLng).icon(
          BitmapDescriptorFactory.fromBitmap(pin)));
    }
  }

  private void addCircle(LatLng latLng) {
    if (googleMap != null) {
      CircleOptions circleOptions = (new CircleOptions().center(latLng).radius(200).fillColor(
          0x26f33d3d).strokeColor(0x80f33d3d)).strokeWidth(2);
      googleMap.addCircle(circleOptions);
    }
  }

  private Bitmap scaleImage(int id, int lessSideSize) {
    Resources res = getResources();
    Bitmap b = null;
    BitmapFactory.Options o = new BitmapFactory.Options();
    o.inJustDecodeBounds = true;

    BitmapFactory.decodeResource(res, id, o);

    float sc = 0.0f;
    int scale = 1;
    // if image height is greater than width
    if (o.outHeight > o.outWidth) {
      sc = o.outHeight / lessSideSize;
      scale = Math.round(sc);
    }
    // if image width is greater than height
    else {
      sc = o.outWidth / lessSideSize;
      scale = Math.round(sc);
    }

    // Decode with inSampleSize
    BitmapFactory.Options o2 = new BitmapFactory.Options();
    o2.inSampleSize = scale;
    b = BitmapFactory.decodeResource(res, id, o2);
    return b;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
