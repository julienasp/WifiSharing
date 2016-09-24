package com.example.juasp.wifisharing;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    protected void activatingWifi(){
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "Activating WiFi...", Toast.LENGTH_LONG).show();
            mWifiManager.setWifiEnabled(true);
            Log.d("activatingWifi()", "activatingWifi: WiFi is now active.");
        }
        else{
            Log.d("activatingWifi()", "activatingWifi: WiFi was already activated.");
        }
    }

    protected List<ScanResult> getWifiScanResult(){
        //Preconditions
        activatingWifi(); //Need wifi on

        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = mWifiManager.getScanResults();

        return results;
    }
    protected void hydratingSpinnerSSID(){
        List<ScanResult> results = getWifiScanResult();

        List<String> spinnerArray =  new ArrayList<String>();

        for (ScanResult result : results) {
            spinnerArray.add(result.SSID);
            Log.d("hydratingSpinnerSSID()", "the SSID : " + result.SSID + " was added to the spinnerSSID.");
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
