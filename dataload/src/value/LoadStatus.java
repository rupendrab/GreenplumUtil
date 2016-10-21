package value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LoadStatus
{
    private LoadInput input;
    private boolean success;
    private String tableName;
    private long loadCount;
    private Exception exception;
    private Date startTime;
    private Date endTime;
    
    private SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
    
    public LoadStatus(LoadInput input)
    {
        super();
        this.input = input;
        setStartTime();
    }

    public LoadInput getInput()
    {
        return input;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public long getLoadCount()
    {
        return loadCount;
    }

    public void setLoadCount(long loadCount)
    {
        this.loadCount = loadCount;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public void setStartTime()
    {
        this.startTime = Calendar.getInstance().getTime();
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }
 
    public void setEndTime()
    {
        this.endTime = Calendar.getInstance().getTime();
    }
    
    public JsonObject toJSON()
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("dirName", input.getDirName());
        obj.addProperty("zipFileName", input.getZipFileName());
        obj.addProperty("fileName", input.getFileName());
        obj.addProperty("delimiter", input.getDelimiter());
        obj.addProperty("success", success);
        obj.addProperty("tableName", tableName);
        obj.addProperty("loadCount", loadCount);
        if (exception != null)
        {
            obj.addProperty("exception", exception.getMessage());
        }
        else
        {
            obj.addProperty("exception", "");
        }
        if (startTime != null)
        {
            obj.addProperty("startTime", fmt.format(startTime));
        }
        if (endTime != null)
        {
            obj.addProperty("endTime", fmt.format(endTime));
        }
        return obj;
    }
    
    public static LoadStatus fromJson(String jsonData)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        Gson gson = new Gson();
        
        JsonObject obj = gson.fromJson(jsonData, JsonObject.class);
        String dirName = obj.get("dirName").isJsonNull() ? null : obj.get("dirName").getAsString();
        String zipFileName = obj.get("zipFileName").isJsonNull() ? null : obj.get("zipFileName").getAsString();
        String fileName = obj.get("fileName").isJsonNull() ? null : obj.get("fileName").getAsString();
        String delimiter = obj.get("delimiter").isJsonNull() ? null : obj.get("delimiter").getAsString();
        boolean success = obj.get("success").isJsonNull() ? false : obj.get("success").getAsBoolean();
        String tableName = obj.get("tableName").isJsonNull() ? null : obj.get("tableName").getAsString();
        long loadCount = obj.get("loadCount").isJsonNull() ? 0 : obj.get("loadCount").getAsLong();
        
        LoadInput inp = new LoadInput(dirName, zipFileName, fileName, delimiter);
        LoadStatus stat = new LoadStatus(inp);
        
        stat.setSuccess(success);
        stat.setTableName(tableName);
        stat.setLoadCount(loadCount);
        
        JsonElement startTimeElem = obj.get("startTime");
        if (startTimeElem != null)
        {
            try
            {
                Date startTime = fmt.parse(startTimeElem.getAsString());
                stat.setStartTime(startTime);
            }
            catch (ParseException e)
            {
            }
        }

        JsonElement endTimeElem = obj.get("endTime");
        if (endTimeElem != null)
        {
            try
            {
                Date endTime = fmt.parse(endTimeElem.getAsString());
                stat.setEndTime(endTime);
            }
            catch (ParseException e)
            {
            }
        }

        return stat;
    }
    
}
