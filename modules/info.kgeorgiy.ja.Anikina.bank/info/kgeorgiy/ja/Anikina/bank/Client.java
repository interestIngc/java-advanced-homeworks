package info.kgeorgiy.ja.Anikina.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }
        if (args.length != 5) {
            System.err.println("invalid number of args, 5 needed");
            return;
        }

        String firstName = args[0];
        String secondName = args[1];
        String passport = args[2];
        String subId = args[3];
        int amount = Integer.parseInt(args[4]);

        final AbstractPerson person = bank.addPerson(firstName, secondName, passport);
        final Account account = bank.createAccount(passport, subId);
        System.out.println("person is " + person.toString());
        System.out.println("current amount on account is " + account.getAmount());
        System.out.println("setting " + amount);
        account.setAmount(amount);
        System.out.println("current amount on account is " + account.getAmount());
    }
}
