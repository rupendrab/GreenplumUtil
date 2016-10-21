package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import db.GreenplumConnection;
import value.ColumnType;
import value.FormatTranslation;

public class AnalyzeFileColumns
{
    String fileName;
    String zipFileName;
    ReadFile dataReaderFile;
    EntryReader dataReaderZipFile;
    Iterable<String> dataReader;
    Iterable<String[]> dataReaderCSV;
    String delimiter;
    String headerLine;
    String[] headerFields;
    ColumnType[] anal;
    int maxLines = -1;
    boolean csv = false;
    ReadFromCSV csvReader;

    public AnalyzeFileColumns(String fileName, String delimiter) throws IOException
    {
        super();
        this.fileName = fileName;
        this.delimiter = delimiter;
        this.csv = delimiter == null;
        if (csv)
        {
            this.csvReader = new ReadFromCSV(fileName);
            this.dataReaderCSV = this.csvReader;
        }
        else
        {
            this.dataReaderFile = new ReadFile(this.fileName);
            this.dataReader = this.dataReaderFile;
        }
    }

    public AnalyzeFileColumns(String fileName, String delimiter, int maxLines) throws IOException
    {
        this(fileName, delimiter);
        this.maxLines = maxLines;
    }

    public AnalyzeFileColumns(String zipFileName, String fileName, String delimiter) throws IOException
    {
        super();
        this.zipFileName = zipFileName;
        this.fileName = fileName;
        this.delimiter = delimiter;
        this.csv = delimiter == null;
        if (csv)
        {
            this.csvReader = new ReadFromCSV(zipFileName, fileName);
            this.dataReaderCSV = this.csvReader;
        }
        else
        {
            this.dataReaderZipFile = new EntryReader(this.zipFileName, this.fileName);
            this.dataReader = this.dataReaderZipFile;
        }
    }

    public AnalyzeFileColumns(String zipFileName, String fileName, String delimiter, int maxLines) throws IOException
    {
        this(zipFileName, fileName, delimiter);
        this.maxLines = maxLines;
    }

    public void process() throws IOException
    {
        if (csv)
        {
            process(dataReaderCSV);
        }
        else
        {
            process(dataReader);
        }
    }

    public <T> String[] getFields(T line)
    {
        if (line instanceof String)
        {
            return ((String) line).split(delimiter, -1);
        }
        else if (line instanceof String[])
        {
            return (String[]) line;
        }
        return null;
    }

    public long loadToTable(GreenplumConnection con, String schemaName, String tableName, String configDir, String tempDir)
            throws IOException, SQLException, ParseException
    {
        if (csv)
        {
            return loadToTable(con, schemaName, tableName, configDir, tempDir, dataReaderCSV);
        }
        else
        {
            return loadToTable(con, schemaName, tableName, configDir, tempDir, dataReader);
        }
    }

    public <T> long loadToTable(GreenplumConnection con, String schemaName, String tableName, String configDir, String tempDir,
            Iterable<T> reader) throws IOException, SQLException, ParseException
    {
        long loaded = 0;

        // Prepare Table Name
        String finalTable = ((schemaName == null || schemaName.equals("")) ? "" : schemaName + ".")
                + ((tableName == null || tableName.equals("")) ? Util.getFileType(fileName) : tableName);

        // Prepare Copy Command
        String copyCmd = "COPY " + finalTable + " FROM STDIN";

        // Get Format Translators
        JsonArray arr = loadAnalysis(configDir);
        HashMap<Integer, FormatTranslation> timeFormats = getTimeFormats(arr);
        HashMap<Integer, Boolean> isString = getIsString(arr);

        // Prepare data file for load
        String tempFileName = tempDir + "/" + finalTable + "_" + Calendar.getInstance().getTimeInMillis() + ".txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(tempFileName)));
        int lineNo = 0;
        for (T line : reader)
        {
            lineNo += 1;
            if (lineNo > 1) // Skip the header line
            {
                String[] fields = getFields(line);
                out.write(Util.getTsvLine(fields, timeFormats, isString));
                out.write("\n");
                // if (lineNo > 100) break;
            }
        }
        Util.closeIO(out);

        // Load data
        CopyManager copyManager = new CopyManager((BaseConnection) con.getConnection());
        BufferedReader dataReader = new BufferedReader(new FileReader(new File(tempFileName)));
        loaded = copyManager.copyIn(copyCmd, dataReader);
        Util.closeIO(dataReader);
        
        // Remove the temporary file
        new File(tempFileName).delete();
        return loaded;
    }

    public <T> void process(Iterable<T> reader) throws IOException
    {
        int lineNo = 0;
        for (T line : reader)
        {
            lineNo++;
            String[] fields = getFields(line);
            if (lineNo == 1)
            {
                headerFields = fields;
                anal = new ColumnType[headerFields.length];
                int i = 0;
                for (String headerField : headerFields)
                {
                    anal[i] = new ColumnType(headerField);
                    i++;
                }
                continue;
            }
            int i = 0;
            for (String field : fields)
            {
                anal[i].addObservation(field);
                i++;
            }
            if (lineNo % 1000000 == 0)
            {
                System.out.println("Processed " + lineNo + " lines");
            }
            if (maxLines != -1 && lineNo >= maxLines)
            {
                break;
            }
        }
        System.out.println("Completed " + lineNo + " lines");
    }

    public void showDetails()
    {
        String tableName = Util.getFileType(fileName);
        System.out.println("create table " + tableName);
        System.out.println("(");
        int i = 0;
        int noCols = anal.length;
        for (ColumnType col : anal)
        {
            i += 1;
            System.out.println(col.pgDataType() + (i < noCols ? "," : ""));
        }
        System.out.println(")");
        System.out.println("distributed randomly");
    }
    
    public void saveAnalysis(String configDir) throws IOException
    {
        String confFileName = configDir + "/" + Util.getFileType(fileName) + ".conf";
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(confFileName)));
        for (ColumnType col : anal)
        {
            writer.write(col.toJsonObject().toString());
            writer.write("\r\n");
        }
        Util.closeIO(writer);
    }
    
    public JsonArray loadAnalysis(String configDir) throws IOException
    {
        Gson gson = new Gson();
        String confFileName = configDir + "/" + Util.getFileType(fileName) + ".conf";
        JsonArray arr = new JsonArray();
        for (String line : new ReadFile(confFileName))
        {
            JsonObject obj = gson.fromJson(line, JsonObject.class);
            arr.add(obj);
        }
        return arr;
    }
    
    public HashMap<Integer, Boolean> getIsString(JsonArray configArr)
    {
        HashMap<Integer, Boolean> out = new HashMap<Integer,Boolean>();
        int columnPosition = 0;
        for (JsonElement elem : configArr)
        {
            if (elem.isJsonObject())
            {
                JsonObject obj = (JsonObject) elem;
                String dataType = obj.get("dataType").getAsString();
                out.put(columnPosition, dataType.equals("VARCHAR") || dataType.equals("CHAR"));
            }
            else
            {
                out.put(columnPosition, false);
            }
            columnPosition++;
        }
        return out;
    }
    
    public HashMap<Integer, FormatTranslation> getTimeFormats(JsonArray configArr)
    {
        HashMap<Integer, FormatTranslation> out = new HashMap<Integer,FormatTranslation>();
        int columnPosition = 0;
        for (JsonElement elem : configArr)
        {
            if (elem.isJsonObject())
            {
                JsonObject obj = (JsonObject) elem;
                String dataType = obj.get("dataType").getAsString();
                if (dataType.equals("DATE") || dataType.equals("DATETIME"))
                {
                    // System.out.println(columnPosition + " : " + dataType);
                    String typeName = dataType.equals("DATE") ? "datesWorked" : "dateTimesWorked";
                    SimpleDateFormat toFormat = dataType.equals("DATE") ? new SimpleDateFormat("yyyy.MM.dd") : new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
                    JsonArray dateFormats = (JsonArray) obj.get(typeName);
                    SimpleDateFormat[] formats = new SimpleDateFormat[dateFormats.size()];
                    int i = 0;
                    for (JsonElement dateFormat : dateFormats)
                    {
                        formats[i++] = new SimpleDateFormat(dateFormat.getAsString()); 
                    }
                    out.put(columnPosition, new FormatTranslation(toFormat, formats));
                }
                else
                {
                    out.put(columnPosition, null);
                }
            }
            else
            {
                out.put(columnPosition, null);
            }
            columnPosition++;
        }
        return out;
    }

    public static void main(String[] args) throws Exception
    {
        long startTime = Calendar.getInstance().getTimeInMillis();
        String citiDir = "\\\\vaswapp083p\\RRP\\QFC\\CITI\\";
        // String fileName = citiDir + "ce_fdic_a1_20110531.gz";
        // String fileName = citiDir + "ce_fdic_20110531_collateral.gz";
        // String fileName = citiDir + "ce_fdic_20110531_cpty_details.gz";
        // String fileName = citiDir + "ce_fdic_20110531_cpty_report.gz";
        // AnalyzeFileColumns afc = new AnalyzeFileColumns(fileName, "~");
        String boaDir = "\\\\vaswapp083p\\RRP\\QFC\\BOA\\";
        String zipFileName = boaDir + "A1_A2_GCI_Hierarchy_10282011.zip";
        String fileName = "GCI_Hierarchy_10282011.txt";
        AnalyzeFileColumns afc = new AnalyzeFileColumns(zipFileName, fileName, null);
        String confDir = "C:\\Users\\rubandyopadhyay\\Documents\\QFC\\BOA";
        /*
         * afc.process(); afc.showDetails(); long endTime =
         * Calendar.getInstance().getTimeInMillis(); System.out.println("");
         * System.out.println("...");
         * System.out.println(String.format("Time elapsed = %5.2f seconds",
         * (endTime - startTime) / 1000.0));
         */
        String password = System.getenv("auth");
        GreenplumConnection con = new GreenplumConnection("vaslgpm001p", "5432", "gpprod", "rubandyopadhyay", password);
        long loaded = afc.loadToTable(con, "workspace", null, confDir, "c:/tmp");
        System.out.println("Loaded " + loaded + " records.");
        con.closeAll();
        /*
        afc.process();
        afc.saveAnalysis(confDir);
        */
        /*
        JsonArray arr = afc.loadAnalysis(confDir);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // System.out.println(gson.toJson(arr));
        HashMap<Integer, FormatTranslation> timeFormats = afc.getTimeFormats(arr);
        System.out.println(timeFormats);
        */
        long endTime = Calendar.getInstance().getTimeInMillis();
        System.out.println(String.format("Time elapsed = %5.2f seconds", (endTime - startTime) / 1000.0));
    }

}
