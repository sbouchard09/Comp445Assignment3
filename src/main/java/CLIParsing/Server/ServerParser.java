package CLIParsing.Server;

import com.beust.jcommander.Parameter;

public class ServerParser {

    @Parameter(names = {"help"}, help = true)
    private Boolean help = false;

    @Parameter(names = {"-v"})
    private Boolean debug = false;

    @Parameter(names = {"-p"})
    private Integer port;

    @Parameter(names = {"-d"})
    private String directory;


    public Boolean getDebug() {
        return this.debug;
    }

    public Integer getPort() {
        return this.port;
    }

    public String getDirectory() {
        return this.directory;
    }

    public Boolean getHelp() {
        return help;
    }
}
