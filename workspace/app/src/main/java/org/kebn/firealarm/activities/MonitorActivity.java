package org.kebn.firealarm.activities;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseObject;

import org.kebn.firealarm.R;
import org.kebn.firealarm.events.RequestActiveAlarmEvent;
import org.kebn.firealarm.events.SendAlarmEvent;
import org.kebn.firealarm.events.UpdateMapMarkers;
import org.kebn.firealarm.handlers.ErrorHandler;
import org.kebn.firealarm.utils.GetGeoAddressAsync;
import org.kebn.firealarm.utils.LogUtil;
import org.kebn.firealarm.utils.MarkerUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MonitorActivity extends BaseActivity {
  private GoogleMap    googleMap;
  private Subscription updatableLocationSubscription;
  private List<Marker> markers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_monitor);
    EventBus.getDefault().register(this);
    googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    markers = new ArrayList<>();
    EventBus.getDefault().post(new RequestActiveAlarmEvent());

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  public void getMyLocation() {
    LocationRequest request = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1).setInterval(100);
    updatableLocationSubscription = getLocationProvider().getUpdatedLocation(request).subscribe(
        new Action1<Location>() {
          @Override
          public void call(Location location) {
            updateCamToLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            addMarker(new LatLng(location.getLatitude(), location.getLongitude()), null, null);
            getAddress(location);
          }
        }, new ErrorHandler());
  }

  public void getAddress(final Location location) {
    Observable<List<Address>> reverseGeocodeObservable =
        getLocationProvider().getReverseGeocodeObservable(location.getLatitude(),
            location.getLongitude(), 5);
    reverseGeocodeObservable.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<List<Address>>() {
          @Override
          public void call(List<Address> addresses) {
            List<Address> availableAddresses = new ArrayList<Address>();
            if (!addresses.isEmpty()) {
              availableAddresses.addAll(addresses);
            } else {
              try {
                availableAddresses.addAll((List) new GetGeoAddressAsync().execute(
                    location.getLatitude(), location.getLongitude()).get().getResult());
              } catch (Exception e) {
                LogUtil.e("Error", e);
              }
            }
            EventBus.getDefault().post(new SendAlarmEvent(availableAddresses.get(0), location));
          }
        }, new ErrorHandler());
  }


  private void updateCamToLocation(LatLng latLng) {
    if (googleMap != null) {
      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
  }

  private void addMarker(LatLng latLng, String title, String snippet) {
    if (googleMap != null) {
      Bitmap pin = MarkerUtil.scaleImage(getResources(), R.mipmap.fire, 50);
      markers.add(googleMap.addMarker(
          new MarkerOptions()
              .position(latLng)
              .icon(BitmapDescriptorFactory.fromBitmap(pin))
              .title(title)
              .snippet(snippet)));
      addCircle(latLng);
    }
  }

  private void addCircle(LatLng latLng) {
    if (googleMap != null) {
      CircleOptions circleOptions = (new CircleOptions().center(latLng).radius(200).fillColor(
          0x26f33d3d).strokeColor(0x80f33d3d)).strokeWidth(2);
      googleMap.addCircle(circleOptions);
    }
  }

  private void showMarkersBound() {
    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (Marker marker : markers) {
      builder.include(marker.getPosition());
    }
    final LatLngBounds bounds = builder.build();
    final int padding = 80;

    if (googleMap != null) {
      if (markers.size() == 1) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 15));
      } else {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
      }
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_monitor, menu);
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

  /**
   * Updates markers in the map
   *
   * @param event
   */
  public void onEventMainThread(UpdateMapMarkers event) {
    markers.clear();
    Iterator itr = event.parseObjects.iterator();
    while (itr.hasNext()) {
      ParseObject object = (ParseObject) itr.next();
      String title = object.getCreatedAt().toString();
      String snippet = object.getString("address");
      addMarker(new LatLng(object.getDouble("latitude"), object.getDouble("longitude")), title,
          snippet);
    }
    showMarkersBound();
  }
}
