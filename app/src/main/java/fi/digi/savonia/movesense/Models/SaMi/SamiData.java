package fi.digi.savonia.movesense.Models.SaMi;

public class SamiData
{
    public SamiData(String tag, String value)
    {
        Tag = tag;
        TextValue = value;
    }

    public SamiData(String tag, int value)
    {
        Tag = tag;
        Value = Double.valueOf(value);

    }

    public SamiData(String tag, long value)
    {
        Tag = tag;
        LongValue = value;
    }

    public SamiData(String tag, double value)
    {
        Tag = tag;
        Value = Double.valueOf(value);
    }

    public SamiData(String tag, float value)
    {
        Tag = tag;
        Value = Double.valueOf(value);
    }
    public String TextValue;
    public Double Value = null;
    public Long LongValue = null;
    public String Tag;
}