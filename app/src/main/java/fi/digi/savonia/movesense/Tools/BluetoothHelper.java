package fi.digi.savonia.movesense.Tools;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.exceptions.BleScanException;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

import fi.digi.savonia.movesense.Tools.Listeners.BluetoothActionListener;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class BluetoothHelper {

    RxBleClient rxBleClient;
    Disposable scanSubscription;
    Context _context;
    BluetoothActionListener _bluetoothActionListener;

    List<String> FoundDevices = new ArrayList<String>();

    public BluetoothHelper(Context context)
    {
        _context = context;
        rxBleClient = RxBleClient.create(context);
    }

    public void SetBluetoothActionListener(BluetoothActionListener bluetoothActionListener)
    {
        _bluetoothActionListener = bluetoothActionListener;
    }

    public void CheckRequirements()
    {
        switch (rxBleClient.getState()) {

            case READY:
                _bluetoothActionListener.ReadyToScan();
                break;
            case BLUETOOTH_NOT_AVAILABLE:
                _bluetoothActionListener.BluetoothNotAvailable();
                break;
                // basically no functionality will work here
            case LOCATION_PERMISSION_NOT_GRANTED:
                _bluetoothActionListener.LocationPermissionNotGranted();
                break;
                // scanning and connecting will not work
            case BLUETOOTH_NOT_ENABLED:
                _bluetoothActionListener.BluetoothNotEnabled();
                break;
                // scanning and connecting will not work
            case LOCATION_SERVICES_NOT_ENABLED:
                _bluetoothActionListener.LocationNotEnabled();
                break;
                // scanning will not work
            default:
                _bluetoothActionListener.Error("Unknown error");
                break;
        }
    }

    public boolean IsScanning()
    {
        if(scanSubscription != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void dispose() {
        scanSubscription.dispose();
        Log.i("Scan","Scan stop");
    }


    private void onScanResult(ScanResult scanResult)
    {
        String name = scanResult.getBleDevice().getName();
        if(name!=null && name.startsWith("Movesense"))
        {
            boolean found = false;
            if(FoundDevices.size()!=0)
            {
                if(!FoundDevices.contains(name))
                {
                    FoundDevices.add(name);
                    found = true;
                }
            }
            else
            {
                FoundDevices.add(name);
                found=true;
            }

            if(found)
            {
                Log.i("Movesense found",name);
                _bluetoothActionListener.BleDeviceFound(scanResult.getBleDevice());
            }

        }
    }


    private void onScanFailure(Throwable throwable) {
        Log.i("Scan","Scan failure : " + throwable.getLocalizedMessage());
        _bluetoothActionListener.Error(throwable.getMessage());
    }

    public void Scan(long timeout)
    {
        FoundDevices.clear();
        ScanSettings scanSettings = new ScanSettings.Builder().build();
        ScanFilter scanFilter = new ScanFilter.Builder().build();


        scanSubscription = rxBleClient.scanBleDevices(scanSettings,scanFilter)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(this::dispose)
                .subscribe(this::onScanResult,this::onScanFailure);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanSubscription.dispose();
            }
        },timeout);
    }

    public void StopScan()
    {
        if(IsScanning())
        {
            dispose();
        }
    }


}
