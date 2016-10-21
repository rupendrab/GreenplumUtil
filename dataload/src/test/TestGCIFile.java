package test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.ReadFile;

public class TestGCIFile
{
    public static Pattern intPattern = Pattern.compile("^[0-9]+$");
    
    public static void process(String fileName) throws IOException
    {
        int nullCnt = 0;
        int lineNo = 0;
        for (String line : new ReadFile(fileName))
        {
            lineNo += 1;
            String[] fields = line.split("\\t");
            String copId = fields[6];
            if (copId.equals("\\N"))
            {
                nullCnt += 1;
                continue;
            }
            if (copId.equals("N"))
            {
                System.out.println(lineNo + " : " + line);
                break;
            }
            else
            {
                Matcher m = intPattern.matcher(copId);
                if (! m.find())
                {
                    System.out.println(lineNo + " : " + line);
                    break;
                }
            }
        }
        System.out.println("Done, Read = " + lineNo + ", Nulls = " + nullCnt);
    }
    
    public static void main(String[] args) throws Exception
    {
        process("C:/tmp/workspace.GCI_Hierarchy_1476966783832.txt");
    }

}
