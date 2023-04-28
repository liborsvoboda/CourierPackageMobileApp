package zhe.it_tech613.com.cmpcourier.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;


public class ConfirmDlg extends Dialog {

    @SuppressLint("SetTextI18n")
    public ConfirmDlg(@NonNull Context context,String str_header, String pay_type, final DialogNumberListener listener) {
        super(context);
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_payment);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView header = findViewById(R.id.header);
        header.setText(str_header);
        Button btn_yes = (Button) findViewById(R.id.btn_card);
        Button btn_no = (Button) findViewById(R.id.btn_cash);
        TextView body = (TextView) findViewById(R.id.content);
        body.setText(pay_type);
        btn_yes.setText(Constant.czlanguageStrings.getYES());
        btn_no.setText(Constant.czlanguageStrings.getNO());
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnYesClick(ConfirmDlg.this);
            }
        });
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnNoClick(ConfirmDlg.this);
            }
        });
    }
    public interface DialogNumberListener {
        public void OnYesClick(Dialog dialog);
        public void OnNoClick(Dialog dialog);
    }
}