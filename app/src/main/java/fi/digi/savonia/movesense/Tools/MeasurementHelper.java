package fi.digi.savonia.movesense.Tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.util.Pair;
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
import fi.digi.savonia.movesense.Models.Movesense.Float3DVector;
import fi.digi.savonia.movesense.Models.SaMi.SamiData;
import fi.digi.savonia.movesense.Models.SaMi.SamiMeasurement;
import fi.digi.savonia.movesense.Models.SaMi.SamiMeasurementPackage;

public class MeasurementHelper {


    public String TemperatureDataTag = "Temperature";
    public String BatteryVoltageDataTag = "Battery Voltage Level";
    public String ECGDataTag = "Electrocardiogram";
    public String IMUDataTag = "Inertial Measurement Unit";
    public String HRDataTag = "Heartrate";
    public String HRDataTagRR = "RR interval";
    public String LinearAccelerationDataTag = "Linear Acceleration";
    public String GyroscopeAccelerationDataTag = "Gyroscope";
    public String MagnetometerDataTag = "Magnetometer";
    public String MeasurementTag = "Measurement Tag";
    public String MeasurementObject = "Movesense Test";
    public String MeasurementNote = "Measurement Note";

    private Gson gson;

    private long sendInterval;
    private SamiMeasurementHelper samiMeasurementHelper;
    private Timer timer;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public String key = "savoniatest";

    private Queue<Pair<TemperatureData,Long>> temperatureData = new LinkedList<>();
    private Queue<Pair<BatteryVoltageData,Long>> batteryVoltageData = new LinkedList<>();
    private Queue<Pair<LinearAccelerationData,Long>> linearAccelerationData = new LinkedList<>();
    private Queue<Pair<GyroscopeData,Long>> gyroscopeData = new LinkedList<>();
    private Queue<Pair<MagnetometerData,Long>> magnetometerData = new LinkedList<>();
    private Queue<Pair<ECGData,Long>> ecgData = new LinkedList<>();
    private Queue<Pair<Imu6Data,Long>> imu6Data = new LinkedList<>();
    private Queue<Pair<Imu6mData,Long>> imu6mData = new LinkedList<>();
    private Queue<Pair<Imu9Data,Long>> imu9Data = new LinkedList<>();
    private Queue<Pair<HeartrateData,Long>> heartrateData = new LinkedList<>();

    private class SendTask extends TimerTask
    {
        @Override
        public void run() {
            Send();
        }
    }

    public MeasurementHelper(SamiMeasurementHelper samiMeasurementHelper)
    {
        this.samiMeasurementHelper = samiMeasurementHelper;
        gson = new GsonBuilder().create();
    }

    public void SetSendInterval(long milliseconds)
    {
        sendInterval = milliseconds;
    }

    public void Start()
    {
        timer = new Timer();
        timer.schedule(new SendTask(),sendInterval,sendInterval);
    }

    public void SetMeasurementObject(String measurementObject)
    {
        this.MeasurementObject = measurementObject;
    }

    public void SetMeasurementNote(String measurementNote)
    {
        this.MeasurementNote = measurementNote;
    }

    public void SetMeasurementWritekey(String measurementWritekey)
    {
        this.key = measurementWritekey;
    }

    public void Stop()
    {
        if(timer!=null)
        {
            timer.cancel();
            timer = null;
            Send();
        }



    }

    public void AddMeasurement(Object data, MovesenseHelper.Sensor sensor)
    {
        switch (sensor)
        {
            case Temperature:
                temperatureData.add(new Pair<>((TemperatureData) data, Calendar.getInstance().getTimeInMillis()));
                break;
            case BatteryVoltage:
                batteryVoltageData.add(new Pair<>((BatteryVoltageData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case LinearAcceleration:
                linearAccelerationData.add(new Pair<>((LinearAccelerationData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case Gyroscope:
                gyroscopeData.add(new Pair<>((GyroscopeData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case Magnetometer:
                magnetometerData.add(new Pair<>((MagnetometerData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case ECG:
                ecgData.add(new Pair<>((ECGData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case IMU6:
                imu6Data.add(new Pair<>((Imu6Data) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case IMU6m:
                imu6mData.add(new Pair<>((Imu6mData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case IMU9:
                imu9Data.add(new Pair<>((Imu9Data) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case HeartRate:
                heartrateData.add(new Pair<>((HeartrateData) data,Calendar.getInstance().getTimeInMillis()));
                break;
        }
    }

    public void Clean()
    {
        temperatureData.clear();
        batteryVoltageData.clear();
        linearAccelerationData.clear();
        gyroscopeData.clear();
        magnetometerData.clear();
        ecgData.clear();
        imu6Data.clear();
        imu6mData.clear();
        imu9Data.clear();
        heartrateData.clear();
    }

    private void Send()
    {
        if(temperatureData.size()>0 || batteryVoltageData.size()>0 || ecgData.size()>0 || linearAccelerationData.size()>0 || gyroscopeData.size()>0 || magnetometerData.size()>0 || heartrateData.size()>0 || imu6Data.size()>0 || imu6mData.size()>0 || imu9Data.size()>0)
        {
            SamiMeasurementPackage samiMeasurementPackage = new SamiMeasurementPackage();
            samiMeasurementPackage.SetKey(key);
            samiMeasurementPackage.SetMeasurements(convertToMeasurementData());
            samiMeasurementHelper.SendMeasurement(samiMeasurementPackage);
        }

    }

    private String ConvertMillisToISO8601(long millis)
    {
        return sdf.format(new Date(millis));
    }

    private SamiMeasurement[] convertToMeasurementData()
    {
        List<SamiMeasurement> samiMeasurements = new ArrayList<>();
        List<SamiData> samiDatas = new ArrayList<>();
        List<Float3DVector> float3DVectorsAcc = new ArrayList<>();
        List<Float3DVector> float3DVectorsGyro = new ArrayList<>();
        List<Float3DVector> float3DVectorsMagn = new ArrayList<>();
        List<Integer> ecgCache = new ArrayList<>();
        List<Float> hrCache = new ArrayList<>();

        long tempMillis = 0;
        boolean first = true;

        MeasurementTag = "Temperature";

        while(temperatureData.peek()!=null)
        {
            Pair<TemperatureData,Long> temp = temperatureData.poll();
            samiDatas.add(new SamiData(TemperatureDataTag,temp.first.ConvertToCelcius()));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(temp.second),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
        }

        MeasurementTag = "Battery Level";

        while(batteryVoltageData.peek()!=null)
        {
            Pair<BatteryVoltageData,Long> temp = batteryVoltageData.poll();
            samiDatas.add(new SamiData(BatteryVoltageDataTag,temp.first.Percent));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(temp.second),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();

        }

        if(ecgData.peek()!=null)
        {
            MeasurementTag = "Electrocardiogram";

            while(ecgData.peek()!=null)
            {
                Pair<ECGData,Long> temp = ecgData.poll();
                for(int i = 0;i<temp.first.Samples.length;i++)
                {
                    ecgCache.add(temp.first.Samples[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }
            }

            samiDatas.add(new SamiData(ECGDataTag,gson.toJson(ecgCache)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            ecgCache.clear();
            first=true;
        }

        MeasurementTag = "Heart rate";

            if(heartrateData.peek() != null)
            {
                while(heartrateData.peek()!=null)
                {
                    Pair<HeartrateData,Long> temp = heartrateData.poll();

                    hrCache.add(temp.first.average);

                    if(first)
                    {
                        first = false;
                        tempMillis = temp.second;
                    }

                }

                samiDatas.add(new SamiData(HRDataTag,gson.toJson(hrCache)));
                samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
                samiDatas.clear();
                hrCache.clear();
        }

        MeasurementTag = IMUDataTag;

        if(linearAccelerationData.peek() != null)
        {
            while(linearAccelerationData.peek()!=null)
            {
                Pair<LinearAccelerationData,Long> temp = linearAccelerationData.poll();
                for(int i = 0; i<temp.first.ArrayAcc.length;i++)
                {
                    float3DVectorsAcc.add(temp.first.ArrayAcc[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }
            }

            samiDatas.add(new SamiData(LinearAccelerationDataTag,gson.toJson(float3DVectorsAcc)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            float3DVectorsAcc.clear();
            first = true;
        }

        if(gyroscopeData.peek() != null)
        {
            while(gyroscopeData.peek()!=null)
            {
                Pair<GyroscopeData,Long> temp = gyroscopeData.poll();

                for(int i = 0; i<temp.first.ArrayGyro.length;i++)
                {
                    float3DVectorsGyro.add(temp.first.ArrayGyro[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }
            }

            samiDatas.add(new SamiData(GyroscopeAccelerationDataTag,gson.toJson(float3DVectorsGyro)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            float3DVectorsGyro.clear();
            first=true;
        }

        if(magnetometerData.peek() != null)
        {
            while(magnetometerData.peek()!=null)
            {
                Pair<MagnetometerData,Long> temp = magnetometerData.poll();
                for(int i = 0; i<temp.first.ArrayMagn.length;i++)
                {
                    float3DVectorsMagn.add(temp.first.ArrayMagn[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }
            }

            samiDatas.add(new SamiData(MagnetometerDataTag,gson.toJson(float3DVectorsMagn)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            float3DVectorsMagn.clear();
            first = true;
        }

        if(imu6Data.peek() != null)
        {
            while(imu6Data.peek()!=null)
            {
                Pair<Imu6Data,Long> temp = imu6Data.poll();
                for(int i = 0; i<temp.first.ArrayAcc.length;i++)
                {
                    float3DVectorsAcc.add(temp.first.ArrayAcc[i]);
                }
                for(int i = 0; i<temp.first.ArrayGyro.length;i++)
                {
                    float3DVectorsGyro.add(temp.first.ArrayGyro[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }

            }

            samiDatas.add(new SamiData(LinearAccelerationDataTag,gson.toJson(float3DVectorsAcc)));
            samiDatas.add(new SamiData(GyroscopeAccelerationDataTag,gson.toJson(float3DVectorsGyro)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            float3DVectorsAcc.clear();
            float3DVectorsGyro.clear();
            first=true;
        }

        if(imu6mData.peek() != null)
        {
            while(imu6mData.peek()!=null)
            {
                Pair<Imu6mData,Long> temp = imu6mData.poll();
                for(int i = 0; i<temp.first.ArrayAcc.length;i++)
                {
                    float3DVectorsAcc.add(temp.first.ArrayAcc[i]);
                }

                for(int i = 0; i<temp.first.ArrayMagn.length;i++)
                {
                    float3DVectorsMagn.add(temp.first.ArrayMagn[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }

            }

            samiDatas.add(new SamiData(LinearAccelerationDataTag,gson.toJson(float3DVectorsAcc)));
            samiDatas.add(new SamiData(GyroscopeAccelerationDataTag,gson.toJson(float3DVectorsGyro)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            float3DVectorsAcc.clear();
            float3DVectorsMagn.clear();
            first=true;
        }

        if(imu9Data.peek() != null)
        {
            while(imu9Data.peek()!=null)
            {
                Pair<Imu9Data,Long> temp = imu9Data.poll();
                for(int i = 0; i<temp.first.ArrayAcc.length;i++)
                {
                    float3DVectorsAcc.add(temp.first.ArrayAcc[i]);
                }

                for(int i = 0; i<temp.first.ArrayGyro.length;i++)
                {
                    float3DVectorsGyro.add(temp.first.ArrayGyro[i]);
                }

                for(int i = 0; i<temp.first.ArrayMagn.length;i++)
                {
                    float3DVectorsMagn.add(temp.first.ArrayMagn[i]);
                }

                if(first)
                {
                    first = false;
                    tempMillis = temp.second;
                }

            }

            samiDatas.add(new SamiData(LinearAccelerationDataTag,gson.toJson(float3DVectorsAcc)));
            samiDatas.add(new SamiData(GyroscopeAccelerationDataTag,gson.toJson(float3DVectorsGyro)));
            samiDatas.add(new SamiData(MagnetometerDataTag,gson.toJson(float3DVectorsMagn)));
            samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(tempMillis),MeasurementTag,MeasurementNote,MeasurementObject));
            samiDatas.clear();
            float3DVectorsAcc.clear();
            float3DVectorsGyro.clear();
            float3DVectorsMagn.clear();
            first=true;
        }

        return samiMeasurements.toArray(new SamiMeasurement[samiMeasurements.size()]);

    }


}
