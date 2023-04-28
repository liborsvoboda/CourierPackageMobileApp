package zhe.it_tech613.com.cmpcourier.activity;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.ParcelAdapter;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class ListParcelActivity extends AppCompatActivity {

    ListView parcel_listview;
    ParcelAdapter parcelAdapter;
    cmpApi cmpApi;
    Button button_delivery, button_planing;
    RealmResults<ParcelModel> parcelModels;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_parcel);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(PreferenceManager.getName());
        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
        arrowImage.setOnClickListener(view -> onBackPressed());
        TextView version_number = (TextView) findViewById(R.id.logout);
        version_number.setText("v. " + PreferenceManager.versionName);
        ImageView goHome = (ImageView) findViewById(R.id.home);
        goHome.setOnClickListener(v -> startActivity(new Intent(ListParcelActivity.this, MainActivity.class)));

        cmpApi = new cmpApi(ListParcelActivity.this);
        parcel_listview = findViewById(R.id.parcel_listview);
        TextView id = findViewById(R.id.id);
        TextView parcel_no = findViewById(R.id.parcel_no);
        TextView name = findViewById(R.id.client);
        TextView city = findViewById(R.id.city);
        id.setText(Constant.czlanguageStrings.getID());
        //Temporary
        id.setVisibility(View.GONE);

        parcel_no.setText(Constant.czlanguageStrings.getPARCEL_NO() + "\n" + Constant.czlanguageStrings.getNOTE());
        name.setText(Constant.czlanguageStrings.getNAME());
        city.setText(Constant.czlanguageStrings.getCITY());
        ((TextView) findViewById(R.id.cod)).setText(Constant.czlanguageStrings.getCOD() + "\n" + Constant.czlanguageStrings.getTYPE() +
                "\n" + "Objednavka");
        id.setOnClickListener(v -> {
            Log.e("id", "clicked");
//                Collections.sort(parcelModels, (o1, o2)-> o1.getOrder().compareTo(o2.getOrder()));
            parcelModels = parcelModels.sort("order", Sort.ASCENDING);
            setList();
        });
        parcel_no.setOnClickListener(v -> {
//                Collections.sort(parcelModels, (o1, o2)-> o1.getBarcode().compareTo(o2.getBarcode()));
            parcelModels = parcelModels.sort("barcode", Sort.ASCENDING);
            setList();
        });
        name.setOnClickListener(v -> {
//                Collections.sort(parcelModels, (o1, o2)-> o1.getClient().compareTo(o2.getClient()));
            parcelModels = parcelModels.sort("client", Sort.ASCENDING);
            setList();
        });
        city.setOnClickListener(v -> {
//                Collections.sort(parcelModels, (o1, o2)-> o1.getCity().compareTo(o2.getCity()));
            parcelModels = parcelModels.sort("city", Sort.ASCENDING);
            setList();
        });
        button_delivery = (Button) findViewById(R.id.btn_delivery);
        button_planing = (Button) findViewById(R.id.btn_planing);
        button_delivery.setText(Constant.czlanguageStrings.getDELIVERY());
        button_planing.setText(Constant.czlanguageStrings.getPLANING());
        button_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrders(1);
            }
        });
        button_planing.setOnClickListener(v -> getOrders(2));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getOrders(1);
    }

    private void getOrders(int type) {
        String Tag_req = "req_remove_order";
        try {
            cmpApi.kpHUD.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = PreferenceManager.getOrders_url +
                PreferenceManager.getID() + "&type=" + type;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("getOrders_url_response", url + " " + response.toString());
//                        Toast.makeText(ListParcelActivity.this, response.toString(),Toast.LENGTH_LONG).show();
                    try {
                        cmpApi.kpHUD.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    parcelModels = cmpApi.parseGetOrders(response, type);
                    if (parcelModels != null) {
                        setList();
                    } else
                        Toast.makeText(ListParcelActivity.this, Constant.czlanguageStrings.getFAIL_GET_INITIAL_DATA(), Toast.LENGTH_LONG).show();
                }, error -> {
                    if ((cmpApi.kpHUD != null) && cmpApi.kpHUD.isShowing())
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
                error.printStackTrace();
            }
        });
        // add the request object to the queue to be executed
        PreferenceManager.getInstance().addToRequestQueue(jsonObjectRequest, Tag_req);
    }

    private void setList() {
        parcelAdapter = new ParcelAdapter(parcelModels);
        parcel_listview.setAdapter(parcelAdapter);
        parcel_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParcelModel counter;
                counter = parcelModels.get(i);
                Log.e("selected_id", String.valueOf(i));
                assert counter != null;
                Intent intent = new Intent(ListParcelActivity.this, AfterSelectionActivity.class);
                intent.putExtra("telephone", counter.getTelephone());
                intent.putExtra("latitude", counter.getLatitude());
                intent.putExtra("longitude", counter.getLongitude());
                intent.putExtra("client", counter.getClient());
                intent.putExtra("order", counter.getOrder());
                intent.putExtra("barcode", counter.getBarcode());
                startActivity(intent);
            }
        });
    }
}
