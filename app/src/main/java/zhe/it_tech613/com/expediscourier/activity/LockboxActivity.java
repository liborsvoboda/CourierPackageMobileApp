package zhe.it_tech613.com.cmpcourier.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class LockboxActivity extends AppCompatActivity implements View.OnClickListener {

    private cmpApi cmpApi;
    private List<String> lockBoxes = new ArrayList<>();
    private Button btnScan, btnOk, btnDone;
    private EditText etLockbox;
    private TextView tvCount;
    private String barcode, client;
    private int type, count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockbox);
        cmpApi = new cmpApi(this);

        Intent intent = getIntent();
        barcode = intent.getStringExtra("barcode");
        client = intent.getStringExtra("client");
        type = intent.getIntExtra("type", -1);

        btnScan = findViewById(R.id.btnScan);
        btnOk = findViewById(R.id.btnOk);
        btnDone = findViewById(R.id.btnDone);
        etLockbox = findViewById(R.id.etLockbox);
        tvCount = findViewById(R.id.tvCount);
        etLockbox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String lockBoxNumber = etLockbox.getText().toString().trim();
                Toast.makeText(LockboxActivity.this, "Lockbox number: " + lockBoxNumber, Toast.LENGTH_LONG).show();
                btnOk.performClick();
            }
            return false;
        });
        btnScan.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnDone.setOnClickListener(this);
        getCount();
        setTvCount();

        btnScan.setText(Constant.czlanguageStrings.getSCAN_LOCK_BOX_NUMBER());
        btnOk.setText(Constant.czlanguageStrings.getOK());
        btnDone.setText(Constant.czlanguageStrings.getDONE());
        etLockbox.setHint(Constant.czlanguageStrings.getOR_ENTER_LOCK_BOX_NUMBER());
    }

    private void getCount() {
        cmpApi.kpHUD.show();
        String url = String.format(PreferenceManager.getLockBoxCount, barcode);
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("response", response.toString());
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(LockboxActivity.this, error, Toast.LENGTH_LONG).show();
                        } else {
                            count = Integer.parseInt(response.getString("count"));
                            setTvCount();
                        }
                        cmpApi.kpHUD.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, url);
    }

    @SuppressLint("SetTextI18n")
    private void setTvCount() {
        tvCount.setText(count + " / " + lockBoxes.size());
        etLockbox.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String lockBoxNumber = data.getStringExtra("barcode");
                if (lockBoxes.contains(lockBoxNumber.trim())) {
                    Toast.makeText(this, "Already exists lockbox number", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Lockbox number: " + lockBoxNumber, Toast.LENGTH_LONG).show();
                    etLockbox.setText(lockBoxNumber);
                    btnOk.performClick();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent exit_intent=new Intent(this, ChangeStatusActivity.class);
        exit_intent.putExtra("barcode",barcode);
        exit_intent.putExtra("client",client);
        startActivity(exit_intent);
        finish();
    }

    private void addLockBox() {
        String lockBoxNumber = etLockbox.getText().toString().trim();
        if (lockBoxes.contains(lockBoxNumber) || lockBoxNumber.equals("")) return;
        cmpApi.kpHUD.show();
        String url = String.format(PreferenceManager.addLockBox, PreferenceManager.getID(), type, lockBoxNumber, barcode);
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("response", response.toString());
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(LockboxActivity.this, error, Toast.LENGTH_LONG).show();

                        } else {
                            lockBoxes.add(lockBoxNumber);
                            if (lockBoxes.size() == count) {
                                btnDone.performClick();
                            }
                            setTvCount();
                        }
                        cmpApi.kpHUD.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, url);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnScan:
                Intent intent = new Intent(this,ScannerActivity.class);
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
                break;
            case R.id.btnOk:
                addLockBox();
                break;
            case R.id.btnDone:
                lockBoxDone();
                break;
        }
    }

    private void lockBoxDone() {
        cmpApi.kpHUD.show();
        String url = String.format(PreferenceManager.lockBoxDone, PreferenceManager.getID(), barcode);
        Log.e("url", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("response", response.toString());
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        } else {
                            startActivity(new Intent(this,ListParcelActivity.class));
                            finish();
                        }
                        cmpApi.kpHUD.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
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
            public void retry(VolleyError error) {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, url);
    }
}