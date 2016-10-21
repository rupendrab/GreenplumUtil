package test;

import java.util.ArrayList;


import util.Util;

public class TestFiles
{
    public static void main(String[] args)
    {
        ArrayList<String> files = Util.getFiles("\\\\vaswapp083p\\RRP\\QFC\\BOA", false, "A1_A2*.zip");
        int noFiles = 0;
        for (String file : files)
        {
            System.out.println(file);
            noFiles++;
        }
        System.out.println("Number of files = " + noFiles);
        System.out.println(Util.getFileType("A1_FDIC_02242012.txt"));
        System.out.println(Util.getFileType("A2_FDIC_02242012.txt"));
        System.out.println(Util.getFileType("FDIC_A1_02242012.txt"));
        System.out.println(Util.getFileType("FDIC_A2_02242012.txt"));
    }

}
