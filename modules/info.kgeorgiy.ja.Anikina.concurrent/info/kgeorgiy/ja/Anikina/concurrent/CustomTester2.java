package info.kgeorgiy.ja.Anikina.concurrent;

import info.kgeorgiy.java.advanced.mapper.ListMapperTest;
import info.kgeorgiy.java.advanced.mapper.ScalarMapperTest;

public class CustomTester2 extends Tester {
    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest2.class)
                .add("scalar", ScalarMapperTest.class)
                .add("list", ListMapperTest.class)
                .run(args);
    }

}
