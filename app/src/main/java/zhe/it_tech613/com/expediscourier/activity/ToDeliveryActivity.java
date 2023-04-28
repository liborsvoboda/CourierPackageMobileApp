package zhe.it_tech613.com.cmpcourier.activity;

import android.content.Intent;
import android.media.MediaPlayer;
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

import java.util.List;

import io.realm.RealmResults;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class ToDeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    private cmpApi cmpApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_delivery);
        cmpApi=new cmpApi(this);
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
                startActivity(new Intent(ToDeliveryActivity.this,MainActivity.class));
            }
        });

        Button btn_list_parcel = (Button) findViewById(R.id.btn_list_parcel);
        Button btn_read = (Button) findViewById(R.id.btn_read);

        btn_list_parcel.setText(Constant.czlanguageStrings.getLIST_OF_PARCELS());
        btn_read.setText(Constant.czlanguageStrings.getREAD());
        btn_list_parcel.setOnClickListener(this);
        btn_read.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String barcode = data.getStringExtra("barcode");
                checkFromServer(barcode);
            }
        }
    }

    private void checkFromServer(String barcode){
        String Tag_req="req_remove_order";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.getOrders_url+
                PreferenceManager.getID()+"&type=1";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("resetOrder_response",url+" "+response.toString());
                    cmpApi.kpHUD.dismiss();
                    RealmResults<ParcelModel> parcelModels=cmpApi.parseGetOrders(response,1);
                    if (parcelModels!=null) {
                        ParcelModel parcelModel = parcelModels.getRealm().where(ParcelModel.class).equalTo("barcode",barcode).findFirst();
                        if (parcelModel!=null){
                            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.success);
                            mp.start();
                            Intent intent=new Intent(ToDeliveryActivity.this,ChangeStatusActivity.class);
                            intent.putExtra("barcode",barcode);
                            intent.putExtra("client",parcelModel.getClient());
                            startActivity(intent);
                        }else {
                            MediaPlayer mp = MediaPlayer.create(ToDeliveryActivity.this, R.raw.error);
                            mp.start();
                        }
                    } else Toast.makeText(ToDeliveryActivity.this, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(),Toast.LENGTH_LONG).show();
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
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest,Tag_req);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_list_parcel:
                startActivity(new Intent(ToDeliveryActivity.this,ListParcelActivity.class));
//                finish();
                break;
            case R.id.btn_read:
                Intent intent = new Intent(ToDeliveryActivity.this,ScannerActivity.class);
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
                break;
        }
    }
}
