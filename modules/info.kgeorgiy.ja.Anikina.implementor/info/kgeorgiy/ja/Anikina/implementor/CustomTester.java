package info.kgeorgiy.ja.Anikina.implementor;


import info.kgeorgiy.java.advanced.implementor.ClassImplementorTest;
import info.kgeorgiy.java.advanced.implementor.ClassJarImplementorTest;
import info.kgeorgiy.java.advanced.implementor.InterfaceImplementorTest;
import info.kgeorgiy.java.advanced.implementor.Tester;

public class CustomTester extends Tester {

    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest.class)
                .add("interface", InterfaceImplementorTest.class)
                .add("class", ClassImplementorTest.class)
                .add("jar-class", ClassJarImplementorTest.class)
                .run(args);
    }
}