package fi.digi.savonia.movesense.Models.SaMi;

import java.sql.Timestamp;

public class SamiMeasurementPackage {
    public String key;
    public SamiMeasurement[] measurements;


    public void SetKey(String key)
    {
        this.key = key;
    }

    public void SetMeasurements(SamiMeasurement[] measurements)
    {
        this.measurements = measurements;
    }
}

