package org.example.threadpool;

import org.example.enums.HttpStatus;
import org.example.response.HttpResponse;
import org.example.server.RouteResolver;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class SocketRunnable implements Runnable {

    protected Socket client = null;

    public SocketRunnable(Socket clientSocket) {
        this.client = clientSocket;
    }

    public void run() {
        try {
            long time = System.currentTimeMillis();
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String currLine;

            while (!Objects.equals(currLine = br.readLine(), "")) {
                sb.append(currLine + "\r\n");
            }

            sb.append("\r\n");

            String request = sb.toString();

            String[] requestLines = request.split("\r\n");
            String[] httpRequestLine = requestLines[0].split(" ");

            HttpResponse response = RouteResolver.process(httpRequestLine);
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
