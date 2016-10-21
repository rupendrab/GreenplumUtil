package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ReadZipfile
{
    String zipfileName;
    ZipFile zipFile;
    Enumeration<? extends ZipEntry> entries;
    ZipEntry[] entryArr;
    
    Pattern ints = Pattern.compile("[0-9]+");

    public ReadZipfile(String zipfileName) throws IOException
    {
        super();
        this.zipfileName = zipfileName;
        this.zipFile = new ZipFile(this.zipfileName);
        this.entries = zipFile.entries();
        ArrayList<ZipEntry> entryList = new ArrayList<ZipEntry>();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            entryList.add(entry);
            System.out.println(entry.getName() + " : " + entry.getClass().getName() + " : " + getFileType(entry));
        }
        this.entryArr = new ZipEntry[entryList.size()];
        entryList.toArray(this.entryArr);
    }

    public String getFileType(ZipEntry entry)
    {
        if (entry.isDirectory())
        {
            return "d";
        }
        return Util.getFileType(entry.getName());
    }
    
    public String getZipfileName()
    {
        return zipfileName;
    }

    public void setZipfileName(String zipfileName)
    {
        this.zipfileName = zipfileName;
    }

    private ZipEntry getEntry(String fileName)
    {
        for (ZipEntry entry: entryArr)
        {
            if (entry.getName().equals(fileName))
            {
                return entry;
            }
        }
        return null;
    }
    
    int getLines(byte[] buffer, int buflen, ArrayList<String> lines, boolean endLine)
    {
        return 0;
    }
    
    public void readZippedOne(String fileName) throws IOException
    {
        if (!fileName.endsWith(".gz"))
            return;
        ZipEntry entry = getEntry(fileName);
        if (entry == null)
        {
            System.err.println("No such file : " + fileName);
            return;
        }
        int noLines = 0;
        EntryReader reader = new EntryReader(zipFile, entry);
        for (String line : reader)
        {
            noLines += 1;
            if (noLines <= 10)
            {
                System.out.println(line);
            }
        }
        System.out.println("Number of lines = " + noLines);
    }

    public static void main(String[] args) throws Exception
    {
        String zipfileName = "\\\\vaswapp083p\\RRP\\QFC\\CITI\\CitiHistorical.zip";
        ReadZipfile rzf = new ReadZipfile(zipfileName);
        System.out.println(Arrays.toString(rzf.entryArr));
        rzf.readZippedOne("CitiHistorical/ce_fdic_20110531_collateral.gz");
    }

}
