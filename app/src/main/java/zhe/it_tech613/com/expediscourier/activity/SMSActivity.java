package zhe.it_tech613.com.cmpcourier.activity;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class SMSActivity extends BasePermissionAppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private cmpApi cmpApi;
    Button btn_7_9,btn_9_11,btn_11_13,btn_13_15,btn_15_17,btn_17_19;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        TextView tvTitle=(TextView)findViewById(R.id.tvTitle);
        tvTitle.setText(PreferenceManager.getName());
        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
        arrowImage.setOnClickListener(view -> onBackPressed());
        TextView version_number=(TextView)findViewById(R.id.logout);
        version_number.setText("v. "+PreferenceManager.versionName);
        ImageView goHome=(ImageView)findViewById(R.id.home);
        goHome.setOnClickListener(v -> startActivity(new Intent(SMSActivity.this,MainActivity.class)));

        cmpApi=new cmpApi(this);
        btn_7_9=(Button)findViewById(R.id.btn_7_9);
        btn_9_11=(Button)findViewById(R.id.btn_9_11);
        btn_11_13=(Button)findViewById(R.id.btn_11_13);
        btn_13_15=(Button)findViewById(R.id.btn_13_15);
        btn_15_17=(Button)findViewById(R.id.btn_15_17);
        btn_17_19=(Button)findViewById(R.id.btn_17_19);
        Intent intent=getIntent();
        final String barcode=intent.getStringExtra("barcode");
        btn_7_9.setOnClickListener(view -> {
            sendSMS_server(barcode,7,9);

//                getReadSMSPermission(new RequestPermissionAction() {
//                    @Override
//                    public void permissionDenied() {
//                        // Call Back, when permission is Denied
//                    }
//
//                    @Override
//                    public void permissionGranted() {
//                        // Call Back, when permission is Granted
//                        sendSMS("+8618640211091","Your parcel arrived here!");
//                    }
//                });
        });
        btn_9_11.setOnClickListener(view -> sendSMS_server(barcode,9,11));
        btn_11_13.setOnClickListener(view -> sendSMS_server(barcode,11,13));
        btn_13_15.setOnClickListener(view -> sendSMS_server(barcode,13,15));
        btn_15_17.setOnClickListener(view -> sendSMS_server(barcode,15,17));
        btn_17_19.setOnClickListener(view -> sendSMS_server(barcode,17,19));
    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    private void sendSMS_server(String barcode,int from_time,int to_time){
        String Tag_req="req_remove_order";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.sendSMS_url+
                barcode+Constant.separator+
                from_time +Constant.separator+
                to_time +Constant.sp;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("resetOrder_response",url+" "+response.toString());
                    cmpApi.kpHUD.dismiss();
                    if (cmpApi.parseCheckBarcode(response)) Toast.makeText(SMSActivity.this, R.string.success_sms_alert,Toast.LENGTH_LONG).show();
                    else Toast.makeText(SMSActivity.this, R.string.fail_sms_alert,Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SMSActivity.this,ListParcelActivity.class));
                    finish();
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
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }
}
