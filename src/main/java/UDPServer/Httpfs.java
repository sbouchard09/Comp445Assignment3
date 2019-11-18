package UDPServer;

import CLIParsing.Server.ServerParser;
import com.beust.jcommander.JCommander;

import java.io.IOException;

public class Httpfs {

    private static int port = 8007;

    public static void main(String[] args) {
        ServerParser serverParser = new ServerParser();
        JCommander parser = JCommander.newBuilder().addObject(serverParser).build();
        parser.parse(args);

        UDPServer server = new UDPServer();
        try {
            server.listenAndServe(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
