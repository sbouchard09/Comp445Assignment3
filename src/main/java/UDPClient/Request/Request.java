package UDPClient.Request;

/**
 * A request object should be able to be broken down into max 1024 byte-sized packets
 */
import UDPClient.UDPClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public abstract class Request {

    protected Map<String, String> headers;
    protected boolean isVerbose;
    protected String url;
    protected UDPClient udpClient;
    protected String outputFileName = "";
    protected String routerHost = "localhost";
    protected String serverHost = "localhost";
    protected String requestMethod; // GET | POST
    protected int routerPort = 3000;
    protected int serverPort = 8007;

    public Request() { }

    public Request(String requestMethod, UDPClient udpClient, String url, boolean isVerbose, Map<String, String> headers) {
        this.url = url;
        this.isVerbose = isVerbose;
        this.headers = headers;
    }

    public abstract void sendRequest();

    public void setConnection(UDPClient udpClient) {
        this.udpClient = udpClient;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRouterHost() {
        return routerHost;
    }

    public void setRouterHost(String routerHost) {
        this.routerHost = routerHost;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getRouterPort() {
        return routerPort;
    }

    public void setRouterPort(int routerPort) {
        this.routerPort = routerPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void viewOutput() {

        String body;
        String response = udpClient.getResponse();

        if(isVerbose) {
            if(outputFileName.equals("")) { // note: outputFileName not implemented
                System.out.println(response);
            } else {

            }
        } else {
            // body is separated from the header by \r\n
            try{
                body = response.split("\r\n\r\n")[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                body = response;
            }

            if(outputFileName.equals("")) { // note: outputFileName not implemented
                System.out.println(body);
            } else {

            }
        }
    }

    protected String prepareRequest(URL url, String body) throws MalformedURLException, UnknownHostException {
        String request = "Hello World";
        String host = url.getHost();
        String path = url.getPath();

        if(path == null || path.length() == 0) {
            path = "/";
        }
        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(requestMethod + " " + path + " HTTP/1.0\r\n");
        requestBuilder.append("Host: " + host + "\r\n");
        if(requestMethod.equals("POST")) {
            body = format(body);
            if(!headers.containsKey("Content-Length") && body.length() > 1) {
                headers.put("Content-Length", "" + body.length());
            }
            requestBuilder.append(parseHeaders());
            requestBuilder.append(body);
        } else {
            requestBuilder.append(parseHeaders());
        }
        return requestBuilder.toString();
    }

    private String parseHeaders() {
        StringBuilder headerBuilder = new StringBuilder();

        for(Map.Entry<String, String> header : headers.entrySet()) {
            headerBuilder.append(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        headerBuilder.append("\r\n");

        return headerBuilder.toString();
    }

    // format: '{"key": value, "key": value, ...}' to key=value&key=value&..
    private String format(String body) {
        if(body == null) {
            return "";
        }

        try {
            String[] arguments = body.split(":");
            StringBuilder bodyBuilder = new StringBuilder();
            for(int i = 0; i < arguments.length; i+=2) {
                bodyBuilder.append(arguments[i].replaceAll("[^a-zA-Z0-9]", "") + "=" +
                        arguments[i + 1].replaceAll("[^a-zA-Z0-9]", ""));
                if(i + 2 < arguments.length) {
                    bodyBuilder.append("&");
                }
            }
            return bodyBuilder.toString().trim();
        } catch(Exception e) { // wrong body format
            return body.trim();
        }
    }
}
