package info.kgeorgiy.ja.Anikina.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String,Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Map<String, Account>> accountsPerPerson =
            new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Person addPerson(String firstName,
                                    String secondName,
                                    String passport) throws RemoteException {
        if (!persons.containsKey(passport)) {
            System.out.println("adding person with passport " + passport);
            final RemotePerson current = new RemotePerson(
                    firstName,
                    secondName,
                    passport,
                    this);
            UnicastRemoteObject.exportObject(current, port);
            accountsPerPerson.put(passport, new ConcurrentHashMap<>());
            persons.put(passport, current);
            return current;
        }
        if (!checkPerson(firstName, secondName, passport)) {
            System.out.println("incorrect data, person with passport " +
                    passport + " already exists!");
            return null;
        }
        return persons.get(passport);
    }

    @Override
    public LocalPerson getLocalPerson(String passport) {
        Person person = persons.get(passport);
        if (person != null) {
            return new LocalPerson(
                    person.getFirstName(),
                    person.getSecondName(),
                    passport,
                    accountsPerPerson.get(passport));
        }
        return null;
    }

    @Override
    public Person getRemotePerson(String passport) {
        System.out.println("getting remote person with passport " + passport);
        return persons.get(passport);
    }

    @Override
    public Account createAccount(final String passport,
                                 final String subId) throws RemoteException {
        String id = getId(passport, subId);
        System.out.println("Creating account " + id);

        Person person = getRemotePerson(passport);
        if (person == null) {
            System.out.println("no such person exists");
            return null;
        }

        if (!accounts.containsKey(id)) {
            final Account account = new RemoteAccount(id);
            UnicastRemoteObject.exportObject(account, port);
            accounts.put(id, account);
            accountsPerPerson.get(passport).put(subId, account);
            return account;
        } else {
            return getAccount(id);
        }
    }

    private String getId(String passport, String subId) {
        return passport + ":" + subId;
    }

    @Override
    public boolean checkPerson(String firstName, String secondName, String passport) {
        Person person = persons.get(passport);
        if (person == null) {
            return false;
        }
        return person.getFirstName().equals(firstName)
                && person.getSecondName().equals(secondName);
    }

    @Override
    public Map<String, Account> getPersonAccounts(String passport) {
        Person person = persons.get(passport);
        if (person == null) {
            return null;
        }
        return accountsPerPerson.get(person.getPassport());
    }

    private Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Account getAccount(final String passport, final String subId) {
        return accounts.get(getId(passport, subId));
    }
}
