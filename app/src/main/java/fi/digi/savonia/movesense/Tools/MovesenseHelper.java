package fi.digi.savonia.movesense.Tools;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsConnectionListener;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsHeader;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;

import java.util.Timer;
import java.util.TimerTask;

import fi.digi.savonia.movesense.Models.MeasurementInterval;
import fi.digi.savonia.movesense.Models.Movesense.Data.BatteryVoltageData;
import fi.digi.savonia.movesense.Models.Movesense.Data.ECGData;
import fi.digi.savonia.movesense.Models.Movesense.Data.GyroscopeData;
import fi.digi.savonia.movesense.Models.Movesense.Data.HeartrateData;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu6Data;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu6mData;
import fi.digi.savonia.movesense.Models.Movesense.Data.Imu9Data;
import fi.digi.savonia.movesense.Models.Movesense.Data.LinearAccelerationData;
import fi.digi.savonia.movesense.Models.Movesense.Data.MagnetometerData;
import fi.digi.savonia.movesense.Models.Movesense.Data.TemperatureData;
import fi.digi.savonia.movesense.Models.Movesense.Info.ECGInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.GyroscopeInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.HeartrateInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.ImuInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.LinearAccelerationInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.MagnetometerInfo;
import fi.digi.savonia.movesense.Models.Movesense.Info.TemperatureInfo;
import fi.digi.savonia.movesense.Tools.Listeners.MovesenseActionListener;

public class MovesenseHelper {

    private Mds mMovesense;

    private MovesenseActionListener movesenseActionListener;

    private Gson gson = new GsonBuilder().create();

    private final String URI_CONNECTEDDEVICES = "suunto://MDS/ConnectedDevices";
    private final String URI_EVENTLISTENER = "suunto://MDS/EventListener";
    private final String SCHEME_PREFIX = "suunto://";
    private final String INFO_ENDFIX = "/Info";

    private final String URI_ECG = "/Meas/ECG";
    private final String URI_HEARTRATE = "/Meas/Hr";
    private final String URI_TEMPERATURE = "/Meas/Temp";
    private final String URI_LINEAR_ACCELERATION = "/Meas/Acc";
    private final String URI_GYROSCOPE = "/Meas/Gyro";
    private final String URI_MAGNETOMETER = "/Meas/Magn";
    private final String URI_BATTERY = "/System/Energy/Level";
    private final String URI_IMU6 = "/Meas/IMU6";
    private final String URI_IMU6m = "/Meas/IMU6m";
    private final String URI_IMU9 = "/Meas/IMU9";
    private final String URI_IMU_INFO = "/Meas/IMU";

    private String deviceAddress;
    private boolean isConnected = false;

    private final Handler handler = new Handler();

    private Timer temperatureMeasurementTimer;
    private Timer batteryVoltageMeasurementTimer;
    private TimerTask temperatureMeasurementTask;
    private TimerTask batteryVoltageMeasurementTask;
    private MdsSubscription ecgSubscription;
    private MdsSubscription hrSubscription;
    private MdsSubscription accSubscription;
    private MdsSubscription gyroSubscription;
    private MdsSubscription imu6Subscription;
    private MdsSubscription imu6mSubscription;
    private MdsSubscription imu9Subscription;
    private MdsSubscription magnSubscription;

    private long temperatureMeasurementInterval= 5000;
    private long batteryVoltageMeasurementInterval = 5000;
    private String linearAccelerationDataRate;
    private String gyroscopeDataRate;
    private String magnetometerDataRate;
    private String ecgDataRate;
    private String imuDataRate;
    private int HRDataRate;
    private int HRCounter = 0;

    public enum Sensor
    {
        Temperature,
        BatteryVoltage,
        LinearAcceleration,
        Gyroscope,
        Magnetometer,
        ECG,
        IMU6,
        IMU6m,
        IMU9,
        HeartRate
    }

    private String serial;

    private class TemperatureTask extends TimerTask
    {

        @Override
        public void run() {
            InternalGet(Sensor.Temperature);
        }
    }

    private class BatteryVoltageTask extends TimerTask
    {

        @Override
        public void run() {
            InternalGet(Sensor.BatteryVoltage);
        }
    }

    private void InitTimer(Sensor sensor)
    {

        switch (sensor)
        {
            case Temperature:
                temperatureMeasurementTimer = new Timer();
                temperatureMeasurementTimer.schedule(new TemperatureTask(),temperatureMeasurementInterval,temperatureMeasurementInterval);
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementTimer = new Timer();
                batteryVoltageMeasurementTimer.schedule(new BatteryVoltageTask(),batteryVoltageMeasurementInterval,batteryVoltageMeasurementInterval);
                break;
        }

    }


    private MdsConnectionListener mdsConnectionListener = new MdsConnectionListener() {
        @Override
        public void onConnect(String s) {

        }

        @Override
        public void onConnectionComplete(String s, String s1) {
            serial = s1;
            movesenseActionListener.ConnectionResult(true);
            isConnected=true;
        }

        @Override
        public void onError(MdsException e) {
            movesenseActionListener.OnError(e);
        }

        @Override
        public void onDisconnect(String s) {
            movesenseActionListener.OnDisconnect(s);
            isConnected=false;
        }
    };

    private boolean ArrayContains(int[] array, int value)
    {
        for (int i= 0; i<array.length;i++)
        {
            if(array[i] == value)
            {
                return true;
            }
        }

        return false;
    }

    public void SetDataRate(MeasurementInterval measurementInterval)
    {
        switch (measurementInterval.sensor)
        {
            case Temperature:
                temperatureMeasurementInterval = measurementInterval.GetSelectedValue() * 1000;
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementInterval = measurementInterval.GetSelectedValue() * 1000 * 60;
                break;
            case LinearAcceleration:
                linearAccelerationDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case Gyroscope:
                gyroscopeDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case Magnetometer:
                magnetometerDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case ECG:
                ecgDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case IMU6:
            case IMU6m:
            case IMU9:
                imuDataRate = measurementInterval.GetSelectedValueString(false);
                break;
            case HeartRate:
                HRDataRate = measurementInterval.GetSelectedValue();
                break;
        }
    }

    public void SetDataRate(Sensor sensor, int rate)
    {
        switch (sensor)
        {
            case Temperature:
                temperatureMeasurementInterval = rate * 1000;
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementInterval = rate * 1000;
                break;
            case LinearAcceleration:
                linearAccelerationDataRate = String.valueOf(rate);
                break;
            case Gyroscope:
                gyroscopeDataRate = String.valueOf(rate);
                break;
            case Magnetometer:
                magnetometerDataRate = String.valueOf(rate);
                break;
            case ECG:
                ecgDataRate = String.valueOf(rate);
                break;
            case IMU6:
            case IMU6m:
            case IMU9:
                imuDataRate = String.valueOf(rate);
                break;
            case HeartRate:
                HRDataRate = rate;
                break;
        }
    }


    public void SetMovesenseActionListener(MovesenseActionListener movesenseActionListener)
    {
        this.movesenseActionListener = movesenseActionListener;
    }

    public MovesenseHelper(Context context)
    {
        mMovesense = Mds.builder().build(context);
    }

    public void Connect(String deviceAddress)
    {
        mMovesense.connect(deviceAddress, mdsConnectionListener);
        this.deviceAddress = deviceAddress;

    }
    public void Disconnect()
    {
        if(isConnected)
        {
            UnsubscribeAll();
            mMovesense.disconnect(deviceAddress);
        }
    }

    private void InternalGet(Sensor sensor)
    {
        String getPath = "";
        switch (sensor)
        {
            case Temperature:
                getPath = URI_TEMPERATURE;
                break;
            case BatteryVoltage:
                getPath = URI_BATTERY;
                break;
        }

        String uriBase = SCHEME_PREFIX + serial + getPath;

        mMovesense.get(uriBase, null, new MdsResponseListener() {
            @Override
            public void onSuccess(String data, MdsHeader header) {

                ConcurrencyHelper.GetInstance().Run(() -> {
                    Object oData = ConvertStringToObject(data,sensor,false);

                    movesenseActionListener.OnDataReceived(oData,sensor);
                }, ConcurrencyHelper.ThreadType.Data);
            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    private MdsSubscription InternalSubscribe(String serial, String uri, Sensor sensor)
    {
        return mMovesense.subscribe(URI_EVENTLISTENER, formatContractToJson(serial, uri), new MdsNotificationListener() {
            @Override
            public void onNotification(String s) {
                boolean convert = true;
                if(sensor == Sensor.HeartRate)
                {
                    if(HRDataRate != 1)
                    {
                        HRCounter+=1;
                        if(HRCounter == HRDataRate)
                        {
                            HRCounter = 0;

                        }
                        else
                        {
                            convert = false;
                        }
                    }
                }

                if(convert)
                {
                    ConcurrencyHelper.GetInstance().Run(() -> {
                        Object oData = ConvertStringToObject(s,sensor,false);

                        movesenseActionListener.OnDataReceived(oData,sensor);
                    }, ConcurrencyHelper.ThreadType.Data);
                }


            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    private MdsSubscription InternalSubscribe(String serial, String uri, String rate, Sensor sensor)
    {
        return mMovesense.subscribe(URI_EVENTLISTENER, formatContractToJson(serial, uri+"/"+rate), new MdsNotificationListener() {
            @Override
            public void onNotification(String s) {
                ConcurrencyHelper.GetInstance().Run(() -> {
                    Object oData = ConvertStringToObject(s,sensor,false);
                    movesenseActionListener.OnDataReceived(oData,sensor);
                }, ConcurrencyHelper.ThreadType.Data);

            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    public void SubscribeAll(MeasurementInterval[] measurementIntervals)
    {
        for(int i = 0; i<measurementIntervals.length;i++)
        {
            if(measurementIntervals[i].Enabled)
            {
                SetDataRate(measurementIntervals[i]);
                Subscribe(measurementIntervals[i].sensor);
            }
        }

    }

    public boolean Subscribe(Sensor sensor)
    {
        switch (sensor)
        {
            case Temperature:
                InitTimer(sensor);
                break;
            case BatteryVoltage:
                InitTimer(sensor);
                break;
            case LinearAcceleration:
                accSubscription = InternalSubscribe(serial,URI_LINEAR_ACCELERATION,linearAccelerationDataRate, sensor);
                break;
            case Gyroscope:
                gyroSubscription = InternalSubscribe(serial,URI_GYROSCOPE,gyroscopeDataRate, sensor);
                break;
            case Magnetometer:
                magnSubscription = InternalSubscribe(serial,URI_MAGNETOMETER,magnetometerDataRate, sensor);
                break;
            case ECG:
                ecgSubscription = InternalSubscribe(serial,URI_ECG,ecgDataRate, sensor);
                break;
            case IMU6:
                imu6Subscription = InternalSubscribe(serial,URI_IMU6,imuDataRate, sensor);
                break;
            case IMU6m:
                imu6mSubscription = InternalSubscribe(serial,URI_IMU6m,imuDataRate, sensor);
                break;
            case IMU9:
                imu9Subscription = InternalSubscribe(serial,URI_IMU9,imuDataRate, sensor);
                break;
            case HeartRate:
                hrSubscription = InternalSubscribe(serial,URI_HEARTRATE, sensor);
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean Unsubscribe(Sensor sensor)
    {
        switch (sensor)
        {
            case Temperature:
                temperatureMeasurementTimer.cancel();
                temperatureMeasurementTimer = null;
                break;
            case BatteryVoltage:
                batteryVoltageMeasurementTimer.cancel();
                temperatureMeasurementTimer = null;
                break;
            case LinearAcceleration:
                accSubscription.unsubscribe();
                accSubscription = null;
                break;
            case Gyroscope:
                gyroSubscription.unsubscribe();
                gyroSubscription = null;
                break;
            case Magnetometer:
                magnSubscription.unsubscribe();
                magnSubscription = null;
                break;
            case ECG:
                ecgSubscription.unsubscribe();
                ecgSubscription = null;
                break;
            case IMU6:
                imu6Subscription.unsubscribe();
                imu6Subscription = null;
                break;
            case IMU6m:
                imu6mSubscription.unsubscribe();
                imu6mSubscription = null;
            case IMU9:
                imu9Subscription.unsubscribe();
                imu9Subscription = null;
                break;
            case HeartRate:
                hrSubscription.unsubscribe();
                hrSubscription = null;
                break;
            default:
                return false;
        }
        return true;
    }

    public void UnsubscribeAll()
    {
        if(temperatureMeasurementTimer!=null)
        {
            temperatureMeasurementTimer.cancel();
            temperatureMeasurementTimer = null;
        }

        if(batteryVoltageMeasurementTimer!=null)
        {
            batteryVoltageMeasurementTimer.cancel();
            batteryVoltageMeasurementTimer = null;
        }

        if(accSubscription!=null)
        {
            accSubscription.unsubscribe();
            accSubscription = null;
        }

        if(gyroSubscription!=null)
        {
            gyroSubscription.unsubscribe();
            gyroSubscription = null;
        }

        if(magnSubscription!=null)
        {
            magnSubscription.unsubscribe();
            magnSubscription = null;
        }

        if(ecgSubscription!=null)
        {
            ecgSubscription.unsubscribe();
            ecgSubscription = null;
        }

        if(imu6Subscription!=null)
        {
            imu6Subscription.unsubscribe();
            imu6Subscription = null;
        }

        if(imu6mSubscription!=null)
        {
            imu6mSubscription.unsubscribe();
            imu6mSubscription = null;
        }

        if(imu9Subscription!=null)
        {
            imu9Subscription.unsubscribe();
            imu9Subscription = null;
        }

        if(hrSubscription!=null)
        {
            hrSubscription.unsubscribe();
            hrSubscription = null;
        }
    }

    private String formatContractToJson(String serial, String uri) {
        StringBuilder sb = new StringBuilder();
        return sb.append("{\"Uri\": \"").append(serial).append(uri).append("\"}").toString();
    }

    private String pathFormatHelper(String serial, String path) {
        final StringBuilder sb = new StringBuilder();
        return sb.append(SCHEME_PREFIX).append(serial).append("/").append(path).toString();
    }

    public void GetInfo(Sensor sensor)
    {
        String getPath = "";
        switch (sensor)
        {
            case Temperature:
                getPath = URI_TEMPERATURE;
                break;
            case BatteryVoltage:
                getPath = URI_BATTERY;
                break;
            case LinearAcceleration:
                getPath = URI_LINEAR_ACCELERATION;
                break;
            case Gyroscope:
                getPath = URI_GYROSCOPE;
                break;
            case Magnetometer:
                getPath = URI_MAGNETOMETER;
                break;
            case ECG:
                getPath = URI_ECG;
                break;
            case HeartRate:
                getPath = URI_HEARTRATE;
                break;
            case IMU6:
                getPath = URI_IMU_INFO;
                break;
            case IMU9:
                getPath = URI_IMU_INFO;
                break;
        }

        String uriBase = SCHEME_PREFIX + serial + getPath + INFO_ENDFIX;

        mMovesense.get(uriBase, null, new MdsResponseListener() {
            @Override
            public void onSuccess(String data, MdsHeader header) {

                ConcurrencyHelper.GetInstance().Run(() -> {
                    Object oData = ConvertStringToObject(data,sensor,true);

                    movesenseActionListener.OnInfoReceived(oData,sensor);
                }, ConcurrencyHelper.ThreadType.Data);


            }

            @Override
            public void onError(MdsException e) {
                movesenseActionListener.OnError(e);
            }
        });
    }

    private Object ConvertStringToObject(String sData, Sensor sensor, Boolean info)
    {
        JsonElement content;
        String sContent;
        if(info || sensor == Sensor.Temperature || sensor == Sensor.BatteryVoltage)
        {

            content = JsonParser.parseString(sData).getAsJsonObject().get("Content");
        }
        else
        {
            content = JsonParser.parseString(sData).getAsJsonObject().get("Body");
        }

        sContent = content.toString();

        if(info)
        {
            switch (sensor)
            {

                case Temperature:
                    return gson.fromJson(sContent, TemperatureInfo.class);
                case LinearAcceleration:
                    return gson.fromJson(sContent, LinearAccelerationInfo.class);
                case Gyroscope:
                    return gson.fromJson(sContent, GyroscopeInfo.class);
                case Magnetometer:
                    return gson.fromJson(sContent, MagnetometerInfo.class);
                case ECG:
                    return gson.fromJson(sContent, ECGInfo.class);
                case IMU6:
                    return gson.fromJson(sContent, ImuInfo.class);
                case IMU6m:
                    return gson.fromJson(sContent, ImuInfo.class);
                case IMU9:
                    return gson.fromJson(sContent, ImuInfo.class);
                case HeartRate:
                    return gson.fromJson(sContent, HeartrateInfo.class);
            }
        }
        else
        {
            switch (sensor)
            {

                case Temperature:
                    return gson.fromJson(sContent, TemperatureData.class);
                case BatteryVoltage:
                    BatteryVoltageData batteryVoltageData = new BatteryVoltageData();
                    batteryVoltageData.Percent = content.getAsInt();
                    return batteryVoltageData;
                    //return gson.fromJson(sContent, BatteryVoltageData.class);
                case LinearAcceleration:
                    return gson.fromJson(sContent, LinearAccelerationData.class);
                case Gyroscope:
                    return gson.fromJson(sContent, GyroscopeData.class);
                case Magnetometer:
                    return gson.fromJson(sContent, MagnetometerData.class);
                case ECG:
                    return gson.fromJson(sContent, ECGData.class);
                case IMU6:
                    return gson.fromJson(sContent, Imu6Data.class);
                case IMU6m:
                    return gson.fromJson(sContent, Imu6mData.class);
                case IMU9:
                    return gson.fromJson(sContent, Imu9Data.class);
                case HeartRate:
                    return gson.fromJson(sContent, HeartrateData.class);
            }
        }

        return null;
    }


}
