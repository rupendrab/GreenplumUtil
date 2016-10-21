package test;

import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;

import db.GreenplumConnection;
import util.DataLoader;
import util.LoadLog;
import util.Util;
import value.LoadInput;

public class LoadTestParallel
{
    String confDir;
    String zipFileName;
    LoadLog loadLog;
    
    public LoadTestParallel(String confDir, String zipFileName)
    {
        super();
        this.confDir = confDir;
        this.zipFileName = zipFileName;
    }
    
    public void process(GreenplumConnection con) throws Exception
    {
        loadLog = new LoadLog("C:\\Users\\rubandyopadhyay\\Documents\\QFC\\loadlog.log");
        ZipEntry[] entries = Util.getZipEntries(zipFileName);
        
        Hashtable<String, DataLoader> loaders = new Hashtable<String, DataLoader>();
        Hashtable<String, ConcurrentLinkedQueue<LoadInput>> queues = new Hashtable<String, ConcurrentLinkedQueue<LoadInput>>();
        Hashtable<String, Thread> threads = new Hashtable<String, Thread>();

        for (ZipEntry entry : entries)
        {
            String fileName = entry.getName();
            String fileType = Util.getFileType(fileName);
            ConcurrentLinkedQueue<LoadInput> queue = queues.get(fileType);
            if (queue == null)
            {
                queue = new ConcurrentLinkedQueue<LoadInput>();
                queues.put(fileType, queue);
            }
            LoadInput loadInput = new LoadInput(null, zipFileName, fileName, null);
            queue.add(loadInput);
            DataLoader dataLoader = loaders.get(fileType);
            if (dataLoader == null)
            {
                dataLoader = new DataLoader(confDir, con, loadLog, queue);
                Thread t = new Thread(dataLoader);
                threads.put(fileType, t);
            }
        }
        for (Entry<String, Thread> e : threads.entrySet())
        {
            e.getValue().start();
        }
        for (Entry<String, Thread> e : threads.entrySet())
        {
            e.getValue().join();
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        String confDir = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\BOA";
        String boaDir = "\\\\vaswapp083p\\RRP\\QFC\\BOA\\";
        String zipFileName = boaDir + "A1_A2_GCI_Hierarchy_01252013.zip";
        LoadTestParallel loadTestParallel = new LoadTestParallel(confDir, zipFileName);
        String password = System.getenv("auth");
        GreenplumConnection con = new GreenplumConnection("vaslgpm001p", "5432", "gpprod", "rubandyopadhyay", password);
        loadTestParallel.process(con);
        con.closeAll();
    }
}
