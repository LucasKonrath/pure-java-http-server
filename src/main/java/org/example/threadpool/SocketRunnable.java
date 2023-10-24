package org.example.threadpool;

import org.example.server.RouteResolver;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
            if (method1 == null) {
                os.write("<h1>404 NOT FOUND</h1>\r\n\r\n".getBytes());
                os.flush();
            } else {
                Object controllerClass = unsafe.allocateInstance(method1.getDeclaringClass());

                os.write(("<h1>" + method1.invoke(controllerClass).toString() + "</h1>\r\n\r\n").getBytes());
                os.flush();
            }

            os.close();
            System.out.println("Request processed, time: " + (System.currentTimeMillis() - time));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
