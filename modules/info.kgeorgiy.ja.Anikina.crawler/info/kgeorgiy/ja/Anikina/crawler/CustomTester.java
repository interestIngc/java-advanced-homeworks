package info.kgeorgiy.ja.Anikina.crawler;


import info.kgeorgiy.java.advanced.crawler.EasyCrawlerTest;
import info.kgeorgiy.java.advanced.crawler.HardCrawlerTest;
import info.kgeorgiy.java.advanced.crawler.Tester;

public class CustomTester extends Tester {

    public static void main(String[] args) {
        new CustomTester()
                .add("Custom", CustomTest.class)
                .add("easy", EasyCrawlerTest.class)
                .add("hard", HardCrawlerTest.class)
                .run(args);
    }
}

