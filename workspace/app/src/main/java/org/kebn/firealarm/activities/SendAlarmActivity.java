package org.kebn.firealarm.activities;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.material.widget.CircleButton;

import org.kebn.firealarm.R;
import org.kebn.firealarm.events.AlarmSentEvent;
import org.kebn.firealarm.events.SendAlarmEvent;
import org.kebn.firealarm.handlers.ErrorHandler;
import org.kebn.firealarm.utils.AddressUtil;
import org.kebn.firealarm.utils.GetGeoAddressAsync;
import org.kebn.firealarm.utils.LogUtil;
import org.kebn.firealarm.views.StatefullButton;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SendAlarmActivity extends BaseActivity {
  private Subscription    updatableLocationSubscription;
  private Location        currentLocation;
  private Address         currentAddress;
  private StatefullButton activityButton;
  private TextView        addressTextView;

  private CircleButton   fabButton;
  private RelativeLayout mapContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
    if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
    View v = View.inflate(this, R.layout.activity_send_alarm, null);
    setContentView(v);
    mapContainer = (RelativeLayout) findViewById(R.id.layout_map_container);
    fabButton = (CircleButton) findViewById(R.id.circle_button);
//    activityButton = (StatefullButton) findViewById(R.id.button_send);
//    addressTextView = (TextView) findViewById(R.id.text_address);

//    fetchingLocation();
    v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
        .OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        int mapHeight = mapContainer.getMeasuredHeight();
        int fabHeight = fabButton.getMeasuredHeight();
        RelativeLayout.LayoutParams params =
            (RelativeLayout.LayoutParams) fabButton.getLayoutParams();
        params.topMargin = mapHeight - (fabHeight / 2);
        LogUtil.e("margin = " + params.topMargin);
        fabButton.setLayoutParams(params);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  private void fetchingLocation() {
    activityButton.setOnStateListener(new StatefullButton.OnStateListener() {
      @Override
      public void onPreparing() {
        if (currentLocation == null) {
          getMyLocation();
        } else {
          if (currentAddress == null) {
            getAddress(currentLocation);
          }
        }
      }

      @Override
      public void onSend() {
        EventBus.getDefault().post(new SendAlarmEvent(currentAddress, currentLocation));
      }

      @Override
      public void onRetry() {
        activityButton.setState(StatefullButton.State.FETCHING);
      }
    });
    //setting states should be called after setting an onStateListener
    activityButton.setState(StatefullButton.State.FETCHING);
  }

  /**
   * fetches your current location
   */
  public void getMyLocation() {
    LocationRequest request = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1).setInterval(100);
    updatableLocationSubscription = getLocationProvider().getUpdatedLocation(request).subscribe(
        new Action1<Location>() {
          @Override
          public void call(Location location) {
            currentLocation = location;
            if (currentLocation == null) {
              activityButton.setState(StatefullButton.State.FAILED);
            } else {
              getAddress(currentLocation);
            }
          }
        }, new ErrorHandler() {
          @Override
          public void call(Throwable throwable) {
            super.call(throwable);
            activityButton.setState(StatefullButton.State.FAILED);
          }
        });
  }

  /**
   * fetches the address of current location
   *
   * @param location
   */
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
                activityButton.setState(StatefullButton.State.FAILED);
                LogUtil.e("Error", e);
              }
            }
            currentAddress = availableAddresses.get(0);
            addressTextView.setText(AddressUtil.getCompleteAddress(currentAddress));
            activityButton.setState(StatefullButton.State.READY);
          }
        }, new ErrorHandler() {
          @Override
          public void call(Throwable throwable) {
            super.call(throwable);
            activityButton.setState(StatefullButton.State.FAILED);
          }
        });
  }


  public void onEventMainThread(AlarmSentEvent event) {
    activityButton.setEnabled(false);
    activityButton.setText("Alarm Sent!");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_send_alarm, menu);
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
