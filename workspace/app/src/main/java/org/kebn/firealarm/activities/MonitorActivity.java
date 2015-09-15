package org.kebn.firealarm.activities;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.kebn.firealarm.R;
import org.kebn.firealarm.events.ExtinguishFireEvent;
import org.kebn.firealarm.events.RequestActiveAlarmEvent;
import org.kebn.firealarm.events.SendNotifToClientEvent;
import org.kebn.firealarm.events.UpdateMapEvent;
import org.kebn.firealarm.events.UpdateMapMarkers;
import org.kebn.firealarm.models.LocationObject;
import org.kebn.firealarm.utils.LocationUtil;
import org.kebn.firealarm.utils.LogUtil;
import org.kebn.firealarm.utils.MarkerUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Subscription;

public class MonitorActivity extends BaseActivity {
  private GoogleMap                       googleMap;
  private Subscription                    updatableLocationSubscription;
  private List<Marker>                    markers;
  private HashMap<Marker, LocationObject> hashMap;

  private MaterialDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
    setContentView(R.layout.activity_monitor);
    EventBus.getDefault().register(this);
    googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
      @Override
      public View getInfoWindow(Marker marker) {
        return null;
      }

      @Override
      public View getInfoContents(Marker marker) {
        View v = View.inflate(MonitorActivity.this, R.layout.layout_extinguish_fire, null);
        TextView textTitle = (TextView) v.findViewById(R.id.text_title);
        TextView textSnippet = (TextView) v.findViewById(R.id.text_snippet);
        textTitle.setText(marker.getTitle());
        textSnippet.setText(marker.getSnippet());
        googleMap.setOnInfoWindowClickListener(windowClickListener);
        return v;
      }
    });

    progressDialog = new MaterialDialog.Builder(this).content("refreshing map").progress(true, 0)
        .build();

    markers = new ArrayList<>();
    hashMap = new HashMap<>();
    EventBus.getDefault().post(new RequestActiveAlarmEvent());

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  private void addMarker(LatLng latLng, String title, String snippet, ParseObject object) {
    if (googleMap != null) {
      addMarkerRequestEvent(latLng, title, snippet, object);
    }
  }

  public void addMarkerRequestEvent(LatLng latLng, String title, String snippet,
      ParseObject object) {
    for (Marker m : markers) {
      ParseObject po = hashMap.get(m).objects.get(hashMap.get(m).objects.size() - 1);
      Location loc1 = LocationUtil.createLocation(new LatLng(po.getDouble("latitude"),
          po.getDouble("longitude")));
      Location loc2 = LocationUtil.createLocation(latLng);
      float distance = loc1.distanceTo(loc2);
      if (distance <= 10f) {
        LocationObject lo = hashMap.get(m);
        if (lo.objects == null) {
          lo.objects = new ArrayList<>();
        }
        lo.objects.add(object);
        return;
      }
    }
    addMarkerResultEvent(latLng, title, snippet, object);
  }

  public void addMarkerResultEvent(LatLng latLng, String title, String snippet,
      ParseObject object) {
    String sn = snippet.substring(0, snippet.indexOf(','));
    Bitmap pin = MarkerUtil.scaleImage(getResources(), R.drawable.burning_house, 70);
    Marker marker = googleMap.addMarker(
        new MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromBitmap(pin))
            .title(title)
            .snippet(sn));
    hashMap.put(marker, new LocationObject().add(object));
    markers.add(marker);
    addCircle(latLng);
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
    if (markers.isEmpty()) { return; }
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


  /**
   * Updates markers in the map
   *
   * @param event
   */
  public void onEventMainThread(UpdateMapMarkers event) {
    if (progressDialog.isShowing()) { progressDialog.dismiss(); }
    markers.clear();
    googleMap.clear();
    Iterator itr = event.parseObjects.iterator();
    SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
    while (itr.hasNext()) {
      ParseObject object = (ParseObject) itr.next();
      String title = dt.format(object.getCreatedAt());
      String snippet = object.getString("address");
      addMarker(new LatLng(object.getDouble("latitude"), object.getDouble("longitude")), title,
          snippet, object);
    }
    showMarkersBound();
  }

  public void onEventMainThread(UpdateMapEvent event) {
    EventBus.getDefault().post(new RequestActiveAlarmEvent());
    EventBus.getDefault().post(new SendNotifToClientEvent(event.parseObject));
  }

  public void onEventMainThread(SendNotifToClientEvent event) {
    LogUtil.e("Sending notification to client");
    String installationId = event.parseObject.getString("installationId");
    ParseQuery query = ParseInstallation.getQuery();
    query.whereEqualTo("installationId", installationId);

    ParsePush push = new ParsePush();
    push.setQuery(query);
    //push.setChannel("client-channel");
    push.setMessage("Fire has been pulled out!");
    push.sendInBackground(new SendCallback() {
      @Override
      public void done(ParseException e) {
        LogUtil.e("push notification sent!");
      }
    });
  }


  private GoogleMap.OnInfoWindowClickListener windowClickListener =
      new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
          marker.hideInfoWindow();
          progressDialog.show();
          EventBus.getDefault().post(new ExtinguishFireEvent(hashMap.get(marker)));
        }
      };
}
