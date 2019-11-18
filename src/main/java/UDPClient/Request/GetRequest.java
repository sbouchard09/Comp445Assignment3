package UDPClient.Request;

import UDPClient.UDPClient;

import java.io.IOException;
import java.net.*;
import java.util.Map;

public class GetRequest extends Request {

    private InetAddress address;

    public GetRequest(UDPClient udpClient, String url, boolean isVerbose, Map<String, String> headers) {
        super("GET", udpClient, url, isVerbose, headers);
    }

    @Override
    public void sendRequest(){
        try {
            URL url = new URL(this.url);
            serverHost = url.getHost();
            if(url.getPort() != -1) serverPort = url.getPort();

            udpClient.setMessage(prepareRequest(url, ""));
            SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            udpClient.runClient(routerAddress, serverAddress);
            super.viewOutput();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
