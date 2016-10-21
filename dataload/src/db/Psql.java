package db;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;

public class Psql
{
    private GreenplumConnection con;
    private Connection conn;
    private Statement stmt;
    private BufferedReader in;

    private static Pattern firstWord = Pattern.compile("(\\w+)");
    private String[] queryCommands = { "select", "show" };

    public Psql(String server, String port, String db, String user) throws SQLException
    {
        String password = System.getenv("auth");
        con = new GreenplumConnection(server, port, db, user, password);
        conn = con.conn;
        stmt = conn.createStatement();
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    private void closeDbObject(AutoCloseable obj)
    {
        if (obj != null)
        {
            try
            {
                obj.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void end()
    {
        System.out.println("Exiting application...");
        closeDbObject(stmt);
        con.closeAll();
    }

    private String getFirstWord(String text)
    {
        Matcher m = firstWord.matcher(text);
        if (m.find())
        {
            return m.group(1);
        }
        return null;
    }

    private int commandType(String text)
    {
        String firstWord = getFirstWord(text);
        if (firstWord == null)
        {
            return 0;
        }
        if (firstWord.equals("\\q"))
        {
            // end();
            System.exit(0);
        }
        for (String q : queryCommands)
        {
            if (q.equalsIgnoreCase(firstWord))
            {
                return 1;
            }
        }
        return 0;
    }

    public boolean runAnyUserSQL(String sql)
    {
        int type = commandType(sql);
        try
        {
            if (type == 1)
            {
                runQuery(sql);
            }
            else
            {
                runCommand(sql);
                System.out.println("OK");
            }
            SQLWarning warn = stmt.getWarnings();
            if (warn != null)
            {
                System.err.println(warn.getMessage());
            }
        }
        catch (SQLException se)
        {
            System.err.println(se.getErrorCode() + " : " + se.getMessage());
            return false;
        }
        return true;
    }

    public void runCommand(String sql) throws SQLException
    {
        stmt.executeUpdate(sql);
    }

    public void runQuery(String sql) throws SQLException
    {
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery(sql);
            formattedOutput(rs);
            /*
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            while (rs.next())
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= cols; i++)
                {
                    sb.append(rs.getString(i));
                    sb.append("\t");
                }
                System.out.println(sb.toString());
            }
            */
        }
        finally
        {
            closeDbObject(rs);
        }
    }

    public void addQuotes(String line, int[] quotes)
    {
        if (line != null)
        {
            for (char c : line.toCharArray())
            {
                switch (c)
                {
                case '"':
                    quotes[0]++;
                    break;
                case '\'':
                    quotes[1]++;
                    break;
                }
            }
        }
    }
    
    public String acceptCommand() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        System.out.print("psql> ");
        boolean continueInput = true;
        int[] quotes = {0,0};
        int lineNo = 0;
        while (continueInput)
        {
            String line = in.readLine();
            if (line.trim().equals("\\q"))
            {
                System.exit(0);
            }
            lineNo++;
            if (lineNo == 1)
            {
                if (line.trim().startsWith("\\"))
                {
                    return line.trim();
                }
            }
            sb.append("\n" + line);
            addQuotes(line, quotes);
            boolean okQuotes = (quotes[0] % 2 == 0 && quotes[1] % 2 == 0);
            if (okQuotes && line.trim().endsWith(";"))
            {
                break;
            }
        }
        return sb.toString();
    }

    public ArrayList<String[]> evaluateFirst1000(ResultSet rs, int[] lengths) throws SQLException
    {
        ArrayList<String[]> out = new ArrayList<String[]>();
        int noCols = lengths.length;
        while (rs.next())
        {
            String[] record = new String[noCols];
            for(int i=1; i<=noCols; i++)
            {
                record[i-1] = rs.getString(i);
                if (record[i-1] != null && record[i-1].length() > lengths[i-1])
                {
                    lengths[i-1] = record[i-1].length();
                }
            }
            out.add(record);
        }
        return out;
    }
    
    public String getFormatString(int[] lengths)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int length : lengths)
        {
            sb.append("%-" + (length+1) + "s|");
        }
        return sb.toString();
    }
    
    public void printRecord(String[] columns, String formatString)
    {
        System.out.println(String.format(formatString, (Object[]) columns));
    }
    
    public String headerUnderLine(int[] lengths)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int length : lengths)
        {
            for (int i=0; i<length+1; i++)
            {
                sb.append("-");
            }
            sb.append("|");
        }
        return sb.toString();
    }
    
    public void formattedOutput(ResultSet rs) throws SQLException
    {
        ResultSetMetaData rsmd = rs.getMetaData();
        int noColumns = rsmd.getColumnCount();
        String[] headers = new String[noColumns];
        int[] lengths = new int[noColumns];
        for (int i=1; i<=noColumns; i++)
        {
            headers[i-1] = rsmd.getColumnName(i);
            lengths[i-1] = headers[i-1].length();
        }
        ArrayList<String[]> records = evaluateFirst1000(rs, lengths);
        if (records.size() > 0)
        {
            String formatString = getFormatString(lengths);
            printRecord(headers, formatString);
            System.out.println(headerUnderLine(lengths));
            for (String[] record : records)
            {
                printRecord(record, formatString);
            }
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        Psql psql = new Psql("vaslgpm001p", "5432", "gpprod", "rubandyopadhyay");
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run()
            {
                psql.end();
            }
        });

        while (true)
        {
            String sql = psql.acceptCommand();
            psql.runAnyUserSQL(sql);
        }
    }
}
