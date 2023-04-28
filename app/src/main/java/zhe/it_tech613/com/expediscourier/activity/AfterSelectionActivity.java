package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class AfterSelectionActivity extends AppCompatActivity {
    private static final String TAG ="Map Activity";
    private static final String FINE_LOCATION= android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE=1234;
    Button btn_call, btn_navigate, btn_sms, btn_exit;
    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
    private static final int REQUEST_PHONE_CALL = 1000;
    private Location mLocation;
    private Boolean mLocationPermissionGranted = false;
    double myLatitude, myLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_selection);
        TextView tvTitle=(TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(PreferenceManager.getName());
        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
        arrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        TextView version_number=(TextView)findViewById(R.id.logout);
        version_number.setText("v. "+PreferenceManager.versionName);

        ImageView goHome=(ImageView)findViewById(R.id.home);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AfterSelectionActivity.this,MainActivity.class));
                finish();
            }
        });
        final Intent intent=getIntent();
        final String telephone=intent.getStringExtra("telephone");
        final double latitude=intent.getDoubleExtra("latitude",0);
        final double longitude=intent.getDoubleExtra("longitude",0);
        final String client=intent.getStringExtra("client");
        final int order=intent.getIntExtra("order",0);
        final String barcode=intent.getStringExtra("barcode");
        btn_call = (Button) findViewById(R.id.btn_call);
        btn_call.setText(Constant.czlanguageStrings.getCALL());
        btn_navigate = (Button) findViewById(R.id.btn_navigate);
        btn_navigate.setText(Constant.czlanguageStrings.getNAVIGATE());
        btn_sms = (Button) findViewById(R.id.btn_sms);
        btn_sms.setText(Constant.czlanguageStrings.getSMS());
        btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_exit.setText(Constant.czlanguageStrings.getEXIT());

        btn_call.setBackgroundColor(Color.parseColor("#92d050"));
        btn_exit.setBackgroundColor(Color.parseColor("#ff0000"));
        btn_navigate.setBackgroundColor(Color.parseColor("#0066ff"));
        btn_sms.setBackgroundColor(Color.parseColor("#ffc000"));
//        final Button btn_change_color=(Button)findViewById(R.id.btn_change_color);
//        btn_change_color.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                btn_change_color.setBackgroundColor(Color.parseColor("#ffffff"));
//                btn_call.setBackgroundColor(Color.parseColor("#FF91CF50"));
//                btn_exit.setBackgroundColor(Color.parseColor("#FFFD0001"));
//                btn_navigate.setBackgroundColor(Color.parseColor("#FF0066FE"));
////                btn_sms.setBackgroundColor(Color.parseColor("#ffffff"));
//            }
//        });
        getLocationPermission();

        mLocation= PreferenceManager.gpsTracker.getLocation();
        if (mLocation!=null) {
            myLatitude = mLocation.getLatitude();
            Log.e(TAG, "myLatitude:" + myLatitude);
            myLongitude = mLocation.getLongitude();
            Log.e(TAG, "myLongitude:" + myLongitude);
        }else {
            myLatitude=0;
            myLongitude=0;
            Toast.makeText(this,Constant.czlanguageStrings.getLOCATION_ALERT(),Toast.LENGTH_LONG).show();
        }
        btn_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent smsintent=new Intent(AfterSelectionActivity.this, SMSActivity.class);
                smsintent.putExtra("barcode",barcode);
                startActivity(smsintent);
            }
        });
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneIntent.setData(Uri.parse("tel:" + telephone));
                if (ActivityCompat.checkSelfPermission(AfterSelectionActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AfterSelectionActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                } else {
                    startActivity(phoneIntent);
                }
            }
        });
        String[] show_app_names;
        String[] total_app_names = {"Mapy Google","Waze","Mapy.cz"};// {"Mapy", "Sygic", "Osmand", "Osmand +","Waze","Mapsme","Navigator","Herewego","Copilot","Genius","Mapy.cz"}
        ArrayList<String> installed_app_names=new ArrayList<>();
        ArrayList<String> total_apps=new ArrayList<>();
        Map<String, String> name_packages=new HashMap<>();
        final String googlemap_package="com.google.android.apps.maps",
                sygic_package="com.sygic.aura",
                osmand_package="net.osmand",
                osmandplus_package="net.osmand.plus",
                waze_package="com.waze",
                mapsme="com.mapswithme.maps.pro",
                mapfactor="com.mapfactor.navigator",
                herewego="com.here.app.maps",
                copilot="com.alk.copilot.mapviewer",
                genius="hr.mireo.arthur",
                mapy_cz="cz.seznam.mapy";
        total_apps.add(googlemap_package);
//        total_apps.add(sygic_package);
//        total_apps.add(osmand_package);
//        total_apps.add(osmandplus_package);
        total_apps.add(waze_package);
//        total_apps.add(mapsme);
//        total_apps.add(mapfactor);
//        total_apps.add(herewego);
//        total_apps.add(copilot);
//        total_apps.add(genius);
        total_apps.add(mapy_cz);
        for (int i=0;i<total_apps.size();i++){
            String packagename=total_apps.get(i);
            if (isPackageInstalled(packagename,getPackageManager())){
                name_packages.put(total_app_names[i],packagename);
                installed_app_names.add(total_app_names[i]);
            }
        }
        show_app_names= GetStringArray(installed_app_names);
        btn_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Uri location = Uri.parse("geo:"+latitude+","+longitude+"?z=14");
//                location=Uri.parse("google.navigation:q=43.595715, 121.820426");//42.144974, 123.385414//"google.navigation:q="
//                Log.e("location","geo:"+latitude+","+longitude+"?z=14");
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
//                mapIntent.setData(location);
//                if (mapIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(mapIntent);
//                }

                AlertDialog.Builder builder = new AlertDialog.Builder(AfterSelectionActivity.this);
                builder.setTitle("Vyberte aplikaci Navigace");
                builder.setItems(show_app_names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        String appname=name_packages.get(show_app_names[which]);
                        switch (appname){
                            case "com.google.android.apps.maps":
                                if (isPackageInstalled(googlemap_package,AfterSelectionActivity.this.getPackageManager())){
                                    Uri uri=Uri.parse("google.navigation:q="+latitude+", "+longitude);//42.144974, 123.385414//"google.navigation:q=";
//                                    Uri uri = Uri.parse("geo:"+latitude+","+longitude+"?z=14");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    intent.setPackage(appname);
                                    startActivity(intent);
//                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(googlemap_package);
//                                    startActivity(i);
                                }
//                                else {
//                                    try {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + googlemap_package)));
//                                    } catch (android.content.ActivityNotFoundException anfe) {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + googlemap_package)));
//                                    }
//                                }
                                break;
                            case "com.sygic.aura":
                                if (isPackageInstalled(sygic_package,AfterSelectionActivity.this.getPackageManager())){
                                    String type = "drive";
                                    String str = "com.sygic.aura://coordinate|" + latitude + "|" + longitude + "|" + type;
//                                    str = "com.sygic.aura://coordinate|41.832884|123.359652|" + type;
                                    Log.e("uri",str);
                                    Intent intent1=new Intent(Intent.ACTION_VIEW, Uri.parse(str));
                                    intent1.setPackage(appname);
                                    startActivity(intent1);
//                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(sygic_package);
//                                    startActivity(i);
                                }
//                                else {
//                                    try {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + sygic_package)));
//                                    } catch (android.content.ActivityNotFoundException anfe) {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + sygic_package)));
//                                    }
//                                }
                                break;
                            case "net.osmand":
                                if (isPackageInstalled(osmand_package,AfterSelectionActivity.this.getPackageManager())){
                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(osmand_package);
                                    startActivity(i);
                                }
//                                else {
//                                    try {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + uber_package)));
//                                    } catch (android.content.ActivityNotFoundException anfe) {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + uber_package)));
//                                    }
//                                }
                                break;
                            case "net.osmand.plus":
                                if (isPackageInstalled(osmandplus_package,AfterSelectionActivity.this.getPackageManager())){
                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(osmandplus_package);
                                    startActivity(i);
                                }
                                break;
                            case "com.waze":
                                if (isPackageInstalled(waze_package,AfterSelectionActivity.this.getPackageManager())){
                                    String uri = "https://www.waze.com/ul?ll=" + latitude + ", " + longitude + "&navigate=yes&zoom=17";
//                                    uri="https://www.waze.com/ul?ll=41.832884, 123.359652&navigate=yes&zoom=17";
                                    Log.e("uri",uri);
                                    Intent intent1=new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                    intent1.setPackage(appname);
                                    startActivity(intent1);
//                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(waze_package);
//                                    startActivity(i);
                                }
//                                else {
//                                    try {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + waze_package)));
//                                    } catch (android.content.ActivityNotFoundException anfe) {
//                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + waze_package)));
//                                    }
//                                }
                                break;
                            case "com.mapswithme.maps.pro":
                                if (isPackageInstalled(mapsme,AfterSelectionActivity.this.getPackageManager())){
                                    Intent intent1=new Intent(appname+".action.BUILD_ROUTE");
                                    intent1.setPackage(appname);
                                    intent1.putExtra("lat_from", myLatitude);
                                    intent1.putExtra("lon_from", myLongitude);
                                    intent1.putExtra("saddr", "Start point");
                                    intent1.putExtra("lat_to", latitude);
                                    intent1.putExtra("lon_to", longitude);
                                    intent1.putExtra("daddr", "End point");
                                    intent1.putExtra("router", "vehicle");
                                    startActivity(intent1);
                                }
                                break;

                            case "com.mapfactor.navigator":
                                if (isPackageInstalled(mapfactor,AfterSelectionActivity.this.getPackageManager())){
                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(mapfactor);
                                    i.setPackage(appname);
                                    startActivity(i);
                                }
                                break;
                            case "com.here.app.maps":
                                if (isPackageInstalled(herewego,AfterSelectionActivity.this.getPackageManager())){
                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(herewego);
                                    i.setPackage(appname);
                                    startActivity(i);
                                }
                                break;
                            case "com.alk.copilot.mapviewer":
                                if (isPackageInstalled(copilot,AfterSelectionActivity.this.getPackageManager())){
                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(copilot);
                                    i.setPackage(appname);
                                    startActivity(i);
                                }
                                break;
                            case "hr.mireo.arthur":
                                if (isPackageInstalled(genius,AfterSelectionActivity.this.getPackageManager())){
                                    Intent i=AfterSelectionActivity.this.getPackageManager().getLaunchIntentForPackage(genius);
                                    i.setPackage(appname);
                                    startActivity(i);
                                }
                                break;
                            case "cz.seznam.mapy":
                                if (isPackageInstalled(mapy_cz,AfterSelectionActivity.this.getPackageManager())){
                                    String uri = "https://mapy.cz/zakladni?x="+longitude+"&y="+latitude+"&z=17";
//                                    uri="https://www.waze.com/ul?ll=41.832884, 123.359652&navigate=yes&zoom=17";
                                    Log.e("uri",uri);
                                    Intent intent1=new Intent(Intent.ACTION_VIEW);
                                    intent1.setData(Uri.parse(uri));
                                    intent1.setPackage(appname);
                                    startActivity(intent1);
                                }
                                break;
                        }
                    }
                });
                builder.show();
            }
        });
        btn_exit.setOnClickListener(view -> {
            Intent exit_intent=new Intent(AfterSelectionActivity.this, ChangeStatusActivity.class);
            exit_intent.putExtra("barcode",barcode);
            exit_intent.putExtra("client",client);
            startActivity(exit_intent);
            finish();
        });
        // add PhoneStateListener
//        PhoneCallListener phoneListener = new PhoneCallListener();
//        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    // Function to convert ArrayList<String> to String[]
    public static String[] GetStringArray(ArrayList<String> arr)
    {

        // declaration and initialise String Array
        String str[] = new String[arr.size()];

        // ArrayList to Array Conversion
        for (int j = 0; j < arr.size(); j++) {

            // Assign each value to String array
            str[j] = arr.get(j);
        }

        return str;
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
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
                }
                break;
            }
            case REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        startActivity(phoneIntent);
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: getting Location Permission");
        String[] permissions={FINE_LOCATION,COURSE_LOCATION,Manifest.permission.CALL_PHONE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(AfterSelectionActivity.this,
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                     mLocationPermissionGranted=true;
                } else {
                    ActivityCompat.requestPermissions(this,
                            permissions,
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
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

//    private class PhoneCallListener extends PhoneStateListener {
//
//        private boolean isPhoneCalling = false;
//
//        String LOG_TAG = "LOGGING 123";
//
//        @Override
//        public void onCallStateChanged(int state, String incomingNumber) {
//
//            if (TelephonyManager.CALL_STATE_RINGING == state) {
//                // phone ringing
//                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
//            }
//
//            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
//                // active
//                Log.i(LOG_TAG, "OFFHOOK");
//
//                isPhoneCalling = true;
//            }
//
//            if (TelephonyManager.CALL_STATE_IDLE == state) {
//                // run when class initial and phone call ended,
//                // need detect flag from CALL_STATE_OFFHOOK
//                Log.i(LOG_TAG, "IDLE");
//
//                if (isPhoneCalling) {
//
//                    Log.i(LOG_TAG, "restart app");
//
//                    // restart app
//                    Intent i = AfterSelectionActivity.this.getBaseContext().getPackageManager()
//                            .getLaunchIntentForPackage(
//                                    AfterSelectionActivity.this.getBaseContext().getPackageName());
//                    assert i != null;
//                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(i);
//                    isPhoneCalling = false;
//                }
//
//            }
//        }
//    }

}
