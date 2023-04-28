package zhe.it_tech613.com.cmpcourier.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.dialog.ConfirmDlg;
import zhe.it_tech613.com.cmpcourier.model.Status;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.DownloadEsignTask;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;
import zhe.it_tech613.com.cmpcourier.utils.UploadEsignTask;

public class EsignActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView description;
    private ProgressBar progress_bar;
    private LinearLayout lay_progress;
    private CheckBox checkbox;
    private TextView remainsFiles;
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esign);
        ImageView arrowImage = (ImageView) findViewById(R.id.imageleft);
        arrowImage.setOnClickListener(this::onClick);
        ImageView goHome=(ImageView)findViewById(R.id.home);
        goHome.setOnClickListener(this::onClick);
//        Button btn_upload = findViewById(R.id.btn_upload);
        Button btn_download = findViewById(R.id.btn_download);
        btn_download.setText(Constant.czlanguageStrings.getDOWNLOAD()+"/"+Constant.czlanguageStrings.getUPLOAD());
//        btn_upload.setText(Constant.czlanguageStrings.getUPLOAD());
        progress_bar = findViewById(R.id.progress_bar);
        description = findViewById(R.id.description);
        lay_progress = findViewById(R.id.lay_progress);
        checkbox = findViewById(R.id.checkbox);
        checkbox.setText(Constant.czlanguageStrings.getALLOW_MOBILE_DATA());

        checkFiles();

        btn_download.setOnClickListener(this);
//        btn_upload.setOnClickListener(this);
        if (getIntent().hasExtra(ChangeStatusActivity.KEY_DOWNLOAD_ZIP)){
            requestCode = getIntent().getIntExtra(ChangeStatusActivity.KEY_DOWNLOAD_ZIP,0);
            if (requestCode==ChangeStatusActivity.REQUEST_CODE_DOWNLOAD_ZIP){
//                btn_upload.setEnabled(false);
//                btn_upload.setBackgroundResource(R.drawable.grey_background);
                btn_download.performClick();
            }
        }
    }

    @Override
    public void onClick(View view) {
        description.setText("");
        progress_bar.setProgress(0);
        switch (view.getId()){
            case R.id.home:
            case R.id.imageleft:
                onBackPressed();
                break;
            case R.id.btn_download:
                //upload
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                    // Do whatever
                    try{
                        File directory = new File(PreferenceManager.uploadFolder);
                        File[] files = directory.listFiles();
                        if (files.length > 0 ) {uploadProcess();}
                    } catch (Exception e) {}

                }else {
                    Toast.makeText(this,"Please check your wifi connection",Toast.LENGTH_LONG).show();
                    if (checkbox.isChecked()){
                        ConfirmDlg confirmDlg = new ConfirmDlg(EsignActivity.this,"Confirm", Constant.czlanguageStrings.getUPLOAD_MOBILE_DATA(), new ConfirmDlg.DialogNumberListener() {
                            @Override
                            public void OnYesClick(Dialog dialog) {
                                dialog.dismiss();
                                try{
                                    File directory = new File(PreferenceManager.uploadFolder);
                                    File[] files = directory.listFiles();
                                    if (files.length > 0 ) {uploadProcess();}
                                } catch (Exception e) {}
                            }

                            @Override
                            public void OnNoClick(Dialog dialog) {
                                dialog.dismiss();
                            }
                        });
                        confirmDlg.show();
                    }
                }
//                break;
//            case R.id.btn_download:
                //download
                File downloadFolder = new File(PreferenceManager.downLoadFolder);
                File[] files = downloadFolder.listFiles();
                if (files!=null){
                    for (File file:files){
                        if (file.isDirectory()) continue;
                        Calendar lastModifyDate = Calendar.getInstance();
                        lastModifyDate.setTime(new Date(file.lastModified()));
                        Calendar now = Calendar.getInstance();
                        if (lastModifyDate.get(Calendar.DAY_OF_YEAR)<now.get(Calendar.DAY_OF_YEAR)) {
                            Log.e("EsignActivity",file.getAbsolutePath()+" deleted");
                            file.delete();
                        }else Log.e("EsignActivity",file.getAbsolutePath()+" skipped");
                    }
                }
                connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                    // Do whatever
                    downloadProcess();
                }else {
                    Toast.makeText(this,"Please check your wifi connection",Toast.LENGTH_LONG).show();
                    if (checkbox.isChecked()){
                        ConfirmDlg confirmDlg = new ConfirmDlg(EsignActivity.this,"Select Payment", Constant.czlanguageStrings.getDOWNLOAD_MOBILE_DATA(), new ConfirmDlg.DialogNumberListener() {
                            @Override
                            public void OnYesClick(Dialog dialog) {
                                dialog.dismiss();
                                downloadProcess();
                            }

                            @Override
                            public void OnNoClick(Dialog dialog) {
                                dialog.dismiss();
                            }
                        });
                        confirmDlg.show();
                    }
                }
                break;
        }
        checkFiles();
    }

    private void downloadProcess() {
        lay_progress.setVisibility(View.VISIBLE);
        DownloadEsignTask downloadEsignTask = new DownloadEsignTask();
        downloadEsignTask.onGetResult(new DownloadEsignTask.ConnectionInterface() {
            @Override
            public void downloadListener(Status result) {
                runOnUiThread(()->{
                    switch (result){
                        case Failed:
                            Toast.makeText(EsignActivity.this,Constant.czlanguageStrings.getDOWNLOAD1(),Toast.LENGTH_LONG).show();
                            lay_progress.setVisibility(View.GONE);
                            if (requestCode==ChangeStatusActivity.REQUEST_CODE_DOWNLOAD_ZIP) {
                                setResult(ChangeStatusActivity.RESULT_FAILED);
                                finish();
                            }
                            break;

                        case Success:
                            Toast.makeText(EsignActivity.this,Constant.czlanguageStrings.getDOWNLOAD2(),Toast.LENGTH_LONG).show();
                            lay_progress.setVisibility(View.GONE);
                            if (requestCode==ChangeStatusActivity.REQUEST_CODE_DOWNLOAD_ZIP) {
                                setResult(RESULT_OK);
                                finish();
                            }
                            break;

                        case NoFiles:
                            Toast.makeText(EsignActivity.this,Constant.czlanguageStrings.getNO_FILES(),Toast.LENGTH_LONG).show();
                            lay_progress.setVisibility(View.GONE);
                            if (requestCode==ChangeStatusActivity.REQUEST_CODE_DOWNLOAD_ZIP) {
                                setResult(ChangeStatusActivity.RESULT_NO_FILES);
                                finish();
                            }
                            break;

                        case ConnectedFTP:
                            description.setText(Constant.czlanguageStrings.getFTP1());
                            break;
                        case ConnectingFTP:
                            description.setText(Constant.czlanguageStrings.getFTP2());
                            break;
                        case GetFiles:
                            description.setText(Constant.czlanguageStrings.getDOWNLOAD3());
                            break;
                        case AccessDirectory:
                            description.setText(Constant.czlanguageStrings.getDOWNLOAD4());
                            break;
                    }
                });
            }

            @Override
            public void updateListener(String string, int progress) {
                runOnUiThread(()->{
                    description.setText(string);
                    progress_bar.setProgress(progress);
                    Log.e("progress",string);
                });
            }
        });
        downloadEsignTask.execute();
        checkFiles();
    }

    private void checkFiles(){
        remainsFiles = findViewById(R.id.remainsFiles);
        try{
            File checkDirectory = new File(PreferenceManager.uploadFolder);
            File[] checkFiles = checkDirectory.listFiles();
            remainsFiles.setText("Souborů k odeslání: " + checkFiles.length);
        } catch (Exception e) {}
    }

    private void uploadProcess() {
        lay_progress.setVisibility(View.VISIBLE);
        UploadEsignTask uploadEsignTask = new UploadEsignTask();
        uploadEsignTask.onGetResult(new UploadEsignTask.ConnectionInterface() {
            @Override
            public void uploadListener(Status result) {
                runOnUiThread(()->{
                    switch (result){
                        case Failed:
                            Toast.makeText(EsignActivity.this,Constant.czlanguageStrings.getUPLOAD1(),Toast.LENGTH_LONG).show();
                            lay_progress.setVisibility(View.GONE);
                            break;
                        case Success:
                            Toast.makeText(EsignActivity.this,Constant.czlanguageStrings.getUPLOAD2(),Toast.LENGTH_LONG).show();
                            lay_progress.setVisibility(View.GONE);
                            break;
                        case NoFiles:
                            Toast.makeText(EsignActivity.this,Constant.czlanguageStrings.getNO_FILES(),Toast.LENGTH_LONG).show();
                            lay_progress.setVisibility(View.GONE);
                            break;

                        case ConnectedFTP:
                            description.setText(Constant.czlanguageStrings.getFTP1());
                            break;
                        case ConnectingFTP:
                            description.setText(Constant.czlanguageStrings.getFTP2());
                            break;
                        case GetFiles:
                            description.setText(Constant.czlanguageStrings.getUPLOAD3());
                            break;
                        case AccessDirectory:
                            description.setText(Constant.czlanguageStrings.getUPLOAD4());
                            break;
                    }
                });
            }

            @Override
            public void updateListener(String string, int progress) {
                runOnUiThread(()->{
                    description.setText(string);
                    progress_bar.setProgress(progress);
                    Log.e("progress",string);
                });
            }

            @Override
            public void getBarcodes(List<String> barcodes) {
                if (barcodes.isEmpty()) return;
                sendArchivedBarcodeList(barcodes);
            }

            private void sendArchivedBarcodeList(List<String> barcodes) {
                String delim = ",";
                int i = 0;
                StringBuilder sb = new StringBuilder();
                while (i < barcodes.size() - 1) {
                    sb.append(barcodes.get(i));
                    sb.append(delim);
                    i++;
                }
                sb.append(barcodes.get(i));
                String res = sb.toString();
                String Tag_req = "getArchivedBarcodes";
                String url= String.format(PreferenceManager.getArchivedBarcodes,res, PreferenceManager.getID());
                Log.e("Barcodes", res+" "+url);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, response -> {
                            Log.e("getArchivedBarcodes",response.toString());

                        }, error -> {
                            Log.e("Error", error.toString());
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
        });
        uploadEsignTask.execute();
        checkFiles();
    }
}
