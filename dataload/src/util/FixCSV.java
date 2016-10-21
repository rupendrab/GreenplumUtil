package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixCSV
{
    String zipFileName;
    String fileName;
    String fixDir;
    String outDir;
    
    Pattern fixPattern = Pattern.compile("^\\s*([0-9]+):\\s(.*)$");

    public FixCSV(String zipFileName, String fileName, String fixDir, String outDir)
    {
        super();
        this.zipFileName = zipFileName;
        this.fileName = fileName;
        this.fixDir = fixDir;
        this.outDir = outDir;
    }
    
    public void process() throws IOException
    {
        String fixFileName = ValidateCSV.getFixFileName(fileName);
        File fixFile = new File(fixDir + "/" + fixFileName);
        
        // Prepare Input Reader
        Iterable<String> reader = null;
        if (zipFileName != null)
        {
            reader = new EntryReader(zipFileName, fileName);
        }
        else
        {
            reader = new ReadFile(fileName);
        }
        
        // Prepare fix data
        Hashtable<Integer, String> fixData = new Hashtable<Integer, String>();
        for (String line : new ReadFile(fixFile.getAbsolutePath()))
        {
            Matcher m = fixPattern.matcher(line);
            if (m.find())
            {
                fixData.put(Integer.parseInt(m.group(1)), m.group(2));
            }
        }
        
        String outFileName = new File(outDir + "/" + fileName).getAbsolutePath();
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFileName)));

        int lineNo = 0;
        int written = 0;
        int fixed = 0;
        for (String line : reader)
        {
            lineNo += 1;
            String newLine = fixData.get(lineNo);
            if (newLine != null)
            {
                out.write(newLine);
                fixed++;
            }
            else
            {
                out.write(line);
            }
            written++;
            out.write("\r\n");
        }
        Util.closeIO(out);
        System.out.println(String.format("Fix file %s : Read = %d, Written = %d, fixed = %d", outFileName, lineNo, written, fixed));
    }
    
    public static void main(String[] args) throws Exception
    {
        String boaDir = "\\\\vaswapp083p\\RRP\\QFC\\BOA";
        String zipFileName = boaDir + "/" + "A1_A2_GCI_Hierarchy_10282011.zip";
        String fileName = "GCI_Hierarchy_10282011.txt";
        String fixDir = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\Fix";
        String outDir = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\Fixed";
        FixCSV fixCSV = new FixCSV(zipFileName, fileName, fixDir, outDir);
        fixCSV.process();
    }
}
