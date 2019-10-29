package UDPServer;

import CLIParsing.Server.ServerParser;
import com.beust.jcommander.JCommander;

public class Server {

    public static void main(String[] args) {
        ServerParser serverParser = new ServerParser();
        JCommander parser = JCommander.newBuilder().addObject(serverParser).build();
        parser.parse(args);
    }
}
