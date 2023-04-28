package zhe.it_tech613.com.cmpcourier.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import zhe.it_tech613.com.cmpcourier.R;


public class ReasonDlg extends Dialog {

    Context context;
    DialogNumberListener listener;
    EditText reason;
    TextView length_indicator;
    Button btn_ok;
    Button btn_cancel;
    String str_reason;
    public ReasonDlg(@NonNull Context context, final DialogNumberListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_reason);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        reason = (EditText)findViewById(R.id.text_reason);
        btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_cancel= (Button)findViewById(R.id.btn_skip);
        length_indicator=(TextView)findViewById(R.id.length_indicator);
        reason.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                    length_indicator.setText("Max ( "+s.length()+"/255 )");
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                str_reason = reason.getText().toString();
                listener.OnYesClick(ReasonDlg.this, str_reason);
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.OnCancelClick(ReasonDlg.this);
            }
        });
    }
    public interface DialogNumberListener {
        public void OnYesClick(Dialog dialog, String reason);
        public void OnCancelClick(Dialog dialog);
    }
}