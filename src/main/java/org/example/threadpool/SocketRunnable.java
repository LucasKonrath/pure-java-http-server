package org.example.threadpool;

import org.example.enums.HttpStatus;
import org.example.response.HttpResponse;
import org.example.server.RouteResolver;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.stream.Collectors;

public class SocketRunnable implements Runnable {

    protected Socket client = null;

    public SocketRunnable(Socket clientSocket) {
        this.client = clientSocket;
    }

    public void run() {
        try {
            long time = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            InputStream is = client.getInputStream();
            do {
                sb.append((char) is.read());
            } while (is.available() > 0);
            String request = sb.toString();


//            String request = br.lines().collect(Collectors.joining("\r\n"));

            String[] requestLines = request.split("\r\n");
            String[] httpRequestLine = requestLines[0].split(" ");

            if(httpRequestLine.length == 1){
                return;
            }

            String body = null;
            if("POST".equals(httpRequestLine[0]) || "PUT".equals(httpRequestLine[0])) {
                body = requestLines[requestLines.length - 1];
            }

            HttpResponse response = RouteResolver.process(httpRequestLine, body);
            writeToOutputStream(response);
            System.out.println("Request processed, time: " + (System.currentTimeMillis() - time));
        } catch (Exception e) {
            e.printStackTrace();
            HttpResponse response = new HttpResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
            try {
                writeToOutputStream(response);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    private void writeToOutputStream(HttpResponse response) throws IOException {
        OutputStream os = client.getOutputStream();
        os.write(String.format("HTTP/1.1 %d\r\n", response.getStatus().getCode()).getBytes());
        os.write("ContentType: text/html\r\n".getBytes());
        os.write("\r\n".getBytes());
        os.write(("<h1>" + response.getResponseObject().toString() + "</h1>\r\n\r\n").getBytes());
        os.flush();
        os.close();
    }
}
