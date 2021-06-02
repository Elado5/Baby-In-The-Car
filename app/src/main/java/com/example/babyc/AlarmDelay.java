package com.example.babyc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmDelay extends AppCompatActivity {

    //Shared prefs -> taking context from main activty static context so we can use it
    Context applicationContext =  MainActivity.getContextOfApplication();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    SharedPreferences.Editor editor = prefs.edit();

    Button applyBtn;
    Button cancelBtn;
    TextView curDelay;
    EditText ET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_delay);

        applyBtn = (Button) findViewById(R.id.applyBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        ET = (EditText) findViewById(R.id.userInput);
        curDelay = (TextView) findViewById(R.id.curDelay);

        curDelay.setText("Current alarm delay: " + prefs.getInt("alarmDelay", 0));

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String output = ET.getText().toString();
                if(isNumeric(output) && (Integer.parseInt(output) >= 90 && Integer.parseInt(output) <=300) ){
                    editor.putInt("alarmDelay", Integer.parseInt(output));
                    editor.apply();
                    Toast.makeText(getApplicationContext(),"Changed successfully :)", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Chose between 90-300 seconds, and only numbers.", Toast.LENGTH_LONG).show();
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
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}