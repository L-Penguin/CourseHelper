package com.tiancai.app.coursehelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.tiancai.app.lib.*;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.tiancai.app.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void sendMessage(View view) {
        Intent intent = new Intent(this, GraphActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        Log.d("mainactivity", "sendMessage: " + editText.getText().toString());
        intent.putExtra(EXTRA_MESSAGE, editText.getText().toString());
        startActivity(intent);
    }
}
