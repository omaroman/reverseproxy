package models;


public class JavaInfo {
    public String javaversion;
    public String javavendor;
    public String javahome;
    public String[] javalibpath;
    public String[] javaclasspath;

    public String vmversion;
    public String vmvendor;
    public String vmname;

    public String osname;
    public String osversion;
    public String osarch;

    public String vmmax;
    public String vmfree;
    public String vmtotal;
    public String processors;

    public String separatorfile;
    public String separatorpath;
    public String separatorline;


    public JavaInfo()
    {
        separatorfile = System.getProperty("file.separator", "");
        separatorpath = System.getProperty("path.separator", "");
        separatorline = System.getProperty("line.version", "");

        javaversion = System.getProperty("java.version", "");
        javavendor = System.getProperty("java.vendor", "");
        javahome = System.getProperty("java.home", "");
        javalibpath = System.getProperty("java.library.path", "").split(separatorpath);
        javaclasspath = System.getProperty("java.class.path", "").split(separatorpath);

        vmversion = System.getProperty("java.vm.version", "");
        vmvendor = System.getProperty("java.vm.vendor", "");
        vmname = System.getProperty("java.vm.name", "");

        osname = System.getProperty("os.name", "");
        osversion = System.getProperty("os.version", "");
        osarch = System.getProperty("os.arch", "");

        vmmax = Long.toString(Runtime.getRuntime().maxMemory());
        vmfree = Long.toString(Runtime.getRuntime().freeMemory());
        vmtotal = Long.toString(Runtime.getRuntime().totalMemory());
        processors = Integer.toString(Runtime.getRuntime().availableProcessors());
    }
}
