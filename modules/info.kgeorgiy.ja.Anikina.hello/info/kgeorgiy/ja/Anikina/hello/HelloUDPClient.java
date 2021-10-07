package info.kgeorgiy.ja.Anikina.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {

    private final int TIME_OUT = 1000;

    public HelloUDPClient() {
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("invalid number of arguments");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            new HelloUDPClient().run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("invalid argument passed to function");
        }
    }


    private DatagramPacket makePacket(String request, SocketAddress address) {
        byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(
                requestBytes,
                requestBytes.length,
                address
                );
    }


    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        InetSocketAddress serverAddress = new InetSocketAddress(host, port);
        for (int i = 0; i < threads; i++) {
            final int finalI = i;
            executorService.submit(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.setSoTimeout(TIME_OUT);
                    for (int j = 0; j < requests; j++) {
                        String request = Utils.makeRequest(prefix, finalI, j);
                        DatagramPacket packet = makePacket(request, serverAddress);

                        int size = socket.getReceiveBufferSize();
                        DatagramPacket response = new DatagramPacket(
                                new byte[size],
                                size
                        );

                        while (true) {
                            try {
                                socket.send(packet);
                                socket.receive(response);
                                String reply = Utils.getStringFromPacket(response);
                                if (reply.contains(request)) {
                                    System.out.println("Request is: " + request);
                                    System.out.println("Response is: " + reply);
                                    break;
                                }
                            } catch (IOException e) {
                                System.err.println("error of connection occured");
                            }
                        }
                    }

                } catch (SocketException e) {
                    System.err.println("error occurred while connecting to server");
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(threads * requests * 10, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }
}
