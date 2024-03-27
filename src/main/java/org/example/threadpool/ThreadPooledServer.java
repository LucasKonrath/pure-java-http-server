package org.example.threadpool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPooledServer implements Runnable {

    private int port = 8080;
    protected ServerSocket serverSocket = null;
    protected AtomicBoolean isRunning = new AtomicBoolean(true);

    protected Thread runningThread;

    protected ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();

    public ThreadPooledServer(int port){
        this.port = port;
    }

    public void run (){
        synchronized (this){
            this.runningThread = Thread.currentThread();
        }

        try {
            this.serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Error Opening Socket", e);
        }

        while(isRunning.get()){
            Socket clientSocket;
            try {
                clientSocket = this.serverSocket.accept();

            } catch (Exception e){
                if(!isRunning.get()){
                    System.out.println("Server is stopped");
                    break;
                }
                e.printStackTrace();
                throw new RuntimeException("Error accepting connection", e);
            }

            this.threadPool.execute(new SocketRunnable(clientSocket));
        }
        this.threadPool.shutdown();
    }
}
