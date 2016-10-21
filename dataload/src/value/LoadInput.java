package value;

public class LoadInput
{
    private String dirName;
    private String zipFileName;
    private String fileName;
    private String delimiter;

    public LoadInput(String dirName, String zipFileName, String fileName, String delimiter)
    {
        super();
        this.dirName = dirName;
        this.zipFileName = zipFileName;
        this.fileName = fileName;
        this.delimiter = delimiter;
    }

    public String getDirName()
    {
        return dirName;
    }

    public void setDirName(String dirName)
    {
        this.dirName = dirName;
    }

    public String getZipFileName()
    {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName)
    {
        this.zipFileName = zipFileName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getDelimiter()
    {
        return delimiter;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public boolean isZippedInput()
    {
        return zipFileName != null;
    }

    @Override
    public String toString()
    {
        return "LoadInput [dirName=" + dirName + ", zipFileName=" + zipFileName + ", fileName=" + fileName
                + ", delimiter=" + delimiter + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dirName == null) ? 0 : dirName.hashCode());
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((zipFileName == null) ? 0 : zipFileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoadInput other = (LoadInput) obj;
        if (dirName == null)
        {
            if (other.dirName != null)
                return false;
        }
        else if (!dirName.equals(other.dirName))
            return false;
        if (fileName == null)
        {
            if (other.fileName != null)
                return false;
        }
        else if (!fileName.equals(other.fileName))
            return false;
        if (zipFileName == null)
        {
            if (other.zipFileName != null)
                return false;
        }
        else if (!zipFileName.equals(other.zipFileName))
            return false;
        return true;
    }

}
