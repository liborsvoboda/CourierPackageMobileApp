package zhe.it_tech613.com.cmpcourier.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import zhe.it_tech613.com.cmpcourier.utils.Constant;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private static final String TAG = "ScannerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestMultiplePermission();
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }


    private void requestMultiplePermission() {

        ActivityCompat.requestPermissions(ScannerActivity.this, new String[]
                {
                        Manifest.permission.CAMERA
                }, Constant.MUTIPLEREQUESTCODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case Constant.MUTIPLEREQUESTCODE:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraPermission) {
                        Toast.makeText(ScannerActivity.this, Constant.czlanguageStrings.getPERMISSION_GRANTED_ALERT(), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ScannerActivity.this,Constant.czlanguageStrings.getPERMISSION_DENIED_ALERT(), Toast.LENGTH_SHORT).show();

                    }
                }

                break;
        }
    }

    @Override
    public void handleResult(Result result) {
// Do something with the result here
        Log.v(TAG, result.getText()); // Prints scan results
        Log.v(TAG, result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        final String url = result.getText();
//        new AlertDialog.Builder(this)
//                .setTitle(Constant.czlanguageStrings.getRESULT())
//                .setMessage(url)
//                .setPositiveButton(Constant.czlanguageStrings.getOK(), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                        Intent data = new Intent();
//                        //---set the data to pass back---
//                        data.putExtra("barcode",url);
//                        setResult(RESULT_OK, data);
//                        //---close the activity---
//                        finish();
//                    }
//                }).show();
////        if (url.contains(C.CHECK_URL)) {
////            Uri uri = Uri.parse(url);
////            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
////            startActivity(launchBrowser);
////        }
//        // If you would like to resume scanning, call this method below:
//        mScannerView.resumeCameraPreview(this);

        Intent data = new Intent();
        //---set the data to pass back---
        data.putExtra("barcode",url);
        setResult(RESULT_OK, data);
        //---close the activity---
        finish();
    }
}
