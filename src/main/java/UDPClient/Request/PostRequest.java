package UDPClient.Request;

import UDPClient.UDPClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Map;

public class PostRequest extends Request {

    String body = "";

    public PostRequest(UDPClient udpClient, String url, boolean isVerbose, Map<String, String> headers, Map<String, String> options) throws IllegalArgumentException {
        super("POST", udpClient, url, isVerbose, headers);
        parseOptions(options);
    }

    private void parseOptions(Map<String, String> options) throws IllegalArgumentException{
        if(options == null) return;

        boolean hasInlineData = false;
        boolean hasInputFile = false;

        if(options.containsKey("inline_data")) {
            hasInlineData = true;
            body = options.get("inline_data");
        }

        if(options.containsKey("input_file")) {
            hasInputFile = true;
            body = options.get("input_file");
        }

        if(hasInlineData && hasInputFile) {
            throw new IllegalArgumentException("Can only have one of [-d] or [-f] options in POST request");
        }
    }

    @Override
    public void sendRequest() {

        try {
            URL url = new URL(this.url);
            serverHost = url.getHost();
            if(url.getPort() != -1) serverPort = url.getPort();

            udpClient.setMessage(prepareRequest(url, body));
            SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            udpClient.runClient(routerAddress, serverAddress);
            super.viewOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}