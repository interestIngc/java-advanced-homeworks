package info.kgeorgiy.ja.Anikina.bank;

import java.io.Serializable;
import java.util.Map;

public class LocalPerson extends AbstractPerson {

    Map<String, Account> accounts;

    public LocalPerson(String firstName, String lastName, String passport, Map<String, Account> accounts) {
        super(firstName, lastName, passport);
        this.accounts = Map.copyOf(accounts);
    }

    public Account getAccount(String subId) {
        return accounts.get(subId);
    }

    public Map<String, Account> getAccounts() {
        return Map.copyOf(accounts);
    }
}
