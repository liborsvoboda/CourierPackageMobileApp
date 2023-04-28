package zhe.it_tech613.com.cmpcourier.activity;

import android.app.Dialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
import zhe.it_tech613.com.cmpcourier.dialog.ReasonDlg;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class SelectReasonActivity extends AppCompatActivity {

    cmpApi cmpApi;
    ReasonDlg reasonDlg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_reason);
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
                startActivity(new Intent(SelectReasonActivity.this,MainActivity.class));
            }
        });

        cmpApi=new cmpApi(this);
        Intent intent=getIntent();
        final String barcode=intent.getStringExtra("barcode");
        Button btn_1=(Button)findViewById(R.id.btn_1);
        btn_1.setText(Constant.czlanguageStrings.getREASON1());
        Button btn_2=(Button)findViewById(R.id.btn_2);
        btn_2.setText(Constant.czlanguageStrings.getREASON2());
        Button btn_3=(Button)findViewById(R.id.btn_3);
        btn_3.setText(Constant.czlanguageStrings.getREASON3());
        Button btn_4=(Button)findViewById(R.id.btn_4);
        btn_4.setText(Constant.czlanguageStrings.getREASON4());
        Button btn_5=(Button)findViewById(R.id.btn_5);
        btn_5.setText(Constant.czlanguageStrings.getREASON5());
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeOrderstatus(barcode,1,"0");
            }
        });
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeOrderstatus(barcode,2,"0");
            }
        });
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeOrderstatus(barcode,3,"0");
            }
        });
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeOrderstatus(barcode,4,"0");
            }
        });
        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reasonDlg=new ReasonDlg(SelectReasonActivity.this, new ReasonDlg.DialogNumberListener() {
                    @Override
                    public void OnYesClick(Dialog dialog, String reason) {
                        reasonDlg.dismiss();
                        changeOrderstatus(barcode,5,reason);
                    }

                    @Override
                    public void OnCancelClick(Dialog dialog) {
                        reasonDlg.dismiss();
                    }
                });
                reasonDlg.show();
            }
        });
    }

    private void changeOrderstatus(String barcode,int reason, String str_reason){
        String Tag_req="req_change_status";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.changeOrderStatus_url+
                "4&barcode="+
                barcode+"&ID="+
                PreferenceManager.getID()+"&reason="+ reason +"&time=0&reason_discription="+str_reason;
        Log.e("reason_url",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("changeOrderesponse",response.toString());
                    cmpApi.kpHUD.dismiss();
                    if (cmpApi.parseCheckBarcode(response)) Toast.makeText(SelectReasonActivity.this,Constant.czlanguageStrings.getSUCCESS_CHANGE_PARCEL_STATUS(),Toast.LENGTH_LONG).show();
                    else Toast.makeText(SelectReasonActivity.this,Constant.czlanguageStrings.getFAIL_CHANGE_PARCEL_ALERT(),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SelectReasonActivity.this,ListParcelActivity.class));
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
