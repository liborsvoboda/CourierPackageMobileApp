package zhe.it_tech613.com.cmpcourier.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.dialog.ConfirmDlg;
import zhe.it_tech613.com.cmpcourier.utils.Constant;
import zhe.it_tech613.com.cmpcourier.utils.cmpApi;

public class DeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    private String barcode;
    private cmpApi cmpApi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        cmpApi = new cmpApi(this);
        barcode = getIntent().getStringExtra("barcode");
        Button btn_delivered = findViewById(R.id.button6);
        Button btn_cancelled = findViewById(R.id.button7);
        btn_delivered.setText(Constant.czlanguageStrings.getDELIVERED());
        btn_delivered.setOnClickListener(this);
        btn_cancelled.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button6:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.button7:
                ConfirmDlg confirmDlg = new ConfirmDlg(this, "Confirm", "Are you sure?", new ConfirmDlg.DialogNumberListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        setResult(RESULT_CANCELED);
                        finish();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        dialog.dismiss();
                    }
                });
                confirmDlg.show();
                break;
        }
    }
}
