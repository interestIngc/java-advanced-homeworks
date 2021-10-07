package info.kgeorgiy.ja.Anikina.bank;

import java.rmi.Remote;
import java.util.Map;

public class RemotePerson extends AbstractPerson {

    final Bank bank;

    public RemotePerson(String firstName, String lastName, String passport, Bank bank) {
        super(firstName, lastName, passport);
        this.bank = bank;
    }

}
