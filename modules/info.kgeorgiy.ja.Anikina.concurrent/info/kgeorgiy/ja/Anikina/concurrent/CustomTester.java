package info.kgeorgiy.ja.Anikina.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIPTest;
import info.kgeorgiy.java.advanced.concurrent.ScalarIPTest;
import info.kgeorgiy.java.advanced.concurrent.Tester;

public class CustomTester extends Tester {

    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest.class)
                .add("scalar", ScalarIPTest.class)
                .add("list", ListIPTest.class)
                .run(args);
    }
}
