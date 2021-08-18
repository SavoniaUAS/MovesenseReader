package fi.digi.savonia.movesense.Models.SaMi;

import java.util.List;

public class SamiMeasurement
{
    public SamiMeasurement(SamiData[] data, String timestampISO8601, String tag, String note, String object)
    {
        Data = data;
        TimestampISO8601 = timestampISO8601;
        Tag = tag;
        Object = object;
        this.Note = note;
    }

    public SamiMeasurement(List<SamiData> data, String timestampISO8601, String tag, String note, String object)
    {
        Data = data.toArray(new SamiData[data.size()]);
        TimestampISO8601 = timestampISO8601;
        Tag = tag;
        Object = object;
        this.Note = note;
    }

    public SamiMeasurement(SamiData datas, String timestampISO8601, String tag, String note, String object)
    {
        Data = new SamiData[]{datas};
        TimestampISO8601 = timestampISO8601;
        Tag = tag;
        Object = object;
        this.Note = note;
    }

    public SamiData[] Data;
    public String TimestampISO8601;
    public String Tag;
    public String Note;
    public String Object;
}
