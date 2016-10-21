package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ReadFromCSV implements Iterable<String[]>
{
    String zipFileName;
    String fileName;
    BufferedReader reader;
    CSVParser csvParser;
    ZipFile zipFile;
    ZipEntry entry;
    boolean isDirectory;
    String type;
    InputStream stream;
    Iterator<CSVRecord> iter;
    
    public ReadFromCSV(String zipFileName, String fileName) throws IOException
    {
        super();
        this.zipFileName = zipFileName;
        this.fileName = fileName;
        this.zipFile = new ZipFile(zipFileName);
        this.entry = zipFile.getEntry(fileName);
        readyForReadZip();
    }

    public ReadFromCSV(String fileName) throws IOException
    {
        super();
        this.fileName = fileName;
        readyForRead();
    }

    public void readyForRead() throws IOException
    {
        closeAll();
        this.isDirectory = new File(fileName).isDirectory();
        this.type = fileName.endsWith(".gz") ? "gz" : "regular";
        openFile();
        csvParser = new CSVParser(reader, CSVFormat.RFC4180);
        iter = csvParser.iterator();
    }

    public void readyForReadZip() throws IOException
    {
        closeAll();
        this.isDirectory = entry.isDirectory();
        this.type = entry.getName().endsWith(".gz") ? "gz" : "regular";
        openFile();
        csvParser = new CSVParser(reader, CSVFormat.RFC4180);
        iter = csvParser.iterator();
    }
    
    public void openFile() throws IOException
    {
        if (isDirectory)
        {
            return;
        }
        if (type.equals("gz"))
        {
            openGZFile();
        }
        else
        {
            openRegularFile();
        }
    }
    
    public void openStream() throws IOException
    {
        if (zipFile != null)
        {
            stream = zipFile.getInputStream(entry);
        }
        else
        {
            stream = new FileInputStream(fileName);
        }
    }

    public void openStreamGZ() throws IOException
    {
        if (zipFile != null)
        {
            stream = new GZIPInputStream(zipFile.getInputStream(entry));
        }
        else
        {
            stream = new GZIPInputStream(new FileInputStream(fileName));
        }
    }
    
    public void openGZFile() throws IOException
    {
        openStreamGZ();
        reader = new BufferedReader(new InputStreamReader(stream));
    }
    
    public void openRegularFile() throws IOException
    {
        openStream();
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    public void closeAll()
    {
        Util.closeIO(csvParser);
        Util.closeIO(stream);
        Util.closeIO(reader);
    }
    
    @Override
    public Iterator<String[]> iterator()
    {
        return new Iterator<String[]>() {
            
            @Override
            public boolean hasNext()
            {
                if (isDirectory)
                {
                    return false;
                }
                boolean hasNext = iter.hasNext();
                if (! hasNext)
                {
                    closeAll();
                }
                return hasNext;
            }            
            @Override
            public String[] next()
            {
                CSVRecord nextRecord = iter.next();
                Iterator<String> valuesIter = nextRecord.iterator();
                ArrayList<String> valuesList = new ArrayList<String>();
                while (valuesIter.hasNext())
                {
                    valuesList.add(valuesIter.next());
                }
                String[] fields = new String[valuesList.size()];
                valuesList.toArray(fields);
                return fields;
            }
        };
    }

}
