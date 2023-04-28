package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import zhe.it_tech613.com.cmpcourier.MapsActivity;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.Status;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.DownloadEsignTask;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.LocationUpdateService;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;
import zhe.it_tech613.com.cmpcourier.utils.RSA;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_parcel_import;
    private cmpApi cmpApi;
    private TextView undeliver_number;
    private LocationUpdateService mService;
    private static String TAG = "Mainactivity";
    private Intent intent;
    private boolean mIsBound=false;
    private static final String FINE_LOCATION= android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    private Boolean mLocationPermissionGranted = false;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cmpApi=new cmpApi(this);

        TextView tvTitle=(TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(PreferenceManager.getName());
//        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
//        arrowImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });
//        TextView version_number=(TextView)findViewById(R.id.logout);
//        version_number.setText("v. "+PreferenceManager.versionName);

        Button btn_logout = (Button) findViewById(R.id.btn_logout);
        //btn_sms,
        Button btn_esign = findViewById(R.id.btn_esign);
        btn_esign.setText(Constant.czlanguageStrings.getESIGN());
        btn_logout.setText(Constant.czlanguageStrings.getLOGOUT()+":\n"+PreferenceManager.getName());
//        btn_sms=(Button)findViewById(R.id.btn_sms);
//        btn_sms.setText(Constant.czlanguageStrings.getSMS());
        Button btn_sorting = (Button) findViewById(R.id.btn_sorting);
        btn_sorting.setText(Constant.czlanguageStrings.getSORTING());
        Button btn_to_delivery = (Button) findViewById(R.id.btn_to_delivery);
        btn_to_delivery.setText(Constant.czlanguageStrings.getTO_DELIVERY());
        btn_parcel_import = (Button) findViewById(R.id.btn_parcel_import);
        btn_parcel_import.setText(Constant.czlanguageStrings.getPARCELS_IMPORT());
        TextView undeliver_text=(TextView)findViewById(R.id.undeliver_text);
        undeliver_text.setText(Constant.czlanguageStrings.getNO_UNDELIVERED());
        undeliver_number=(TextView)findViewById(R.id.undeliver_number);
        btn_esign.setOnClickListener(this);
        btn_to_delivery.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
        btn_parcel_import.setOnClickListener(this);
        btn_sorting.setOnClickListener(this);
//        btn_sms.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                finish();
//            }
//        });
        intent=new Intent(getApplication(), LocationUpdateService.class);
        getLocationPermission();
//        try {
//            PreferenceManager.setCertificate(RSA.getKey(PreferenceManager.downLoadFolder+"/cmpESign-00000039.cer"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: getting Location Permission");
        String[] permissions={FINE_LOCATION,COURSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted=true;
                //initialize map
                startMyLocationService();
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
                    startMyLocationService();
                }

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startMyLocationService() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            getApplication().startForegroundService(intent);
        else getApplication().startService(intent);
        bindToService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferenceManager.getID()!=-1)
            startTracking();
        else stopTracking();
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        assert manager != null;
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
    }

    @Override
    protected void onDestroy() {
        unbindFromService();
        try {
            stopService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void startTracking(){
        PreferenceManager.stop_location_service = false;
        try {
            mService.startTracking();
        }catch (Exception e){
            e.printStackTrace();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                getApplication().startForegroundService(intent);
            }
            else {
                getApplication().startService(intent);
            }
            bindToService();
        }
    }

    public void stopTracking(){
        PreferenceManager.stop_location_service = true;
        try {
            stopService(intent);
            mService.stopTracking();
            unbindFromService();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bindToService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getApplication().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void unbindFromService() {
        Log.e(TAG,"unbindFromService");
        if (mIsBound) {
            // Detach our existing connection.
            getApplication().unbindService(mConnection);
            mIsBound = false;
            mService = null;
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            Log.e(TAG,"Service Connected");
            mService = ((LocationUpdateService.LocalBinder) service).getService();
            mService.registerClient(MainActivity.this);
            if (!PreferenceManager.stop_location_service) mService.startTracking();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Log.e(TAG,"Service DisConnected");
            mService.unRegisterClient(MainActivity.this);
            mService = null;
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkButtons();
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void checkButtons(){
        String Tag_req="req_login";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.checkButtons_url+PreferenceManager.getID()+Constant.sp;
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                responseObj -> {
                    try {
                        cmpApi.kpHUD.dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (responseObj!=null){
                        Log.e("checkButtons_url",url+" "+responseObj.toString());
                        try {
                            String status= responseObj.getString("value");
                            String undeliver=responseObj.getString("undeliver");
                            PreferenceManager.setFtpESIGN_Path(responseObj.getString("esign_download_path"));
                            JSONArray jsonArray = responseObj.getJSONArray("barcodes");
                            List<String> barcodes = new ArrayList<>();
                            for (int i=0;i<jsonArray.length();i++){
                                barcodes.add(jsonArray.getString(i));
                            }
                            int num_esign = Integer.parseInt(responseObj.getString("num_esign"));
                            if (barcodes.size()>0 && !checkDownloadFolder(barcodes)) {
                                btn_parcel_import.setEnabled(false);
                                btn_parcel_import.setBackgroundResource(R.color.gray);
                            }else {
                                btn_parcel_import.setEnabled(true);
                                btn_parcel_import.setBackgroundResource(R.drawable.orangebuttonselector);
                            }
//                                switch (status){
//                                    case "0"://all enable
////                                        btn_to_delivery.setClickable(true);
////                                        btn_to_delivery.setBackgroundResource(R.drawable.orangebuttonselector);
////                                        btn_sms.setClickable(true);
////                                        btn_sms.setBackgroundResource(R.drawable.orangebuttonselector);
////                                        btn_sorting.setClickable(true);
////                                        btn_sorting.setBackgroundResource(R.drawable.orangebuttonselector);
//                                        undeliver_number.setText(undeliver);
//                                        break;
//                                    case "1"://to_delivery disable,others enable
////                                        btn_to_delivery.setClickable(false);
////                                        btn_to_delivery.setBackgroundResource(R.drawable.grey_background);
//
////                                        btn_sms.setClickable(true);
////                                        btn_sms.setBackgroundResource(R.drawable.orangebuttonselector);
////                                        btn_sorting.setClickable(true);
////                                        btn_sorting.setBackgroundResource(R.drawable.orangebuttonselector);
//                                        undeliver_number.setText(undeliver);
//                                        break;
//                                    case "2"://to_delivery enable,others disable
////                                        btn_to_delivery.setClickable(true);
////                                        btn_to_delivery.setBackgroundResource(R.drawable.orangebuttonselector);
//
////                                        btn_sms.setClickable(false);
////                                        btn_sms.setBackgroundResource(R.drawable.grey_background);
////                                        btn_sorting.setClickable(false);
////                                        btn_sorting.setBackgroundResource(R.drawable.grey_background);
//                                        undeliver_number.setText(undeliver);
//                                        break;
//                                    case "3"://all disable
////                                        btn_to_delivery.setClickable(false);
////                                        btn_to_delivery.setBackgroundResource(R.drawable.grey_background);
////                                        btn_sms.setClickable(false);
////                                        btn_sms.setBackgroundResource(R.drawable.grey_background);
////                                        btn_sorting.setClickable(false);
////                                        btn_sorting.setBackgroundResource(R.drawable.grey_background);
//                                        undeliver_number.setText(undeliver);
//                                        break;
//                                    default://all disable
////                                        btn_to_delivery.setClickable(false);
////                                        btn_to_delivery.setBackgroundResource(R.drawable.grey_background);
////                                        btn_sms.setClickable(false);
////                                        btn_sms.setBackgroundResource(R.drawable.grey_background);
////                                        btn_sorting.setClickable(false);
////                                        btn_sorting.setBackgroundResource(R.drawable.grey_background);
//                                        break;
//                                }
                            undeliver_number.setText(undeliver);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cmpApi.kpHUD.dismiss();
            }
        });

        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(stringRequest,Tag_req);
    }

    private boolean checkDownloadFolder(List<String> num_esign) {
        Log.e("checkDownloadFolder","start");
        File directory = new File(PreferenceManager.downLoadFolder);
        File[] files = directory.listFiles();
        int num_zip = 0;
        for (String s:num_esign){
            boolean isContains = false;
            for (File file:files){
                if (file.getName().equals(s+".zip")) {
                    isContains=true;
                    break;
                }
            }
            if (!isContains) return false;
        }

        Log.e("num_zip",num_zip+" "+num_esign.size());
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_parcel_import:
                startActivity(new Intent(MainActivity.this,ParcelimportActivity.class));
//                finish();
                break;
            case R.id.btn_sorting:
                startActivity(new Intent(MainActivity.this,MapsActivity.class));
//                finish();
                break;
            case R.id.btn_to_delivery:
                startActivity(new Intent(MainActivity.this,ToDeliveryActivity.class));
//                finish();
                break;
            case R.id.btn_esign:
                startActivity(new Intent(MainActivity.this,EsignActivity.class));
//                finish();
                break;
            case R.id.btn_logout:
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                PreferenceManager.logoutManager();
                break;
        }
    }
}
