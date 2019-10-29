package UDPClient;

import CLIParsing.Client.GetParser;
import CLIParsing.Client.PostParser;
import com.beust.jcommander.JCommander;

public class Client {

    public static void main(String[] args) {
        GetParser getParser = new GetParser();
        PostParser postParser = new PostParser();
        JCommander parser = JCommander.newBuilder().addCommand("get", getParser).addCommand("post", postParser).build();
        parser.parse(args);
    }
}
