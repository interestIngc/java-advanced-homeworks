package info.kgeorgiy.ja.Anikina.mapper;


import info.kgeorgiy.java.advanced.mapper.ListMapperTest;
import info.kgeorgiy.java.advanced.mapper.ScalarMapperTest;

public class CustomTester extends Tester {

    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest.class)
                .add("scalar", ScalarMapperTest.class)
                .add("list", ListMapperTest.class)
                .run(args);
    }
}
