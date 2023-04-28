package zhe.it_tech613.com.cmpcourier.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScanner;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.model.ParcelModel;
import zhe.it_tech613.com.cmpcourier.utils.BarcodeListAdapter;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class ParcelimportActivity extends AppCompatActivity {

    Button newBarcode,addparcel,refuseparcel;
    ListView list_barcode;
    TextView counter;
    EditText tv_barcode;
    BarcodeListAdapter barcodeAdapter;
    long selected_id;
    cmpApi cmpApi;
    ParcelModel selected_parcel=null;
    private boolean from_listview=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacelimport);
        cmpApi=new cmpApi(ParcelimportActivity.this);
        getInitialData();
        initWidget();
    }

    private void initWidget(){
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
                onBackPressed();
            }
        });

        newBarcode=(Button)findViewById(R.id.newBarcode);
        newBarcode.setText(Constant.czlanguageStrings.getNEW_BARCODE());
        addparcel=(Button)findViewById(R.id.add);
        addparcel.setText(Constant.czlanguageStrings.getADD());
        refuseparcel=(Button)findViewById(R.id.not);
        refuseparcel.setText(Constant.czlanguageStrings.getNOT());
        list_barcode=(ListView)findViewById(R.id.list_barcode);
        counter=(TextView)findViewById(R.id.counter);
        tv_barcode =(EditText) findViewById(R.id.barcode);
        addparcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addButtonEvent(false);
            }
        });
        refuseparcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refuseButtonEvent();
            }
        });
        newBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startScan();
                scanButtonEvent();
            }
        });

        tv_barcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkfromserver(tv_barcode.getText().toString(),1, false);
                }
            }
        });
        tv_barcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Do whatever you want here
                    checkfromserver(tv_barcode.getText().toString(),1, false);
                    return true;
                }
                return false;
            }
        });
    }

    private void scanButtonEvent() {
        Intent intent = new Intent(ParcelimportActivity.this,ScannerActivity.class);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
//        IntentIntegrator integrator = new IntentIntegrator(yourActivity);
//        integrator.initiateScan();
    }

    private void refuseButtonEvent() {
        if (barcodeAdapter.getCount()==0) return;
        if (!selected_parcel.isValid()) return;
        if (selected_parcel.getCount_box()==0 || from_listview){
            if (tv_barcode.getText().toString().trim().equals("")) {
                Toast.makeText(ParcelimportActivity.this,
                        Constant.czlanguageStrings.getBARCODE_UNSELECT_ALLERT(),
                        Toast.LENGTH_LONG).
                        show();
                return;
            }
//            changeStatus(2,false);
            checkfromserver(tv_barcode.getText().toString().trim(),2,false);
        }else Toast.makeText(ParcelimportActivity.this,
                Constant.czlanguageStrings.getUNABLE_CHANGE_ALERT(),
                Toast.LENGTH_LONG).show();
    }

    private void addButtonEvent(boolean open_scanner) {
        if (barcodeAdapter.getCount()==0) return;
        if (selected_parcel==null) return;
        if (selected_parcel.getCount_box()==0 || from_listview){
            if (tv_barcode.getText().toString().trim().equals("")) {
                Toast.makeText(ParcelimportActivity.this,
                        Constant.czlanguageStrings.getBARCODE_UNSELECT_ALLERT(),
                        Toast.LENGTH_LONG).
                        show();
                return;
            }
//            changeStatus(1,open_scanner);
            checkfromserver(tv_barcode.getText().toString().trim(),1,false);
        }else Toast.makeText(ParcelimportActivity.this,
                Constant.czlanguageStrings.getUNABLE_CHANGE_ALERT(),
                Toast.LENGTH_LONG).show();
    }

    private void checkfromserver(final String barcode, int type, boolean from_scanner){
        String Tag_req="req_scanner";
        cmpApi.kpHUD.show();
        int fromScanner=from_scanner?1:0;
        final int[] count = new int[1];
        String url= PreferenceManager.checkbarcode_url+barcode+Constant.separator+PreferenceManager.getID()+Constant.separator+fromScanner+Constant.separator+type+Constant.sp;
        Log.e(Tag_req,url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    cmpApi.kpHUD.dismiss();
                    JSONObject responseObj=null;

                    try {
                        responseObj=new JSONObject(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.e(Tag_req,response);
                    if (responseObj!=null){
                        updateListView(cmpApi.parseInitialData(responseObj));
                        if (cmpApi.parseCheckBarcode(responseObj)) {
                            try {
                                count[0] =responseObj.getInt("count");
                                if (count[0] ==1){
                                    MediaPlayer mp = MediaPlayer.create(ParcelimportActivity.this, R.raw.success);
                                    mp.start();
                                    tv_barcode.setText("");
                                    addparcel.setEnabled(true);
                                    addparcel.setBackgroundResource(R.drawable.orangebuttonselector);
                                    refuseparcel.setEnabled(true);
                                    refuseparcel.setBackgroundResource(R.drawable.orangebuttonselector);
//                                        if (!from_scanner){
//                                            addparcel.setEnabled(true);
//                                            addparcel.setBackgroundResource(R.drawable.orangebuttonselector);
//                                            refuseparcel.setEnabled(true);
//                                            refuseparcel.setBackgroundResource(R.drawable.orangebuttonselector);
//                                        }else {
//                                            addparcel.setEnabled(false);
//                                            addparcel.setBackgroundResource(R.drawable.grey_background);
//                                            refuseparcel.setEnabled(false);
//                                            refuseparcel.setBackgroundResource(R.drawable.grey_background);
//                                        }

                                }else if (count[0] ==0){
                                    AlertDialog alert = new AlertDialog.Builder(ParcelimportActivity.this).create();
                                    alert.setTitle(getString(R.string.app_name));
                                    alert.setMessage(Constant.czlanguageStrings.getNOT_FOR_YOU());
                                    alert.setButton("OK", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int which) {
                                            alert.dismiss();
                                            if(from_scanner) scanButtonEvent();
                                        }
                                    });
                                    alert.show();
//                                        Toast.makeText(ParcelimportActivity.this, Constant.czlanguageStrings.getNOT_FOR_YOU(),Toast.LENGTH_LONG).show();
                                    MediaPlayer mp = MediaPlayer.create(ParcelimportActivity.this, R.raw.error);
                                    mp.start();
                                    addparcel.setEnabled(true);
                                    addparcel.setBackgroundResource(R.drawable.orangebuttonselector);
                                    refuseparcel.setEnabled(true);
                                    refuseparcel.setBackgroundResource(R.drawable.orangebuttonselector);
                                    tv_barcode.setText("");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else {
                            Toast.makeText(ParcelimportActivity.this, Constant.czlanguageStrings.getNO_BARCODE_ALERT(),Toast.LENGTH_LONG).show();
                            MediaPlayer mp = MediaPlayer.create(ParcelimportActivity.this, R.raw.error);
                            mp.start();
                            addparcel.setEnabled(false);
                            addparcel.setBackgroundResource(R.drawable.grey_background);
                            refuseparcel.setEnabled(false);
                            refuseparcel.setBackgroundResource(R.drawable.grey_background);
                            tv_barcode.setText("");
//                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),uri);
//                                ringtone.play();
                        }
                    }
                    if(from_scanner && count[0]!=0) scanButtonEvent();
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cmpApi.kpHUD.dismiss();
            }
        });

        stringRequest.setRetryPolicy(new RetryPolicy() {
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
        PreferenceManager.getInstance().addToRequestQueue(stringRequest,Tag_req);
    }

    private void getInitialData(){
        String Tag_req="req_initialdata";
        cmpApi.kpHUD.show();
        String url=PreferenceManager.getinitialdata_url+PreferenceManager.getID();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("getinitialdata_response",response.toString());
                        cmpApi.kpHUD.dismiss();
                        updateListView(cmpApi.parseInitialData(response));
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cmpApi.kpHUD.dismiss();
                    }
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

    @SuppressLint("SetTextI18n")
    private void updateListView(RealmResults<ParcelModel> realmList){
        barcodeAdapter=new BarcodeListAdapter(realmList);
        list_barcode.setAdapter(barcodeAdapter);
        list_barcode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParcelModel counter = barcodeAdapter.getItem(i);
                Log.e("selected_id",String.valueOf(i));
                assert counter != null;
                tv_barcode.setText(counter.getBarcode());
                selected_parcel=counter;
                from_listview=true;
//                if (selected_parcel.getCount_box()>0){
//                    addparcel.setEnabled(false);
//                    addparcel.setBackgroundResource(R.drawable.grey_background);
//                    refuseparcel.setEnabled(false);
//                    refuseparcel.setBackgroundResource(R.drawable.grey_background);
//                }else {
//                    addparcel.setEnabled(true);
//                    addparcel.setBackgroundResource(R.drawable.orangebuttonselector);
//                    refuseparcel.setEnabled(true);
//                    refuseparcel.setBackgroundResource(R.drawable.orangebuttonselector);
//                }
//                selected_id=counter.getId();
            }
        });
        counter.setText(Constant.czlanguageStrings.getCOUNT_BOXES()+ Constant.x +Constant.czlanguageStrings.getFROM()+ Constant.y);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != MaterialBarcodeScanner.RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(Constant.czlanguageStrings.getNO_CAMERA_PERMISSION())
                .setPositiveButton(Constant.czlanguageStrings.getOK(), listener)
                .show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        RealmList<ParcelModel> realmList=new RealmList<ParcelModel>();
        RealmResults<ParcelModel> realmResults=PreferenceManager.realm.where(ParcelModel.class).findAll().sort("barcode", Sort.ASCENDING);
        updateListView(realmResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==0){
            if (resultCode==RESULT_OK){
                String barcode = data.getStringExtra("barcode");
                checkfromserver(barcode,1, true);
            }
        }
    }

    private void changeStatus(final int status,boolean open_scanner){
        String Tag_req="req_change_status";
        cmpApi.kpHUD.show();
        String url= PreferenceManager.changeParcelStatus_url+
                String.valueOf(status)+"&barcode="+
                tv_barcode.getText().toString()+"&ID="+
                PreferenceManager.getID();
        Log.e("changestatus_url",url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    Log.e("changestatus_response",response.toString());
                    cmpApi.kpHUD.dismiss();
                    updateListView(cmpApi.parseChangeParcelStatus(response));
                    if (open_scanner) scanButtonEvent();
                    tv_barcode.setText("");
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
