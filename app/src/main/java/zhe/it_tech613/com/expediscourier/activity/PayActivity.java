package zhe.it_tech613.com.cmpcourier.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

import static zhe.it_tech613.com.cmpcourier.activity.ChangeStatusActivity.REQUEST_CODE_DELIVERY;

public class PayActivity extends AppCompatActivity implements View.OnClickListener {

    private String barcode;
    private cmpApi cmpApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        cmpApi = new cmpApi(this);
        barcode = getIntent().getStringExtra("barcode");
        ParcelModel parcelModel= PreferenceManager.realm.where(ParcelModel.class)
                .equalTo("barcode",barcode)
                .findFirst();
        ((TextView)findViewById(R.id.textView3)).setText(parcelModel.getCod());
        Button btn_card = findViewById(R.id.button4);
        Button btn_cash = findViewById(R.id.button5);
        btn_card.setText(Constant.czlanguageStrings.getCARD());
        btn_cash.setText(Constant.czlanguageStrings.getCASH());
        btn_card.setOnClickListener(this);
        btn_cash.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setResult(resultCode);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button4:
                setPaymentMode(0);
                break;
            case R.id.button5:
                setPaymentMode(1);
                break;
        }
    }

    private void setPaymentMode(int mode){
        String Tag_req="setPaymentMode";
        String url=String.format(PreferenceManager.setPaymentMode,barcode,mode);
        Log.e("url",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    cmpApi.kpHUD.dismiss();
                    try {
                        String value=response.getString("value");
                        if (value.equals("0")){
                            Log.e(Tag_req,"success to set payment mode");
                        }else if (value.equals("-1")){
                            Log.e(Tag_req,"failed to set payment mode");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PayActivity.this,Constant.czlanguageStrings.getUPLOAD_FAILED(),Toast.LENGTH_LONG).show();
                    }
                    Intent intent=new Intent(this,DeliveryActivity.class);
                    intent.putExtra("barcode",barcode);
                    startActivityForResult(intent,REQUEST_CODE_DELIVERY);
                }, error -> {
                    cmpApi.kpHUD.dismiss();
                });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }
}
