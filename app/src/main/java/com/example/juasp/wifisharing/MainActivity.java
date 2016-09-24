package com.example.juasp.wifisharing;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.text.InputType;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MA-";
    private CheckBox checkBoxPassword = null;
    private EditText editTextPassword = null;
    private Spinner spinnerSSID = null;

    protected void activatingWifi(){
        Log.d(TAG+"activatingWifi", "executing activatingWifi()...");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            Log.d(TAG+"activatingWifi()", "waiting on callback");
        }

        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "Activating WiFi...", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
            Log.d(TAG+"activatingWifi", "activatingWifi: WiFi is now active.");
        }
        else{
            Log.d(TAG+"activatingWifi", "activatingWifi: WiFi was already activated.");
        }
    }

    protected List<ScanResult> getWifiScanResult(){
        Log.d(TAG+"getWifiScanResult", "executing getWifiScanResult()...");
        //Preconditions
        activatingWifi(); //Activating Wifi + Permissions

        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> results = mWifiManager.getScanResults();

        Log.d(TAG+"getWifiScanResult", results.toString());
        return results;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG+"PermissionsResult:", "executing onRequestPermissionsResult() ...");
        Log.d(TAG+"PermissionsResult:", "permissions[] = " + permissions.toString());
        Log.d(TAG+"PermissionsResult:", "grantResults[] = " + grantResults.toString());
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hydratingSpinnerSSID();
            }
            else activatingWifi();
        }
    }

    protected void hydratingSpinnerSSID(){
        Log.d(TAG+"hydratingSpinnerSSID", "executing hydratingSpinnerSSID()...");
        List<ScanResult> results = getWifiScanResult();

        List<String> spinnerArray =  new ArrayList<String>();
        Log.d(TAG+"hydratingSpinnerSSID", results.toString());
        spinnerArray.add("");
        for (ScanResult result : results) {
            spinnerArray.add(result.SSID);
            Log.d(TAG+"hydratingSpinnerSSID", "the SSID : " + result.SSID + " was added to the spinnerSSID.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mSpinnerSSID = (Spinner) findViewById(R.id.spinner_ssid);
        mSpinnerSSID.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkBoxPassword = (CheckBox) findViewById(R.id.checkBoxPassword);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        spinnerSSID = (Spinner) findViewById(R.id.spinner_ssid);

        checkBoxPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    editTextPassword.setInputType(129);
                }
            }
        });

        spinnerSSID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String ssidName = (String) spinnerSSID.getSelectedItem();
                Log.d(TAG + "setOnItemSelected", "ssid selectionné est: " + ssidName);
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        hydratingSpinnerSSID();

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter != null && nfcAdapter.isEnabled()){
            Toast.makeText(this,"NFC available!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"NFC not available!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
