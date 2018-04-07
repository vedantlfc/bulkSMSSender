package com.vedant.brainded.excel2sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.nbsp.materialfilepicker.utils.FileTypeUtils;
import com.opencsv.CSVReader;



import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2 ;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 3 ;
    public static final int PERMISSIONS_REQUEST_CODE = 0;
    public static final int FILE_PICKER_REQUEST_CODE = 1;

    Button pickButton;
    Button sendSmSButton;
    //EditText phNum;
    EditText smsText;
    //String phoneNo;
    String message;
    List<String> numbers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pickButton = (Button) findViewById(R.id.fab_add);
        sendSmSButton = (Button) findViewById(R.id.btn_send);
        //phNum = (EditText) findViewById(R.id.phNum);
        smsText = (EditText) findViewById(R.id.message_box);

        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndOpenFilePicker();
            }
        });

        sendSmSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhoneState();

            }
        });

    }

    protected void getPhoneState() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                Log.d("Cy", "ty");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } else{
            getSMSPermissions();
        }
    }

    private void sendSMS(String phoneNo) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        //phoneNo = phNum.getText().toString();
        message = smsText.getText().toString();

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, sentPI, deliveredPI);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faileed, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            e.getCause();
        }


    }

    protected void getSMSPermissions() {


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else{

            if(!numbers.isEmpty()){
                for (String number: numbers) {
                    sendSMS(number);
                    Log.d("number: ", number);
                }
            } else {
                Toast.makeText(this, "Contacts file not uploaded", Toast.LENGTH_LONG).show();
            }


        }
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showError();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            openFilePicker();
        }
    }

    private void showError() {
        Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {

                    showError();
                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!numbers.isEmpty()){
                        for (String number: numbers) {
                            sendSMS(number);
                            Log.d("number: ", number);
                        }
                    } else {
                        Toast.makeText(this, "Contacts file not uploaded", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Log.d("Path: ", "dsd");
                    showError();
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();

                }
            }
            break;
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSMSPermissions();

                } else {
                    Log.d("Path: ", "dsd");
                    showError();
                    Toast.makeText(getApplicationContext(),
                            "Phone State Permission Not Granted.", Toast.LENGTH_LONG).show();

                }
            }
        }


    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withHiddenFiles(true)
                .withTitle("Select a CSV File")
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            if (path != null) {
                Log.d("Path: ", path);
                //Toast.makeText(this, "Picked file: " + path, Toast.LENGTH_LONG).show();
                readCSV(path);
            }
        }
    }


    protected void readCSV(String patho){
        CSVReader reader;
        try
        {
            String extension = patho.substring(patho.length() - 3);
            Log.d("Path: ", extension);
            if (!extension.equals("csv")){
                Toast.makeText(getApplicationContext(), "Doesn't support "+extension+" use csv", Toast.LENGTH_SHORT).show();
                Log.d("Path: ", "use csv");
                throw new FileNotFoundException(extension);

            }
            reader = new CSVReader(new FileReader(patho));
            String[] row;
            numbers.clear();
            while ((row = reader.readNext()) != null)
            {
                for (int i = 0; i < row.length; i++)
                {
                    // display CSV values
                    System.out.println("Cell column index: " + i);
                    System.out.println("Cell Value: " + row[i]);
                    Log.d("Path: ", row[i]);
                    if(android.text.TextUtils.isDigitsOnly(row[i]) && row[i].length()==10){
                        numbers.add(row[i]);
                    }

                    System.out.println("-------------");
                }
            }
            Toast.makeText(getApplicationContext(), numbers.size()+" contacts found", Toast.LENGTH_SHORT).show();
            //pickButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_black_24dp));
            for (String number: numbers) {
                Log.d("number: ", number);
            }
        }

        catch (FileNotFoundException e)
        {
            System.err.println(e.getMessage());
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }




}