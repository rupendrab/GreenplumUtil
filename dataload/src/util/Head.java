package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Head
{
    public static void getHead(String fileName, int lines, String outFileName) throws IOException
    {
        int lineNo = 0;
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(outFileName)));
        for (String line : new ReadFile(fileName))
        {
            lineNo += 1;
            if (lineNo <= lines)
            {
                out.write(line);
                out.write("\n");
            }
            else
            {
                break;
            }
        }
        Util.closeIO(out);
    }
    
    public static void main(String[] args) throws Exception
    {
        getHead("C:/tmp/workspace.GCI_Hierarchy_1476966783832.txt", 100, "C:/tmp/gci_head.txt");
    }

}
