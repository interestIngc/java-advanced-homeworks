package info.kgeorgiy.ja.Anikina.bank;

import java.io.Serializable;
import java.util.Map;

public abstract class AbstractPerson implements Serializable, Person {
    protected String firstName;
    protected String secondName;
    protected String passport;

    public AbstractPerson(String firstName, String secondName, String passport) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.passport = passport;
    }

    public String getFirstName() {
        System.out.println("first name for person is " + firstName);
        return firstName;
    }

    public String getSecondName() {
        System.out.println("last name for person is " + secondName);
        return secondName;
    }

    public String getPassport() {
        System.out.println("passport for person " + firstName + " " + secondName + " is " + passport);
        return passport;
    }

    public String toString() {
        return getFirstName() + " " + getSecondName() + " " + getPassport();
    }

}
