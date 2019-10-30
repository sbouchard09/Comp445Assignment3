package UDPClient;

import CLIParsing.Client.CLIParameter;
import CLIParsing.Client.GetParser;
import CLIParsing.Client.PostParser;
import com.beust.jcommander.JCommander;

public class Client {

    private static String requestType = "";

    public static void main(String[] args) {
        CLIParameter parameters = new CLIParameter();
        GetParser getParser = new GetParser();
        PostParser postParser = new PostParser();
        JCommander parser = JCommander.newBuilder().addObject(parameters).addCommand("get", getParser).addCommand("post", postParser).build();
        parser.parse(args);

        requestType = parser.getParsedCommand();

        if(parameters.getHelp()) {
            Help help = new Help(requestType);
            help.printHelp();
        } else {
            if(requestType.equalsIgnoreCase("GET")) {

            } else if(requestType.equalsIgnoreCase("POST")) {

            }
        }
    }
}
