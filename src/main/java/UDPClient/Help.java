package UDPClient;

public class Help {

    private String requestType;

    public Help(String requestType) {
        this.requestType = requestType;
    }

    public String printHelp() {
        StringBuilder sb = new StringBuilder();

        if(requestType == null) { // no request type, default help
            sb.append("httpc is a curl-like application but supports HTTP protocols only\n");
            sb.append("Usage:\n");
            sb.append("\thttpc command [arguments]\n");
            sb.append("The commands are:\n");
            sb.append("\tget      executes a HTTP GET request and prints the response\n");
            sb.append("\tpost     executes a HTTP POST request and prints the response\n");
            sb.append("\thelp     prints this screen\n\n");
            sb.append("Use \"httpc help [command]\" for more information about a command.\n");
        } else if(requestType.equals("post")) {
            sb.append("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n\n");
            sb.append("Post executes a HTTP POST request for a given URL with inline data or from file\n\n");
            sb.append("    -v             Prints the detail of the response such as protocol, status, and headers\n");
            sb.append("    -h key:value   Associates the headers to HTTP Request with the format 'key:value'\n");
            sb.append("    -d string      Associates an inline data to the body HTTP POST request\n");
            sb.append("    -f file        Associates the content of a file to the body HTTP POST request\n");
            sb.append("    -o filename    Allows the HTTP client to write the body of the response to a file\n\n");
            sb.append("Either [-d] or [-f] can be used but not both");
        } else if(requestType.equals("get")) {
            sb.append("usage: httpc get [-v] [-h key:value] [-d inline-data] [-f file] URL\n\n");
            sb.append("Post executes a HTTP GET request for a given URL with inline data or from file\n\n");
            sb.append("    -v             Prints the detail of the response such as protocol, status, and headers\n");
            sb.append("    -h key:value   Associates the headers to HTTP Request with the format 'key:value'\n");
            sb.append("    -o filename    Allows the HTTP client to write the body of the response to a file\n");
        }

        return sb.toString();
    }
}
