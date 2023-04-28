package zhe.it_tech613.com.cmpcourier.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;


public class SelectPaymentDlg extends Dialog {

    @SuppressLint("SetTextI18n")
    public SelectPaymentDlg(@NonNull Context context, float pay_type, final DialogNumberListener listener) {
        super(context);
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_payment);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btn_card = (Button) findViewById(R.id.btn_card);
        Button btn_cash = (Button) findViewById(R.id.btn_cash);
        TextView body = (TextView) findViewById(R.id.content);
        body.setText(Constant.czlanguageStrings.getCASH_ON_DELIVERY()+" : " +pay_type+"\n "+Constant.czlanguageStrings.getPAYMENT_BY());
        btn_card.setText(Constant.czlanguageStrings.getCARD());
        btn_cash.setText(Constant.czlanguageStrings.getCASH());
        btn_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnCardClick(SelectPaymentDlg.this);
            }
        });
        btn_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnCashClick(SelectPaymentDlg.this);
            }
        });
    }
    public interface DialogNumberListener {
        public void OnCardClick(Dialog dialog);
        public void OnCashClick(Dialog dialog);
    }
}