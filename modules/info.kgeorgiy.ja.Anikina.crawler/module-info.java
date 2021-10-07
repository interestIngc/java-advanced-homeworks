module info.kgeorgiy.ja.Anikina.crawler {
    requires transitive info.kgeorgiy.java.advanced.crawler;
    requires java.compiler;

    exports info.kgeorgiy.ja.Anikina.crawler;

    opens info.kgeorgiy.ja.Anikina.crawler to junit;
}