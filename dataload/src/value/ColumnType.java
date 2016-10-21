package value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ColumnType
{
    String name;
    DataType dataType = DataType.NONE;
    int maxLen;
    int sampleCnt;
    int nullCnt;
    long maxIntVal;
    int maxBeforeDecimal;
    int maxAfterDecimal;
    String varcharProof;
    int[] vlengths = {10, 50, 100};

    HashSet<SimpleDateFormat> datesWorked = new HashSet<SimpleDateFormat>();
    HashSet<SimpleDateFormat> dateTimesWorked = new HashSet<SimpleDateFormat>();
    
    Pattern integerPattern = Pattern.compile("^([\\+\\-])?[0-9,]+$");
    Pattern numericPattern = Pattern.compile("^([\\+\\-])?([0-9,]+)\\.([0-9]*)$");
    
    String[] dateFormatStrings = {
            "yyyy.MM.dd",
            "yyyy/MM/dd",
            "yyyy-MM-dd",
            "yy.MM.dd",
            "yy/MM/dd",
            "yy-MM-dd",
            "MM.dd.yyyy",
            "MM/dd/yyyy",
            "MM-dd-yyyy",
            "MM.dd.yy",
            "MM/dd/yy",
            "MM-dd-yy",
            "MMM dd, yyyy",
            "MMM dd,yyyy",
            "MMM dd yyyy",
            "MMM dd, yy",
            "MMM dd,yy",
            "MMM dd yy",
            "dd-MMM-yy",
            "dd-MMM-yyyy"
    };
    
    String[] timeFormatStrings = {
            "hh:mm:ss:SSSa",
            "hh.mm.ss.SSSa",
            "hh:mm:ss:SSS a",
            "hh.mm.ss.SSS a",
            "HH:mm:ss:SSS",
            "HH.mm.ss.SSS",
            "hh:mm:ss a",
            "hh.mm.ss a",
            "HH:mm:ss",
            "HH.mm.ss",
    };
    
    SimpleDateFormat[] dateFormats;
    SimpleDateFormat[] timeFormats;
    SimpleDateFormat[] dateTimeFormats;
    
    private void makeDateFormats()
    {
        dateFormats = new SimpleDateFormat[dateFormatStrings.length];
        for (int i=0; i<dateFormatStrings.length; i++)
        {
            dateFormats[i] = new SimpleDateFormat(dateFormatStrings[i]);
        }
    }
    
    private void makeTimeFormats()
    {
        timeFormats = new SimpleDateFormat[timeFormatStrings.length];
        for (int i=0; i<timeFormatStrings.length; i++)
        {
            timeFormats[i] = new SimpleDateFormat(timeFormatStrings[i]);
        }
    }
    
    private void makeDateTimeFormats()
    {
        dateTimeFormats = new SimpleDateFormat[dateFormatStrings.length * timeFormatStrings.length];
        int i = 0;
        for (String dfmt : dateFormatStrings)
        {
            for (String tfmt : timeFormatStrings)
            {
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat(dfmt + " " + tfmt);
                dateTimeFormats[i++] = dateTimeFormat;
            }
        }
    }
    
    public ColumnType(String name)
    {
        super();
        this.name = name;
        makeDateFormats();
        makeTimeFormats();
        makeDateTimeFormats();
    }
    
    public void addObservation(String value)
    {
        sampleCnt += 1;
        if (value == null || value.equals(""))
        {
            nullCnt +=1;
            return;
        }
        int len = value.length();
        if (len > maxLen) {
            maxLen = len;
        }
        if (dataType == DataType.NONE)
        {
            if (isDateTime(value))
            {
                dataType = DataType.DATETIME;
            }
            else if (isDate(value))
            {
                dataType = DataType.DATE;
            }
            else if (isInteger(value))
            {
                dataType = DataType.INTEGER;
            }
            else if (isNumeric(value))
            {
                dataType = DataType.NUMERIC;
            }
            else
            {
                dataType = DataType.VARCHAR;
            }
        }
        else if (dataType == DataType.VARCHAR)
        {
            return;
        }
        else if (dataType == DataType.INTEGER)
        {
            if (! isInteger(value))
            {
                if (isNumeric(value))
                {
                    dataType = DataType.NUMERIC;
                }
                else
                {
                    dataType = DataType.VARCHAR;
                    varcharProof = value;
                }
            }
        }
        else if (dataType == DataType.NUMERIC)
        {
            if (! isNumeric(value))
            {
                dataType = DataType.VARCHAR;
                varcharProof = value;
            }
        }
        else if (dataType == DataType.DATE)
        {
            if (! isDate(value))
            {
                dataType = DataType.VARCHAR;
                varcharProof = value;
            }
        }
        else if (dataType == DataType.DATETIME)
        {
            if (! isDateTime(value))
            {
                dataType = DataType.VARCHAR;
                varcharProof = value;
            }
        }
    }
    
    public boolean isInteger(String value)
    {
        Matcher m = integerPattern.matcher(value);
        if (m.matches())
        {
            long val = Long.parseLong(value.replaceAll(",", ""));
            if (Math.abs(val) > maxIntVal)
            {
                maxIntVal = Math.abs(val);
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean isNumeric(String value)
    {
        Matcher m = numericPattern.matcher(value);
        if (m.matches())
        {
            String beforeDecimal = m.group(2).replaceAll(",", "");
            String afterDecimal = m.group(3);
            int beforeDecimalLen = beforeDecimal.length();
            int afterDecimalLen = afterDecimal.length();
            if (beforeDecimalLen > maxBeforeDecimal)
            {
                maxBeforeDecimal = beforeDecimalLen;
            }
            if (afterDecimalLen > maxAfterDecimal)
            {
                maxAfterDecimal = afterDecimalLen;
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean isDate(String value, SimpleDateFormat format)
    {
        try
        {
            format.parse(value);
            return true;
        }
        catch (ParseException pe)
        {
            return false;
        }
    }
    
    public boolean isDate(String value)
    {
        for (SimpleDateFormat fmt : datesWorked)
        {
            if (isDate(value, fmt)) {
                return true;
            }
        }
        for (SimpleDateFormat fmt : dateFormats)
        {
            if (datesWorked.contains(fmt))
            {
                continue;
            }
            if (isDate(value, fmt)) {
                datesWorked.add(fmt);
                return true;
            }
        }
        return false;
    }
    
    public boolean isDateTime(String value)
    {
        for (SimpleDateFormat fmt : dateTimesWorked)
        {
            if (isDate(value, fmt)) {
                return true;
            }
        }
        for (SimpleDateFormat fmt : dateTimeFormats)
        {
            if (dateTimesWorked.contains(fmt))
            {
                continue;
            }
            if (isDate(value, fmt)) {
                dateTimesWorked.add(fmt);
                return true;
            }
        }
        return false;
    }

    
    @Override
    public String toString()
    {
        String[] datesWorkedArr = new String[datesWorked.size()];
        int i = 0;
        for (SimpleDateFormat df : datesWorked)
        {
            datesWorkedArr[i++] = df.toPattern();
        }
        
        String[] dateTimesWorkedArr = new String[dateTimesWorked.size()];
        i = 0;
        for (SimpleDateFormat df : dateTimesWorked)
        {
            dateTimesWorkedArr[i++] = df.toPattern();
        }

        return "ColumnType [name=" + name + ", dataType=" + dataType + ", maxLen=" + maxLen + ", sampleCnt=" + sampleCnt
                + ", nullCnt=" + nullCnt + ", maxIntVal=" + maxIntVal + ", maxBeforeDecimal=" + maxBeforeDecimal
                + ", maxAfterDecimal=" + maxAfterDecimal
                + ", varcharProof=" + varcharProofBytes() 
                + ", datesWorked=" + Arrays.toString(datesWorkedArr)
                + ", dateTimesWorked=" + Arrays.toString(dateTimesWorkedArr) + "]";
    }
    
    public JsonObject toJsonObject()
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("dataType", dataType.toString());
        obj.addProperty("maxLen", maxLen);
        obj.addProperty("sampleCnt", sampleCnt);
        obj.addProperty("nullCnt", nullCnt);
        obj.addProperty("maxIntVal", maxIntVal);
        obj.addProperty("maxBeforeDecimal", maxBeforeDecimal);
        obj.addProperty("maxAfterDecimal", maxAfterDecimal);
        
        JsonArray datesWorkedArr = new JsonArray();
        for (SimpleDateFormat df : datesWorked)
        {
            datesWorkedArr.add(df.toPattern());
        }
        obj.add("datesWorked", datesWorkedArr);
        
        JsonArray dateTimesWorkedArr = new JsonArray();
        for (SimpleDateFormat df : dateTimesWorked)
        {
            dateTimesWorkedArr.add(df.toPattern());
        }
        obj.add("dateTimesWorked", dateTimesWorkedArr);

        return obj;
    }

    public String varcharProofBytes()
    {
        if (varcharProof == null)
        {
            return null;
        }
        return "(" + varcharProof + ")" + Arrays.toString(varcharProof.getBytes());
    }
    
    private int bumpUp(int val)
    {
        for (int i=0; i<vlengths.length; i++)
        {
            if (val * 2 <= vlengths[i])
            {
                return vlengths[i];
            }
        }
        return -1;
    }
    
    public String pgDataType()
    {
        StringBuilder sb = new StringBuilder(20);
        sb.append("  ");
        switch (dataType)
        {
        case VARCHAR: 
            String lenPart = "";
            int newLen = bumpUp(maxLen);
            if (newLen != -1)
            {
                lenPart = "(" + newLen + ")";
            }
            sb.append(String.format("%-40s%s%s", name, dataType.toString().toLowerCase(), lenPart));
            break;
        case DATE:
            sb.append(String.format("%-40s%s", name, dataType.toString().toLowerCase()));
            break;
        case DATETIME:
            sb.append(String.format("%-40s%s", name, "timestamp"));
            break;
        case NUMERIC:
            sb.append(String.format("%-40s%s(%d,%d)", name, "numeric", (maxBeforeDecimal + maxAfterDecimal), maxAfterDecimal));
            break;
        case INTEGER:
            String type = "int";
            if (maxIntVal >= 1000000000)
            {
                type = "bigint";
            }
            sb.append(String.format("%-40s%s", name, type));
            break;
        case NONE:
            sb.append(String.format("%-40s%s", name, "UNDETERMINED (All values null)"));
            break;
        default:
            break;
        }
        return sb.toString();
    }
    
    public void runObservations(String[] vals)
    {
        for (String val : vals)
        {
            addObservation(val);
            // System.out.println("val = " + val + " : " + d1);
        }
    }

    public static void main(String[] args)
    {
        ColumnType d = new ColumnType("test");
        System.out.println("Checking dates");
        System.out.println("==============");
        System.out.println(d.isDate("01/01/2010"));
        System.out.println(d.isDate("1/1/2010"));
        System.out.println(d.isDate("Jan 01 2023"));
        System.out.println(d.isDate("Jan 1, 2023"));
        System.out.println(d.isDate("1-Jan-2023"));
        System.out.println(d.isDate("October 10, 2016"));
        System.out.println(d.isDate("Oct 10,2016"));
        System.out.println(d.isDate("May 31 2011 11:59:00:000PM"));
        
        System.out.println("Checking date times");
        System.out.println("===================");
        System.out.println(d.isDateTime("1/1/2010 10:20:30"));
        System.out.println(d.isDateTime("1/1/2010 10:20:30 PM"));
        System.out.println(d.isDateTime("1/1/2010 10:20:30.345 PM"));
        System.out.println(d.isDateTime("May 31 2011 11:59:00:000PM"));
        
        System.out.println("Multiple observations test");
        System.out.println("==========================");
        
        ColumnType d1 = new ColumnType("d1");
        String[] vals = {"", "", "12", "13", "15.2", "-12.3", "123,456.456"};
        d1.runObservations(vals);
        System.out.println(d1);

        d1 = new ColumnType("d1");
        String[] vals2 = {"", "", "May 31 2011 11:59:00:000PM", ""};
        d1.runObservations(vals2);
        System.out.println(d1);

        d1 = new ColumnType("d1");
        String[] vals3 = {"US", "US", "May 31 2011 11:59:00:000PM", ""};
        d1.runObservations(vals3);
        System.out.println(d1);

        d1 = new ColumnType("d1");
        String[] vals4 = {"-1234", "-12345", "-123456", ""};
        d1.runObservations(vals4);
        System.out.println(d1);
        
        System.out.println(d1.bumpUp(2));
        System.out.println(d1.bumpUp(10));
        System.out.println(d1.bumpUp(20));
        System.out.println(d1.bumpUp(100));
    }
}
