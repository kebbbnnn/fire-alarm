package org.kebn.firealarm.activities;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.material.widget.CircleButton;
import com.material.widget.PaperButton;

import org.kebn.firealarm.R;
import org.kebn.firealarm.events.AlarmSentEvent;
import org.kebn.firealarm.events.SendAlarmEvent;
import org.kebn.firealarm.handlers.ErrorHandler;
import org.kebn.firealarm.utils.AddressUtil;
import org.kebn.firealarm.utils.GetGeoAddressAsync;
import org.kebn.firealarm.utils.MarkerUtil;
import org.kebn.firealarm.widget.CardViewPlus;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SendAlarmActivity extends BaseActivity {

  private Subscription updatableLocationSubscription;
  private Location     currentLocation;
  private Address      currentAddress;

  private CircleButton   fabButton;
  private RelativeLayout mapContainer;
  private PaperButton    buttonSend;
  private CardViewPlus   cardReveal;

  private GoogleMap googleMap;

  private static final Style INFO;
  private static final Style ALERT;
  private static final Style CONFIRM;

  private boolean canDisplay = false;

  private SupportAnimator mAnimator;

  static {
    INFO = new Style.Builder().setBackgroundColorValue(0xff6dad69).build();
    ALERT = new Style.Builder().setBackgroundColorValue(0xffb6003b).build();
    CONFIRM = new Style.Builder().setBackgroundColorValue(0xff1a4b73).build();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
    if (getSupportActionBar() != null) { getSupportActionBar().hide(); }
    final View root = View.inflate(this, R.layout.activity_send_alarm, null);
    initViews(root);
    attachListeners();
    displayCrouton();
    setupGoogleMap();
    changeFabTransform(root);
    getMyLocation();
    initialCardRevealState();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }


  /**
   * initializes the views of this {@link android.app.Activity}
   */
  private void initViews(View root) {
    setContentView(root);
    mapContainer = (RelativeLayout) findViewById(R.id.layout_map_container);
    fabButton = (CircleButton) findViewById(R.id.circle_button);
    buttonSend = (PaperButton) findViewById(R.id.button_send);
    cardReveal = (CardViewPlus) findViewById(R.id.card_reveal);
  }

  /**
   * attaches listeners to {@link android.view.View}
   */
  private void attachListeners() {
    buttonSend.setOnClickListener(clickListener);
    fabButton.setOnClickListener(clickListener);
  }

  /**
   * initial state of {@link org.kebn.firealarm.widget.CardViewPlus}
   */
  private void initialCardRevealState() {
    mAnimator = defaultReveal();
    mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    mAnimator.setDuration(500);
    mAnimator.reverse();
    mAnimator = null;
  }

  /**
   * default reveal animation
   *
   * @return
   */
  private SupportAnimator defaultReveal() {
    int cx = cardReveal.getRight();
    int cy = cardReveal.getBottom();
    float finalRadius = hypo(cardReveal.getWidth(), cardReveal.getHeight());

    SupportAnimator animator = ViewAnimationUtils.createCircularReveal(cardReveal, cx, cy, 0,
        finalRadius);
    animator.addListener(new SupportAnimator.AnimatorListener() {
      @Override
      public void onAnimationStart() {
        cardReveal.setVisibility(View.VISIBLE);
      }

      @Override
      public void onAnimationEnd() {
      }

      @Override
      public void onAnimationCancel() {
      }

      @Override
      public void onAnimationRepeat() {
      }
    });
    return animator;
  }

  /**
   * animates reveal animation to {@link org.kebn.firealarm.widget.CardViewPlus}
   */
  private void cardRevealAnimation() {
    if (mAnimator != null && !mAnimator.isRunning()) {
      mAnimator = mAnimator.reverse();
      mAnimator.addListener(new SupportAnimator.AnimatorListener() {
        @Override
        public void onAnimationStart() {

        }

        @Override
        public void onAnimationEnd() {
          mAnimator = null;
          cardReveal.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel() {

        }

        @Override
        public void onAnimationRepeat() {

        }
      });
    } else if (mAnimator != null) {
      mAnimator.cancel();
      return;
    } else {
      mAnimator = defaultReveal();
    }

    mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    mAnimator.setDuration(500);
    mAnimator.start();
  }

  static float hypo(int a, int b) {
    return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
  }


  /**
   * changes the transform of {@link com.material.widget.CircleButton}
   *
   * @param root
   */
  private void changeFabTransform(final View root) {
    root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
        .OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {  // Removing global
          root.getViewTreeObserver().removeOnGlobalLayoutListener(this);// layout listener
        } else {                                                        // to avoid recursive
          root.getViewTreeObserver().removeGlobalOnLayoutListener(this);// method invocation
        }
        int mapHeight = mapContainer.getMeasuredHeight();
        int fabHeight = fabButton.getMeasuredHeight();
        RelativeLayout.LayoutParams params =
            (RelativeLayout.LayoutParams) fabButton.getLayoutParams();
        params.topMargin = mapHeight - (fabHeight / 2);
        fabButton.setLayoutParams(params);
      }
    });
  }

  /**
   * Displays the {@link de.keyboardsurfer.android.widget.crouton.Crouton} tip
   */
  private void displayCrouton() {
    Configuration.Builder builder = new Configuration.Builder();
    builder.setDuration(Configuration.DURATION_LONG);
    Crouton crouton = Crouton.make(this, View.inflate(this, R.layout.layout_message, null),
        R.id.root_layout,
        builder.build());
    crouton.setLifecycleCallback(new LifecycleCallback() {
      @Override
      public void onDisplayed() {
        canDisplay = false;
      }

      @Override
      public void onRemoved() {
        canDisplay = true;
      }
    });
    crouton.show();
  }


  /**
   * sets up {@link com.google.android.gms.maps.GoogleMap}
   */
  private void setupGoogleMap() {
    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == 2) {
      GooglePlayServicesUtil.getErrorDialog(2, this, 1000);
    } else {
      if (googleMap == null) {
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMyLocationEnabled(false);
        googleMap.setTrafficEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
          @Override
          public void onMarkerDragStart(Marker marker) {

          }

          @Override
          public void onMarkerDrag(Marker marker) {

          }

          @Override
          public void onMarkerDragEnd(Marker marker) {
            updateCamToLocation(marker.getPosition());
            updateCurrentLocation(marker.getPosition());
          }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
          @Override
          public void onMapClick(LatLng latLng) {
            googleMap.clear();
            updateCamToLocation(latLng);
            updateCurrentLocation(latLng);
            addMarker(latLng);
          }
        });
      }
    }
  }


  /**
   * fetches your current {@link android.location.Location}
   */
  public void getMyLocation() {
    LocationRequest request = LocationRequest.create().setPriority(
        LocationRequest.PRIORITY_HIGH_ACCURACY).setNumUpdates(1).setInterval(100);
    updatableLocationSubscription = getLocationProvider().getUpdatedLocation(request).subscribe(
        new Action1<Location>() {
          @Override
          public void call(Location location) {
            if (location != null) {
              currentLocation = location;
              getAddress(currentLocation);
              LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
              updateCamToLocation(latLng);
              addMarker(latLng);
            }
          }
        }, new ErrorHandler() {
          @Override
          public void call(Throwable throwable) {
            super.call(throwable);
            errorMessage("location");
          }
        });
  }

  /**
   * Updates {@link android.location.Location}
   */
  private void updateCurrentLocation(LatLng latLng) {
    if (currentLocation == null) {
      currentLocation = new Location("default-location");
    }
    currentLocation.setLatitude(latLng.latitude);
    currentLocation.setLongitude(latLng.longitude);
    getAddress(currentLocation);
  }

  /**
   * Updates camera to fetched {@link android.location.Location}
   *
   * @param latLng
   */
  private void updateCamToLocation(LatLng latLng) {
    if (googleMap != null) {
      googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
  }

  /**
   * Adds {@link com.google.android.gms.maps.model.Marker} to map
   *
   * @param latLng
   */
  private void addMarker(LatLng latLng) {
    if (googleMap != null) {
      Bitmap pin = MarkerUtil.scaleImage(getResources(), R.drawable.flat_pin, 80);
      googleMap.addMarker(
          new MarkerOptions()
              .position(latLng)
              .draggable(true)
              .icon(BitmapDescriptorFactory.fromBitmap(pin)));
    }
  }


  /**
   * fetches the address of current {@link android.location.Location}
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
                errorMessage("address");
              }
            }
            currentAddress = availableAddresses.get(0);
            croutonizeAddress(AddressUtil.getCompleteAddress(currentAddress));
          }
        }, new ErrorHandler() {
          @Override
          public void call(Throwable throwable) {
            super.call(throwable);
            errorMessage("address");
          }
        });
  }

  /**
   * Display error message
   *
   * @param apiString
   */
  private void errorMessage(String apiString) {
    Crouton.makeText(SendAlarmActivity.this, "Error occurred while fetching " + apiString + ".",
        ALERT).show();
  }

  /**
   * Shows address in {@link de.keyboardsurfer.android.widget.crouton.Crouton}
   *
   * @param address
   */
  private void croutonizeAddress(String address) {
    if (canDisplay) { Crouton.cancelAllCroutons(); }
    Crouton.makeText(SendAlarmActivity.this, address, INFO).show();
  }

  public void onEventMainThread(AlarmSentEvent event) {
    new MaterialDialog.Builder(SendAlarmActivity.this)
        .cancelable(false)
        .content("Fire alarm successfully sent!")
        .positiveText(android.R.string.ok)
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(MaterialDialog dialog) {
            super.onPositive(dialog);
          }
        }).show();
  }

  private View.OnClickListener clickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      switch (view.getId()) {
      case R.id.button_send:
        if (currentLocation != null && currentAddress != null) {
          EventBus.getDefault().post(new SendAlarmEvent(currentAddress, currentLocation));
        } else {
          Crouton.makeText(SendAlarmActivity.this, "Wait until we finish fetching your location.",
              CONFIRM).show();
        }
        break;
      case R.id.circle_button:
        cardRevealAnimation();
        break;
      }
    }
  };
}
