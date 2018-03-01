package uk.ac.cam.cl.waytotheclinic;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LandingPage  extends AppCompatActivity implements LocationFragment.LocationListener, NavigationView.OnNavigationItemSelectedListener {
    private final String LOCATION_FRAGMENT_TAG = "location-fragment";
    private final int LOCATION_PERMISSIONS = 1;

    private String[] places = new String[]{"Belgium", "Frodo", "France", "Italy", "Germany", "Spain"};
    private Location mCurrentLocation;
    ConstraintLayout top_green_box;
    AutoCompleteTextView search_box;
    DrawerLayout drawer_layout;
    NavigationView nav_view;
    ImageButton menu_button;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        top_green_box = findViewById(R.id.top_green_box);
        search_box = findViewById(R.id.search_box);
        drawer_layout = findViewById(R.id.drawer_layout);
        nav_view = findViewById(R.id.nav_view);
        menu_button = findViewById(R.id.menu_button);

        LocationFragment locationFragment = new LocationFragment();
        MapFragment mapFragment = new MapFragment();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(locationFragment, LOCATION_FRAGMENT_TAG).commit();
        fm.beginTransaction().replace(R.id.map_id, mapFragment).commit();

        // Search box functionality
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_dropdown_item_1line, places);
        search_box.setAdapter(adapter);


        // On click, the menu button opens the side menu and closes the keyboard (if open)
        menu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                if (!drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    drawer_layout.openDrawer(GravityCompat.START);
                }
            }
        });


        // When opening the side menu, close keyboard. This handles the case of swipe opening.
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerOpened(View drawerView) {}

            @Override
            public void onDrawerClosed(View drawerView) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });


        // I really wish I could get rid of this but I need syncState()
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer_layout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer_layout.addDrawerListener(toggle);
        toggle.syncState();

        nav_view.setNavigationItemSelectedListener(this);


        // Implementation of smooth sliding transition for the green box containing the search bar.
        top_green_box.setOnClickListener(null);
        top_green_box.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
        View.OnTouchListener swipeListener = new View.OnTouchListener() {
            private Float y1 = 0.0F;
            private Float y2 = 0.0F;
            final Float minSwipeDist = 50.0F;
            private ConstraintSet constraintSet = new ConstraintSet();

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y1 = motionEvent.getY();
                    case MotionEvent.ACTION_UP:
                        y2 = motionEvent.getY();
                        if (y2 - y1 > minSwipeDist) {
                            Toast.makeText(getApplicationContext(), "Down swipe", Toast.LENGTH_SHORT).show();

                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) top_green_box.getLayoutParams();
                            params.height = dpToPx(300.0F);
                            top_green_box.setLayoutParams(params);

                            // Changing menu button color from green to white
                            ImageViewCompat.setImageTintList(menu_button,
                                    ColorStateList.valueOf(ContextCompat.getColor(
                                            getApplicationContext(), R.color.colorWhite)));

                            constraintSet.clone(top_green_box);
                            constraintSet.connect(R.id.search_box, ConstraintSet.START, R.id.top_white_box, ConstraintSet.START, dpToPx(8.0F));
                            constraintSet.applyTo(top_green_box);
                        } else if (y1 - y2 > minSwipeDist) {
                            Toast.makeText(getApplicationContext(), "Up swipe", Toast.LENGTH_SHORT).show();

                            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) top_green_box.getLayoutParams();
                            params.height = dpToPx(60.0F);
                            top_green_box.setLayoutParams(params);

                            // Change menu button color from white to green
                            ImageViewCompat.setImageTintList(menu_button,
                                    ColorStateList.valueOf(ContextCompat.getColor(
                                            getApplicationContext(), R.color.colorDarkGreen)));

                            constraintSet.clone(top_green_box);
                            constraintSet.connect(R.id.search_box, ConstraintSet.START, R.id.menu_button, ConstraintSet.END, dpToPx(12.0F));
                            constraintSet.applyTo(top_green_box);
                        }
                    default:
                        return false;
                }
            }
        };

        // Apply above listener to multiple elements
        top_green_box.setOnTouchListener(swipeListener);
        menu_button.setOnTouchListener(swipeListener);
        search_box.setOnTouchListener(swipeListener);


        // Closes keyboard when search bar not focused
        search_box.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions,
                                            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        Fragment frg = getSupportFragmentManager().findFragmentByTag(LOCATION_FRAGMENT_TAG);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.attach(frg);
        ft.detach(frg);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        drawer_layout = findViewById(R.id.drawer_layout);
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_draw_drawer, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer_layout = findViewById(R.id.drawer_layout);

        // Handle side-menu item-clicks
        switch (item.getItemId()) {
            case R.id.nav_first_floor:
                // TODO Switch map to first floor
                Toast.makeText(getApplicationContext(), "First floor", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_second_floor:
                // TODO Switch map to second floor
                Toast.makeText(getApplicationContext(), "Second floor", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer_layout.closeDrawer(GravityCompat.START);
        return true;
    }

    public int dpToPx(Float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    // Methods below here are called by LocationFragment, part of the interface LocationFragment.LocationListener

    @Override
    public boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermissions() {
        ActivityCompat.requestPermissions(this,
            new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                          Manifest.permission.ACCESS_WIFI_STATE},
            LOCATION_PERMISSIONS);
    }

    @Override
    public WifiManager getWifiManager() {
        return (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void startLocationUpdates(LocationRequest lr, LocationCallback lc) {
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(lr, lc, null);
    }

    @Override
    public void updateLocation(Location l) {
        // TODO: implement this
        Log.i("waytotheclinic", "waytotheclinic location updated: " + l.toString());
    }
}
