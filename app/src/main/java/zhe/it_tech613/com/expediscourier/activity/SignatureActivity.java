package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.dialog.SelectPaymentDlg;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;
import zhe.it_tech613.com.cmpcourier.utils.UploadPhotosTask;


public class SignatureActivity extends Activity implements View.OnTouchListener {
    String TAG = "SignatureActivity";
    public float oldx, oldy, dx, dy, nx, ny;
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    String client, barcode;
    final int REQUEST_STORAGE = 200;
    private cmpApi cmpApi;
    private float pay_amount = -1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        cmpApi = new cmpApi(SignatureActivity.this);
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
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignatureActivity.this, MainActivity.class));
            }
        });

        TextView tv_name = (TextView) findViewById(R.id.tv_name);
        tv_name.setText(Constant.czlanguageStrings.getNAME());
        imageView = (ImageView) findViewById(R.id.draw);
        initImageView();
        Button erase = (Button) findViewById(R.id.erase);
        erase.setText(Constant.czlanguageStrings.getERASE());
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initImageView();
            }
        });
        Button save = (Button) findViewById(R.id.save);
        save.setText(Constant.czlanguageStrings.getSAVE());
        Intent intent = getIntent();
        client = intent.getStringExtra("client");
        barcode = intent.getStringExtra("barcode");
        if (intent.hasExtra("pay_type"))
            pay_amount = intent.getFloatExtra("pay_type", -1.0f);

        save.setOnClickListener(view -> {
            if (isStoragePermissionGranted()) {
                saveFunction();
            }
        });
        TextView name = findViewById(R.id.name);
        name.setText(client);
    }

    private void saveFunction() {
        if (pay_amount > 0) {
            SelectPaymentDlg selectPaymentDlg = new SelectPaymentDlg(SignatureActivity.this, pay_amount, new SelectPaymentDlg.DialogNumberListener() {
                @Override
                public void OnCardClick(Dialog dialog) {
                    dialog.dismiss();
                    updatePayment(0);
                }

                @Override
                public void OnCashClick(Dialog dialog) {
                    dialog.dismiss();
                    updatePayment(1);
                }
            });
            selectPaymentDlg.show();
        } else SaveImage(bitmap);
    }

    private void updatePayment(int payment) {
        String Tag_req = "req_signature";
        String url = String.format(PreferenceManager.updatePayment_url, barcode, payment);
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        cmpApi.kpHUD.dismiss();
                        try {
                            String value = response.getString("value");
                            if (value.equals("0")) {
                                Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getUPLOAD_SUCCESS(), Toast.LENGTH_LONG).show();
                            } else if (value.equals("1")) {
                                Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getUPLOAD_FAILED(), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getUPLOAD_FAILED(), Toast.LENGTH_LONG).show();
                        }
                        SaveImage(bitmap);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cmpApi.kpHUD.dismiss();
                        Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getUPLOAD_FAILED(), Toast.LENGTH_LONG).show();
                    }
                });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, Tag_req);
    }

    private void SaveImage(Bitmap finalBitmap) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //your codes here
        cmpApi.kpHUD.show();
        File myDir = new File(PreferenceManager.photoFolder);//Constant.sp+getString(R.string.app_name)
        myDir.mkdirs();
        PreferenceManager.getInstance().file_names.add(barcode + "_s");
        int lastIndex = PreferenceManager.getInstance().file_names.size()-1;
        PreferenceManager.getInstance().files.add(new File(myDir, PreferenceManager.getInstance().file_names.get(lastIndex) + ".png"));
        if (PreferenceManager.getInstance().files.get(lastIndex).exists())
            PreferenceManager.getInstance().files.get(lastIndex).delete();
        try {
            FileOutputStream out = new FileOutputStream(PreferenceManager.getInstance().files.get(lastIndex));
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            copyFileOrDirectory(PreferenceManager.photoFolder, PreferenceManager.photoFolder + "_backup");
//            uploadMultipart(file_path);
//            uploadImage(fname);
            UploadPhotosTask uploadPhotosTask = new UploadPhotosTask();
            uploadPhotosTask.execute();
            uploadPhotosTask.onGetResult(result -> {
                if (result.size() == lastIndex + 1) {
                    boolean success = true;
                    for (int i =0; i<lastIndex+1;i++) {
                        boolean aBoolean = result.get(i);
                        if (!aBoolean) {
                            success = false;
                            Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getUPLOAD_FAILED() + " " + PreferenceManager.getInstance().file_names.get(i), Toast.LENGTH_LONG).show();
                        }
                    }
                    if (success)
                        Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getUPLOAD_SUCCESS(), Toast.LENGTH_LONG).show();
                }
                cmpApi.kpHUD.dismiss();
                startActivity(new Intent(SignatureActivity.this, ListParcelActivity.class));
                finish();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
//    private void uploadMultipart(String file_path) {
//        try {
//            String uploadUUID = UUID.randomUUID().toString();
//            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd--hh:mm:ss");
//            String now=format.format(new Date());
//            String uploadId =
//                    new MultipartUploadRequest(SignatureActivity.this, uploadUUID, Constant.uploadImage_url)
//                            // starting from 3.1+, you can also use content:// URI string instead of absolute file
//                            .addFileToUpload(file_path, "uploads")
//                            .addParameter("name", PreferenceManager.getEmail()+"_signature"+"_"+now)
//                            .setNotificationConfig(new UploadNotificationConfig())
//                            .setMaxRetries(2)
//                            .startUpload();
//        } catch (Exception exc) {
//            Log.e("AndroidUploadService", exc.getMessage(), exc);
//        }
//    }

//    private boolean uploadImage(String fname){
//        Map<String,String> header=new HashMap<String, String>();
//        try {
//            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd--hh:mm:ss");
//            String now=format.format(new Date());
//            MultipartBody.Builder builder=new MultipartBody.Builder();
//            builder.setType(MultipartBody.FORM).addFormDataPart("uploads", fname,
//                    RequestBody.create(MediaType.parse("image/png"), PreferenceManager.getInstance().signImage));
//            builder.addFormDataPart("name", PreferenceManager.getEmail()+"_signature"+"_"+now);
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
//                    assert response.body() != null;
//                    String response_str=response.body().string();
//                    Log.e("responseListener","success_upload"+response_str);
//                    try {
//                        JSONObject jsonObject = new JSONObject(response_str);
//                        boolean result=jsonObject.getBoolean("result");
//                        if (result)
//                            runOnUiThread(new Runnable() {
//                                public void run() {
//                                    Toast.makeText(SignatureActivity.this,getResources().getString(R.string.upload_success),Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        else {
//                            String message=jsonObject.getString("message");
//                            runOnUiThread(new Runnable() {
//                                public void run() {
//                                    Toast.makeText(SignatureActivity.this,message,Toast.LENGTH_LONG).show();
//                                }
//                            });
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    startActivity(new Intent(SignatureActivity.this,ListParcelActivity.class));
//                }
//            });
//
//            return true;
//        } catch (Exception ex) {
//            // Handle the error
//            Log.e("errorListener","failed_send upload request");
//            return false;
//        }
//    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(SignatureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
//            case REQUEST_STORAGE: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    Toast.makeText(SignatureActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(SignatureActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }

            case REQUEST_STORAGE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getPERMISSION_GRANTED_ALERT(), Toast.LENGTH_SHORT).show();
                    saveFunction();
                } else {
                    Toast.makeText(SignatureActivity.this, Constant.czlanguageStrings.getPERMISSION_DENIED_ALERT(), Toast.LENGTH_SHORT).show();
                }

                // other 'case' lines to check for other
                // permissions this app might request
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initImageView() {
        Display mydisplay = getWindowManager().getDefaultDisplay();
        float dw = mydisplay.getWidth();
        float dh = mydisplay.getHeight();
        bitmap = Bitmap.createBitmap((int) dw, (int) dh, Bitmap.Config.ARGB_8888);
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(this);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                dx = motionEvent.getX();
                dy = motionEvent.getY();
                oldx = dx;
                oldy = dy;
                break;
            case MotionEvent.ACTION_MOVE:
                nx = motionEvent.getX();
                ny = motionEvent.getY();
                canvas.drawLine(oldx, oldy, nx, ny, paint);
                imageView.invalidate();
                oldx = nx;
                oldy = ny;
                break;
        }
        return true;
    }
}
