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
import com.parse.ParseObject;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.kebn.firealarm.R;
import org.kebn.firealarm.events.AlarmSentEvent;
import org.kebn.firealarm.events.ProcessAlarmForGraphEvent;
import org.kebn.firealarm.events.RequestAlarmEvent;
import org.kebn.firealarm.events.SendAlarmEvent;
import org.kebn.firealarm.handlers.ErrorHandler;
import org.kebn.firealarm.utils.AddressUtil;
import org.kebn.firealarm.utils.GetGeoAddressAsync;
import org.kebn.firealarm.utils.MarkerUtil;
import org.kebn.firealarm.widget.CardViewPlus;
import org.kebn.firealarm.widget.WhatToDoDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

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

  private static final List<String> months;

  private Subscription updatableLocationSubscription;
  private Location     currentLocation;
  private Address      currentAddress;

  private CircleButton   fabButton;
  private RelativeLayout mapContainer;
  private PaperButton    buttonSend;
  private CardViewPlus   cardReveal;
  // private LineView       lineView;
  private ValueLineChart mCubicValueLineChart;

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
    months = new ArrayList<>();
    months.add("Jan");
    months.add("Feb");
    months.add("Mar");
    months.add("Apr");
    months.add("May");
    months.add("Jun");
    months.add("Jul");
    months.add("Aug");
    months.add("Sep");
    months.add("Oct");
    months.add("Nov");
    months.add("Dec");
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
    lineViewTest();
    EventBus.getDefault().post(new RequestAlarmEvent());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }


  //region line chart test
  private void lineViewTest() {

  }
  //endregion

  /**
   * initializes the views of this {@link android.app.Activity}
   */
  private void initViews(View root) {
    setContentView(root);
    mapContainer = (RelativeLayout) findViewById(R.id.layout_map_container);
    fabButton = (CircleButton) findViewById(R.id.circle_button);
    buttonSend = (PaperButton) findViewById(R.id.button_send);
    cardReveal = (CardViewPlus) findViewById(R.id.card_reveal);
    //lineView = (LineView) findViewById(R.id.line_view);
    mCubicValueLineChart = (ValueLineChart) findViewById(R.id.cubiclinechart);
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
    //a bit hacky but will find out proper way soon
    mAnimator = defaultReveal();
    startSupportAnimator(400);
    mAnimator.reverse();
    mAnimator.addListener(animatorListener);
    startSupportAnimator(400);
  }

  /**
   * Plays {@link io.codetail.animation.SupportAnimator}
   *
   * @param duration
   */
  private void startSupportAnimator(int duration) {
    mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    mAnimator.setDuration(duration);
    mAnimator.start();
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
      mAnimator.addListener(animatorListener);
    } else if (mAnimator != null) {
      mAnimator.cancel();
      return;
    } else {
      mAnimator = defaultReveal();
    }
    startSupportAnimator(400);
  }

  /**
   * Gets the hypotenuse
   *
   * @param a
   * @param b
   * @return
   */
  static float hypo(int a, int b) {
    return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
  }

  private SupportAnimator.AnimatorListener animatorListener =
      new SupportAnimator.AnimatorListener() {
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
      };

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
            updateCamToLocation(latLng);//updates map camera
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
      Bitmap pin = MarkerUtil.scaleImage(getResources(), R.drawable.fire_pin, 100);
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

          @Override
          public void onAny(MaterialDialog dialog) {
            super.onAny(dialog);
            WhatToDoDialog.create(false, getResources().getColor(R.color.green)).show(
                getSupportFragmentManager(), "whattodo");
          }
        }).show();
  }

  public void onEventAsync(ProcessAlarmForGraphEvent event) {
    NavigableMap<Integer, List<ParseObject>> navigableMap = new TreeMap<>();
    for (ParseObject obj : event.parseObjects) {
      List<ParseObject> yearList = navigableMap.get(obj.getCreatedAt().getMonth());
      if (yearList == null) {
        navigableMap.put(obj.getCreatedAt().getMonth(), yearList = new ArrayList<>());
      }
      yearList.add(obj);
    }
    int max = 0;
    for (int i = 0; i < 11; i++) {
      if (navigableMap.get(i) != null && navigableMap.get(i).size() > max) {
        max = navigableMap.get(i).size();
      }
    }
    ValueLineSeries series = new ValueLineSeries();
    series.setColor(0xFF56B7F1);
    series.addPoint(new ValueLinePoint("Jan", navigableMap.get(0) != null ? navigableMap.get(0)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Feb", navigableMap.get(1) != null ? navigableMap.get(1)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Mar", navigableMap.get(2) != null ? navigableMap.get(2)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Apr", navigableMap.get(3) != null ? navigableMap.get(3)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("May", navigableMap.get(4) != null ? navigableMap.get(4)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Jun", navigableMap.get(5) != null ? navigableMap.get(5)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Jul", navigableMap.get(6) != null ? navigableMap.get(6)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Aug", navigableMap.get(7) != null ? navigableMap.get(7)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Sep", navigableMap.get(8) != null ? navigableMap.get(8)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Oct", navigableMap.get(9) != null ? navigableMap.get(9)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Nov", navigableMap.get(10) != null ? navigableMap.get(10)
        .size() / max : 0));
    series.addPoint(new ValueLinePoint("Dec", navigableMap.get(11) != null ? navigableMap.get(11)
        .size() / max : 0));

    mCubicValueLineChart.addSeries(series);
    mCubicValueLineChart.post(new Runnable() {
      @Override
      public void run() {
        mCubicValueLineChart.startAnimation();
      }
    });
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
