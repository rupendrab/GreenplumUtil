package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import db.GreenplumConnection;
import util.Util;

public class TestCopy
{
    public static long copyToTable(String tableName, String fileName, GreenplumConnection con) throws SQLException, IOException
    {
        long loaded = 0;
        CopyManager copyManager = new CopyManager((BaseConnection) con.getConnection());
        // BufferedReader dataReader = new BufferedReader(new FileReader(new File(fileName)));
        FileInputStream dataReader = new FileInputStream(new File(fileName));
        // String copyCmd = "COPY " + tableName + " FROM STDIN NEWLINE 'CRLF' FILL MISSING FIELDS";
        String copyCmd = "COPY " + tableName + " FROM STDIN";
        loaded = copyManager.copyIn(copyCmd, dataReader);
        // loaded = copyManager.copyIn();
        Util.closeIO(dataReader);
        return loaded;
    }
    
    public static void main(String[] args) throws Exception, IOException
    {
        String password = System.getenv("auth");
        GreenplumConnection con = new GreenplumConnection("vaslgpm001p", "5432", "gpprod", "rubandyopadhyay", password);
        copyToTable("workspace.gci_hierarchy", "C:/tmp/gci_head.txt", con);
        con.closeAll();
    }

}
