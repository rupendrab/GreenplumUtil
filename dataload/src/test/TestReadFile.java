package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import util.EntryReader;
import util.ReadFile;
import util.ReadFromCSV;
import util.Util;

public class TestReadFile
{
    public void test01(String fileName) throws IOException
    {
        int noLines = 0;
        for (String line: new ReadFile(fileName))
        {
            noLines++;
            System.out.println(line);
        }
        System.out.println("Number of lines read = " + noLines);
    }
    
    public <T> String toString(T val)
    {
        if (val instanceof String[])
        {
            return Arrays.toString((String[]) val);
        }
        return val.toString();
    }
    
    public <T> void read(Iterable<T> reader, int head, int tail)
    {
        int noLines = 0;
        LinkedList<T> tailLines = new LinkedList<T>();
        for (T line: reader)
        {
            noLines++;
            if (noLines <= head)
            {
                System.out.println(String.format("(%d)\t%s", noLines, toString(line)));
            }
            else
            {
                tailLines.push(line);
                if (tailLines.size() > tail)
                {
                    tailLines.removeLast();
                }
            }
        }
        for (int i=0; i<3; i++)
        {
            System.out.println("....");
        }
        int n = tailLines.size();
        for (int i=0; i<n; i++)
        {
            System.out.println(String.format("(%d)\t%s", noLines - n + i + 1, toString(tailLines.removeLast())));
        }
        System.out.println("Number of lines read = " + noLines);
    }
    

    public void test02(String fileName, int head, int tail) throws IOException
    {
        read(new ReadFile(fileName), head, tail);
    }
    
    public void test03(String zipFileName, String fileName, int head, int tail) throws IOException
    {
        read(new EntryReader(zipFileName, fileName), head, tail);
    }

    public void test_csv(String fileName) throws IOException
    {
        BufferedReader reader = null;
        CSVParser cp= null;
        try
        {
            reader = new BufferedReader(new FileReader(new File(fileName)));
            cp = new CSVParser(reader, CSVFormat.RFC4180);
            int lineNo = 0;
            for (CSVRecord record : cp)
            {
                lineNo ++;
                System.out.println(record);
                if (lineNo >= 10)
                {
                    break;
                }
            }
        }
        finally
        {
            Util.closeIO(cp);
            Util.closeIO(reader);
        }
    }
    
    public void test_csv_02(String zipFileName, String fileName, int head, int tail) throws IOException
    {
        read(new ReadFromCSV(zipFileName, fileName), head, tail);
    }
    
    public void test_csv_03(String fileName, int head, int tail) throws IOException
    {
        read(new ReadFromCSV(fileName), head, tail);
    }

    public static void main(String[] args) throws Exception
    {
        TestReadFile tst = new TestReadFile();
        // tst.test01("C:\\Users\\rubandyopadhyay\\Documents\\Python\\sk-04.gz");
        // tst.test02("\\\\vaswapp083p\\RRP\\QFC\\CITI\\ce_fdic_20110531_collateral.gz",  10,  10);
        // tst.test02("\\\\vaswapp083p\\RRP\\QFC\\CITI\\ce_fdic_a1_20110531.gz",  10,  10);
        // tst.test03("\\\\vaswapp083p\\RRP\\QFC\\BOA\\A1_A2_GCI_Hierarchy_01252013.zip", "FDIC_A1_01252013.txt", 10, 10);
        // tst.test_csv("\\\\vaswapp083p\\RRP\\QFC\\BOA\\A1_A2_GCI_Hierarchy_01252013\\FDIC_A1_01252013.txt");
        // tst.test_csv_02("\\\\vaswapp083p\\RRP\\QFC\\BOA\\A1_A2_GCI_Hierarchy_01252013.zip", "FDIC_A1_01252013.txt", 10, 10);
        // tst.test_csv_03("\\\\vaswapp083p\\RRP\\QFC\\BOA\\A1_A2_GCI_Hierarchy_01252013\\FDIC_A1_01252013.txt", 10, 10);
        tst.test02("c:/tmp/workspace.FDIC_A1_1476805355446.txt", 10, 10);
    }

}
