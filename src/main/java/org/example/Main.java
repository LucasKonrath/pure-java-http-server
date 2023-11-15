package org.example;

import org.example.threadpool.ThreadPooledServer;


public class Main {

    // Make get with parameters
    public static void main(String[] args) throws Exception {
        ThreadPooledServer server = new ThreadPooledServer(9001);
        new Thread(server).start();

        while (true){}
    }
}