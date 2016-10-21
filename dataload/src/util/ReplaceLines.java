package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReplaceLines
{
    String zipFileName;
    String fileName;
    int startLine;
    int endLine;
    String fixFileName;
    String outFileName;
    Iterable<String> inputReader;
    
    public ReplaceLines(String zipFileName, String fileName, int startLine, int endLine,
            String fixFileName, String outFileName) throws IOException
    {
        super();
        this.zipFileName = zipFileName;
        this.fileName = fileName;
        this.startLine = startLine;
        this.endLine = endLine;
        this.fixFileName = fixFileName;
        this.outFileName = outFileName;
        decideInputReader();
    }
    
    private void decideInputReader() throws IOException
    {
        if (zipFileName != null)
        {
            inputReader = new EntryReader(zipFileName, fileName);
        }
        else
        {
            inputReader = new ReadFile(fileName);
        }
    }
    
    public int process() throws IOException
    {
        int lineNo = 0;
        int linesWritten = 0;
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFileName)));
        boolean replaced = false;
        for (String line : inputReader)
        {
            lineNo += 1;
            if (lineNo < startLine || lineNo > endLine)
            {
                out.write(line);
                out.write("\r\n");
                linesWritten++;
            }
            else
            {
                if (! replaced)
                {
                    for (String newline : new ReadFile(fixFileName))
                    {
                        out.write(newline);
                        out.write("\r\n");
                        linesWritten++;
                    }
                    replaced = true;
                }
            }
        }
        Util.closeIO(out);
        return linesWritten;
    }
    
    public static void main(String[] args) throws Exception
    {
        String zipFileName = null;
        String fileName = null;
        int startLine = 0;
        int endLine = 0;
        String fixFileName = null;
        String outFileName = null;
        
        zipFileName = Util.readFromUser("Zip File Name");
        if (zipFileName.trim().equals(""))
        {
            zipFileName = null;
        }
        
        fileName = Util.readFromUser("File Name");
        
        String startLineStr = Util.readFromUser("Replace Range Start Line");
        try
        {
            startLine = Integer.parseInt(startLineStr);
        }
        catch (Exception e)
        {
            System.err.println("Invalid start line !!!");
            System.exit(1);
        }

        String endLineStr = Util.readFromUser("Replace Range End Line");
        try
        {
            endLine = Integer.parseInt(endLineStr);
        }
        catch (Exception e)
        {
            System.err.println("Invalid start line !!!");
            System.exit(1);
        }
        
        fixFileName = Util.readFromUser("File to fix from");
        outFileName = Util.readFromUser("Output File");
        
        ReplaceLines replaceLines = new ReplaceLines(zipFileName, fileName, startLine, endLine, fixFileName, outFileName);
        System.out.println("Fixing file...");
        int linesWritten = replaceLines.process();
        System.out.println("Fixed. " + linesWritten + " lines written to " + outFileName);
        
        /*
           Zip File Name : \\vaswapp083p\RRP\QFC\BOA\A1_A2_GCI_Hierarchy_10282011.zip
           File Name : GCI_Hierarchy_10282011.txt
           Replace Range Start Line : 126357
           Replace Range End Line : 126357
           File to fix from : C:\Users\rubandyopadhyay\Documents\QFC\fix_01.txt
           Output File : C:\Users\rubandyopadhyay\Documents\QFC\GCI_Hierarchy_10282011.txt
           Fixing file...
           Fixed. 4219618 lines written to C:\Users\rubandyopadhyay\Documents\QFC\GCI_Hierarchy_10282011.txt
         * 
         */
}
    
    
}
