package util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import db.GreenplumConnection;
import value.LoadInput;
import value.LoadStatus;

public class DataLoader implements Runnable
{
    private String confDir;
    private GreenplumConnection con;
    private LoadLog loadLog;
    ConcurrentLinkedQueue<LoadInput> workQueue;

    private String SCHEMANAME = "workspace";
    private String TEMPDIR = "C:/tmp";

    public DataLoader(String confDir, GreenplumConnection con, LoadLog loadLog,
            ConcurrentLinkedQueue<LoadInput> workQueue)
    {
        super();
        this.confDir = confDir;
        this.con = con.cloneConnection();
        this.loadLog = loadLog;
        this.workQueue = workQueue;
    }

    private void sleep(int seconds)
    {
        try
        {
            Thread.sleep(seconds * 1000);
        }
        catch (InterruptedException ie)
        {
        }
    }

    private LoadInput getNextInput()
    {
        for (int i = 0; i < 5; i++)
        {
            LoadInput input = workQueue.poll();
            if (input != null)
            {
                System.out.println("Got input: " + input);
                return input;
            }
            else
            {
                sleep(5);
            }
        }
        return null;
    }

    long processInput(LoadInput input)
    {
        AnalyzeFileColumns afc = null;
        long loaded = 0;
        LoadStatus loadStatus = new LoadStatus(input);
        if (loadLog.isProcessed(input))
        {
            System.out.println(String.format("Already Loaded %s : %s", 
                    input.getZipFileName(),
                    input.getFileName()
                    ));
            return loaded;
        }
        try
        {
            if (input.isZippedInput())
            {
                afc = new AnalyzeFileColumns(input.getZipFileName(), input.getFileName(), input.getDelimiter());
                loaded = afc.loadToTable(con, SCHEMANAME, null, confDir, TEMPDIR);
                System.out.println(String.format("Loaded %s : %s, records = %d", 
                        input.getZipFileName(),
                        input.getFileName(),
                        loaded
                        ));
            }
            else
            {
                String newFileName = new File(input.getDirName() + "/" + input.getFileName()).getAbsolutePath();
                afc = new AnalyzeFileColumns(newFileName, input.getDelimiter());
                loaded = afc.loadToTable(con, SCHEMANAME, null, confDir, TEMPDIR);
                System.out.println(String.format("Loaded %s, records = %d", 
                        newFileName,
                        loaded
                        ));
            }
            loadStatus.setSuccess(true);
            loadStatus.setTableName(Util.getFileType(input.getFileName()));
            loadStatus.setLoadCount(loaded);
        }
        catch (Exception e)
        {
            loadStatus.setSuccess(false);
            loadStatus.setException(e);
        }
        loadStatus.setEndTime();
        try
        {
            loadLog.addLog(loadStatus);
        }
        catch (IOException ioe)
        {
            System.err.println("Error while loading log : " + loadStatus.getInput().toString());
            System.err.println(ioe.getMessage());
            ioe.printStackTrace(System.err);
        }
        return loaded;
    }

    @Override
    public void run()
    {
        LoadInput input = null;
        while ((input = getNextInput()) != null)
        {
            processInput(input);
        }
        con.closeAll();
    }

}
