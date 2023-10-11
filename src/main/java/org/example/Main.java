package org.example;
import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
       try(ServerSocket socket = new ServerSocket(9001)){
        while (true){
            try(Socket client = socket.accept()){
                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String currLine;
                while(!(currLine = br.readLine()).isBlank()){
                    sb.append(currLine + "\r\n");
                }

                String request = sb.toString();

                String[] requestLines = request.split("\r\n");
                String[] httpRequestLine = requestLines[0].split(" ");
                String method = httpRequestLine[0];
                String path = httpRequestLine[1];
                String version = httpRequestLine[2];
                String host = requestLines[1].split(" ")[1];

                OutputStream os = client.getOutputStream();
                os.write("HTTP/1.1 200 OK\r\n".getBytes());
                os.write("ContentType: text/html\r\n".getBytes());
                os.write("\r\n".getBytes());

                if(path.equals("/uuid")){
                    os.write(("<h1>" + UUID.randomUUID() + "</h1>").getBytes());
                } else {
                    Path pathToFile = Paths.get("src/main/resources/test.html");
                    os.write(Files.readAllBytes(pathToFile));
                }

                os.write("\r\n\r\n".getBytes());
                os.flush();
            }
        }
       }
    }
}