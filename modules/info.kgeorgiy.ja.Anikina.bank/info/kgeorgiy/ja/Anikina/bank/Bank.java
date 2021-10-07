package info.kgeorgiy.ja.Anikina.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Bank extends Remote {

    Account createAccount(final String passport,
                          final String subId) throws RemoteException;

    Map<String, Account> getPersonAccounts(String passport);

    Person getRemotePerson(String passport);

    LocalPerson getLocalPerson(String passport);

    boolean checkPerson(String firstName, String secondName, String passport);

    Account getAccount(final String passport, final String subId);

    Person addPerson(String firstName, String secondName, String passport) throws RemoteException;
}
