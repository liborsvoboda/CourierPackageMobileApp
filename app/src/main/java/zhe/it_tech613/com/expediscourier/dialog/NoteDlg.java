package zhe.it_tech613.com.cmpcourier.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.Constant;

public class NoteDlg extends Dialog {

    public NoteDlg(@NonNull Context context,String name, final DialogNumberListener listener) {
        super(context);
        //        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_note);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Button btn_fill = (Button) findViewById(R.id.btn_fill);
        Button btn_save = (Button) findViewById(R.id.btn_save);
        btn_fill.setText(Constant.czlanguageStrings.getFILL_NAME());
        btn_save.setText(Constant.czlanguageStrings.getSAVE());
        EditText etName = findViewById(R.id.etName);
        EditText etIdCard = findViewById(R.id.etIdCard);
        etName.setText(Constant.czlanguageStrings.getNAME());
        etIdCard.setText(Constant.czlanguageStrings.getID_CARD());
        btn_fill.setOnClickListener(view -> {
            etName.setText(name);
        });
        btn_save.setOnClickListener(view -> listener.OnSaveClick(NoteDlg.this, etName.getText().toString(), etIdCard.getText().toString()));
    }
    public interface DialogNumberListener {
        void OnSaveClick(Dialog dialog, String name, String id_card);
    }
}