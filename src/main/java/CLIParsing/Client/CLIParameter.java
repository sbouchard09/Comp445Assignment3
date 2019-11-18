package CLIParsing.Client;

import com.beust.jcommander.Parameter;

public class CLIParameter {

    @Parameter(names = {"router-host"})
    private String routerHost;

    @Parameter(names = {"router-port"})
    private Integer routerPort;

    @Parameter(names = {"server-host"})
    private String serverHost;

    @Parameter(names = {"server-port"})
    private Integer serverPort;

    @Parameter(names = {"help"}, help = true)
    private Boolean help = false;

    public String getRouterHost() {
        return routerHost;
    }

    public Integer getRouterPort() {
        return routerPort;
    }

    public String getServerHost() {
        return serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public Boolean getHelp() {
        return help;
    }
}
