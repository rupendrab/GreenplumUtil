package util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import value.FormatTranslation;

public class Util
{
    static Hashtable<String, String> fileTypesTranslator;
    
    public static void loadFileTypesTranslator()
    {
        fileTypesTranslator = new Hashtable<String, String>();
        InputStream inp = null;
        BufferedReader reader = null;
        try
        {
            inp = Util.class.getResourceAsStream("/conf/file_types_translator.txt");
            reader = new BufferedReader(new InputStreamReader(inp));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                String[] fields = line.split("=");
                if (fields.length == 2)
                {
                    fileTypesTranslator.put(fields[0], fields[1]);
                }
            }
        }
        catch (IOException ioe)
        {
        }
        finally
        {
            closeIO(reader);
            closeIO(inp);
        }
    }
    
    public static String toPgDateTime(String str, SimpleDateFormat toFormat, SimpleDateFormat[] fromFormats) throws ParseException
    {
        int i = 0;
        for (SimpleDateFormat fromFormat: fromFormats)
        {
            try
            {
                Date dt = fromFormat.parse(str);
                return toFormat.format(dt);                
            }
            catch (ParseException pe)
            {
                if (i == fromFormats.length - 1)
                {
                    throw pe;
                }
            }
        }
        return "\\N";
    }
    
    public static String getFileType(String fileName)
    {
        File f = new File(fileName);
        String baseName = f.getName();
        baseName = baseName.replaceAll("\\.[^\\.]*$", "");
        String fileType = baseName.replaceAll("[0-9]{8}", "").replaceAll("_+", "_");
        fileType = fileType.replaceAll("_*$", "");
        String newFileType = fileTypesTranslator.get(fileType);
        if (newFileType != null)
        {
            return newFileType;
        }
        return fileType;
    }
    
    public static String replaceBadChars(String val)
    {
        StringBuilder sb = new StringBuilder();
        for (char c: val.toCharArray())
        {
            if ((int) c <= 127)
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    public static String getTsvCol(String value, FormatTranslation formatTranslation, boolean isString) throws ParseException
    {
        if (value == null || value.equals(""))
        {
            return "\\N";
        }
        if (! isString)
        {
            value = replaceBadChars(value).trim();
        }
        if (formatTranslation != null)
        {
            return toPgDateTime(value, formatTranslation.getTo(), formatTranslation.getFrom());
        }
        else
        {
            return getTsvCol(value);
        }
    }
    
    public static String getTsvCol(String value)
    {
        if (value == null || value.equals(""))
        {
            return "\\N";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray())
        {
            switch(c)
            {
            case '\\': sb.append(c); sb.append(c); break;
            case '\r': sb.append('\\'); sb.append('r'); break;
            case '\n': sb.append('\\'); sb.append('n'); break;
            case '\t': sb.append('\\'); sb.append('t'); break;
            default: sb.append(c); break;
            }
        }
        return sb.toString();
        /*
        return value.
                replaceAll("\r", "\\\\r").
                replaceAll("\t", "\\\t").
                replaceAll("\n", "\\\n");
        */
    }
    
    public static String getTsvLine(String[] data, HashMap<Integer, FormatTranslation> tr, HashMap<Integer, Boolean> isString) throws ParseException
    {
        if (data == null || data.length == 0)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String field : data)
        {
            if (i > 0)
            {
                sb.append("\t");
            }
            sb.append(getTsvCol(field, tr.get(i), isString.get(i)));
            i++;
        }
        return sb.toString();
    }

    public static String getTsvLine(String[] data)
    {
        if (data == null || data.length == 0)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String field : data)
        {
            if (i > 0)
            {
                sb.append("\t");
            }
            i++;
            sb.append(getTsvCol(field));
        }
        return sb.toString();
    }
    
    public static void closeIO(Closeable f)
    {
        try
        {
            if (f != null)
            {
                f.close();
            }
        }
        catch (Exception e)
        {
        }
    }
    
    public static void closeDbObject(AutoCloseable obj)
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
    
    public static ZipEntry[] getZipEntries(String zipFileName) throws IOException
    {
        ZipFile zipFile = new ZipFile(zipFileName);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ArrayList<ZipEntry> entryList = new ArrayList<ZipEntry>();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            entryList.add(entry);
        }
        zipFile.close();
        ZipEntry[] entryArr = new ZipEntry[entryList.size()];
        entryList.toArray(entryArr);
        return entryArr;
    }
    
    public static ArrayList<String> getFiles(String dirName, boolean anyDepth)
    {
        return getFiles(dirName, anyDepth, "*");
    }
    
    public static ArrayList<String> getFiles(String dirName, boolean anyDepth, String fileGLOB)
    {
        File dir = new File(dirName);
        ArrayList<String> fileNamesList = new ArrayList<String>();
        if (! dir.isDirectory())
        {
            return fileNamesList;
        }
        
        if (anyDepth)
        {
            File[] subDirectories = dir.listFiles(new FileFilter() {
                
                @Override
                public boolean accept(File pathname)
                {
                    return pathname.isDirectory();
                }
            });
            
            for (File subDirectory : subDirectories)
            {
                ArrayList<String> subList = getFiles(subDirectory.getAbsolutePath(), anyDepth, fileGLOB);
                fileNamesList.addAll(subList);
            }
        }
        
        String pattern = fileGLOB.replaceAll("\\*", ".*");
        Pattern fileNamePattern = Pattern.compile("^" + pattern + "$");
        
        File[] matchingFiles = dir.listFiles(new FileFilter() {
            
            @Override
            public boolean accept(File pathname)
            {
                return (! pathname.isDirectory()) && fileNamePattern.matcher(pathname.getName()).find();
            }
        });
        
        for (File matchingFile : matchingFiles)
        {
            fileNamesList.add(matchingFile.getAbsolutePath());
        }
        
        return fileNamesList;
    }
    
    public static String readFromUser(String prompt) throws IOException
    {
        System.out.print(prompt + " : ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String val = in.readLine();
        return val;
    }
    
    public static boolean readCSVLine(String line, ArrayList<String> fields)
    {
        boolean inField = false;
        boolean quotedField = false;
        boolean gotDoubleQuote = false;
        int charPos = -1;
        StringBuilder field = new StringBuilder();
        boolean ok = true;
        char lastC = 0;
        for (char c : line.toCharArray())
        {
            // System.out.println(lastC + " : " + fields);
            lastC = c;
            charPos++;
            if (! inField)
            {
                switch(c)
                {
                case '"':
                    inField = true;
                    quotedField = true;
                    gotDoubleQuote = false;
                    field = new StringBuilder();
                    break;
                case ',':
                    fields.add(null);
                    break;
                default:
                    inField = true;
                    quotedField = false;
                    field = new StringBuilder();
                    break;
                }
            }
            else // Inside a field
            {
                if (! quotedField)
                {
                    switch(c)
                    {
                    case ',': 
                        fields.add(field.length() == 0 ? null : field.toString());
                        inField = false;
                        quotedField = false;
                        break;
                    default:
                        field.append(c);
                        break;
                    }
                }
                else // Inside Quoted field
                {
                    switch(c)
                    {
                    case '"':
                        if (gotDoubleQuote)
                        {
                            field.append(c);
                            gotDoubleQuote = false;
                        }
                        else
                        {
                            gotDoubleQuote = true;
                        }
                        break;
                    case ',':
                        if (gotDoubleQuote)
                        {
                            fields.add(field.length() == 0 ? null : field.toString());
                            inField = false;
                            quotedField = false;
                            gotDoubleQuote = false;
                        }
                        else
                        {
                            field.append(c);
                        }
                        break;
                    default:
                        if (gotDoubleQuote)
                        {
                            ok = false;
                            break;
                        }
                        else
                        {
                            field.append(c);
                        }
                    }
                    if (! ok)
                    {
                        break;
                    }
                }
            }
        }
        // Deal with the very last field
        if (inField)
        {
            if (! quotedField)
            {
                fields.add(field.length() == 0 ? null : field.toString());
            }
            else
            {
                if (! gotDoubleQuote)
                {
                    ok = false;
                }
                else
                {
                    fields.add(field.length() == 0 ? null : field.toString());
                }
            }
        }
        return ok;
    }
    
    static
    {
        loadFileTypesTranslator();
    }

}
