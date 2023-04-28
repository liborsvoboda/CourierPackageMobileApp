package zhe.it_tech613.com.cmpcourier;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import zhe.it_tech613.com.cmpcourier.activity.MainActivity;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG ="Map Activity";
    private static final int ERROR_DIALOG_REQUEST_CODE=9001;
    private static final String FINE_LOCATION= android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    cmpApi cmpApi;
    private GoogleMap mMap;
    private int order = 1;
    private Button reset;
    private TextView titleText;
    ArrayList<ParcelModel> parcelModels = new ArrayList<>();
    private Location mLocation;
    private Boolean mLocationPermissionGranted = false;
    double myLatitude, myLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isServiceOK()) {
            Toast.makeText(this, Constant.czlanguageStrings.getGOOGLE_MAP_SUCCESS_ALERT(), Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_maps);
            PackageInfo pinfo = null;
            try {
                pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;
            TextView version_number=(TextView)findViewById(R.id.version_number);
            version_number.setText("v. "+versionName);
            TextView tvTitle=(TextView)findViewById(R.id.tvTitle);
            tvTitle.setText(PreferenceManager.getName());
            ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
            arrowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            ImageView goHome=(ImageView)findViewById(R.id.home);
            goHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MapsActivity.this,MainActivity.class));
                }
            });

            cmpApi = new cmpApi(this);
            reset = (Button) findViewById(R.id.reset);
            reset.setText(Constant.czlanguageStrings.getRESET());
            titleText = (TextView) findViewById(R.id.tvTitle);
//            titleText.setText(Constant.czlanguageStrings.getcmp_MAP());
//            mLocation=PreferenceManager.gpsTracker.getLocation();
//            if (mLocation!=null) {
//                myLatitude = mLocation.getLatitude();
//                Log.e(TAG, "myLatitude:" + myLatitude);
//                myLongitude = mLocation.getLongitude();
//                Log.e(TAG, "myLongitude:" + myLongitude);
//            }else {
//                myLatitude=0;
//                myLongitude=0;
//                Toast.makeText(MapsActivity.this,getString(R.string.location_alert),Toast.LENGTH_LONG).show();
//            }
            initMap();

            getLocationPermission();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: getting Location Permission");
        String[] permissions={FINE_LOCATION,COURSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted=true;
                //initialize map
            }else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private void initMap(){
        Log.d(TAG,"initMap: initializing Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionsResult: called");
        mLocationPermissionGranted=false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG,"onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted=true;
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this, Constant.czlanguageStrings.getMAP_READY(), Toast.LENGTH_SHORT).show();
        mMap.setOnMarkerClickListener(this);
        Intent intent = getIntent();
        if (intent.hasExtra("latitude")) {
            final double latitude = intent.getDoubleExtra("latitude", 0);
            final double longitude = intent.getDoubleExtra("longitude", 0);
            final String client = intent.getStringExtra("client");
            order = intent.getIntExtra("order", 0);
            setPath(latitude,longitude,client);
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPath(latitude,longitude,client);
                }
            });

        } else {
            getlocations();
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetOrder();
                }
            });
        }
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    private void setPath(double parcel_lat,double parcel_lon,String client){
        final LatLng parcel_latLng = new LatLng(parcel_lat, parcel_lon);
        myLongitude=16.374394;
        myLatitude=49.688718;
        final LatLng my_latlng=new LatLng(myLatitude,myLongitude);
        Log.e("parcel_latlng",String.valueOf(parcel_lat)+","+String.valueOf(parcel_lon));
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(my_latlng, 7);
//        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(parcel_latLng).title(Constant.czlanguageStrings.getMARKER_DESCRIPTION() + client));
        mMap.addMarker(new MarkerOptions().position(my_latlng).title("My Location"));

//        Handler handler=new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CameraUpdate cameraUpdate1 = CameraUpdateFactory.newLatLngZoom(parcel_latLng, 7);
//                mMap.animateCamera(cameraUpdate1);
//            }
//        },2000);
        final LatLng zaragoza = new LatLng(0.5*(parcel_lat+myLatitude),0.5*(parcel_lon+myLongitude));

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();


        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCrV-vtijzttxifFj2cl2IOK_3yy5QFIys")
                .build();//AIzaSyAWIA5ERPj1Ap6oDRc-P0QiQFCFh6fjpo0//AIzaSyCrV-vtijzttxifFj2cl2IOK_3yy5QFIys
        DirectionsApiRequest req = DirectionsApi.getDirections(context, String.valueOf(myLatitude)+","+String.valueOf(myLongitude), String.valueOf(parcel_lat)+","+String.valueOf(parcel_lon));
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            mMap.addPolyline(opts);
        }

        mMap.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate cameraUpdate1 = CameraUpdateFactory.newLatLngZoom(zaragoza, 7);
        mMap.animateCamera(cameraUpdate1);
//        Handler handler1=new Handler();
//        handler1.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CameraUpdate cameraUpdate1 = CameraUpdateFactory.newLatLngZoom(zaragoza, 7);
//                mMap.animateCamera(cameraUpdate1);
//            }
//        },5000);
    }
    public boolean isServiceOK(){
        Log.d(TAG,"isServiceOK: checking google services version");

        GoogleApiAvailability googleApiAvailability=GoogleApiAvailability.getInstance();

        int isavailable  = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (isavailable== ConnectionResult.SUCCESS)  {
            Log.d(TAG,"isServiceOK: Google Play Services is working");
            return true;
        }else if (googleApiAvailability.isUserResolvableError(isavailable)){
            Log.d(TAG,"isServiceOK: an error occurred but we can fix it");
            Dialog dialog = googleApiAvailability.getErrorDialog(this,isavailable,0);
            dialog.show();
        }else {
            Toast.makeText(this, Constant.czlanguageStrings.getPLAY_SERVICE_ALERT(),Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getlocations(){
        String Tag_req="req_locations";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.getlocations_url+ PreferenceManager.getID()+Constant.sp;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("getlocations_response",response.toString());
                    cmpApi.kpHUD.dismiss();
                    parcelModels =cmpApi.parseLocations(response);
                    if (parcelModels !=null){
                        for (int i=0;i< parcelModels.size();i++){
                            final ParcelModel parcelModel=parcelModels.get(i);
                            LatLng latLng=new LatLng(parcelModel.getLatitude(),parcelModel.getLongitude());
//                                new Handler().postDelayed(new Runnable()
//                                {
//                                    @Override
//                                    public void run()
//                                    {
//                                        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
//                                        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngZoom(latLng,15);
//                                        mMap.animateCamera(cameraUpdate);
//                                    }
//                                },2000);
                            mMap.addMarker(new MarkerOptions().position(latLng).title(Constant.czlanguageStrings.getMARKER_DESCRIPTION()+String.valueOf(i)));
                            CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngZoom(latLng,7);
                            mMap.animateCamera(cameraUpdate);
                            order =1;
                        }
                    }
                }, error -> cmpApi.kpHUD.dismiss());
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        LatLng latLng=marker.getPosition();
        ParcelModel parcelModel=PreferenceManager
                .realm.where(ParcelModel.class)
                .equalTo("latitude",latLng.latitude)
                .equalTo("longitude",latLng.longitude)
                .findFirst();
        if (parcelModel!=null){
            removeOrder(order,parcelModel.getBarcode());
        }
        marker.remove();
        order +=1;
        return true;
    }

    private void removeOrder(int order,String barcode){
        String Tag_req="req_remove_order";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.removeOrder_url+
                String.valueOf(order)+Constant.separator+
                barcode+Constant.separator+
                PreferenceManager.getID()+Constant.sp;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("removeOrder_response",response.toString());
                    cmpApi.kpHUD.dismiss();
                    if (cmpApi.parseCheckBarcode(response)) {
                        PreferenceManager.realm.executeTransaction(realm -> {
                            RealmResults<ParcelModel> parcelModels=realm.where(ParcelModel.class).equalTo("barcode",barcode).findAll();
                            parcelModels.deleteAllFromRealm();
                        });
                        Toast.makeText(MapsActivity.this, Constant.czlanguageStrings.getSUCCESS_REMOVE_ALERT(),Toast.LENGTH_LONG).show();
                    }
                    else Toast.makeText(MapsActivity.this, Constant.czlanguageStrings.getFAIL_REMOVE_ALERT(),Toast.LENGTH_LONG).show();
                }, error -> cmpApi.kpHUD.dismiss());
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }

    private void resetOrder(){
        String Tag_req="req_remove_order";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.resetOrder_url+
                PreferenceManager.getID()+Constant.sp;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("resetOrder_response",url+" "+response.toString());
                        cmpApi.kpHUD.dismiss();
                        if (cmpApi.parseCheckBarcode(response)) Toast.makeText(MapsActivity.this, Constant.czlanguageStrings.getSUCCESS_RESET_ALERT(),Toast.LENGTH_LONG).show();
                        else Toast.makeText(MapsActivity.this, Constant.czlanguageStrings.getFAIL_RESET_ALERT(),Toast.LENGTH_LONG).show();
                        getlocations();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cmpApi.kpHUD.dismiss();
                    }
                });
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }

}
