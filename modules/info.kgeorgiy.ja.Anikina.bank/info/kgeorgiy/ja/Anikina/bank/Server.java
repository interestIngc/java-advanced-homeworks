package info.kgeorgiy.ja.Anikina.bank;

import java.rmi.*;
import java.rmi.server.*;
import java.net.*;

public final class Server {
    private final static int PORT = 8888;

    public static void main(final String... args) {

        final Bank bank = new RemoteBank(PORT);
        try {
            UnicastRemoteObject.exportObject(bank, PORT);
            Naming.rebind("//localhost/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
