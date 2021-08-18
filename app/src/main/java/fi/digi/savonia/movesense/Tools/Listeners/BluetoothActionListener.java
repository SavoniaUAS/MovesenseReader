package fi.digi.savonia.movesense.Tools.Listeners;

import com.polidea.rxandroidble2.RxBleDevice;

import java.util.Date;

public interface BluetoothActionListener {
    void BleDeviceFound(RxBleDevice bleDevice);
    void ReadyToScan();
    void BluetoothNotEnabled();
    void LocationPermissionNotGranted();
    void LocationNotEnabled();
    void BluetoothNotAvailable();
    void Error(String explanation);
}
