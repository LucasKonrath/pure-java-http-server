package org.example;

import org.example.server.RouteResolver;
import org.example.threadpool.ThreadPooledServer;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {

       // Evolve server
       // Create an @Controller annotation with @Routes annotation - pattern matching - resolve args
       // Make a thread pool - currently single threaded


        ThreadPooledServer server = new ThreadPooledServer(9001);
        new Thread(server).start();

        while (true){}
    }
}