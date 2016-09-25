package com.example.juasp.wifisharing;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.text.InputType;
import android.app.PendingIntent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MA-";
    private CheckBox checkBoxPassword = null;
    private EditText editTextPassword = null;
    private EditText editTextSecurity = null;
    private Spinner spinnerSSID = null;
    private HashMap<String,String> ssidSecurityTypeMap = new HashMap<>();
    private PendingIntent mPendingIntent = null;
    private NfcAdapter nfcAdapter = null;
    private Tag mytag;
    private IntentFilter writeTagFilters[];
    private Context ctx;

    private String findSecurityType(String c){

        if (c.contains("WPA2")) {
            return "WPA2";
        }
        else if (c.contains("WPA")) {
            return "WPA";
        }
        else if (c.contains("WEP")) {
            return "WEP";
        }
        return "OPEN";
    }

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
            ssidSecurityTypeMap.put(result.SSID,findSecurityType(result.capabilities));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner mSpinnerSSID = (Spinner) findViewById(R.id.spinner_ssid);
        mSpinnerSSID.setAdapter(adapter);
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

        //create the message in according with the standard
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
        return recordNFC;
    }

    private void write(String text, Tag tag) throws IOException, FormatException {

        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ctx = this;


        checkBoxPassword = (CheckBox) findViewById(R.id.checkBoxPassword);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        spinnerSSID = (Spinner) findViewById(R.id.spinner_ssid);
        editTextSecurity = (EditText) findViewById(R.id.editTextSecurity);
        Button btnWrite = (Button) findViewById(R.id.button_write);

        btnWrite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try {
                    if(mytag==null){
                        Toast.makeText(ctx, "Error, tag not found!", Toast.LENGTH_LONG ).show();
                    }else{
                        write("test",mytag);
                        Toast.makeText(ctx, "Writing was successful", Toast.LENGTH_LONG ).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(ctx, "Error, IOException", Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(ctx, "Error, FormatException" , Toast.LENGTH_LONG ).show();
                    e.printStackTrace();
                }
            }
        });

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
                if("".equals(ssidName)){
                    editTextPassword.setText("");
                    editTextSecurity.setText("");
                }
                else{
                    editTextPassword.setText("");
                    editTextSecurity.setText(ssidSecurityTypeMap.get(ssidName));
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        hydratingSpinnerSSID();



        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(nfcAdapter != null && nfcAdapter.isEnabled()){
            Toast.makeText(this,"NFC available!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this,"NFC not available!", Toast.LENGTH_LONG).show();
        }

        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent){
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            mytag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Toast.makeText(this, "new intent: " + mytag.toString(), Toast.LENGTH_LONG ).show();
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
