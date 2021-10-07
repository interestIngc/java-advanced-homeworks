package info.kgeorgiy.ja.Anikina.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount implements Account, Serializable {

    private final String id;
    private int amount;

    public LocalAccount(String id) {
        this.id = id;
        this.amount = 0;
    }

    @Override
    public String getId() throws RemoteException {
        return id;
    }

    @Override
    public int getAmount() throws RemoteException {
        return amount;
    }

    @Override
    public void setAmount(int amount) throws RemoteException {
        this.amount = amount;
    }
}
