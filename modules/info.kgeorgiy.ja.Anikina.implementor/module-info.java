module info.kgeorgiy.ja.Anikina.implementor {
    requires transitive info.kgeorgiy.java.advanced.implementor;
    requires java.compiler;

    exports info.kgeorgiy.ja.Anikina.implementor;

    opens info.kgeorgiy.ja.Anikina.implementor to junit;
}