package UDPServer;

import CLIParsing.Server.ServerParser;
import com.beust.jcommander.JCommander;

import java.io.IOException;

public class Httpfs {

    private static int port = 8007;
    private static String directory = "";

    public static void main(String[] args) {
        ServerParser serverParser = new ServerParser();
        JCommander parser = JCommander.newBuilder().addObject(serverParser).build();
        parser.parse(args);

        if(serverParser.getPort() != null) {
            port = serverParser.getPort();
        }

        if(serverParser.getDirectory() != null) {
            directory = serverParser.getDirectory();
        }

        UDPServer server = new UDPServer();
        try {
            server.listenAndServe(port, directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
