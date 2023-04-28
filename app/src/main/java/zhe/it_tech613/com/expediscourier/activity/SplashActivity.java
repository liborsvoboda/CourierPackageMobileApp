package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    cmpApi cmpApi;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        cmpApi=new cmpApi(SplashActivity.this);
        //        ImageView image=(ImageView) findViewById(R.id.image);
        PreferenceManager.realm.executeTransaction(realm -> realm.deleteAll());
        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_CAMERA_REQUEST_CODE);
            }
        }else {
            goto_getLanguage();
        }
    }

    private void goto_getLanguage(){
        try {
            if (PreferenceManager.initializeFolders())
                getlanguage();
            else gotoServerUrlSettingPage();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void gotoServerUrlSettingPage() {
        startActivityForResult(new Intent(this, ServerUrlSettingActivity.class),1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1000){
            if (resultCode==RESULT_OK){
                goto_getLanguage();
            }else finish();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults.length==0) {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, MY_CAMERA_REQUEST_CODE);
                return;
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goto_getLanguage();

            } else {
                getlanguage();
                Toast.makeText(SplashActivity.this,  Constant.czlanguageStrings.getCAMERA_PERMISSION_DENIED_ALERT(), Toast.LENGTH_LONG).show();
            }

        }
    }
    private void getlanguage(){
        String Tag_req="req_splash";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.getlanguage_url;
        Log.e("language_url",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, (Response.Listener<JSONObject>) response -> {
                    cmpApi.kpHUD.dismiss();
                    if (cmpApi.parseLanguage(response)) {
                        TextView welcome_text=(TextView)findViewById(R.id.welcome_text);
                        welcome_text.setText(Constant.czlanguageStrings.getWELCOME_TO_cmp());
                        ConstraintLayout background=(ConstraintLayout)findViewById(R.id.background);
                        int colorFrom = getResources().getColor(R.color.colorPrimary);
                        int colorTo = getResources().getColor(R.color.white);
                        int duration = 1500;
                        ObjectAnimator.ofObject(background, "backgroundColor", new ArgbEvaluator(), colorFrom, colorTo)
                                .setDuration(duration)
                                .start();
                        ObjectAnimator.ofObject(welcome_text, "textColor", new ArgbEvaluator(), colorTo, colorFrom)
                                .setDuration(duration)
                                .start();
                        Animation anim = new AlphaAnimation(0.0f, 1.0f);
                        anim.setDuration(duration);
                        welcome_text.startAnimation(anim);
                        anim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                                finish();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }else Toast.makeText(SplashActivity.this, Constant.czlanguageStrings.getLOGIN_ALERT(),Toast.LENGTH_LONG).show();                    },
                        error -> {
                    error.printStackTrace();
                    cmpApi.kpHUD.dismiss();
                    Toast.makeText(SplashActivity.this, "Server Url Error", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }

}
