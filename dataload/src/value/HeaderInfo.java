package value;

public class HeaderInfo implements Comparable<HeaderInfo>
{
    String type;
    String header;
    
    public HeaderInfo(String type, String header)
    {
        super();
        this.type = type;
        this.header = header;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getHeader()
    {
        return header;
    }

    public void setHeader(String header)
    {
        this.header = header;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (! (obj instanceof HeaderInfo))
        {
            return false;
        }
        HeaderInfo that = (HeaderInfo) obj;
        return this.type.equals(that.type) &&
                this.header.equals(that.header);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += type.hashCode();
        hash = hash * 31 + header.hashCode();
        return hash;
    }

    @Override
    public int compareTo(HeaderInfo o)
    {
        int c = (type.compareTo(o.type));
        if (c != 0)
        {
            return c;
        }
        c = header.compareTo(o.header);
        return c;
    }

}
