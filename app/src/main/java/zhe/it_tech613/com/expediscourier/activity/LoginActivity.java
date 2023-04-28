package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import zhe.it_tech613.com.cmpcourier.BuildConfig;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.Status;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.DownloadCertificateTask;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class LoginActivity extends AppCompatActivity {

    EditText et_email, et_pass;
    Button btn_send;
    cmpApi cmpApi;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        cmpApi = new cmpApi(LoginActivity.this);
        if (PreferenceManager.checkLoginTime() && PreferenceManager.getLoginStatus().equals(Constant.LoginStatus.UserLoggedIn.toString()))
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        et_email = (EditText) findViewById(R.id.email);
        et_email.setHint(Constant.czlanguageStrings.getEMAIL());
        et_pass = (EditText) findViewById(R.id.password);
        et_pass.setHint(Constant.czlanguageStrings.getPASSWORD());
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setText(Constant.czlanguageStrings.getSEND());
        Button btn_server = findViewById(R.id.btn_server);
        btn_server.setOnClickListener(v -> gotoServerUrlSettingPage());
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pinfo.versionName;
            TextView tv_version = (TextView) findViewById(R.id.version_name);
            tv_version.setText("Courier " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_email.getText().toString().trim().length() == 0) {
                    et_email.setError("Napište jméno");
                    return;
                }
                if (et_pass.getText().toString().trim().length() == 0) {
                    et_pass.setError("Zadejte heslo");
                    return;
                }
                login(et_email.getText().toString(), et_pass.getText().toString());
            }
        });
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_CAMERA_REQUEST_CODE);
            }
        } else {
            try {
                PreferenceManager.initializeFolders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void gotoServerUrlSettingPage() {
        startActivityForResult(new Intent(this, ServerUrlSettingActivity.class), 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(this, SplashActivity.class));
            }
            finish();
        }
    }

    private void login(final String email, String password) {
        String Tag_req = "req_login";
        cmpApi.kpHUD.show();
        String url = PreferenceManager.login_url + email + Constant.separator + password + Constant.separator + BuildConfig.VERSION_NAME + Constant.sp;
        Log.e("login", url);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                responseObj -> {
                    if (responseObj != null) {
//                            responseObj.get()
                        Log.e("login", responseObj.toString());
                        if (cmpApi.parseLogin(responseObj)) {
//                            PreferenceManager.setEmail(email);
//                            PreferenceManager.setPass(password);
//                            PreferenceManager.setLoginTime();
//                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            DownloadCertificateTask downloadEsignTask = new DownloadCertificateTask();
                            downloadEsignTask.onGetResult(new DownloadCertificateTask.ConnectionInterface() {
                                @Override
                                public void downloadListener(Status result) {
                                    cmpApi.kpHUD.dismiss();
                                    runOnUiThread(() -> {
                                        switch (result) {
                                            case Failed:
                                                Toast.makeText(LoginActivity.this, Constant.czlanguageStrings.getDOWNLOAD1(), Toast.LENGTH_LONG).show();
                                                break;

                                            case Success:
                                                PreferenceManager.setEmail(email);
                                                PreferenceManager.setPass(password);
                                                PreferenceManager.setLoginTime();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();
                                                break;

                                            case NoFiles:
                                                Toast.makeText(LoginActivity.this, Constant.czlanguageStrings.getNO_FILES(), Toast.LENGTH_LONG).show();
                                                break;

                                            case ConnectedFTP:
                                                break;
                                            case ConnectingFTP:
                                                break;
                                            case GetFiles:
                                                break;
                                            case AccessDirectory:
                                                break;
                                        }
                                    });
                                }

                                @Override
                                public void updateListener(String string, int progress) {
                                    runOnUiThread(() -> {
                                        Log.e("progress", string);
                                    });
                                }
                            });
                            downloadEsignTask.execute();
                        } else
                            Toast.makeText(LoginActivity.this, Constant.czlanguageStrings.getLOGIN_ALERT(), Toast.LENGTH_LONG).show();
                    }
                }, error -> {
            cmpApi.kpHUD.dismiss();
            Toast.makeText(LoginActivity.this, Constant.czlanguageStrings.getLOGIN_ALERT(), Toast.LENGTH_LONG).show();
        });

        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(stringRequest, Tag_req);
    }

    @Override
    public void onBackPressed() {
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    PreferenceManager.initializeFolders();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(LoginActivity.this, Constant.czlanguageStrings.getCAMERA_PERMISSION_ALERT(), Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(LoginActivity.this, Constant.czlanguageStrings.getCAMERA_PERMISSION_DENIED_ALERT(), Toast.LENGTH_LONG).show();

            }

        }
    }

    private void writeToFile(String data, File file) {
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.println(data);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("Write file", "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
