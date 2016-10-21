package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class HeadTail
{
    public static void getHeadTail(String fileName, int startLine, int endLine, String outFileName) throws IOException
    {
        int lineNo = 0;
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFileName)));
        for (String line : new ReadFile(fileName))
        {
            lineNo += 1;
            if (lineNo >= startLine && lineNo <= endLine)
            {
                out.write(line);
                out.write("\r\n");
                System.out.println(lineNo + " : " + line);
                /*
                String[] fields = line.split("\t");
                System.out.println(Arrays.toString(fields));
                System.out.println(String.format("'%s' '%s'", fields[6], fields[6].replaceAll("^\\s+", "")));
                for (char c: fields[6].toCharArray())
                {
                    System.out.println(String.format("%c = %d", c, (int) c));
                }
                */
            }
            else if (lineNo > endLine)
            {
                break;
            }
        }
        Util.closeIO(out);
    }
    
    public static void getHeadTail(String zipFileName, String fileName, int startLine, int endLine, String outFileName) throws IOException
    {
        int lineNo = 0;
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFileName)));
        for (String line : new EntryReader(zipFileName, fileName))
        {
            lineNo += 1;
            if (lineNo >= startLine && lineNo <= endLine)
            {
                out.write(line);
                out.write("\r\n");
                System.out.println(lineNo + " : " + line);
            }
            else if (lineNo > endLine)
            {
                break;
            }
        }
        Util.closeIO(out);
    }

    public static void main(String[] args) throws Exception
    {
        // getHeadTail("C:/tmp/workspace.GCI_Hierarchy_1476984869758.txt", 4357209, 4357211,"C:/tmp/gci_head.txt");
        // getHeadTail("\\\\vaswapp083p\\RRP\\QFC\\BOA\\A1_A2_GCI_Hierarchy_10282011.zip", "GCI_Hierarchy_10282011.txt", 126348, 126368, "c:/tmp/x.txt");
        // getHeadTail("\\\\vaswapp083p\\RRP\\QFC\\BOA\\A1_A2_GCI_Hierarchy_09302011.zip", "GCI_Hierarchy_09302011.txt", 1, 10, "c:/tmp/x.txt");
        // getHeadTail("C:\\Users\\rubandyopadhyay\\Documents\\QFC\\GCI_Hierarchy_10282011.txt", 126348, 126368, "c:/tmp/x.txt");
        
        
        String zipFileName = Util.readFromUser("Zip File Name");
        
        String fileName = Util.readFromUser("File Name");
        
        int startLine = 0;
        int endLine = 0;
        String startLineStr = Util.readFromUser("Range Start Line");
        
        try
        {
            startLine = Integer.parseInt(startLineStr);
        }
        catch (Exception e)
        {
            System.err.println("Invalid start line !!!");
            System.exit(1);
        }

        String endLineStr = Util.readFromUser("Range End Line");
        try
        {
            endLine = Integer.parseInt(endLineStr);
        }
        catch (Exception e)
        {
            System.err.println("Invalid start line !!!");
            System.exit(1);
        }
        
        String outFileName = "C:/tmp/x.txt";
        String outFileNameNew = Util.readFromUser(String.format("Output File (default: %s)", outFileName));
        if (outFileNameNew != null && ! outFileNameNew.trim().equals(""))
        {
            outFileName = outFileNameNew;
        }
        
        if (zipFileName == null || zipFileName.trim().equals(""))
        {
            getHeadTail(fileName, startLine, endLine, outFileName);
        }
        else
        {
            getHeadTail(zipFileName, fileName, startLine, endLine, outFileName);
        }
        
    }

}
