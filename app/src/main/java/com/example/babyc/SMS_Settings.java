package com.example.babyc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SMS_Settings extends AppCompatActivity {
    //Shared prefs -> taking context from main activty static context so we can use it
    Context applicationContext =  MainActivity.getContextOfApplication();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    SharedPreferences.Editor editor = prefs.edit();

    Button applyBtn;
    Button cancelBtn;
    TextView curNumber;
    EditText ET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_settings);

        applyBtn = (Button) findViewById(R.id.applyBtnS);
        cancelBtn = (Button) findViewById(R.id.cancelBtnS);
        ET = (EditText) findViewById(R.id.userInputS);
        curNumber = (TextView) findViewById(R.id.curNumber);

        curNumber.setText("Current Number: " + prefs.getString("SMS_Number", "none"));

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output = ET.getText().toString();
                if(isNumeric(output) && (Long.parseLong(output) >= 99999999L && Long.parseLong(output) <=999999999999L) ){
                    editor.putString("SMS_Number", output);
                    editor.apply();
                    Toast.makeText(getApplicationContext(),"Changed successfully :)", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please enter a valid number", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //function to filter the user input and make sure it's a good int
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Long d = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}