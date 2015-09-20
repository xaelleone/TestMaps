package test.testmaps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public final String arrayName = "The current available venues are:";
    public Location prevLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        RequestQueue queue = Volley.newRequestQueue(this);

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        try {
            prevLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (prevLoc.distanceTo(location) > 50)
                    moveMarker(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
    }

    private void moveMarker(Location location) {
        mMap.clear();
        LatLng curPos = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(curPos));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(curPos, 15);
        mMap.moveCamera(cameraUpdate);

        String url = "http://52.89.79.85:5000/List";

        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            JSONArray arr = response.getJSONArray(arrayName);
                            double latitude;
                            double longitude;
                            for (int i = 0; i < arr.length(); i++)
                            {
                                JSONObject food = arr.getJSONObject(i);
                                String name = food.getString("name");
                                String content = food.getString("content");
                                String coords[] = arr.getJSONObject(i).getString("coordinates").split(", ");
                                latitude = Double.parseDouble(coords[0]);
                                longitude = Double.parseDouble(coords[1]);
                                System.out.println(coords[0] + ", " + coords[1]);
                                mMap.addMarker(new MarkerOptions().title(name)
                                        .snippet(content).position(new LatLng(latitude, longitude)));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}