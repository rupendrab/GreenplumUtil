package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TreeSet;
import java.util.regex.Pattern;

import value.HeaderInfo;

public class AnalUtil
{
    public static String getFileSummary(String fileName) throws IOException
    {
        String fileType = Util.getFileType(fileName);
        ReadFile r = new ReadFile(fileName);
        String out = "";
        String[] data = new String[4];
        int noLines = 0;
        for (String line : r)
        {
            noLines++;
            if (noLines == 1)
            {
                data[0] = fileName; 
                data[1] = fileType;
                data[3] = line;
            }
            data[2] = "" + noLines;
        }
        out = Util.getTsvLine(data);
        return out;
    }
    
    public static String createRegexFromGlob(String glob)
    {
        String out = "^";
        for(int i = 0; i < glob.length(); ++i)
        {
            final char c = glob.charAt(i);
            switch(c)
            {
            case '*': out += ".*"; break;
            case '?': out += '.'; break;
            case '.': out += "\\."; break;
            case '\\': out += "\\\\"; break;
            default: out += c;
            }
        }
        out += '$';
        return out;
    }
    
    public static String[] getAllFiles(String dir, String filePattern)
    {
        File fd = new File(dir);
        if (! fd.exists() || ! fd.isDirectory())
        {
            return new String[0];
        }
        String rPattern = createRegexFromGlob(filePattern);

        return fd.list(new FilenameFilter() {
            
            @Override
            public boolean accept(File d, String fileName)
            {
                return Pattern.matches(rPattern, fileName);
            }
        });
    }
    
    public static void showFileSummary(String dir, String filePattern, String outFile) throws IOException
    {
        BufferedWriter out = null;
        if (outFile != null)
        {
            out = new BufferedWriter(new FileWriter(new File(outFile)));
        }
        for (String fileName : getAllFiles(dir, filePattern))
        {
            String data = getFileSummary(dir + "/" + fileName);
            if (out == null)
            {
                System.out.println(data);
            }
            else
            {
                out.write(data);
                out.write("\r\n");
            }
        }
        if (out != null)
        {
            out.close();
        }
    }
    
    public static void analyzeHeaders(String fileName) throws IOException
    {
        Hashtable<HeaderInfo, TreeSet<String>> anal = new Hashtable<>();
        for (String line : new ReadFile(fileName))
        {
            String[] fields = line.split("\t");
            HeaderInfo key = new HeaderInfo(fields[1], fields[3]);
            TreeSet<String> t = anal.get(key);
            if (t != null)
            {
                t.add(fields[0]);
            }
            else
            {
                t = new TreeSet<String>();
                t.add(fields[0]);
                anal.put(key,  t);
            }
        }
        HeaderInfo[] keys = new HeaderInfo[anal.size()];
        anal.keySet().toArray(keys);
        Arrays.sort(keys);
        for (HeaderInfo key : keys)
        {
            System.out.println(key.getType() + "\t" + key.getHeader());
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        System.out.println("Started at: " + Calendar.getInstance().getTime());
        String citiDir = "\\\\vaswapp083p\\RRP\\QFC\\CITI\\";
        System.out.println(getFileSummary(citiDir + "ce_fdic_a1_20110531.gz"));
        /*
        String[] files = getAllFiles("\\\\vaswapp083p\\RRP\\QFC\\CITI", "*.gz");
        for (String file : files)
        {
            System.out.println(file);
        }
        */
        /*
        showFileSummary(
                "\\\\vaswapp083p\\RRP\\QFC\\CITI", 
                "*.gz", 
                "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\Citi_File_Summary.txt");
        */
        /*
        showFileSummary(
                "\\\\vaswapp083p\\RRP\\QFC\\CITI", 
                "*.gz", 
                null);
        System.out.println("Done");
        */
        // analyzeHeaders("C:\\Users\\rubandyopadhyay\\Documents\\QFC\\Citi_File_Summary.txt");
        System.out.println("Completed at: " + Calendar.getInstance().getTime());
    }

}
