package zhe.it_tech613.com.cmpcourier.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

import zhe.it_tech613.com.cmpcourier.R;

public class LocationUpdateService extends IntentService {

    private static final String TAG = "LocationUpdateService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_TIME_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 1f;
    private Handler service = new Handler();
    private Runnable mRunable;
    private Activity activity;
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    private String lastUpdateDate = "";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
//    private LocationListener mLocationListener;

    public LocationUpdateService(Context ctx) {
        super("AppsLoggingService");
    }

    public LocationUpdateService() {
        super("AppsLoggingService");
    }

    public void registerClient(Activity mainActivity) {
        activity=mainActivity;
    }

    public void unRegisterClient(Activity mainActivity) {
        activity=null;
    }

    public class LocalBinder extends Binder {
        public LocationUpdateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationUpdateService.this;
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        LocationListener(String provider) {
            //Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);
            onLocationChangedWork(mLastLocation);
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
//            new LocationListener(LocationManager.GPS_PROVIDER),
//            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            Thread.sleep(LOCATION_TIME_INTERVAL);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void createRunnable(final Context context) {
        service.removeCallbacks(mRunable);
        mRunable= () -> {
            //Log.e(TAG,"Starting request");
//            try {
//                mLocationManager.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER, LOCATION_TIME_INTERVAL, LOCATION_DISTANCE,
//                        mLocationListeners[2]);
//            } catch (java.lang.SecurityException ex) {
//                //Log.e(TAG, "fail to request location update, ignore", ex);
//            } catch (IllegalArgumentException ex) {
//                //Log.e(TAG, "network provider does not exist, " + ex.getMessage());
//            }
//            try {
//                mLocationManager.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER, LOCATION_TIME_INTERVAL, LOCATION_DISTANCE,
//                        mLocationListeners[1]);
//            } catch (java.lang.SecurityException ex) {
//                //Log.e(TAG, "fail to request location update, ignore", ex);
//            } catch (IllegalArgumentException ex) {
//                //Log.e(TAG, "network provider does not exist, " + ex.getMessage());
//            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_TIME_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (SecurityException ex) {
                //Log.e(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                //Log.e(TAG, "gps provider does not exist " + ex.getMessage());
            }

//                Location mLocation = PreferenceManager.getAppContext().gpsTracker.getLocation();
//                onLocationChangedWork(mLocation);
            if (!PreferenceManager.stop_location_service)
                runNextRunnable();
        };
        mRunable.run();
    }

    private void runNextRunnable() {
        service.removeCallbacks(mRunable);
        long next = SystemClock.uptimeMillis() + LOCATION_TIME_INTERVAL;
        service.postAtTime(mRunable,next);
    }

    void onLocationChangedWork(Location location) {
        //Log.e(TAG,"Location changed");
        if (PreferenceManager.stop_location_service) {
            stopSelf();
            //Log.e(TAG,"Location changed but PreferenceManager.appDriverModel==null || PreferenceManager.stop_location_service");
            return;
        }
        updateLocation(location);
    }

    private void updateLocation(Location location){
        if (location==null) return;
        //Log.e(TAG,"location not null");
        if (PreferenceManager.getID()!=-1) {
//                if (PreferenceManager.getFireUID().equals("")) return;
//                if (activity!=null)
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (activity!=null) Toast.makeText(activity,"onLocationChanged: " + location,Toast.LENGTH_SHORT).show();
//                        }
//                    });
            String now = dateFormat.format(new Date());
            if (lastUpdateDate.equals(now)) return;
            //Log.e(TAG, "onLocationChanged: " + location.getLongitude());
            lastUpdateDate = now;
//            FirebaseFirestore.getInstance()
//                    .collection(Constants.collection_drivers)
//                    .document(PreferenceManager.getEmail()).update(
//                    Constants.latitude, String.valueOf(location.getLatitude()),
//                    Constants.longitude, String.valueOf(location.getLongitude()),
//                    Constants.update_date, FieldValue.serverTimestamp());
            //                        Constants.uid, PreferenceManager.getFireUID(),
            Intent intent = new Intent ("locationUpdate"); //put the same message as in the filter you used in the activity when registering the receiver
            intent.putExtra("location",location);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            PreferenceManager.stop_location_service=true;
            stopSelf();
            //Log.e(TAG,"stopped itself");
        }
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e(TAG, "onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(1, getNotification("Location Service",getString(R.string.app_name)+" is Running!"));
//        else startForeground(1,new Notification());
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.e(TAG, "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(1, getNotification("Location Service",getString(R.string.app_name)+" is Running!"));
//        else startForeground(1,new Notification());
        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CollectData");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
//        createRunnable(this);
//        startTracking();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification(String title, String body) {
        String CHANNEL_ID = BuildConfig.APPLICATION_ID;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true)
//                .setPriority(NotificationManager.IMPORTANCE_MIN)
//                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle(title)
                .setContentText(body)
                .build();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Log.e(TAG, "onTaskRemoved");
        super.onTaskRemoved(rootIntent);
        service.removeCallbacks(mRunable);
    }

    @Override
    public void onDestroy() {
        //Log.e(TAG, "onDestroy");
        super.onDestroy();
        service.removeCallbacks(mRunable);
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    //Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
        if (!PreferenceManager.stop_location_service){
            Intent restartServiceIntent=new Intent(getApplication(),this.getClass());
            restartServiceIntent.setPackage(getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(restartServiceIntent);
            else startService(restartServiceIntent);
        }else {
            PreferenceManager.stop_location_service=false;
        }
    }

    private void initializeLocationManager() {
        //Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        createRunnable(this);
    }

    public void stopTracking() {
        stopSelf();
    }

}