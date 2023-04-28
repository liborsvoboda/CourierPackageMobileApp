package zhe.it_tech613.com.cmpcourier.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import zhe.it_tech613.com.cmpcourier.R;


public class PinDlg extends Dialog implements View.OnClickListener{
    private DlgPinListener listener;
    private EditText txt_pin;

    public PinDlg(@NonNull Context context, final DlgPinListener listener) {
        super(context);
        this.listener = listener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pin);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btn_ok = (Button) findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        Button btn_send_pass = findViewById(R.id.btn_send_pass);
        txt_pin = (EditText)findViewById(R.id.txt_pin);
        txt_pin.requestFocus();
        TextView tv1 = findViewById(R.id.tv1);
        tv1.setText("Zadej PIN");
        btn_send_pass.setText("Zjisti PIN");
        btn_ok.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_send_pass.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok:
                String pin_code = txt_pin.getText().toString();
                listener.OnYesClick(PinDlg.this, pin_code);
                break;
            case R.id.btn_cancel:
                listener.OnCancelClick(PinDlg.this);
                break;
            case R.id.btn_send_pass:
                listener.OnSendPassClick(PinDlg.this);
                break;
        }
    }

    public interface DlgPinListener {
        void OnYesClick(Dialog dialog, String pin_code);
        void OnCancelClick(Dialog dialog);
        void OnSendPassClick(Dialog dialog);
    }
}
