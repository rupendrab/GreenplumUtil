package value;

import java.text.SimpleDateFormat;
import java.util.Arrays;

public class FormatTranslation
{
    private SimpleDateFormat to;
    private SimpleDateFormat[] from;
    
    public FormatTranslation(SimpleDateFormat to, SimpleDateFormat[] from)
    {
        super();
        this.to = to;
        this.from = from;
    }

    public SimpleDateFormat getTo()
    {
        return to;
    }

    public void setTo(SimpleDateFormat to)
    {
        this.to = to;
    }

    public SimpleDateFormat[] getFrom()
    {
        return from;
    }

    public void setFrom(SimpleDateFormat[] from)
    {
        this.from = from;
    }

    @Override
    public String toString()
    {
        String[] fromStrings = new String[from.length];
        int i = 0;
        for (SimpleDateFormat fmt : from)
        {
            fromStrings[i++] = fmt.toPattern();
        }
        return "FormatTranslation [to=" + to.toPattern() + ", from=" + Arrays.toString(fromStrings) + "]";
    }
    
}
