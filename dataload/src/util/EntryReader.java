package util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EntryReader implements Iterable<String>
{
    ZipFile zipFile;
    ZipEntry entry;
    String type;
    boolean isDirectory;
    BufferedReader reader;
    InputStream stream;
    String nextLine;
    
    public EntryReader(ZipFile zipFile, ZipEntry entry) throws IOException
    {
        super();
        this.zipFile = zipFile;
        this.entry = entry;
        commonConstructor();
    }
    
    public EntryReader(String zipFileName, String fileName) throws IOException
    {
        this.zipFile = new ZipFile(zipFileName);
        this.entry = zipFile.getEntry(fileName);
        commonConstructor();
    }
    
    private void commonConstructor() throws IOException
    {
        this.isDirectory = entry.isDirectory();
        this.type = entry.getName().endsWith(".gz") ? "gz" : "regular";
        openFile();
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
    
    public void openGZFile() throws IOException
    {
        stream = new GZIPInputStream(zipFile.getInputStream(entry));
        reader = new BufferedReader(new InputStreamReader(stream));
    }
    
    public void close(Closeable f)
    {
        try
        {
            if (f != null)
            {
                f.close();
            }
        }
        catch (IOException e)
        {
        }
    }
    
    public void closeAll()
    {
        close(stream);
        close(reader);
    }
    
    public void openRegularFile() throws IOException
    {
        stream = zipFile.getInputStream(entry);
        reader = new BufferedReader(new InputStreamReader(stream));
    }

    @Override
    public Iterator<String> iterator()
    {
        return new Iterator<String>() {
            @Override
            public boolean hasNext()
            {
                if (isDirectory)
                {
                    return false;
                }
                try
                {
                    if (! reader.ready())
                    {
                        return false;
                    }
                    nextLine = reader.readLine();
                    if (nextLine == null)
                    {
                        closeAll();
                        return false;
                    }
                    return true;
                }
                catch (IOException ioe)
                {
                    return false;
                }
            }            
            @Override
            public String next()
            {
                return nextLine;
            }
        };
    }

}
