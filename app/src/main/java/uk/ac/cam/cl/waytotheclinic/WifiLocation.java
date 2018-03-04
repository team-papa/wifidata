package uk.ac.cam.cl.waytotheclinic;

import android.location.Location;
import java.io.Serializable;
import java.util.Map;

class WifiLocation implements Serializable {
    private int floorNumber;
    private double lat;
    private double lon;
    private Map<String, Integer> strengths; // Map of BSSID to strength

    WifiLocation(Location l, Map<String, Integer> s) {
        floorNumber = (int) l.getAltitude();
        lat = l.getLatitude();
        lon = l.getLongitude();
        strengths = s;
    }

    WifiLocation(double lat, double lon, int floor, Map<String, Integer> s) {
        this.lat = lat;
        this.lon = lon;
        floorNumber = floor;

        strengths = s;
    }

    public int getFloor() {
        return floorNumber;
    }

    public Location getLocation() {
        Location l = new Location("From WiFi training data");

        l.setLatitude(lat);
        l.setLongitude(lon);
        l.setAltitude(floorNumber);

        return l;
    }

    public Map<String, Integer> getStrengths() {
        return strengths;
    }
}