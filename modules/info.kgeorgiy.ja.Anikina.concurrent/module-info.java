module info.kgeorgiy.ja.Anikina.concurrent {
    requires transitive info.kgeorgiy.java.advanced.concurrent;
    requires transitive info.kgeorgiy.java.advanced.mapper;
    requires java.compiler;

    exports info.kgeorgiy.ja.Anikina.concurrent;

    opens info.kgeorgiy.ja.Anikina.concurrent to junit;
}