package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import db.GreenplumConnection;
import value.LoadInput;

public class LoadParallel
{
    String confDir;
    String loadLogFile;
    String inputDir;
    String fileNamePattern;
    String entryPattern;
    String delimiter;
    LoadLog loadLog;
    
    // Define Loader workers, threads and queues
    
    Hashtable<String, DataLoader> loaders = new Hashtable<String, DataLoader>();
    Hashtable<String, ConcurrentLinkedQueue<LoadInput>> queues = new Hashtable<String, ConcurrentLinkedQueue<LoadInput>>();
    Hashtable<String, Thread> threads = new Hashtable<String, Thread>();

    public LoadParallel(String confDir, String loadLogFile, String inputDir, String fileNamePattern, String entryPattern, String delimiter)
    {
        super();
        this.confDir = confDir;
        this.loadLogFile = loadLogFile;
        this.inputDir = inputDir;
        this.fileNamePattern = fileNamePattern;
        this.entryPattern = entryPattern == null ? "*" : entryPattern;
        this.delimiter = delimiter;
        this.loadLog = new LoadLog(loadLogFile);
    }
    
    public void process(GreenplumConnection con) throws Exception
    {
        ArrayList<String> filesToLoad = Util.getFiles(inputDir, false, fileNamePattern);
        
        Pattern ep = Pattern.compile("^" + entryPattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*") + "$");
        
        for (String fileNameToLoad : filesToLoad)
        {
            boolean isZipFile = fileNameToLoad.endsWith(".zip");
            if (isZipFile)
            {
                ZipEntry[] entries = Util.getZipEntries(fileNameToLoad);
                for (ZipEntry entry : entries)
                {
                    String fileName = entry.getName();
                    if (! ep.matcher(fileName).find())
                    {
                        continue;
                    }
                    String fileType = Util.getFileType(fileName);
                    ConcurrentLinkedQueue<LoadInput> queue = queues.get(fileType);
                    if (queue == null)
                    {
                        queue = new ConcurrentLinkedQueue<LoadInput>();
                        queues.put(fileType, queue);
                    }
                    LoadInput loadInput = new LoadInput(null, fileNameToLoad, fileName, delimiter);
                    queue.add(loadInput);
                    DataLoader dataLoader = loaders.get(fileType);
                    if (dataLoader == null)
                    {
                        dataLoader = new DataLoader(confDir, con, loadLog, queue);
                        Thread t = new Thread(dataLoader);
                        threads.put(fileType, t);
                    }
                }
            }
            else
            {
                File f = new File(fileNameToLoad);
                String fileName = f.getName();
                String dirName = f.getParentFile().getAbsolutePath();
                if (! ep.matcher(fileName).find())
                {
                    continue;
                }
                String fileType = Util.getFileType(fileName);
                ConcurrentLinkedQueue<LoadInput> queue = queues.get(fileType);
                if (queue == null)
                {
                    queue = new ConcurrentLinkedQueue<LoadInput>();
                    queues.put(fileType, queue);
                }
                LoadInput loadInput = new LoadInput(dirName, null, fileName, delimiter);
                queue.add(loadInput);
                DataLoader dataLoader = loaders.get(fileType);
                if (dataLoader == null)
                {
                    dataLoader = new DataLoader(confDir, con, loadLog, queue);
                    Thread t = new Thread(dataLoader);
                    threads.put(fileType, t);
                }
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
        String confDirNew = Util.readFromUser(String.format("Conf dir (default: %s)", confDir));
        if (confDirNew != null && ! confDirNew.trim().equals(""))
        {
            confDir = confDirNew;
        }
        
        String inputDir = "\\\\vaswapp083p\\RRP\\QFC\\BOA\\";
        String inputDirNew = Util.readFromUser(String.format("Input dir (default: %s)", inputDir));
        if (inputDirNew != null && ! inputDirNew.trim().equals(""))
        {
            inputDir = inputDirNew;
        }
        
        String filePattern = "A1_A2*.zip";
        // String filePattern = "A1_A2*09302011.zip";
        String filePatternNew = Util.readFromUser(String.format("File Pattern (default: %s)", filePattern));
        if (filePatternNew != null && ! filePatternNew.trim().equals(""))
        {
            filePattern = filePatternNew;
        }
        
        
        String loadLogFile = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\loadlog.log";
        String loadLogFileNew = Util.readFromUser(String.format("Load Log File (default: %s)", loadLogFile));
        if (loadLogFileNew != null && ! loadLogFileNew.trim().equals(""))
        {
            loadLogFile = loadLogFileNew;
        }
        
        // String entryPattern = "GCI_Hierarchy*";
        String entryPattern = "*";
        String entryPatternNew = Util.readFromUser(String.format("Entry Pattern (default: %s)", entryPattern));
        if (entryPatternNew != null && ! entryPatternNew.trim().equals(""))
        {
            entryPattern = entryPatternNew;
        }
        
        String delimiter = null;
        // String delimiter = "\\|";
        String delimiterNew = Util.readFromUser(String.format("Delimiter (default: %s)", delimiter));
        if (delimiterNew != null && ! delimiterNew.trim().equals(""))
        {
            delimiter = delimiterNew;
        }
        
        /*
        System.out.println("Conf dir = " + confDir);
        System.out.println("Input dir = " + inputDir);
        System.out.println("File Pattern = " + filePattern);
        System.out.println("Load Log File = " + loadLogFile);
        System.out.println("Entry Pattern = " + entryPattern);
        System.out.println("Delimiter = " + delimiter);
        System.exit(0);
        */

        LoadParallel loadParallel = new LoadParallel(confDir, loadLogFile, inputDir, filePattern, entryPattern, delimiter);
        String password = System.getenv("auth");
        GreenplumConnection con = new GreenplumConnection("vaslgpm001p", "5432", "gpprod", "rubandyopadhyay", password);
        loadParallel.process(con);
        con.closeAll();
    }
}
