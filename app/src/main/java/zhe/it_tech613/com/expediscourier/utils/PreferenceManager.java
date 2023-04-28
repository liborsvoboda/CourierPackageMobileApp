package zhe.it_tech613.com.cmpcourier.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import zhe.it_tech613.com.cmpcourier.R;

public class PreferenceManager extends Application {

    public static final String TAG = PreferenceManager.class.getSimpleName();
    public static boolean stop_location_service = false;

    private RequestQueue mRequestQueue;

    private static PreferenceManager mInstance;

    static SharedPreferences preferences;
    static SharedPreferences.Editor prefEditor;
    public static GPSTracker gpsTracker;
    @SuppressLint("StaticFieldLeak")
    public static Realm realm;

    public static String LoginStatus = "LoginStatus";
    public static String Token = "Token";
    public static String ID = "ID";
    public static String Email = "Email";
    public static String Location = "Location";
    public static String FTP_LOGIN = "FTP_LOGIN";
    public static String FTP_PASS = "FTP_PASS";
    public static String FTP_IP = "FTP_IP";
    public static String FTP_PATH = "FTP_PATH";
    public static String FTP_ESIGN_LOGIN = "FTP_ESIGN_LOGIN";
    public static String FTP_ESIGN_PASS = "FTP_ESIGN_PASS";
    public static String FTP_ESIGN_IP = "FTP_ESIGN_IP";
    public static String FTP_ESIGN_PATH = "FTP_ESIGN_PATH";
    public static String State = "state";
    public static final String ServerUrl = "serverUrl";
    public static final String Certificate = "Certificate";
    public static String Name = "name";
    public static String Pass = "password";
    public static String LoginTime = "logintime";
    public static String Address = "address";
    public static String Phone = "phone";
    public static String versionName;
    public static String base_url = "http://10.10.11.3/api_courier_app/";//  http://cmp.schamann.net/
    //http://cmp.cz/test_api/

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String login_url = base_url + "mobile/cmpLogin/";
    public static String getinitialdata_url = base_url + "RestController.php?view=getinitialdata&ID=";
    public static String getlanguage_url = base_url + "mobile/getlanguage/cmp/";
    public static String changeParcelStatus_url = base_url + "RestController.php?view=changeParcelStatus&process_type=";
    public static String default_company = "mobile/Skovajsa s.r.o.";
    public static String checkbarcode_url = base_url + "mobile/checkbarcode/";
    public static String uploadImage_url = base_url + "uploadImage.php";
    public static String getlocations_url = base_url + "mobile/getlocations/";
    public static String removeOrder_url = base_url + "mobile/removeOrder/";
    public static String resetOrder_url = base_url + "mobile/resetOrder/";
    public static String getOrders_url = base_url + "RestController.php?view=getOrders&ID=";
    public static String sendSMS_url = base_url + "mobile/sendSMS/";
    public static String getEsign_url = base_url + "mobile/getEsign/";
    public static String getPin_url = base_url + "mobile/getPin/";
    public static String sendPin_url = base_url + "mobile/sendPin/";
    public static String sendFile_url = base_url + "mobile/sendFile/";
    public static String changeOrderStatus_url = base_url + "RestController.php?view=changeOrderStatus&process_type=";
    public static String checkButtons_url = base_url + "mobile/checkButtons/";
    public static String updatePayment_url = base_url + "mobile/updatePayment/%s===%d/";
    public static String setPaymentMode = base_url + "mobile/setPaymentMode/%s===%d/";
    public static String idWrong_url = base_url + "mobile/idWrong/%s===%s/";
    public static String updateLocation = base_url + "mobile/updateLocation/%s===%f===%f/";
    public static String getArchivedBarcodes = base_url + "mobile/getArchivedBarcodes/%s===%d/";
    public static String addNoteAfterDelivery = base_url + "mobile/addNoteAfterDelivery/%d===%s===%s===%s/";
    public static String addLockBox = base_url + "mobile/addLockBox/%s===%d===%s===%s/";
    public static String lockBoxDone = base_url + "mobile/lockBoxDone/%d===%s/";
    public static String getLockBoxCount = base_url + "mobile/getLockBoxCount/%s/";

    public List<File> files = new ArrayList<>(3);
    public List<String> file_names = new ArrayList<>(3);
    public static String signosign_package_name = "de.signotec.signosign";

    public static final String app_folder = "cmp_Mobile";
    public static final String Esign = ".Esign";
    public static final String Download = "Download";
    public static final String Upload = "Upload";
    public static final String Signed = "Signed";
    public static final String loged = "Log";
    public static String myFolder, esignFolder, uploadFolder, downLoadFolder, signedFolder, photoFolder,logFolder;
    public static File logFile = new File(logFolder + "/log.txt");

//    public void uploadFile(ConnectionInterface uploadListener) {
//        List<Boolean> statuses=new ArrayList<>();
//        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd");
//        String date_string=dateFormat.format(new Date());
//        String[] date_str_array=dateFormat.format(new Date()).split("/");
//        String server_file_Path=getFtpPath()+getLocation()+"/"+date_string+"/"+getName().replace(" ","_");//getFtpPath()+
//        try {
//            changeWorkingDir(getFtpPath());
//            changeWorkingDir(getFtpPath()+getLocation());
//            changeWorkingDir(getFtpPath()+getLocation()+"/"+date_str_array[0]);
//            changeWorkingDir(getFtpPath()+getLocation()+"/"+date_str_array[0]+"/"+date_str_array[1]);
//            changeWorkingDir(getFtpPath()+getLocation()+"/"+date_str_array[0]+"/"+date_str_array[1]+"/"+date_str_array[2]);
//            if (!mFtpClient.changeWorkingDirectory(server_file_Path)) {
//                mFtpClient.makeDirectory(server_file_Path);
//                Log.e("folder creation 3","ok");
//                mFtpClient.changeWorkingDirectory(server_file_Path);
////                Toast.makeText(this,"folder creation 3",Toast.LENGTH_LONG).show();
//            }
//            FileInputStream srcFileStream = null;
//            for (int i=0;i<3;i++){
//                String full_path = server_file_Path + "/" + file_names.get(i)+".png";
//                srcFileStream = new FileInputStream(files.get(i));
//                mFtpClient.changeWorkingDirectory(full_path);
//                mFtpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
//                boolean status=mFtpClient.storeFile(full_path, srcFileStream);
//                statuses.add(status);
////                Toast.makeText(this,"upload photo "+i+" "+full_path+status,Toast.LENGTH_LONG).show();
//                Log.e("Status", full_path+ " " + statuses.get(i));
//            }
//            if (srcFileStream!=null) srcFileStream.close();
//            uploadListener.uploadListener(statuses);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void resetAll() {
        prefEditor.clear();
        prefEditor.commit();
    }

    public static void clear() {
        prefEditor.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {

        super.onCreate();
        mInstance = this;
        preferences = getSharedPreferences("cmp", Context.MODE_PRIVATE);
        prefEditor = preferences.edit();
        gpsTracker = new GPSTracker(mInstance);

        prefEditor.apply();
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert pinfo != null;
        versionName = pinfo.versionName;
        // -----------------------------------------------------------------------------------------
        Realm.init(this);
        final RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("cmp.realm")
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .migration(new RealmMigrations())
                .build();

        Realm.setDefaultConfiguration(realmConfiguration);

        realm = Realm.getInstance(realmConfiguration);
        base_url = readRawTextFile(this, R.raw.base_url);
        //http://cmp.cz/test_api/
        //http://cmp.schamann.net/
        //Find the directory for the SD Card using the API
        //*Don't* hardcode "/sdcard"
        myFolder = Environment.getExternalStorageDirectory() + File.separator + app_folder;
//        myFolder = getExternalFilesDir(null).getAbsolutePath() +File.separator+ app_folder;
        photoFolder = myFolder + File.separator + "Photos";
        esignFolder = myFolder + File.separator + PreferenceManager.Esign;
        uploadFolder = esignFolder + File.separator + PreferenceManager.Upload;
        downLoadFolder = esignFolder + File.separator + PreferenceManager.Download;
        signedFolder = downLoadFolder + File.separator + PreferenceManager.Signed;
        logFolder = myFolder + File.separator + PreferenceManager.loged;
        try {
            initializeFolders();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("locationUpdate"));

    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //put here whaterver you want your activity to do with the intent received
            // destLocation==null?"0.0":location.bearingTo(destLocation)+""
            android.location.Location location = intent.getExtras().getParcelable("location");
//            Log.e(TAG,"bReceiver received "+location.toString());
            String Tag_req = "updateLocation";
            String url = String.format(PreferenceManager.updateLocation, PreferenceManager.getID(), location.getLatitude(), location.getLongitude());
//            Log.e("url",url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, response -> {
//                        Log.e(Tag_req,response.toString());
//                        Toast.makeText(getApplicationContext(),response.toString()+" location "+location.getLatitude()+" "+location.getLongitude(),Toast.LENGTH_LONG).show();
                    }, error -> {
                        Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_LONG).show();
                    });
            // add the request object to the queue to be executed
            addToRequestQueue(jsonObjectRequest, Tag_req);
        }
    };

    public static void setURLS() {
        base_url = base_url.trim();
        login_url = base_url + "mobile/cmpLogin/";
        getinitialdata_url = base_url + "RestController.php?view=getinitialdata&ID=";
        getlanguage_url = base_url + "mobile/getlanguage/cmp/";
        changeParcelStatus_url = base_url + "RestController.php?view=changeParcelStatus&process_type=";
        default_company = "mobile/Skovajsa s.r.o.";
        checkbarcode_url = base_url + "mobile/checkbarcode/";
        uploadImage_url = base_url + "uploadImage.php";
        getlocations_url = base_url + "mobile/getlocations/";
        removeOrder_url = base_url + "mobile/removeOrder/";
        resetOrder_url = base_url + "mobile/resetOrder/";
        getOrders_url = base_url + "RestController.php?view=getOrders&ID=";
        sendSMS_url = base_url + "mobile/sendSMS/";
        getEsign_url = base_url + "mobile/getEsign/";
        getPin_url = base_url + "mobile/getPin/";
        sendPin_url = base_url + "mobile/sendPin/";
        sendFile_url = base_url + "mobile/sendFile/";
        changeOrderStatus_url = base_url + "RestController.php?view=changeOrderStatus&process_type=";
        checkButtons_url = base_url + "mobile/checkButtons/";
        updatePayment_url = base_url + "mobile/updatePayment/%s===%d/";
        setPaymentMode = base_url + "mobile/setPaymentMode/%s===%d/";
        idWrong_url = base_url + "mobile/idWrong/%s===%s/";
        updateLocation = base_url + "mobile/updateLocation/%s===%f===%f/";
        getArchivedBarcodes = base_url + "mobile/getArchivedBarcodes/%s===%d/";
        addNoteAfterDelivery = base_url + "mobile/addNoteAfterDelivery/%d===%s===%s===%s/";
        addLockBox = base_url + "mobile/addLockBox/%s===%d===%s===%s/";
        lockBoxDone = base_url + "mobile/lockBoxDone/%d===%s/";
        getLockBoxCount = base_url + "mobile/getLockBoxCount/%s/";
    }

    public static boolean initializeFolders() throws Exception {
        File myDir = new File(myFolder);//Constant.sp+getString(R.string.app_name)
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        myDir = new File(photoFolder);
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        myDir = new File(logFolder);
        logFile = new File(logFolder + "/log.txt");
        try {
            if (!logFile.exists() && !logFile.createNewFile())
                throw new Exception("File Creation Error");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        myDir = new File(esignFolder);
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        File noMedia = new File(esignFolder + "/.nomedia");
        try {
            if (!noMedia.exists() && !noMedia.createNewFile())
                throw new Exception("File Creation Error");
            byte[] data1 = {1, 1, 0, 0};
            if (noMedia.exists()) {
                OutputStream fo = new FileOutputStream(noMedia);
                fo.write(data1);
                fo.close();
                System.out.println("file created: " + noMedia);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        myDir = new File(uploadFolder);
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        myDir = new File(downLoadFolder);
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        myDir = new File(signedFolder);
        if (!myDir.exists() && !myDir.mkdir()) throw new Exception("Folder Creation Error");
        if (getServerUrl().equals("")) return false;
        base_url = getServerUrl();
        setURLS();
        return true;
    }

    public static void writeToFile(String data, File file) {
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(data);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToFile(String data, File file) {
        try {
            FileOutputStream f = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(f);
            pw.println(data);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized PreferenceManager getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void onTerminate() {
        realm.close();
        realm.deleteAll();
        super.onTerminate();
    }

    public static void setState(String state) {
        prefEditor.putString(State, state);
        prefEditor.apply();
    }

    public static String getState() {
        return preferences.getString(State, "");
    }

    public static void setCertificate(String certificate) {
        prefEditor.putString(Certificate, certificate);
        prefEditor.apply();
    }

    public static String getCertificate() {
        return preferences.getString(Certificate, "");
    }

    public static void setServerUrl(String serverUrl) {
        prefEditor.putString(ServerUrl, serverUrl);
        prefEditor.apply();
    }

    public static String getServerUrl() {
        return preferences.getString(ServerUrl, "");
    }


    public static String getLocation() {
        return preferences.getString(Location, "null");
    }

    public static void setLocation(String location) {
        prefEditor.putString(Location, location);
        prefEditor.apply();
    }

    public static String getFtpESIGN_Login() {
        return preferences.getString(FTP_ESIGN_LOGIN, "null");
    }

    public static String getFtpESIGN_Pass() {
        return preferences.getString(FTP_ESIGN_PASS, "null");
    }

    public static String getFtpESIGN_Ip() {
        return preferences.getString(FTP_ESIGN_IP, "null");
    }

    public static String getFtpESIGN_Path() {
        return preferences.getString(FTP_ESIGN_PATH, "null");
    }

    public static void setFtpESIGN_Ip(String ftpPath) {
        prefEditor.putString(FTP_ESIGN_IP, ftpPath);
        prefEditor.apply();
    }

    public static void setFtpESIGN_Login(String ftpPath) {
        prefEditor.putString(FTP_ESIGN_LOGIN, ftpPath);
        prefEditor.apply();
    }

    public static void setFtpESIGN_Pass(String ftpPath) {
        prefEditor.putString(FTP_ESIGN_PASS, ftpPath);
        prefEditor.apply();
    }

    public static void setFtpESIGN_Path(String ftpPath) {
        prefEditor.putString(FTP_ESIGN_PATH, ftpPath);
        prefEditor.apply();
    }

    public static String getFtpLogin() {
        return preferences.getString(FTP_LOGIN, "null");
    }

    public static String getFtpPass() {
        return preferences.getString(FTP_PASS, "null");
    }

    public static String getFtpIp() {
        return preferences.getString(FTP_IP, "null");
    }

    public static String getFtpPath() {
        return preferences.getString(FTP_PATH, "null");
    }

    public static String getFtpCertificatePath() {
        return "SFTP/CERTIFICATES/PFX";
    }

    public static void setFtpIp(String ftpPath) {
        prefEditor.putString(FTP_IP, ftpPath);
        prefEditor.apply();
    }

    public static void setFtpLogin(String ftpPath) {
        prefEditor.putString(FTP_LOGIN, ftpPath);
        prefEditor.apply();
    }

    public static void setFtpPass(String ftpPath) {
        prefEditor.putString(FTP_PASS, ftpPath);
        prefEditor.apply();
    }

    public static void setFtpPath(String ftpPath) {
        prefEditor.putString(FTP_PATH, ftpPath);
        prefEditor.apply();
    }

    public static void setName(String name) {
        prefEditor.putString(Name, name);
        prefEditor.apply();
    }

    public static String getName() {
        return preferences.getString(Name, "");
    }

    public static void setPass(String pass) {
        prefEditor.putString(Pass, pass);
        prefEditor.apply();
    }

    public static String getPass() {
        return preferences.getString(Pass, "");
    }

    public static void setLoginTime() {
        Date now = new Date();
        prefEditor.putString(LoginTime, dateFormat.format(now));
        prefEditor.apply();
    }

    public static void resetLoginTime() {
        prefEditor.putString(LoginTime, "0000-00-00");
        prefEditor.apply();
    }

    public static boolean checkLoginTime() {
        String last_loginTime = preferences.getString(LoginTime, "0000-00-00");
        Date now = new Date();
        return last_loginTime.equals(dateFormat.format(now));
    }

    public static void setAddress(String address) {
        prefEditor.putString(Address, address);
        prefEditor.apply();
    }

    public static String getAddress() {
        return preferences.getString(Address, "");
    }

    public static void setPhone(String phone) {
        prefEditor.putString(Phone, phone);
        prefEditor.apply();
    }

    public static String getPhone() {
        return preferences.getString(Phone, "");
    }

    public static void setLoginStatus(String loginStatus) {
        prefEditor.putString(LoginStatus, loginStatus);
        prefEditor.apply();
    }

    public static String getLoginStatus() {
        return preferences.getString(LoginStatus, "");
    }


    public static void setToken(String token) {
        prefEditor.putString(Token, token);
        prefEditor.apply();
    }

    public static String getToken() {
        return preferences.getString(Token, "");
    }

    public static void setID(long id) {
        prefEditor.putLong(ID, id);
        prefEditor.apply();
    }

    public static long getID() {
        return preferences.getLong(ID, -1);
    }

    public static void setEmail(String email) {
        prefEditor.putString(Email, email);
        prefEditor.apply();
    }

    public static String getEmail() {
        return preferences.getString(Email, "");
    }

    public static void logoutManager() {
        resetLoginTime();
        setPass("");
        setEmail("");
        setID(-1);
        setName("");
        setLoginStatus(Constant.LoginStatus.UserNotSignedIn.toString());
    }

    public static String readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
}
