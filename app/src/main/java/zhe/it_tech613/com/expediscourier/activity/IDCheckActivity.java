package zhe.it_tech613.com.cmpcourier.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import zhe.it_tech613.com.cmpcourier.BuildConfig;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

import static zhe.it_tech613.com.cmpcourier.activity.ChangeStatusActivity.REQUEST_CODE_DELIVERY;
import static zhe.it_tech613.com.cmpcourier.activity.ChangeStatusActivity.REQUEST_TAKE_PHOTO;
import static zhe.it_tech613.com.cmpcourier.activity.ChangeStatusActivity.REQUEST_TAKE_PHOTO_2;
import static zhe.it_tech613.com.cmpcourier.activity.ChangeStatusActivity.RESULT_FAILED;
import static zhe.it_tech613.com.cmpcourier.utils.Constant.ZMENA_ID_0;
import static zhe.it_tech613.com.cmpcourier.utils.Constant.ZMENA_ID_1;

public class IDCheckActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "IDCheck";
    private cmpApi cmpApi;
    private ParcelModel parcelModel=null;
    private String barcode;
    private int ZMENA_ID = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cmpApi = new cmpApi(this);
        setContentView(R.layout.activity_idcheck);
        Button ID_OK = findViewById(R.id.button);
        Button ID_CHANGE = findViewById(R.id.button2);
        Button ID_WRONG = findViewById(R.id.button3);
        ID_OK.setText(Constant.czlanguageStrings.getID_OK());
        ID_CHANGE.setText(Constant.czlanguageStrings.getID_CHANGE());
        ID_WRONG.setText(Constant.czlanguageStrings.getID_WRONG());
        ID_OK.setOnClickListener(this);
        ID_CHANGE.setOnClickListener(this);
        ID_WRONG.setOnClickListener(this);
        if (getIntent().hasExtra("barcode")){
            barcode = getIntent().getStringExtra("barcode");
            parcelModel=PreferenceManager.realm.where(ParcelModel.class)
                    .equalTo("barcode",barcode)
                    .findFirst();
        }
        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
        arrowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ImageView goHome = (ImageView) findViewById(R.id.home);
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IDCheckActivity.this,MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                ZMENA_ID = 0;
                gotoNextActivity();
                break;
            case R.id.button2:
//                takePhoto(REQUEST_TAKE_PHOTO);
                ZMENA_ID = 1;
                gotoNextActivity();
                break;
            case R.id.button3:
                IDWrong();
                break;
        }
    }

    private void IDWrong() {
        String Tag_req="req_IDWrong";
        String url=String.format(PreferenceManager.idWrong_url,barcode,PreferenceManager.getID());
        Log.e("url",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    cmpApi.kpHUD.dismiss();
                    try {
                        String value=response.getString("value");
                        if (value.equals("0")){
                            Log.e(TAG,"success upload id wrong");
                        }else if (value.equals("-1")){
                            Log.e(TAG,"failed to save id wrong second query");
                        }else Log.e(TAG,"failed to save id wrong first query");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setResult(RESULT_FAILED);
                    finish();

                }, error -> {
                    cmpApi.kpHUD.dismiss();
                    Toast.makeText(this,"Network Error",Toast.LENGTH_LONG).show();
                });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode== REQUEST_TAKE_PHOTO || requestCode== REQUEST_TAKE_PHOTO_2) && resultCode== Activity.RESULT_OK){
            if (requestCode== REQUEST_TAKE_PHOTO){
                takePhoto(REQUEST_TAKE_PHOTO_2);
            }else {
                gotoNextActivity();
            }
        }
        if (requestCode==REQUEST_CODE_DELIVERY){
            if(resultCode == RESULT_OK){
                setResult(ZMENA_ID==0?ZMENA_ID_0:ZMENA_ID_1);
            }else setResult(resultCode);
            finish();
        }
    }

    private void gotoNextActivity() {
        Intent intent;
        if (!parcelModel.getCod().equals("") && !parcelModel.getCod().equals("null") && Float.parseFloat(parcelModel.getCod())>0) intent=new Intent(this,PayActivity.class);
        else intent=new Intent(this,DeliveryActivity.class);
        intent.putExtra("barcode",barcode);
        startActivityForResult(intent,REQUEST_CODE_DELIVERY);
    }

    private void takePhoto(int REQUEST_CAMERA) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (REQUEST_CAMERA==REQUEST_TAKE_PHOTO) {
                PreferenceManager.getInstance().file_names = new ArrayList<>(3);
                PreferenceManager.getInstance().files = new ArrayList<>(3);
                PreferenceManager.getInstance().file_names.add(barcode+"_1");
                // place where to store camera taken picture
                PreferenceManager.getInstance().files.add(createTemporaryFile(PreferenceManager.getInstance().file_names.get(0)));
                Uri mImageUri1 = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".share", PreferenceManager.getInstance().files.get(0));
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri1);
            }
            else if (REQUEST_CAMERA==REQUEST_TAKE_PHOTO_2) {
                PreferenceManager.getInstance().file_names.add(barcode+"_2");
                // place where to store camera taken picture
                PreferenceManager.getInstance().files.add(createTemporaryFile(PreferenceManager.getInstance().file_names.get(1)));
                Uri mImageUri2 = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".share", PreferenceManager.getInstance().files.get(1));
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri2);
            }
        }
        catch(Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(IDCheckActivity.this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
            return;
        }
        //start camera intent
        startActivityForResult(takePhotoIntent,REQUEST_CAMERA);
    }

    private File createTemporaryFile(String part){
        File myDir = new File(PreferenceManager.photoFolder);//Constant.sp+getString(R.string.app_name)
        try{
            if(myDir.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        String fname = part+ ".png";
        return new File (myDir, fname);
    }
}
