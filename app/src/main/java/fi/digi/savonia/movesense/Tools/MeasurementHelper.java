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
    public String Acc_X_Axis ="Acc_X";
    public String Acc_Y_Axis ="Acc_Y";
    public String Acc_Z_Axis ="Acc_Z";

    public String Gyro_X_Axis ="Gyro_X";
    public String Gyro_Y_Axis ="Gyro_Y";
    public String Gyro_Z_Axis ="Gyro_Z";

    public String Magn_X_Axis ="Magn_X";
    public String Magn_Y_Axis ="Magn_Y";
    public String Magn_Z_Axis ="Magn_Z";

    public String GyroscopeDataTag = "Gyroscope";
    public String MagnetometerDataTag = "Magnetometer";
    public String MeasurementTag = "Measurement Tag";
    public String MeasurementObject = "Movesense Test";
    public String MeasurementNote = "Measurement Note";

    private Gson gson;

    private long sendInterval;
    private SamiMeasurementHelper samiMeasurementHelper;
    private Timer timer;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private long sensorTimestamp = 0;
    private long timestamp;

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

    private void SetTimestamp(long tempTimestamp)
    {
        if(sensorTimestamp==0)
        {
            sensorTimestamp = tempTimestamp;
            timestamp = Calendar.getInstance().getTimeInMillis();
        }
    }

    private long GetSensorRelativeTimestamp(long tempSensorTimestamp)
    {
        return timestamp + (tempSensorTimestamp-sensorTimestamp);
    }

    public void AddMeasurement(Object data, MovesenseHelper.Sensor sensor)
    {
        switch (sensor)
        {
            case Temperature:
                SetTimestamp(((TemperatureData) data).Timestamp);
                temperatureData.add(new Pair<>((TemperatureData) data, GetSensorRelativeTimestamp(((TemperatureData) data).Timestamp)));
                break;
            case BatteryVoltage:
                batteryVoltageData.add(new Pair<>((BatteryVoltageData) data,Calendar.getInstance().getTimeInMillis()));
                break;
            case LinearAcceleration:
                SetTimestamp(((LinearAccelerationData) data).Timestamp);
                linearAccelerationData.add(new Pair<>((LinearAccelerationData) data,GetSensorRelativeTimestamp(((LinearAccelerationData) data).Timestamp)));
                break;
            case Gyroscope:
                SetTimestamp(((GyroscopeData) data).Timestamp);
                gyroscopeData.add(new Pair<>((GyroscopeData) data,GetSensorRelativeTimestamp(((GyroscopeData) data).Timestamp)));
                break;
            case Magnetometer:
                SetTimestamp(((MagnetometerData) data).Timestamp);
                magnetometerData.add(new Pair<>((MagnetometerData) data,GetSensorRelativeTimestamp(((MagnetometerData) data).Timestamp)));
                break;
            case ECG:
                SetTimestamp(((ECGData) data).Timestamp);
                ecgData.add(new Pair<>((ECGData) data,GetSensorRelativeTimestamp(((ECGData) data).Timestamp)));
                break;
            case IMU6:
                SetTimestamp(((Imu6Data) data).Timestamp);
                imu6Data.add(new Pair<>((Imu6Data) data,GetSensorRelativeTimestamp(((Imu6Data) data).Timestamp)));
                break;
            case IMU6m:
                SetTimestamp(((Imu6mData) data).Timestamp);
                imu6mData.add(new Pair<>((Imu6mData) data,GetSensorRelativeTimestamp(((Imu6mData) data).Timestamp)));
                break;
            case IMU9:
                SetTimestamp(((Imu9Data) data).Timestamp);
                imu9Data.add(new Pair<>((Imu9Data) data,GetSensorRelativeTimestamp(((Imu9Data) data).Timestamp)));
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
            sensorTimestamp = 0;
        }

    }

    private String ConvertMillisToISO8601(long millis)
    {
        return sdf.format(new Date(millis));
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private long SequalTimestamp(long initialTimestamp,double timeDifferenceConstantMillis,int index)
    {
        return (long) (initialTimestamp+(timeDifferenceConstantMillis*index));
    }

    private SamiMeasurement[] convertToMeasurementData()
    {
        List<SamiMeasurement> samiMeasurements = new ArrayList<>();
        List<SamiData> samiDatas = new ArrayList<>();

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
            long first =0,second =0;
            int differenceMillis=0;

            double timeDifferentConstant = 1/(250)*1000; //  TODO Mukautuva aikaero jälkeenpäin

            while(ecgData.peek()!=null)
            {
                Pair<ECGData,Long> temp = ecgData.poll();
                if(first ==0)
                {
                    first=temp.first.Timestamp;
                }
                else if(second==0)
                {
                    second=temp.first.Timestamp;
                }
                if(first!=0 & second!=0 & differenceMillis == 0)
                {
                    differenceMillis = (int) (second-first);
                    timeDifferentConstant = differenceMillis/15.0;
                }



                for(int i = 0;i<temp.first.Samples.length;i++)
                {
                    samiDatas.add(new SamiData("Average",temp.first.Samples[i]));
                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();
                }

            }

        }

        MeasurementTag = "Heart rate";

            if(heartrateData.peek() != null)
            {
                while(heartrateData.peek()!=null)
                {
                    Pair<HeartrateData,Long> temp = heartrateData.poll();

                    samiDatas.add(new SamiData("Average",temp.first.average));
                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(temp.second),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();

                }

        }

        MeasurementTag = IMUDataTag;

        if(linearAccelerationData.peek() != null)
        {
            while(linearAccelerationData.peek()!=null)
            {
                Pair<LinearAccelerationData,Long> temp = linearAccelerationData.poll();

                double timeDifferentConstant = 1/(13*temp.first.ArrayAcc.length)*1000;

                for(int i = 0; i<temp.first.ArrayAcc.length;i++)
                {
                    samiDatas.add(new SamiData(Acc_X_Axis,temp.first.ArrayAcc[i].x));
                    samiDatas.add(new SamiData(Acc_Y_Axis,temp.first.ArrayAcc[i].y));
                    samiDatas.add(new SamiData(Acc_Z_Axis,temp.first.ArrayAcc[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();
                }

            }

        }

        MeasurementTag = IMUDataTag;

        if(gyroscopeData.peek() != null)
        {
            while(gyroscopeData.peek()!=null)
            {
                Pair<GyroscopeData,Long> temp = gyroscopeData.poll();

                int timeDifferentConstant = 1/(13*temp.first.ArrayGyro.length)*1000;

                for(int i = 0; i<temp.first.ArrayGyro.length;i++)
                {
                    samiDatas.add(new SamiData(Gyro_X_Axis,temp.first.ArrayGyro[i].x));
                    samiDatas.add(new SamiData(Gyro_Y_Axis,temp.first.ArrayGyro[i].y));
                    samiDatas.add(new SamiData(Gyro_Z_Axis,temp.first.ArrayGyro[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();
                }

            }
        }

        MeasurementTag = IMUDataTag;

        if(magnetometerData.peek() != null)
        {
            while(magnetometerData.peek()!=null) {
                Pair<MagnetometerData, Long> temp = magnetometerData.poll();

                int timeDifferentConstant = 1/(13*temp.first.ArrayMagn.length)*1000;

                for (int i = 0; i < temp.first.ArrayMagn.length; i++) {
                    samiDatas.add(new SamiData(Magn_X_Axis, temp.first.ArrayMagn[i].x));
                    samiDatas.add(new SamiData(Magn_Y_Axis, temp.first.ArrayMagn[i].y));
                    samiDatas.add(new SamiData(Magn_Z_Axis, temp.first.ArrayMagn[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas, ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)), MeasurementTag, MeasurementNote, MeasurementObject));
                    samiDatas.clear();
                }
            }

        }

        MeasurementTag = IMUDataTag;

        if(imu6Data.peek() != null)
        {
            while(imu6Data.peek()!=null) {
                Pair<Imu6Data, Long> temp = imu6Data.poll();

                int timeDifferentConstant = 1/(13*temp.first.ArrayAcc.length)*1000;

                for (int i = 0; i < temp.first.ArrayAcc.length; i++) {
                    samiDatas.add(new SamiData(Acc_X_Axis, temp.first.ArrayAcc[i].x));
                    samiDatas.add(new SamiData(Acc_Y_Axis, temp.first.ArrayAcc[i].y));
                    samiDatas.add(new SamiData(Acc_Z_Axis, temp.first.ArrayAcc[i].z));

                    samiDatas.add(new SamiData(Gyro_X_Axis, temp.first.ArrayGyro[i].x));
                    samiDatas.add(new SamiData(Gyro_Y_Axis, temp.first.ArrayGyro[i].y));
                    samiDatas.add(new SamiData(Gyro_Z_Axis, temp.first.ArrayGyro[i].z));

                    samiMeasurements.add(new SamiMeasurement(samiDatas, ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)), MeasurementTag, MeasurementNote, MeasurementObject));
                    samiDatas.clear();
                }
                /*
                for (int i = 0; i < temp.first.ArrayGyro.length; i++) {
                    samiDatas.add(new SamiData(Gyro_X_Axis, temp.first.ArrayGyro[i].x));
                    samiDatas.add(new SamiData(Gyro_Y_Axis, temp.first.ArrayGyro[i].y));
                    samiDatas.add(new SamiData(Gyro_Z_Axis, temp.first.ArrayGyro[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas, ConvertMillisToISO8601(temp.second), MeasurementTag, MeasurementNote, MeasurementObject));
                    samiDatas.clear();
                }

                 */
            }

        }

        MeasurementTag = IMUDataTag;

        if(imu6mData.peek() != null)
        {
            while(imu6mData.peek()!=null) {
                Pair<Imu6mData, Long> temp = imu6mData.poll();

                int timeDifferentConstant = 1/(13*temp.first.ArrayAcc.length)*1000;

                for (int i = 0; i < temp.first.ArrayAcc.length; i++) {
                    samiDatas.add(new SamiData(Acc_X_Axis, temp.first.ArrayAcc[i].x));
                    samiDatas.add(new SamiData(Acc_Y_Axis, temp.first.ArrayAcc[i].y));
                    samiDatas.add(new SamiData(Acc_Z_Axis, temp.first.ArrayAcc[i].z));

                    samiDatas.add(new SamiData(Magn_X_Axis, temp.first.ArrayMagn[i].x));
                    samiDatas.add(new SamiData(Magn_Y_Axis, temp.first.ArrayMagn[i].y));
                    samiDatas.add(new SamiData(Magn_Z_Axis, temp.first.ArrayMagn[i].z));

                    samiMeasurements.add(new SamiMeasurement(samiDatas, ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)), MeasurementTag, MeasurementNote, MeasurementObject));
                    samiDatas.clear();
                }
                /*
                for (int i = 0; i < temp.first.ArrayMagn.length; i++) {
                    samiDatas.add(new SamiData(Magn_X_Axis, temp.first.ArrayMagn[i].x));
                    samiDatas.add(new SamiData(Magn_Y_Axis, temp.first.ArrayMagn[i].y));
                    samiDatas.add(new SamiData(Magn_Z_Axis, temp.first.ArrayMagn[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas, ConvertMillisToISO8601(temp.second), MeasurementTag, MeasurementNote, MeasurementObject));
                    samiDatas.clear();
                }

                 */
            }


        }

        MeasurementTag = IMUDataTag;

        if(imu9Data.peek() != null)
        {
            while(imu9Data.peek()!=null)
            {
                Pair<Imu9Data,Long> temp = imu9Data.poll();

                int timeDifferentConstant = 1/(13*temp.first.ArrayAcc.length)*1000;

                for(int i = 0; i<temp.first.ArrayAcc.length;i++)
                {
                    samiDatas.add(new SamiData(Acc_X_Axis,temp.first.ArrayAcc[i].x));
                    samiDatas.add(new SamiData(Acc_Y_Axis,temp.first.ArrayAcc[i].y));
                    samiDatas.add(new SamiData(Acc_Z_Axis,temp.first.ArrayAcc[i].z));

                    samiDatas.add(new SamiData(Gyro_X_Axis,temp.first.ArrayGyro[i].x));
                    samiDatas.add(new SamiData(Gyro_Y_Axis,temp.first.ArrayGyro[i].y));
                    samiDatas.add(new SamiData(Gyro_Z_Axis,temp.first.ArrayGyro[i].z));

                    samiDatas.add(new SamiData(Magn_X_Axis,temp.first.ArrayMagn[i].x));
                    samiDatas.add(new SamiData(Magn_Y_Axis,temp.first.ArrayMagn[i].y));
                    samiDatas.add(new SamiData(Magn_Z_Axis,temp.first.ArrayMagn[i].z));

                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(SequalTimestamp(temp.second,timeDifferentConstant,i)),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();
                }

                /*
                for(int i = 0; i<temp.first.ArrayGyro.length;i++)
                {
                    samiDatas.add(new SamiData(Gyro_X_Axis,temp.first.ArrayGyro[i].x));
                    samiDatas.add(new SamiData(Gyro_Y_Axis,temp.first.ArrayGyro[i].y));
                    samiDatas.add(new SamiData(Gyro_Z_Axis,temp.first.ArrayGyro[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(temp.second),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();
                }

                for(int i = 0; i<temp.first.ArrayMagn.length;i++)
                {
                    samiDatas.add(new SamiData(Magn_X_Axis,temp.first.ArrayMagn[i].x));
                    samiDatas.add(new SamiData(Magn_Y_Axis,temp.first.ArrayMagn[i].y));
                    samiDatas.add(new SamiData(Magn_Z_Axis,temp.first.ArrayMagn[i].z));
                    samiMeasurements.add(new SamiMeasurement(samiDatas,ConvertMillisToISO8601(temp.second),MeasurementTag,MeasurementNote,MeasurementObject));
                    samiDatas.clear();
                }
                */


            }

        }

        return samiMeasurements.toArray(new SamiMeasurement[samiMeasurements.size()]);

    }

}
