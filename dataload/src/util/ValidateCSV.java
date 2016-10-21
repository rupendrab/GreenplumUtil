package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ValidateCSV
{
    static Pattern fileNamePattern = Pattern.compile("^(.*?)(\\.[^.]*)?$");
    
    public static String getFixFileName(String fileName)
    {
        String baseName = new File(fileName).getName();
        Matcher m = fileNamePattern.matcher(baseName);
        if (m.find())
        {
            return m.group(1) + "_fix" + (m.group(2) == null ? "" : m.group(2));
        }
        return null;
    }
    
    public static void getInvalidLines(String fileName, String outputDir) throws IOException
    {
        int lineNo = 0;
        int noInvalid = 0;
        File outFile = new File(outputDir + "/" + getFixFileName(fileName));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
        for(String line : new ReadFile(fileName))
        {
            lineNo += 1;
            ArrayList<String> fields = new ArrayList<String>();
            boolean ok = Util.readCSVLine(line, fields);
            if (! ok)
            {
                out.write(String.format("%10d: %s", lineNo, line));
                out.write("\r\n");
                noInvalid++;
            }
        }
        Util.closeIO(out);
        
        if (noInvalid > 0)
        {
            System.out.println(String.format("Number of invalid lines for file %s = %d", fileName, noInvalid));
        }
        else
        {
            outFile.delete();
        }
    }
    
    public static void getInvalidLines(String zipFileName, String fileName, String outputDir) throws IOException
    {
        int lineNo = 0;
        int noInvalid = 0;
        File outFile = new File(outputDir + "/" + getFixFileName(fileName));
        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
        for(String line : new EntryReader(zipFileName, fileName))
        {
            lineNo += 1;
            ArrayList<String> fields = new ArrayList<String>();
            boolean ok = Util.readCSVLine(line, fields);
            if (! ok)
            {
                out.write(String.format("%10d: %s", lineNo, line));
                out.write("\r\n");
                noInvalid++;
            }
        }
        Util.closeIO(out);
        
        if (noInvalid > 0)
        {
            System.out.println(String.format("Number of invalid lines for file %s = %d", fileName, noInvalid));
        }
        else
        {
            outFile.delete();
        }
    }

    public static void processFiles(String dirName, String filePattern, String outputDir) throws IOException
    {
        ArrayList<String> fileNames = Util.getFiles(dirName, false, filePattern);
        
        for (String fileName : fileNames)
        {
            System.out.println(String.format("Processing file %s", fileName));
            getInvalidLines(fileName, outputDir);
        }
    }
    
    public static void processZipFiles(String dirName, String zipFilePattern, String entryPattern, String outputDir) throws IOException
    {
        ArrayList<String> zipFileNames = Util.getFiles(dirName, false, zipFilePattern);
        Pattern ep = Pattern.compile("^" + entryPattern.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*") + "$");
        for (String zipFileName : zipFileNames)
        {
            for (ZipEntry entry : Util.getZipEntries(zipFileName))
            {
                String fileName = entry.getName();
                if (ep.matcher(fileName).find())
                {
                    System.out.println(String.format("Processing file %s : %s", zipFileName, fileName));
                    getInvalidLines(zipFileName, fileName, outputDir);
                }
            }
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        String dirName = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\Fixed";
        String filePattern = "GCI_Hierarchy_*.txt";
        String outputDir = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\Fixed";

        processFiles(dirName, filePattern, outputDir);
        
        /*
        dirName = "\\\\vaswapp083p\\RRP\\QFC\\BOA_Arch";
        String zipFilePattern = "A1_A2_GCI_Hierarchy_*.zip";
        String entryPattern = "GCI_Hierarchy_*.txt";
        processZipFiles(dirName, zipFilePattern, entryPattern, outputDir);
        */
    }

}
