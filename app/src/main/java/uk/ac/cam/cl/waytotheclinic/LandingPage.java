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
import android.os.Environment;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LandingPage  extends AppCompatActivity implements LocationFragment.LocationListener {
    private final String LOCATION_FRAGMENT_TAG = "location-fragment";
    private final int LOCATION_PERMISSIONS = 1;

    private int currentLat;
    private int currentFloor;
    private int currentPosition = 0;

    private List<Location> locations;
    private List<WifiLocation> model;

    private WifiLocater wl;

    private void updateLocation(Location l) {
        currentLat = (int) l.getLatitude();
        currentFloor = (int) l.getAltitude();

        TextView tv = findViewById(R.id.editText);
        tv.setText(currentLat + " " + currentFloor);
        tv.invalidate();
        tv.requestLayout();

        Log.w("waytotheclinic", model.toString());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        LocationFragment locationFragment = new LocationFragment();
        MapFragment mapFragment = new MapFragment();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(locationFragment, LOCATION_FRAGMENT_TAG).commit();
        fm.beginTransaction().replace(R.id.map_id, mapFragment).commit();

        wl = new WifiLocater(getWifiManager());

        findViewById(R.id.ae_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.w("waytotheclinic", "hamburger");

                Map<String, Integer> r = wl.scan();
                model.add(new WifiLocation(currentLat, 0, currentFloor, r));

                try {
                    if (++currentPosition >= locations.size()) {
                        wl.createModel(model);
                        Log.w("waytotheclinic", getFilesDir().toString());

                        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                        File f = new File(path + File.separator + "wifiModel.ser");
                        wl.saveModel(f);
                        
                        TextView tv = findViewById(R.id.editText);
                        tv.setText("Complete");
                        tv.invalidate();
                        tv.requestLayout();
                    } else {
                        updateLocation(locations.get(currentPosition));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        locations = new ArrayList<>();
        locations.add(makeLoc(0, 0));
        locations.add(makeLoc(0, 1));
        locations.add(makeLoc(0, 2));
        locations.add(makeLoc(1, 0));
        locations.add(makeLoc(1, 1));
        locations.add(makeLoc(1, 2));

        model = new ArrayList<>();

        updateLocation(locations.get(0));
    }

    private Location makeLoc(int lat, int floor) {
        Location l = new Location("");
        l.setLatitude(lat);
        l.setAltitude(floor);

        return l;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_draw_drawer, menu);
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
}
