package value;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserCommand
{
    private int  type;
    private String sql;
    private String cmd;
    private boolean plus;
    private String[] cmdArgs;
    
    private static Pattern cmdPattern;
   
    public UserCommand(String sql)
    {
        super();
        this.type = 0;
        this.sql = sql;
    }

    public UserCommand(String cmd, boolean plus, String[] cmdArgs)
    {
        super();
        this.type = 1;
        this.cmd = cmd;
        this.plus = plus;
        this.cmdArgs = cmdArgs;
    }

    public int getType()
    {
        return type;
    }

    public String getSql()
    {
        return sql;
    }

    public String getCmd()
    {
        return cmd;
    }

    public boolean isPlus()
    {
        return plus;
    }

    public String[] getCmdArgs()
    {
        return cmdArgs;
    }
    
    public static UserCommand getPsqlCommand(String str)
    {
       Matcher m = cmdPattern.matcher(str);
       if (m.find())
       {
           String cmd = m.group(1);
           String plus = m.group(2);
           String rest = m.group(3);
           String[] cmdArgs = new String[0];
           if (rest != null)
           {
               cmdArgs = rest.trim().split("\\s+");
           }
           return new UserCommand(cmd, ! (plus == null || plus.equals("")), cmdArgs);
       }
       else
       {
           return null;
       }
    }
    
    @Override
    public String toString()
    {
        return "UserCommand [type=" + type + ", sql=" + sql + ", cmd=" + cmd + ", plus=" + plus + ", cmdArgs="
                + Arrays.toString(cmdArgs) + "]";
    }

    public static void main(String[] args)
    {
        System.out.println(UserCommand.getPsqlCommand("\\dt"));
        System.out.println(UserCommand.getPsqlCommand("\\dt wo*.*a*"));
    }
    
    static
    {
        String cmdPatternStr = "^\\s*(\\\\[a-zA-Z]+)(\\+)?(\\s+.*)?$";
        cmdPattern = Pattern.compile(cmdPatternStr);
    }
}
