package test;

import java.io.IOException;
import java.util.ArrayList;

import util.ReadFile;
import util.Util;

public class TestCSVParser
{

    public static void test01(String fileName, int lines) throws IOException
    {
        int lineNo = 0;
        for(String line : new ReadFile(fileName))
        {
            lineNo += 1;
            if (lineNo > lines)
            {
                break;
            }
            System.out.println(line);
            ArrayList<String> fields = new ArrayList<String>();
            boolean ok = Util.readCSVLine(line, fields);
            System.out.println(ok + " : " + fields);
        }
    }
    
    public static void getInvalidLines(String fileName) throws IOException
    {
        int lineNo = 0;
        int noInvalid = 0;
        for(String line : new ReadFile(fileName))
        {
            lineNo += 1;
            ArrayList<String> fields = new ArrayList<String>();
            boolean ok = Util.readCSVLine(line, fields);
            if (! ok)
            {
                System.out.println(String.format("%10d: %s", lineNo, line));
                noInvalid++;
            }
        }
        System.out.println("Number of invalid lines = " + noInvalid);
    }
    
    public static void main(String[] args) throws Exception
    {
        // test01("C:\\Users\\rubandyopadhyay\\Documents\\QFC\\GCI_Hierarchy_10282011.txt", 1);
        String fileName = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\GCI_Hierarchy_10282011.txt";
        getInvalidLines(fileName);
    }

}
