package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import value.LoadInput;
import value.LoadStatus;

public class LoadLog
{
    private String logFile;
    private Hashtable<LoadInput, LoadStatus> data;
    
    public LoadLog(String logFile)
    {
        super();
        this.logFile = logFile;
        readFile();
    }
    
    private synchronized void readFile()
    {
        data = new Hashtable<LoadInput, LoadStatus>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(new File(logFile)));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                LoadStatus loadStatus = LoadStatus.fromJson(line);
                data.put(loadStatus.getInput(), loadStatus);
            }
        }
        catch (IOException ioe)
        {
        }
        finally
        {
            Util.closeIO(reader);
        }
    }
    
    public synchronized boolean isProcessed(LoadInput inp)
    {
        LoadStatus loadStatus = data.get(inp);
        if (loadStatus == null)
        {
            return false;
        }
        else
        {
            return loadStatus.isSuccess();
        }
    }
    
    public synchronized void addLog(LoadStatus loadStatus) throws IOException
    {
        data.put(loadStatus.getInput(), loadStatus);
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(new File(logFile), true)); 
            writer.write(loadStatus.toJSON().toString());
            writer.write("\r\n");
        }
        finally
        {
            Util.closeIO(writer);
        }
    }

}
