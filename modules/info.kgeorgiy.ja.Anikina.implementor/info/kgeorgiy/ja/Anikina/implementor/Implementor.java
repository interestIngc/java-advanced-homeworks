package info.kgeorgiy.ja.Anikina.implementor;



import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

    /**
     * @author Veronika Anikina
     */
public class Implementor implements JarImpler {

    /**
     * a wrapper for {@link Method}
     */
    public static class NewMethod {
        /**
         * current {@link Method}
         */
        private final Method method;

        /**
         * constructor for {@link NewMethod}
         * @param method {@link Method} that the new instance of
         * {@link NewMethod NewMethod} should be constructed
         */
        public NewMethod(Method method) {
            this.method = method;
        }

        /**
         * checks if two instances of {@link NewMethod} are equal
         * @param obj instance of a {@link NewMethod}
         * @return true if two instances of {@link NewMethod} are equal,
         * they are equal if
         * they have the same return type,
         * same name,
         * same number of parameters and same parameter types
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NewMethod) {
                NewMethod o = (NewMethod) obj;
                if (hashCode() == o.hashCode()) {
                    return method.getReturnType() == o.method.getReturnType()
                            && method.getName().equals(o.method.getName())
                            && Arrays.equals(method.getParameterTypes(), o.method.getParameterTypes());
                }
            }
            return false;
        }

        /**
         * constant for hashing
         */
        private static final int MOD = (int) 1e9;

        /**
         * generates hashcode
         * @return hashcode of this instance of {@link NewMethod} class
         */
        @Override
        public int hashCode() {
            return (method.getReturnType().hashCode()
                    + method.getName().hashCode()
                    + Arrays.hashCode(method.getParameterTypes())) * 30 % MOD;
        }
    }

    /**
     * constructs a new instance of {@link Implementor}
     */

    public Implementor() {
    }

    /**
     *
     * @param args input to the program
     * requires 2 or 3 arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 3) {
            System.err.println("invalid number of args");
            return;
        }
        if (args.length == 3 && !args[0].equals("-jar")) {
            System.err.println("invalid first argument");
        }
        for (String arg : args) {
            if (arg == null) {
                System.err.println("null argument passed");
                return;
            }
        }
        int pos = args[0].equals("-jar") ? 1 : 0;
        try {
            Class<?> clazz = Class.forName(args[pos]);
            if (pos == 0) {
                new Implementor().implement(clazz, Paths.get(args[1]));
            } else {
                new Implementor().implementJar(clazz, Paths.get(args[2]));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("class not found " + e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("invalid path to the file " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("exception while execution occurred " + e.getMessage());
        }
    }

    /**
     * gets directory of <code>token</code>
     * @param token is a class or an interface
     * @return {@link String} from package directory of <code>token</code>
     */

    String getFileDirectory(Class<?> token) {
        return token.getPackage().getName()
                .replace('.', File.separatorChar);
    }

    /**
     * gets name of implementation
     * @param token is a class or an interface
     * @return name of this <code>token</code>'s implementation
     */
    String getFileName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     *
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException if either arguments are invalid,
     * or error while creating directory for implementation occurred,
     * or, if  <code>token</code> is class which doesn't have any public constructors,
     * or if error while writing to the file occurred
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        check(token, root);
        root = getValidPath(token, root, ".java");
        createDirectories(root);
        String file = getHeader(token) + getConstructors(token) + getMethods(token) + "\n}";
        try (BufferedWriter writer = Files.newBufferedWriter(root)) {
            StringBuilder sb = new StringBuilder();
            for (char ch : file.toCharArray()) {
                sb.append(ch >= 128 ? String.format("\\u%04X", (int) ch) : Character.toString(ch));
            }
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new ImplerException("error while writing occurred", e);
        }
    }


    /**
     *
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException if the compiler was not created,
     * or the compilation error occurred,
     * or when error while writing to jar occurred
     * @see Implementor#implement(Class, Path) for other posssible throws
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        check(token, jarFile);
        Path currDir = Paths.get(".");
        implement(token, currDir);
        Path source = getValidPath(token, currDir, ".java");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("error while creating compiler occurred");
        }
        String[] args = {
                "-cp",
                currDir.toString() + File.pathSeparator + getClassPath(token),
                source.toString()
        };
        int res = compiler.run(null, null, null, args);
        if (res != 0) {
            throw new ImplerException("compilation error");
        }
        Path classFile = getValidPath(token, currDir, ".class");
        String className = Path.of(getFileDirectory(token), token.getSimpleName()).toString()
            .replace('\\', '/') + "Impl.class";
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream writer = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            writer.putNextEntry(new ZipEntry(className));
            Files.copy(classFile, writer);
        } catch (IOException e) {
            throw new ImplerException("error while writing to jar ", e);
        }
    }


    /**
     * gets class path of <code>token</code>
     * @param token class or interface
     * @return class path of <code>token</code>
     * @throws ImplerException when class path can't be found
     */
    private String getClassPath(Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new ImplerException("can't get valid class path");
        }
    }


    /**
     * checks if arguments passed to the program are valid
     * @param token is a class or interface
     * @param root the directory where implementation to be located
     * @throws ImplerException if any argument is null, or if <code>token</code> is not
     * a class, or if it's impossible to create implementation for <code>token</code>
     */
    private void check(Class<?> token, Path root) throws ImplerException {
        if (token == null) {
            throw new ImplerException("token is null");
        }
        if (root == null) {
            throw new ImplerException("root is null");
        }
        if (token.isPrimitive() || token.isArray() || token == Enum.class) {
            throw new ImplerException("invalid token");
        }
        if (Modifier.isFinal(token.getModifiers()) || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("invalid token's modifier");
        }
    }

    /**
     * makes {@link Path} path of <code>token</code> implementation
     * @param token class or interface
     * @param root {@link Path} path to create file
     * @param suffix the extension of created file
     * @return {@link Path} of created file
     */

    private Path getValidPath(Class<?> token, Path root, String suffix) {
        return root.resolve(getFileDirectory(token))
                .resolve(getFileName(token) + suffix);
    }

    /**
     * creates directories for the file
     * @param root path location
     * @throws ImplerException if error while creating directory occurred
     */

    private void createDirectories(Path root) throws ImplerException {
        if (root.getParent() != null) {
            try {
                Files.createDirectories(root.getParent());
            } catch (IOException e) {
                throw new ImplerException("can't create directory");
            }
        }
    }

    /**
     * creates header of file
     * @param token class or interface being extended/implemented
     * @return header of created file
     */

    private String getHeader(Class<?> token) {
        String header = "";
        Package currPackage = token.getPackage();
        if (currPackage != null) {
            header = "package " + currPackage.getName() + ";\n";
        }
        header += ("public class " + getFileName(token) + " "
                + (token.isInterface() ? "implements " : "extends ")
                + token.getCanonicalName() + " {\n");
        return header;
    }

    /**
     * creates constructors of <code>token</code> implementation
     * @param token class or interface
     * @return the {@link String} of constructors of <code>token</code>Impl
     * @throws ImplerException if no constructors could be created
     */

    private String getConstructors(Class<?> token) throws ImplerException {
        if (token.isInterface()) return "";
        StringBuilder sb = new StringBuilder();
        for (Constructor<?> constructor : token.getDeclaredConstructors()) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                continue;
            }
            sb.append(methodToString(constructor)).append("\n");
        }
        String res = sb.toString();
        if (res.isEmpty()) {
            throw new ImplerException("can't extend from this class " + token.getCanonicalName());
        }
        return res;
    }


    /**
     * gets abstract methods to be implemented in the <code>arrayOfMethods</code>
     * @param arrayOfMethods array of methods of some ancestor
     * @param methods current set of {@link Method} methods
     */
    private void getAbstractMethods(Method[] arrayOfMethods, Set<NewMethod> methods) {
        for (Method m : arrayOfMethods) {
            if (Modifier.isAbstract(m.getModifiers())) {
                methods.add(new NewMethod(m));
            }
        }
    }

    /**
     * gets all necessary implementations of methods of <code>token</code>Impl
     * @param token class or interface to be extended/implemented
     * @return {@link String} of <code>token</code>Impl methods extended
     */

    private String getMethods(Class<?> token) {
        Set<NewMethod> methods = new HashSet<>();
        getAbstractMethods(token.getMethods(), methods);
        while (token != null) {
            getAbstractMethods(token.getDeclaredMethods(), methods);
            token = token.getSuperclass();
        }
        StringBuilder sb = new StringBuilder();
        for (NewMethod method : methods) {
            sb.append(methodToString(method.method)).append("\n");
        }
        return sb.toString();
    }

    /**
     * gets returnType and name of <code>executable</code>
     * @param executable a {@link Method} or a {@link Constructor}
     * @return {@link String} of <code>executable</code> returnType + name,
     * if it's {@link Method}
     * or just name of declaring class,
     * if it's a {@link Constructor}
     */
    private String returnTypeAndName(Executable executable) {
            if (executable instanceof Method) {
                Method method = (Method) executable;
                return method.getReturnType().getCanonicalName() + " " + method.getName();
            }
            return getFileName(((Constructor<?>) executable).getDeclaringClass());
    }

    /**
     * gets implementation of <code>executable</code>
     * @param executable is {@link Method} or {@link Constructor}
     * @return {@link String} of full method's or constructor's implementation
     */
    private String methodToString(Executable executable) {
        String result;
        int modifier = executable.getModifiers()
                & ~Modifier.TRANSIENT
                & ~Modifier.ABSTRACT
                & ~Modifier.NATIVE;
        result = Modifier.toString(modifier) + (modifier == 0 ? "" : " ")
                + returnTypeAndName(executable) +
                "(" + getParameters(executable, true) + ")"
                + getExceptions(executable.getExceptionTypes())
                + " {\n" + getBody(executable) + "\n}";
        return result;
    }

    /**
     * returns {@link String} from <code>parameter</code>
     * @param parameter is a {@link Parameter}
     * @param type true if type of <code>parameter</code> needed
     * @return {@link String} of <code>parameter</code>'s type and name,
     * if <code>type</code> == true,
     * or just <code>parameter</code>'s name if not
     */
    String getStringFromParameter(Parameter parameter, boolean type) {
            return (type ? parameter.getType().getCanonicalName() + " " : "") + parameter.getName();
    }

    /**
     * gets {@link String} of <code>executable</code>'s all parameters
     * @param executable is {@link Method} or {@link Constructor}
     * @param type true if parameter types are needed
     * @return {@link String} of <code>executable</code> parameters
     */
    private String getParameters(Executable executable, boolean type) {
            return Arrays.stream(executable.getParameters())
                    .map(parameter -> getStringFromParameter(parameter, type))
                    .collect(Collectors.joining(", "));
    }

    /**
     * gets body of <code>executable</code>
     * @param executable is {@link Method} or {@link Constructor}
     * @return {@link String} of <code>executable</code> body
     */
    private String getBody(Executable executable) {
            if (executable instanceof Method) {
                return getBodyOfMethod((Method) executable);
            }
            return getBodyOfConstructor((Constructor<?>) executable);
    }

    /**
     * gets <code>method</code>'s body
     * @param method is {@link Method}
     * @return {@link String} of <code>method</code> body, returning a default value
     */
    private String getBodyOfMethod(Method method) {
        Class<?> type = method.getReturnType();
        if (type.getCanonicalName().equals("void")) {
            return "return;";
        } else if (type.getCanonicalName().equals("boolean")) {
            return "return false;";
        } else if (type.isPrimitive()) {
            return "return 0;";
        }
        return "return null;";
    }


    /**
     * gets body of a <code>constructor</code>
     * @param constructor is {@link Constructor}
     * @return {@link String} of <code>constructor</code> body
     */
    private String getBodyOfConstructor(Constructor<?> constructor) {
        return "super(" + getParameters(constructor, false) + ");\n";
    }

    /**
     * gets a string of {@link Executable} exception types
     * @param types an {@link Array} of {@link Executable} exceptions types
     * @return {@link String} from <code>types</code> in format "throws exception1, exception2..."
     * or empty {@link String}
     */
    private String getExceptions(Class<?>[] types) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i == 0) {
                sb.append(" throws ");
            } else {
                sb.append(", ");
            }
            sb.append(types[i].getCanonicalName());
        }
        return sb.toString();
    }
}
