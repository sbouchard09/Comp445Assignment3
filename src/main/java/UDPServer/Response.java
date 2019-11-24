package UDPServer;

import org.omg.PortableInterceptor.NON_EXISTENT;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Response {

    private final String OK = "200 OK";
    private final String CREATED = "201 Created";
    private final String FORBIDDEN = "403 Forbidden";
    private final String NOT_FOUND = "405 Not Found";

    private HashMap<String, String> headers = new HashMap<>();
    private String body = "";
    private String response;
    private String directory;

    public Response(String directory) {
        if(directory.equals("")) {
            this.directory = System.getProperty("user.dir");
        } else {
            this.directory = directory;
        }
    }

    public void handleRequest(String message) {
        String[] requestArray1 = message.split("\r\n\r\n");
        String[] requestArray2 = requestArray1[0].split("\r\n");
        String[] info = requestArray2[0].split(" ");
        String requestMethod = info[0].replaceAll("[^a-zA-Z0-9]", "").toUpperCase(); // remove all non alphanumeric characters
        String file = info[1];
        if(file.length() > 1) {
            file = file.startsWith("/") ? file.substring(1) : file;
        }

        headers.put("Date", getDate());

        if(requestMethod.equals("GET")) { // get request
            handleGet(file);
        } else if(requestMethod.equals("POST")) { // post request
            try {
                handlePost(file, requestArray1[1]);
            } catch(ArrayIndexOutOfBoundsException e) {
                handlePost(file, "");
            }
        }
    }

    private void handleGet(String fileName) {

        try {

            if(fileName.equals("/")) { // return all files
                File dir = new File(directory);
                File[] list = dir.listFiles();
                StringBuilder directoryBuilder = new StringBuilder();
                StringBuilder responseBuilder = new StringBuilder();
                directoryBuilder.append("{ \n");

                for(int i = 0; i < list.length; i++) {
                    if(list[i].isFile()) {
                        directoryBuilder.append("\tFile: ");
                    } else if (list[i].isDirectory()) {
                        directoryBuilder.append("\tDirectory: ");
                    } else {
                        continue;
                    }
                    directoryBuilder.append(list[i].getName() + "\n");
                }
                directoryBuilder.append("}");

                // add response body
                body = directoryBuilder.toString();

                // add content length
                headers.put("Content-Length", "" + body.length());

                // create response
                createResponse(OK);
            } else { // get file
                File file = new File(fileName);

                if(isForbidden(fileName)) { // 403
                    createResponse(FORBIDDEN);
                } else if(!file.exists()) { // 405
                    createResponse(NOT_FOUND);
                } else if(file.isFile()) { // 200 OK
                    BufferedReader reader = new BufferedReader(new FileReader(fileName));
                    StringBuilder sb = new StringBuilder();
                    String line = reader.readLine();

                    while(line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = reader.readLine();
                    }

                    // add content length, content type, content disposition
                    headers.put("Content-Length", "" + file.length());
                    headers.put("Content-Type", getContentType(fileName));

                    // add body
                    body = sb.toString();

                    // create response
                    createResponse(OK);
                }
            }
        } catch(IOException e) {

        }
    }

    private void handlePost(String fileName, String body) {
        PrintWriter fileWriter = null;

        try {
            if(isForbidden(fileName)) { // 403
                createResponse(FORBIDDEN);
            } else { // 201
                fileWriter = new PrintWriter(new FileOutputStream(fileName, false));
                fileWriter.println(body);
                fileWriter.flush();
                fileWriter.close();

                // add headers
                headers.put("Content-Length", ""+fileName.length());

                // create response
                createResponse(CREATED);
            }


        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void createResponse(String responseStatus) {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.0 " + responseStatus + "\r\n");

        for(Map.Entry<String, String> header : headers.entrySet()) {
            responseBuilder.append(header.getKey() + ": " + header.getValue() + "\r\n");
        }
        responseBuilder.append("\r\n");
        responseBuilder.append(body);

        response = responseBuilder.toString();
    }

    private boolean isForbidden(String fileName){
        File f = new File(fileName);

        File currentDirectory = new File(directory);
        if(f.isDirectory()) {
            return true;
        }

        if(fileName.matches("^[\\w\\-. ]+$")) {
            return false;
        }

        try {
            if(f.getCanonicalPath().contains(currentDirectory.getCanonicalPath())) {
                return false; // able to write here
            }
        } catch(IOException e) {
            return true;
        }

        return true;
    }

    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        String type = "";

        if(extension.equals("txt")) {
            type = "text/plain";
        } else if(extension.equals("json")) {
            type = "application/json";
        } else if(extension.equals("htm") || extension.equals("html")) {
            type = "text/html";
        }

        return type;
    }

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        // standard date format: weekday, day month year hour:min:sec time_zone: found on StackOverflow, can also be found in java documentation: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.CANADA);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(calendar.getTime());
    }

    public String getResponse() {
        return response;
    }
}
