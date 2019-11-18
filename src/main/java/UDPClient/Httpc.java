package UDPClient;

import CLIParsing.Client.CLIParameter;
import CLIParsing.Client.GetParser;
import CLIParsing.Client.PostParser;
import UDPClient.Request.GetRequest;
import UDPClient.Request.PostRequest;
import UDPClient.Request.Request;
import com.beust.jcommander.JCommander;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Httpc {

    private static Map<String, String> headers;
    private static Map<String, String> options;

    public static void main(String[] args) {
        String requestType = "";
        boolean isVerbose = false;
        String url;
        UDPClient udpClient = new UDPClient();
        Request request = null;
        CLIParameter parameters = new CLIParameter();
        GetParser getParser = new GetParser();
        PostParser postParser = new PostParser();
        JCommander parser = JCommander.newBuilder().addCommand("get", getParser).addCommand("post", postParser).addObject(parameters).build();
        parser.parse(args);

        requestType = parser.getParsedCommand();

        if(parameters.getHelp()) {
            Help help = new Help(requestType);
            help.printHelp();

        } else {
            headers = new HashMap<>();
            if(requestType.equalsIgnoreCase("GET")) {
                url = getParser.getUrl();
                isVerbose = getParser.getVerbose();
                parseHeaders(getParser.getHeader());
                request = new GetRequest(udpClient, url, isVerbose, headers);
            } else if(requestType.equalsIgnoreCase("POST")) {
                url = postParser.getUrl();
                isVerbose = postParser.getVerbose();
                parseHeaders(getParser.getHeader());
                options = new HashMap<>();
                if(postParser.getOption() != null) {
                    options.put("input_file", postParser.getOption());
                }
                if(postParser.getInputFile() != null) {
                    options.put("input_file", readInputFile(postParser.getInputFile()));
                }
                try {
                    request = new PostRequest(udpClient, url, isVerbose, headers, options);
                } catch(IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            if(parameters.getRouterHost() != null) {
                request.setRouterHost(parameters.getRouterHost());
            }
            if(parameters.getRouterPort() != null) {
                request.setRouterPort(parameters.getRouterPort());
            }
            if(parameters.getServerHost() != null) {
                request.setServerHost(parameters.getServerHost());
            }
            if(parameters.getServerPort() != null) {
                request.setServerPort(parameters.getServerPort());
            }

            if(request != null) request.sendRequest();
        }
    }

    private static void parseHeaders(String headerString) {
        if(headerString == null || headerString.length() == 0) return;

        String[] header = headerString.split(":");
        headers.put(header[0], header[1]);
    }

    private static String readInputFile(String inputFileName) {
        String output = null;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
            StringBuilder inputBuilder = new StringBuilder();
            String line = reader.readLine();

            while(line != null) {
                inputBuilder.append(line);
                line = reader.readLine();
            }
            output = inputBuilder.toString();
            reader.close();
        } catch(IOException e) {
            System.out.println("File " + inputFileName + " is not available");
        }

        return output;
    }
}
