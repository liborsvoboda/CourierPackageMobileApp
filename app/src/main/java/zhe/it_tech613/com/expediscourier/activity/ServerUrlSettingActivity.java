package zhe.it_tech613.com.cmpcourier.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import zhe.it_tech613.com.cmpcourier.R;
import zhe.it_tech613.com.cmpcourier.utils.PreferenceManager;

public class ServerUrlSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_url_setting);
        EditText editText = findViewById(R.id.editText);
        findViewById(R.id.button8).setOnClickListener(v -> {
            if (TextUtils.isEmpty(editText.getText().toString())) {
                editText.setError("Invalid ServerUrl");
                return;
            }
            String editTextStr = editText.getText().toString().trim();
            if (!editTextStr.endsWith("/")) editTextStr = editTextStr+"/";
            if (editTextStr.startsWith(getString(R.string.http))) PreferenceManager.base_url = editTextStr;
            else PreferenceManager.base_url = getString(R.string.http) + editTextStr;
            PreferenceManager.setServerUrl(PreferenceManager.base_url);
            setResult(RESULT_OK);
            finish();
        });
        findViewById(R.id.btn_later).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
