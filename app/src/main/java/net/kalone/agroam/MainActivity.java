package net.kalone.agroam;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=762;
    private WifiManager wifiManager;
    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        super.setTitle(R.string.full_app_name);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(findViewById(R.id.coordinatorlayout), "Scanning Wifi", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                scanWifi();
            }
        });
        wifiManager=getWifiManager();
        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent notificationIntent = new Intent(this,MainActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

                Notification notification =
                        new Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                                .setContentTitle(getText(R.string.notification_title))
                                .setContentText(getText(R.string.notification_message))
                                .setSmallIcon(R.drawable.icon)
                                .setContentIntent(pendingIntent)
                                .setTicker(getText(R.string.ticker_text))
                                .build();

                int ONGOING_NOTIFICATION_ID=1;
                startForeground(ONGOING_NOTIFICATION_ID, notification);*/
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    wifiManager=getWifiManager();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    public void scanWifi(){
        wifiManager.startScan();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //do something based on the intent's action
                Snackbar.make(findViewById(R.id.coordinatorlayout), "Scanned", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                reconect();
                if (receiver != null) {
                    unregisterReceiver(receiver);
                    receiver = null;
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    public void reconect(){
        WifiInfo wifiInfo=wifiManager.getConnectionInfo();
        String BSSID=wifiInfo.getBSSID();
        String output="Linked:"+BSSID+"  Rssi:"+wifiInfo.getRssi()+"\n";
        int rssi=wifiInfo.getRssi();
        for (ScanResult result:wifiManager.getScanResults()
                ) {
            if(result.SSID.equals("ENOLAK_5G")){
                output+="BSSID:"+result.BSSID+"  Level"+result.level;
                if(!result.BSSID.equals(BSSID) && result.level>rssi+15 && !BSSID.equals("00:00:00:00:00:00") && (rssi != -127))
                {
                    output+="  reLinked";
                    wifiManager.reassociate();
                }
                output+="\n";
            }

        }
        wifiInfo=wifiManager.getConnectionInfo();
        output+="Linkto:"+BSSID+"  Rssi:"+wifiInfo.getRssi()+"\n";
        TextView textView=findViewById(R.id.text);
        textView.setText(output);
    }
    public WifiManager getWifiManager(){
        WifiManager wifiManager=null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                // Permission has already been granted
                Context context = getApplicationContext();
                wifiManager=context.getSystemService(WifiManager.class);
            }

        }else{
            Snackbar.make(findViewById(android.R.id.content), "Too old to be support", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        }
        return wifiManager;
    }
}
