package org.example;

import org.example.server.RouteResolver;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.UUID;

public class Main {
    public static void main(String[] args) throws Exception {

       // Evolve server
       // Create an @Controller annotation with @Routes annotation - pattern matching - resolve args
       // Make a thread pool - currently single threaded

       // handler
       // .routes("/uuid", uuidHandler)
       // .routes("/uuid/{size}", uuidHandlerSize) -> pattern match - less priority
       // .routes/uuid/size -> exact match - more priority

       // uuid/size?weight=300&length=2meters&vsf={'key' : 'object'}

       try(ServerSocket socket = new ServerSocket(9001)){
        while (true){
            try(Socket client = socket.accept()){
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
                String method = httpRequestLine[0];
                String path = httpRequestLine[1];
                String version = httpRequestLine[2];
                String host = requestLines[1].split(" ")[1];

                OutputStream os = client.getOutputStream();
                os.write("HTTP/1.1 200 OK\r\n".getBytes());
                os.write("ContentType: text/html\r\n".getBytes());
                os.write("\r\n".getBytes());

                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                Unsafe unsafe = (Unsafe) f.get(null);

                Method method1 = RouteResolver.resolve(httpRequestLine);
                if(method1 == null){
                    os.write("<h1>404 NOT FOUND</h1>\r\n\r\n".getBytes());
                    os.flush();
                } else {
                    Object controllerClass = unsafe.allocateInstance(method1.getDeclaringClass());

                    os.write(("<h1>" + method1.invoke(controllerClass).toString() + "</h1>\r\n\r\n").getBytes());
                    os.flush();
                }
            }
        }
       }
    }
}