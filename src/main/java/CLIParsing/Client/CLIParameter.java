package CLIParsing.Client;

import com.beust.jcommander.Parameter;

public class CLIParameter {

    @Parameter(names = {"help"}, help = true)
    private Boolean help = false;

    public Boolean getHelp() {
        return help;
    }
}
