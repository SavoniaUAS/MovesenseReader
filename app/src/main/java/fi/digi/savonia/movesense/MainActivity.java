package fi.digi.savonia.movesense;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import fi.digi.savonia.movesense.Fragments.ConfigurationFragment;
import fi.digi.savonia.movesense.Fragments.ParametersFragment;
import fi.digi.savonia.movesense.Fragments.ScanFragment;
import fi.digi.savonia.movesense.Models.MeasurementInterval;
import fi.digi.savonia.movesense.Models.Movesense.Data.GyroscopeData;
import fi.digi.savonia.movesense.Models.Movesense.Data.TemperatureData;
import fi.digi.savonia.movesense.Models.Movesense.Info.ECGInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.GyroscopeInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.LinearAccelerationInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.MagnetometerInfo;
import fi.digi.savonia.movesense.Models.SaMi.SamiData;
import fi.digi.savonia.movesense.Models.SaMi.SamiMeasurement;
import fi.digi.savonia.movesense.Tools.BluetoothHelper;
import fi.digi.savonia.movesense.Tools.Listeners.BluetoothActionListener;
import fi.digi.savonia.movesense.Tools.Listeners.MovesenseActionListener;
import fi.digi.savonia.movesense.Tools.Listeners.SamiMeasurementsActionListener;
import fi.digi.savonia.movesense.Tools.MeasurementHelper;
import fi.digi.savonia.movesense.Tools.MovesenseHelper;
import fi.digi.savonia.movesense.Tools.SamiMeasurementHelper;
import fi.digi.savonia.movesense.Models.SaMi.SamiMeasurementPackage;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.movesense.mds.MdsException;
import com.polidea.rxandroidble2.RxBleDevice;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity implements BluetoothActionListener, MovesenseActionListener, ScanFragment.OnFragmentInteractionListener, ParametersFragment.OnFragmentInteractionListener, ConfigurationFragment.OnFragmentInteractionListener, SamiMeasurementsActionListener {

    final int MY_PERMISSIONS_REQUEST = 5;
    BluetoothHelper bluetoothHelper;
    MovesenseHelper movesenseHelper;
    SamiMeasurementHelper samiMeasurementHelper;
    MeasurementHelper measurementHelper;
    ProgressDialog progressDialog;
    final int limitDataRate = 350;
    final int REQUEST_ENABLE_BT = 876;
    boolean canScan = false;
    final String const_scan_fragment = "";
    final String const_configuration_fragment = "";
    final String const_parameter_fragment = "";
    Page currrentPage = Page.configuration;

    ArrayList<String> infoList = new ArrayList<>();

    public enum Page
    {
        scan,
        configuration,
        parameter
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        RequestIgnoreBatteryOptimization();
        CheckPermissions();

        //Test();


    }

    private void Test()
    {

    }

    protected void setFragment(Fragment fragment, Page page) {
        currrentPage = page;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment,page.name());
        fragmentTransaction.commit();
    }

    private void ConnectToMovesense(RxBleDevice device)
    {
        bluetoothHelper.StopScan();
        movesenseHelper.Connect(device.getMacAddress());
        CreateLoadingDialog(getString(R.string.loading_title),getString(R.string.connecting_message));
    }


    private void CreateLoadingDialog(String title, String message)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void CheckPermissions()
    {
        // Here, thisActivity is the current activity
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //Program();
            ShowConfiguration();

        }
    }

    private void RequestIgnoreBatteryOptimization()
    {
        String packageName = this.getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }

    private void ShowConfiguration() {
        setFragment(new ConfigurationFragment(),Page.configuration);
    }

    @Override
    public void onBackPressed() {

        if(GetFragment(Page.scan)!=null)
        {
            setFragment(new ConfigurationFragment(),Page.configuration);
        }
        else if(GetFragment(Page.configuration)!=null)
        {
            finishAndRemoveTask();
        }
        else  if(GetFragment(Page.parameter)!=null)
        {
            movesenseHelper.Disconnect();
            setFragment(new ScanFragment(),Page.scan);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST)
        {
            if(grantResults[0] ==  PackageManager.PERMISSION_GRANTED)
            {
                ShowConfiguration();
            }
            else
            {
                Toast.makeText(this,getString(R.string.notification_location_is_required),Toast.LENGTH_LONG);
                //TODO something user interaction
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                bluetoothHelper.CheckRequirements();
            }
            else
            {
                Toast.makeText(this,getString(R.string.notification_bluetooth_must_be_on),Toast.LENGTH_LONG);
                //TODO something user interaction
            }
        }
    }

    private void Program(long intervalMS, String sWritekey, String sObject, String sNote) {

        bluetoothHelper = new BluetoothHelper(this);
        bluetoothHelper.SetBluetoothActionListener(this);

        bluetoothHelper.CheckRequirements();

        movesenseHelper = new MovesenseHelper(this);
        movesenseHelper.SetMovesenseActionListener(this);

        samiMeasurementHelper = new SamiMeasurementHelper();
        samiMeasurementHelper.SetListener(this);

        measurementHelper = new MeasurementHelper(samiMeasurementHelper);
        measurementHelper.SetSendInterval(intervalMS);
        measurementHelper.SetMeasurementNote(sNote);
        measurementHelper.SetMeasurementObject(sObject);
        measurementHelper.SetMeasurementWritekey(sWritekey);

        setFragment(new ScanFragment(),Page.scan);
    }

    private Fragment GetFragment(Page page)
    {
        return getSupportFragmentManager().findFragmentByTag(page.name());
    }

    // Bluetooth Action Listener

    @Override
    public void BleDeviceFound(RxBleDevice bleDevice) {
        ScanFragment fragment = (ScanFragment) GetFragment(Page.scan);
        fragment.AddNewBleDevice(bleDevice);
    }

    @Override
    public void ReadyToScan() {
        bluetoothHelper.Scan(30000);
        canScan = true;
    }

    @Override
    public void BluetoothNotEnabled() {

        Intent intentEnableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentEnableBluetooth,REQUEST_ENABLE_BT);
    }

    @Override
    public void LocationPermissionNotGranted() {
        Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void LocationNotEnabled() {
        Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void BluetoothNotAvailable() {
        Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void Error(String explanation) {
        Toast.makeText(this, explanation, Toast.LENGTH_SHORT).show();
    }

    //ScanFragment Interaction Listener

    @Override
    public void onScanButtonPressed() {
        if(canScan)
        {
            bluetoothHelper.StopScan();
            ScanFragment scanFragment = (ScanFragment) GetFragment(Page.scan);
            scanFragment.ClearList();
            bluetoothHelper.Scan(30000);
        }
    }

    @Override
    public void onDeviceSelected(RxBleDevice bleDevice) {

        ConnectToMovesense(bleDevice);

    }

    //Movesense Action Listener

    @Override
    public void ConnectionResult(boolean success) {
        String sConnectResult;
        progressDialog.dismiss();

        if(success)
        {
            sConnectResult = "Connected to the device successfully!";
        }
        else
        {
            sConnectResult = "Failed connecting to the device!";
        }

        Toast.makeText(MainActivity.this, sConnectResult, Toast.LENGTH_SHORT).show();
        setFragment(new ParametersFragment(),Page.parameter);


    }

    private void GetMovesenseSensorInfo() {

        runOnUiThread(() -> {

            ParametersFragment parameters = (ParametersFragment) GetFragment(Page.parameter);

            MeasurementInterval measurementIntervalTemperature = new MeasurementInterval(MovesenseHelper.Sensor.Temperature,new int[]{1,5,10,15,30,1,5,10,15,30,1,2,3,4,6,12,24},-1,"{value} S",5,"{value} M",10,"{value} H");
            MeasurementInterval measurementIntervalBatteryLevel = new MeasurementInterval(MovesenseHelper.Sensor.BatteryVoltage,new int[]{1,5,10,15,30,1,2,3,4,6,12,24},-1,"{value} M", 5,"{value} H");
            MeasurementInterval measurementIntervalHR = new MeasurementInterval(MovesenseHelper.Sensor.HeartRate,new int[]{1,5,10,25,50},-1,"1/{value}");

            parameters.SetMeasurementIntervals(measurementIntervalTemperature, MovesenseHelper.Sensor.Temperature);
            parameters.SetMeasurementIntervals(measurementIntervalBatteryLevel, MovesenseHelper.Sensor.BatteryVoltage);
            parameters.SetMeasurementIntervals(measurementIntervalHR, MovesenseHelper.Sensor.HeartRate);

            //movesenseHelper.GetInfo(MovesenseHelper.Sensor.Temperature);

            //movesenseHelper.GetInfo(MovesenseHelper.Sensor.BatteryVoltage);
            movesenseHelper.GetInfo(MovesenseHelper.Sensor.LinearAcceleration);
            movesenseHelper.GetInfo(MovesenseHelper.Sensor.Gyroscope);
            movesenseHelper.GetInfo(MovesenseHelper.Sensor.Magnetometer);
            movesenseHelper.GetInfo(MovesenseHelper.Sensor.ECG);
            //movesenseHelper.GetInfo(MovesenseHelper.Sensor.HeartRate);

        });

    }

    @Override
    public void OnDisconnect(String reason) {
        Toast.makeText(this,R.string.notification_movesense_disconnect, Toast.LENGTH_SHORT).show();

        if(currrentPage == Page.parameter)
        {
            measurementHelper.Stop();
        }
    }

    @Override
    public void OnError(MdsException mdsException) {

        Toast.makeText(this, R.string.notification_movesense_error, Toast.LENGTH_SHORT).show();

        if(currrentPage == Page.parameter)
        {
            measurementHelper.Stop();
            setFragment(new ScanFragment(),Page.scan);
        }
    }

    @Override
    public void OnError(String reason) {
        Toast.makeText(this, R.string.notification_movesense_error, Toast.LENGTH_SHORT).show();

        if(currrentPage == Page.parameter)
        {
            measurementHelper.Stop();
            setFragment(new ScanFragment(),Page.scan);
        }
    }

    @Override
    public void OnDataReceived(Object Data, MovesenseHelper.Sensor sensor) {

        switch (sensor)
        {
            case Temperature:
                break;
            case BatteryVoltage:
                break;
            case LinearAcceleration:
                break;
            case Gyroscope:
                break;
            case Magnetometer:
                break;
            case ECG:
                break;
            case IMU6:
                break;
            case IMU6m:
                break;
            case IMU9:
                break;
            case HeartRate:
                break;
        }

        measurementHelper.AddMeasurement(Data,sensor);
    }

    @Override
    public void OnInfoReceived(Object Data, MovesenseHelper.Sensor sensor) {
        runOnUiThread(() -> {

            ParametersFragment parameters = (ParametersFragment) GetFragment(Page.parameter);

            switch (sensor)
            {
                case Temperature:
                    break;
                case LinearAcceleration:
                    LinearAccelerationInfo _linearAccelerationInfo = (LinearAccelerationInfo) Data;
                    parameters.SetMeasurementIntervals(new MeasurementInterval(sensor,_linearAccelerationInfo.SampleRates,limitDataRate,"1/{value} S"),sensor);
                    break;
                case Gyroscope:
                    GyroscopeInfo _gyroscopeInfo = (GyroscopeInfo) Data;
                    parameters.SetMeasurementIntervals(new MeasurementInterval(sensor,_gyroscopeInfo.SampleRates,limitDataRate,"1/{value} S"),sensor);
                    break;
                case Magnetometer:
                    MagnetometerInfo _magnetometerInfo = (MagnetometerInfo) Data;
                    parameters.SetMeasurementIntervals(new MeasurementInterval(sensor,_magnetometerInfo.SampleRates,limitDataRate,"1/{value} S"),sensor);
                    break;
                case ECG:
                    ECGInfo _ECGInfo = (ECGInfo) Data;
                    parameters.SetMeasurementIntervals(new MeasurementInterval(sensor,_ECGInfo.AvailableSampleRates,limitDataRate+50,"1/{value} S"),sensor);
                    break;
                case HeartRate:
                    break;
            }
        });




        Log.i("Info_debug received", sensor.name());
    }

    // Parameters Fragment

    @Override
    public void onStartButtonPressed(MeasurementInterval[] measurementIntervals) {

        movesenseHelper.SubscribeAll(measurementIntervals);
        measurementHelper.Start();

    }

    @Override
    public void onStopButtonPressed() {
        movesenseHelper.UnsubscribeAll();
        CreateLoadingDialog(getString(R.string.loading_title),getString(R.string.unsubscribe_message));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        measurementHelper.Stop();
                        progressDialog.dismiss();
                    }
                });
            }
        },5000);
    }

    @Override
    public void onReady() {

        GetMovesenseSensorInfo();
    }

    // SamiMeasurementListener

    @Override
    public void onError() {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.measurement_occured_error), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onSuccess() {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.measurement_success), Toast.LENGTH_SHORT).show());
    }

    // Configuration Fragment

    @Override
    public void onConfigurationConfirm(long intervalMS, String writekey, String object, String note) {
        Program(intervalMS,writekey,object,note);
    }
}
