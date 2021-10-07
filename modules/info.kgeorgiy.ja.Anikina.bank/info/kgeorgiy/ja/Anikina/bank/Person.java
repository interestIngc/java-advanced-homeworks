package info.kgeorgiy.ja.Anikina.bank;

import java.rmi.Remote;

public interface Person extends Remote {

    String getFirstName();

    String getSecondName();

    String getPassport();

}
