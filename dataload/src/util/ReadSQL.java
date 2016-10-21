package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadSQL
{
    private String fileName;
    private String[] lines;
    private Pattern keyPattern = Pattern.compile("^\\[(.+)\\]\\s*$");
    private HashMap<String, HashMap<String,String>> allSQL = new HashMap<String, HashMap<String,String>>(); 
    private static ReadSQL readSQL;

    public static ReadSQL getReadSQL() throws IOException
    {
        if (readSQL == null)
        {
            readSQL = new ReadSQL("/dictionary_queries.sql");
            readSQL.populateAllSQL();
        }
        return readSQL;
    }
    
    private ReadSQL(String fileName) throws IOException
    {
        super();
        this.fileName = fileName;
        readLines();
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName) throws IOException
    {
        this.fileName = fileName;
        readLines();
    }
    
    private void readLines() throws IOException
    {
        ArrayList<String> linesList = new ArrayList<String>();
        InputStream in = getClass().getResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            linesList.add(line);
        }
        lines = new String[linesList.size()];
        linesList.toArray(lines);
        reader.close();
        in.close();
    }
    
    public int occurences(String str, char c)
    {
        int count = 0;
        for (char c1: str.toCharArray())
        {
            if (c1 == c)
            {
                count++;
            }
        }
        return count;
    }
    
    public HashMap<String, HashMap<String,String>> populateAllSQL()
    {
        allSQL = new HashMap<String, HashMap<String,String>>();
        
        String currentCommand = null;
        HashMap<String, String> currentKeys = null;
        
        boolean sqlStart = false;
        
        String currentKey = null;
        StringBuilder sb = new StringBuilder();
        int noDoubleQuotes = 0;
        int noSingleQuotes = 0;

        for (String line : lines)
        {
            if (! sqlStart) // Not inside a SQL
            {
                if (line.startsWith("command="))
                {
                    if (currentCommand != null)
                    {
                        allSQL.put(currentCommand, currentKeys);
                    }
                    currentCommand = line.replaceAll("^command=", "").trim();
                    currentKeys = new HashMap<String, String>();
                    allSQL.put(currentCommand, currentKeys);
                    continue;
                }
                else if (currentKey == null)
                {
                    Matcher m2 = keyPattern.matcher(line);
                    if (m2.find())
                    {
                        currentKey = m2.group(1);
                        // System.out.println("Found key = " + currentKey);
                        sqlStart = false;
                    }
                    continue;
                }
                else if (! line.matches("^\\s*$"))
                {
                    sqlStart = true;
                }
            }
            if (sqlStart) // SQL started on this line or before
            {
                sb.append(line);
                sb.append("\r\n");
                noDoubleQuotes += occurences(line, '"');
                noSingleQuotes += occurences(line, '\'');
                if (line.matches("^.*;\\s*$")) // Possible End of SQL
                {
                    if (noSingleQuotes %2 == 0 && noDoubleQuotes % 2 == 0) // End of SQL
                    {
                        if (currentKey != null)
                        {
                            currentKeys.put(currentKey, sb.toString());
                            currentKey = null;
                            sb = new StringBuilder();
                            sqlStart = false;
                        }
                    }
                }
            }
        }
        return allSQL;
    }
    
    public HashMap<String, String> getSQLForCommand(String cmd)
    {
        return allSQL.get(cmd);
    }
    
    public static void main(String[] args) throws Exception
    {
        ReadSQL readSQL = ReadSQL.getReadSQL();
        System.out.println(readSQL.allSQL);
    }

}
