package info.kgeorgiy.ja.Anikina.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private ExecutorService executorService;
    private DatagramSocket socket;


    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("invalid number of arguments");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            new HelloUDPServer().start(port, threads);
        } catch (NumberFormatException e) {
            System.err.println("invalid argument passed to function");
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            executorService = Executors.newFixedThreadPool(threads);
            final int size = socket.getReceiveBufferSize();
            for (int i = 0; i < threads; i++) {
                executorService.submit(() -> {
                    while (!socket.isClosed()) {
                        try {
                            DatagramPacket request = new DatagramPacket(
                                    new byte[size],
                                    size
                            );
                            socket.receive(request);
                            String response = "Hello, " + new String(
                                    request.getData(),
                                    request.getOffset(),
                                    request.getLength(),
                                    StandardCharsets.UTF_8
                            );
                            byte[] responseToByteArray = response.getBytes(StandardCharsets.UTF_8);
                            DatagramPacket sendResponse = new DatagramPacket(
                                    responseToByteArray,
                                    responseToByteArray.length,
                                    request.getSocketAddress()
                            );
                            socket.send(sendResponse);
                        } catch (IOException ignored) {
                        }
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("error while  opening socket occurred");
        }

    }

    @Override
    public void close() {
        executorService.shutdown();
        if (socket != null) {
            socket.close();
        }
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("thread interrupted");
        }
    }
}
