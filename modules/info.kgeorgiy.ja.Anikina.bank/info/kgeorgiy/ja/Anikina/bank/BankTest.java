package info.kgeorgiy.ja.Anikina.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;


public class BankTest {
    private Registry registry;
    private final int PORT  = 8888;
    private final String NAME = String.format("//localhost:%d/bank", PORT);
    private Bank bank;


    @BeforeAll
    public void createRegistry() throws RemoteException {
        try {
            LocateRegistry.createRegistry(PORT);
        } catch (ExportException e) {
            System.out.println("registry already started");
        }
        registry = LocateRegistry.getRegistry(PORT);
    }

    @BeforeEach
    public void rebindBank() throws RemoteException {
        bank = new RemoteBank(PORT);
        UnicastRemoteObject.exportObject(bank, PORT);
        registry.rebind(NAME, bank);
    }

    @AfterEach
    public void unbindBank() throws RemoteException {
        try {
            registry.unbind(NAME);
        } catch (NotBoundException e) {
            System.out.println("couldn't unbind bank");
        }
    }

    private Bank getBank() {
        Bank bank = null;
        try {
            bank = (Bank) registry.lookup(NAME);
        } catch (NotBoundException e) {
            System.out.println("couldn't find bank");
        } catch (RemoteException ignored) {
        }
        return bank;
    }

    private void checkPerson(Person person,
                             String firstName,
                             String secondName,
                             String passport) {
        assertEquals(person.getFirstName(), firstName);
        assertEquals(person.getSecondName(), secondName);
        assertEquals(person.getPassport(), passport);
    }

    private Person addPerson() throws RemoteException {
        return bank.addPerson("peter",
                "ivanov",
                "111");
    }


    @Test
    public void test1_checkPersonGetAndSet() throws RemoteException {
        Person person = addPerson();
        checkPerson(person,
                "peter", "ivanov", "111");
    }

    @Test
    public void test2_checkAccount() {
        try {
            addPerson();
            Account account = bank.createAccount("111", "aa");
            assertEquals(account.getAmount(), 0);
            account.setAmount(100);
            assertEquals(account.getAmount(), 100);
        } catch (RemoteException ignored) {
        }
    }

    @Test
    public void test3_checkNotExistingPerson() {
        try {
            addPerson();
            assertNull(bank.getRemotePerson("222"));
            Person person = bank.getRemotePerson("111");
            assertNotNull(person);
            checkPerson(person,
                    "peter", "ivanov", "111");
        } catch (RemoteException ignored) {
        }
    }

    @Test
    public void test4_checkManyAccs() {
        try {
            addPerson();
            Account account1 = bank.createAccount("111", "a");
            Account account2 = bank.createAccount("111", "b");
            Account account3 = bank.createAccount("111", "c");
            bank.createAccount("111", "c");
            account1.setAmount(100);
            account2.setAmount(200);
            account3.setAmount(300);
            Map<String, Account> accounts = bank.getPersonAccounts("111");
            assertEquals(accounts.size(), 3);
            TreeMap<String, Account> map = new TreeMap<>(accounts);
            char ch = 'a';
            int pos = 1;
            for (Map.Entry<String, Account> account : map.entrySet()) {
                assertEquals(account.getKey(), String.valueOf(ch));
                assertEquals(account.getValue().getAmount(), pos * 100);
                ch++;
                pos++;
            }
        } catch (RemoteException ignored) {
        }
    }


    @Test
    public void test5_checkLocalPerson()  {
        try {
            addPerson();
            bank.createAccount("111", "a").setAmount(100);
            LocalPerson person = bank.getLocalPerson("111");
            Account account = person.getAccount("a");
            assertEquals(account.getAmount(), 100);
            account.setAmount(200);
            assertNotEquals(account.getAmount(),
                    bank.getAccount("111", "a").getAmount());
        } catch (RemoteException ignored) {
        }
    }

    @Test
    public void test6_checkLocalAndRemote() {
        try {
            addPerson();
            bank.createAccount("111", "a").setAmount(100);
            LocalPerson person1 = bank.getLocalPerson("111");
            person1.getAccount("a").setAmount(150);
            LocalPerson person2 = bank.getLocalPerson("111");
            person2.getAccount("a").setAmount(200);
            assertEquals(person1.getAccount("a").getAmount(), 150);
            assertEquals(person2.getAccount("a").getAmount(), 200);
            assertEquals(bank.getAccount("111", "a").getAmount(), 100);
        } catch (RemoteException ignored) {
        }
    }

    @Test
    public void test7_checkAddAfterFirst() {
        try {
            addPerson();
            bank.createAccount("111", "a").setAmount(100);
            LocalPerson person1 = bank.getLocalPerson("111");
            bank.getAccount("111", "a").setAmount(200);
            LocalPerson person2 = bank.getLocalPerson("111");
            assertEquals(person1.getAccount("a").getAmount(),
                    100);
            assertEquals(person2.getAccount("a").getAmount(),
                    200);
        } catch (RemoteException ignored) {
        }
    }


}
