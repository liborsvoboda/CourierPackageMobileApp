package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import net.lingala.zip4j.exception.ZipException;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import zhe.it_tech613.com.cmpcourier.BuildConfig;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.dialog.NoteDlg;
import zhe.it_tech613.com.cmpcourier.dialog.PinDlg;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

import static zhe.it_tech613.com.cmpcourier.utils.Constant.ZMENA_ID_0;
import static zhe.it_tech613.com.cmpcourier.utils.Constant.ZMENA_ID_1;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.signosign_package_name;

public class ChangeStatusActivity extends AppCompatActivity {
    private cmpApi cmpApi;
    private TextView tv_order_is;
    private Button btn_delivered, btn_not_at_home, btn_wrong_address, btn_rejected, btn_other_time, btn_undelivered, btn_send_pass;
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_TAKE_PHOTO_2 = 2;
    String client;
    String date_time = "";
    int mYear;
    int mMonth;
    int mDay;
    String barcode;
    int mHour;
    int mMinute;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final String TAG = "ChangeStatusActivity";
    private float pay_type = -1;
    private static final int REQUEST_CODE_SIGN = 4711;
    public static final int REQUEST_CODE_DELIVERY = 6000;
    public static final int REQUEST_CODE_DOWNLOAD_ZIP = 5899;
    public static final int REQUEST_CODE_ID_CHECK = 5860;
    public static final String KEY_DOWNLOAD_ZIP = "requestCode";
    public static final int RESULT_NO_FILES = 1234;
    public static final int RESULT_FAILED = 1235;
    private List<String> fileUriList;
    private String esign = null, TEST_DAVKA = null;
    private boolean POCET_ShowInputBox = false, POCET2_GO_SIGN = false;
    private ParcelModel parcelModel;
    private int countRecycling = 0;
    private static int interval = 3000;

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(PreferenceManager.getName());
        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
        arrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        TextView version_number = (TextView) findViewById(R.id.logout);
        version_number.setText("v. " + PreferenceManager.versionName);
        ImageView goHome = (ImageView) findViewById(R.id.home);
        goHome.setOnClickListener(v -> startActivity(new Intent(ChangeStatusActivity.this, MainActivity.class)));
        cmpApi = new cmpApi(this);
        Intent intent = getIntent();
        barcode = intent.getStringExtra("barcode");
        client = intent.getStringExtra("client");
        tv_order_is = findViewById(R.id.tv_order_is);
        btn_send_pass = findViewById(R.id.btn_send_pass);
        btn_send_pass.setText(Constant.czlanguageStrings.getSEND_PASSWORD());
        btn_delivered = findViewById(R.id.btn_delivered);
        btn_delivered.setText(Constant.czlanguageStrings.getDELIVERED());
        btn_not_at_home = findViewById(R.id.btn_not_at_home);
        btn_not_at_home.setText(Constant.czlanguageStrings.getNOT_AT_HOME());
        btn_wrong_address = findViewById(R.id.btn_wrong_address);
        btn_wrong_address.setText(Constant.czlanguageStrings.getWRONG_ADDRESS());
        btn_rejected = findViewById(R.id.btn_rejected);
        btn_rejected.setText(Constant.czlanguageStrings.getREJECTED());
        btn_other_time = findViewById(R.id.btn_other_time);
        btn_other_time.setText(Constant.czlanguageStrings.getOTHER_TIME());
        btn_undelivered = findViewById(R.id.btn_undelivered);
        btn_undelivered.setText(Constant.czlanguageStrings.getUNDELIVERED());
        //Color setting
        btn_delivered.setBackgroundColor(Color.parseColor("#FF009801"));
        btn_not_at_home.setBackgroundColor(Color.parseColor("#FFFD66CB"));
        btn_wrong_address.setBackgroundColor(Color.parseColor("#FFFD6601"));
        btn_rejected.setBackgroundColor(Color.parseColor("#FFFD0001"));
        btn_other_time.setBackgroundColor(Color.parseColor("#FF7F7F01"));
        btn_undelivered.setBackgroundColor(Color.parseColor("#000000"));
        parcelModel = PreferenceManager.realm.where(ParcelModel.class)
                .equalTo("barcode", barcode)
                .findFirst();
        tv_order_is.setText(Constant.czlanguageStrings.getORDER_IS() + " " + parcelModel.getOrderId());
        //listener
        btn_send_pass.setOnClickListener(view -> {
            sendPin(1);
        });
        btn_rejected.setOnClickListener(view -> {
            Intent exit_intent = new Intent(ChangeStatusActivity.this, SelectReasonActivity.class);
            exit_intent.putExtra("barcode", barcode);
            startActivity(exit_intent);
        });
        btn_delivered.setOnClickListener(view -> {
            if (esign == null) return;
            if (esign.equalsIgnoreCase("0")) {
                // && showInputBox
                if (TEST_DAVKA!=null && (TEST_DAVKA.startsWith("DISP_LB_") || TEST_DAVKA.startsWith("VRAT_"))) {
                    Intent intent1 = new Intent(ChangeStatusActivity.this, LockboxActivity.class);
                    intent1.putExtra("barcode",barcode);
                    intent1.putExtra("client",client);
                    intent1.putExtra("type", TEST_DAVKA.startsWith("DISP_LB_")? 0:1);
                    startActivity(intent1);
                    finish();
                } else {
                    if (POCET_ShowInputBox) {
                        changeOrderStatus(barcode, 1, "", false);
                    } else {
                        if (POCET2_GO_SIGN){
                            new NoteDlg(ChangeStatusActivity.this, parcelModel.getClient(), (dialog, name, id_card) -> {
                                dialog.dismiss();
                                addNoteAfterDelivery(PreferenceManager.getID(), barcode, name, id_card);
                            }).show();
                        }else changeOrderStatus(barcode, 1, "", true);
                    }
                }
            } else {
                if (checkBarcodeZip()) unZip(1);
                else {
                    sendFile();
                }
            }
        });
        getEsign(barcode);
        btn_send_pass.setVisibility(View.GONE);
        btn_not_at_home.setOnClickListener(view -> changeOrderStatus(barcode, 2, "", false));
        btn_wrong_address.setOnClickListener(view -> changeOrderStatus(barcode, 3, "", false));
        btn_other_time.setOnClickListener(view -> datePicker(barcode));
        btn_undelivered.setOnClickListener(view -> changeOrderStatus(barcode, 6, "", false));

        if (ContextCompat.checkSelfPermission(ChangeStatusActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ChangeStatusActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ChangeStatusActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_CAMERA_REQUEST_CODE);
            }
        }
    }

    private boolean checkBarcodeZip() {
        File downLoadFolder = new File(PreferenceManager.downLoadFolder);
        File[] files = downLoadFolder.listFiles();
        if (files != null && files.length > 1) {
            for (File file : files) {
                Log.e("fileName", file.getName());
                if (file.getName().equals(barcode + ".zip")) return true;
            }
        }
        return false;
    }

    private void goUnzip(String pin, String pin_code) {
        String fileName;
        fileName = barcode + ".zip";
        if ((fileUriList = Constant.unZipGeneral(ChangeStatusActivity.this,
                PreferenceManager.downLoadFolder + File.separator + fileName, pin, pin_code)).size() > 0) {
            this.pin = pin;
            this.pin1 = pin_code;
            if (isPackageInstalled(signosign_package_name, getPackageManager())) {
                if (fileUriList.get(0).contains("ucastnicka_smlouva.pdf")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Make Photo!")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> {
                                //do things
                                openSignosign();
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    openSignosign();
                }
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + signosign_package_name)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + signosign_package_name)));
                }
            }
        } else
            Toast.makeText(ChangeStatusActivity.this, "Unknown Error Occurred!", Toast.LENGTH_LONG).show();
    }

    private void openSignosign() {
        Intent i = new Intent("android.intent.action.VIEW");
        i.setType("application/pdf");
        i.putExtra("url", fileUriList.get(0));
        startActivityForResult(i, REQUEST_CODE_SIGN);
    }

    private void unZip(String pin, String pin_1) {
        if (pin_1 != null && pin_1.length() == 6) {
            goUnzip(pin, pin_1);
        } else {
            PinDlg pinDlg = new PinDlg(this, new PinDlg.DlgPinListener() {
                @Override
                public void OnYesClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                    goUnzip(pin, pin_code);
                }

                @Override
                public void OnCancelClick(Dialog dialog) {
                    dialog.dismiss();
                }

                @Override
                public void OnSendPassClick(Dialog dialog) {
                    new Handler().postDelayed(() -> {
                        sendPin(0);
                    }, interval);
                    dialog.dismiss();
                }
            });
            pinDlg.show();
        }
    }

    private void sendFile() {
        cmpApi.kpHUD.show();
        String url = PreferenceManager.sendFile_url + barcode + "/";
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("sendFile_url_response", response.toString());
                    try {
                        String value = response.getString("value");
                        if (value.equals("0"))
                            Toast.makeText(ChangeStatusActivity.this, "Server Insert Operation Successfully", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(ChangeStatusActivity.this, "Failed Server Insert Operation", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cmpApi.kpHUD.dismiss();
                    Intent intent = new Intent(ChangeStatusActivity.this, EsignActivity.class);
                    intent.putExtra(KEY_DOWNLOAD_ZIP, REQUEST_CODE_DOWNLOAD_ZIP);
                    startActivityForResult(intent, REQUEST_CODE_DOWNLOAD_ZIP);
                }, error -> {
                    cmpApi.kpHUD.dismiss();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, url);
    }

    private void sendPin(int type) {
        cmpApi.kpHUD.show();
        String url = PreferenceManager.sendPin_url + barcode + "/";
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("sendpin_response", response.toString());
                    try {
                        String value = response.getString("value");
                        if (value.equals("0"))
                            Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getSENT_PASSWORD_SUCCESS(), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getFAILED_SEND_PIN(), Toast.LENGTH_LONG).show();
                        if (type == 0) {
                            countRecycling = 0;
                            new Handler().postDelayed(() -> unZip(0), interval);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cmpApi.kpHUD.dismiss();

                }, error -> {
                    cmpApi.kpHUD.dismiss();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, url);
    }

    private void datePicker(final String barcode) {

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar that_time = Calendar.getInstance();
                        that_time.set(year, monthOfYear, dayOfMonth);
                        Calendar now = Calendar.getInstance();
                        if (now.after(that_time))
                            Toast.makeText(ChangeStatusActivity.this, "You can't select past date!", Toast.LENGTH_LONG).show();
                        else {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            date_time = format.format(that_time.getTime());
                            //*************Call Time Picker Here ********************
                            changeOrderStatus(barcode, 5, date_time, false);
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void tiemPicker(final String barcode) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mHour = hourOfDay;
                        mMinute = minute;
                        changeOrderStatus(barcode, 5, date_time + " " + hourOfDay + ":" + minute, false);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                takePhoto(REQUEST_TAKE_PHOTO_2);
            } else if (requestCode == REQUEST_TAKE_PHOTO_2) {
                gotoSignActivity();
            }
        }
        if (requestCode == REQUEST_CODE_SIGN) {
            switch (resultCode) {
                case 2021:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY CUSTOM ARCHIVING", Toast.LENGTH_LONG).show();
                    break;
                case 2020:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY ARCHIVE", Toast.LENGTH_LONG).show();
                    break;
                case 2025:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY DROPBOX", Toast.LENGTH_LONG).show();
                    break;
                case 2026:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY SENDTOURL", Toast.LENGTH_LONG).show();
                    break;
                case 1001:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY CANCEL FILE SELECT", Toast.LENGTH_LONG).show();
                    break;
                case 1002:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY DROPBOX LOGIN ERROR", Toast.LENGTH_LONG).show();
                    break;
                case 1003:
                    Toast.makeText(this, "SIGNOSIGN FINISHED BY CLOSE", Toast.LENGTH_LONG).show();
                    break;
            }
            Log.e("Files", "size " + fileUriList.size());
            if (fileUriList.size() == 0) return;
            fileUriList.remove(0);
            if (fileUriList.size() > 0) {
                openSignosign();
            } else workAfterSign();
        }
        if (requestCode == REQUEST_CODE_DOWNLOAD_ZIP) {
            switch (resultCode) {
                case RESULT_NO_FILES:
                    Toast.makeText(this, "No Files", Toast.LENGTH_LONG).show();
                    break;
                case RESULT_FAILED:
                    Toast.makeText(this, "Failed to download", Toast.LENGTH_LONG).show();
                    break;
                case RESULT_OK:
                    btn_delivered.performClick();
                    break;
            }
        }
        if (requestCode == REQUEST_CODE_ID_CHECK) {
            switch (resultCode) {
                case ZMENA_ID_0:
                    changeOrderStatus(barcode, 1, "", true, 0);
                    break;
                case ZMENA_ID_1:
                    changeOrderStatus(barcode, 1, "", true, 1);
                    break;
                case RESULT_OK:
                    changeOrderStatus(barcode, 1, "", true);
                    break;
                case RESULT_CANCELED:
                    break;
                case RESULT_FAILED:
                    finish();
                    break;
            }
        }
    }

    private void gotoSignActivity() {
        Intent intent = new Intent(ChangeStatusActivity.this, SignatureActivity.class);
        intent.putExtra("client", client);
        intent.putExtra("barcode", barcode);
        if (pay_type != -1) intent.putExtra("pay_type", pay_type);
        startActivity(intent);
    }

    private void workAfterSign() {
        try {
            if (Constant.zipFiles(this,
                    PreferenceManager.signedFolder,
                    PreferenceManager.uploadFolder + File.separator + barcode + ".zip",
                    barcode, pin, pin1)) {
                Constant.deleteFiles(PreferenceManager.downLoadFolder + File.separator + barcode);
                File file = new File(PreferenceManager.downLoadFolder + File.separator + barcode);
                file.delete();
                Toast.makeText(this, Constant.czlanguageStrings.getSUCCESS_ZIP(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ChangeStatusActivity.this, IDCheckActivity.class);
                intent.putExtra("barcode", barcode);
                startActivityForResult(intent, REQUEST_CODE_ID_CHECK);
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void takePhoto(int REQUEST_CAMERA) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (REQUEST_CAMERA == REQUEST_TAKE_PHOTO) {
                PreferenceManager.getInstance().file_names = new ArrayList<>(3);
                PreferenceManager.getInstance().files = new ArrayList<>(3);
                PreferenceManager.getInstance().file_names.add(barcode + "_1");
                // place where to store camera taken picture
                PreferenceManager.getInstance().files.add(createTemporaryFile(PreferenceManager.getInstance().file_names.get(0)));
                Uri mImageUri1 = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".share", PreferenceManager.getInstance().files.get(0));
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri1);
            } else if (REQUEST_CAMERA == REQUEST_TAKE_PHOTO_2) {
                PreferenceManager.getInstance().file_names.add(barcode + "_2");
                // place where to store camera taken picture
                PreferenceManager.getInstance().files.add(createTemporaryFile(PreferenceManager.getInstance().file_names.get(1)));
                Uri mImageUri2 = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".share", PreferenceManager.getInstance().files.get(1));
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri2);
            }
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(ChangeStatusActivity.this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
            return;
        }
        //start camera intent
        startActivityForResult(takePhotoIntent, REQUEST_CAMERA);
    }

    private File createTemporaryFile(String part) {
        File myDir = new File(PreferenceManager.photoFolder);//Constant.sp+getString(R.string.app_name)
        try {
            if (myDir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String fname = part + ".png";
        return new File(myDir, fname);
    }

//    public Bitmap grabImage(){
//        this.getContentResolver().notifyChange(mImageUri1, null);
//        ContentResolver cr = this.getContentResolver();
//        Bitmap bitmap;
//        try
//        {
//            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri1);
//        }
//        catch (Exception e)
//        {
//            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Failed to load", e);
//            bitmap=null;
//        }
//        return bitmap;
//    }

//
//    private void uploadMultipart(String file_path) {
//        try {
//            String uploadUUID = UUID.randomUUID().toString();
//            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd--hh:mm:ss");
//            String now=format.format(new Date());
//            String uploadId =
//                    new MultipartUploadRequest(ChangeStatusActivity.this, uploadUUID, Constant.uploadImage_url)
//                            // starting from 3.1+, you can also use content:// URI string instead of absolute file
//                            .addFileToUpload(file_path, "uploads")
//                            .addParameter("name", PreferenceManager.getEmail()+"_ID_Photo_"+now)
//                            .setNotificationConfig(new UploadNotificationConfig())
//                            .setMaxRetries(2)
//                            .startUpload();
//        } catch (Exception exc) {
//            Log.e("AndroidUploadService", exc.getMessage(), exc);
//        }
//    }

//    private void uploadImage(File uploadFile,String fname){

    //        try {
//            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd--hh:mm:ss");
//            String now=format.format(new Date());
//            MultipartBody.Builder builder=new MultipartBody.Builder();
//            builder.setType(MultipartBody.FORM).addFormDataPart("uploads", fname,
//                    RequestBody.create(MediaType.parse("image/png"), photo1));
//            builder.addFormDataPart("name", PreferenceManager.getEmail()+"_ID_Photo_"+now);
//            RequestBody requestBody = builder.build();
//
//            okhttp3.Request request = new okhttp3.Request.Builder()
//                    .url(PreferenceManager.uploadImage_url)
//                    .post(requestBody)
//                    .build();
//
//            PreferenceManager.client.newCall(request).enqueue(new Callback() {
//
//                @Override
//                public void onFailure(final Call call, final IOException e) {
//                    Log.e("errorListener","failed_upload");
//
//                }
//
//                @Override
//                public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                    Log.e("responseListener","success_upload");
//
//                }
//            });
//
//            return true;
//        } catch (Exception ex) {
//        // Handle the error
//            Log.e("errorListener","failed_send upload request");
//            return false;
//        }
//    }
    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getCAMERA_PERMISSION_ALERT(), Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getCAMERA_PERMISSION_DENIED_ALERT(), Toast.LENGTH_LONG).show();

            }
        }
    }

    private void getEsign(String barcode) {
        String Tag_req = "req_get_esign";
        cmpApi.kpHUD.show();
        String url = PreferenceManager.getEsign_url + barcode + "/";
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("getesign_response", response.toString());
                    cmpApi.kpHUD.dismiss();
                    try {
                        if (response.getString("value").equalsIgnoreCase("0")) {
                            esign = response.getString("ESIGN");
                            POCET_ShowInputBox = response.getBoolean("showInputBox");
                            POCET2_GO_SIGN = response.getBoolean("goSign");
                            TEST_DAVKA = response.getString("testDavka");
                            Log.e("esign", esign);
                            if (!esign.equalsIgnoreCase("0"))
                                btn_send_pass.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    cmpApi.kpHUD.dismiss();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, Tag_req);
    }

    private String pin, pin1;

    private void unZip(int type) {
        String Tag_req = "req_getPin_url";
        cmpApi.kpHUD.show();
        String url = PreferenceManager.getPin_url + barcode + "/";
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("getpin_response", response.toString());
                    cmpApi.kpHUD.dismiss();
                    try {
                        if (response.getString("value").equalsIgnoreCase("0")) {
                            String pin = response.getString("PIN");
                            String pin1 = response.getString("PIN_1");
//                            if (pin1.equals("null")){
//                                sendPin(0);
//                                return;
//                            }
                            Log.e("esign", pin + " " + pin1);
                            if (type == 1 || type == 0 && countRecycling == 10 || pin1.length() == 6)
                                unZip(pin, pin1);
                            else if (type == 0 && countRecycling < 10) {
                                countRecycling += 1;
                                new Handler().postDelayed(() -> unZip(0), interval);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    cmpApi.kpHUD.dismiss();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, Tag_req);
    }

    private void addNoteAfterDelivery(long id, String barcode, String name, String idcard) {
        String Tag_req = "req_change_status";
        cmpApi.kpHUD.show();
        String url = String.format(PreferenceManager.addNoteAfterDelivery, id, name, idcard, barcode);
        Log.e("addNoteAfterDelivery", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("addNoteAfterDelivery", response.toString());
                    cmpApi.kpHUD.dismiss();
                    changeOrderStatus(barcode, 1, "", true);
                }, error -> {
                    cmpApi.kpHUD.dismiss();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, Tag_req);
    }

    private void changeOrderStatus(String barcode, final int process_type, String time, boolean finish) {
        changeOrderStatus(barcode, process_type, time, finish, -1);
    }

    private void changeOrderStatus(String barcode, final int process_type, String time, boolean finish, int ZMENA_ID) {
        String Tag_req = "req_change_status";
        cmpApi.kpHUD.show();
        String url = PreferenceManager.changeOrderStatus_url +
                process_type + "&barcode=" +
                barcode + "&ID=" +
                PreferenceManager.getID() + "&reason=0&time=" + time + "&reason_discription=0";
        if (ZMENA_ID != -1) url = url + "&zmena_id=" + ZMENA_ID;
        Log.e("changeOrderStatus_url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("changeorderresponse", response.toString());
                    cmpApi.kpHUD.dismiss();
                    if (cmpApi.parseChangeOrderStatus(response)) {
                        Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getSUCCESS_CHANGE_PARCEL_STATUS(), Toast.LENGTH_LONG).show();
//                            startActivity(new Intent(ChangeStatusActivity.this,ListParcelActivity.class));
//                            finish();
                        if (process_type == 1) {
                            String POCET = "0";
                            try {
                                POCET = response.getString("POCET");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                String payment_type = response.getString("payment_type");
                                pay_type = Float.parseFloat(payment_type);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (POCET.equals("1")) {
                                if (!finish) {
                                    takePhoto(REQUEST_TAKE_PHOTO);

                                } else {

                                    if (POCET2_GO_SIGN) {
                                        gotoSignActivity();
                                    }else{
                                        finish();
                                    }

                                }

                            } else {
                                if (POCET2_GO_SIGN) {
                                    gotoSignActivity();
                                }else{
                                    startActivity(new Intent(ChangeStatusActivity.this, ListParcelActivity.class));
                                    finish();
                                }
//                                startActivity(new Intent(ChangeStatusActivity.this, ListParcelActivity.class));
//                                finish();
                            }
                        } else {
                            startActivity(new Intent(ChangeStatusActivity.this, ListParcelActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(ChangeStatusActivity.this, Constant.czlanguageStrings.getFAIL_CHANGE_PARCEL_ALERT(), Toast.LENGTH_LONG).show();
                        startActivity(new Intent(ChangeStatusActivity.this, ListParcelActivity.class));
                        finish();
                    }
                }, error -> {
                    cmpApi.kpHUD.dismiss();
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
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, Tag_req);
    }
}
