module info.kgeorgiy.ja.Anikina.hello {
    requires transitive info.kgeorgiy.java.advanced.hello;
    requires java.compiler;

    exports info.kgeorgiy.ja.Anikina.hello;

    opens info.kgeorgiy.ja.Anikina.hello to junit;
}