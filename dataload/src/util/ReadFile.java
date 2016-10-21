package util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class ReadFile implements Iterable<String>
{
    String fileName;
    File file;
    String type;
    boolean isDirectory;
    BufferedReader reader;
    InputStream stream;
    String nextLine;
    boolean isClosed = true;

    public ReadFile(String fileName) throws IOException
    {
        super();
        this.fileName = fileName;
        this.file = new File(fileName);
        this.isDirectory = file.isDirectory();
        this.type = file.getName().endsWith(".gz") ? "gz" : "regular";
        openFile();
        isClosed = false;
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
        stream = new GZIPInputStream(new FileInputStream(file));
        reader = new BufferedReader(new InputStreamReader(stream));
    }
    
    public void openRegularFile() throws IOException
    {
        stream = new FileInputStream(file);
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
        // System.err.println("Closing Files");
        close(stream);
        close(reader);
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
                if (isClosed)
                {
                    try
                    {
                        openFile();
                    }
                    catch (IOException ioe)
                    {
                        return false;
                    }
                }
                try
                {
                    if (! reader.ready())
                    {
                        closeAll();
                        return false;
                    }
                    nextLine = reader.readLine();
                    // System.out.println("Read line : " + nextLine);
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
