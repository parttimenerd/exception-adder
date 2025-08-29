package me.bechberger.exploder;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.implementation.ExceptionMethod;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.lang.instrument.Instrumentation;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Byte Buddy agent that probabilistically replaces methods with
 * `throw new <Exception>()`. Classes are matched by a glob pattern.
 *
 * Commands:
 *   help   -> show usage and exit
 *   reset  -> reset previous transformations and exit
 *
 * Example:
 *   java -javaagent:exploder.jar="glob=com.example.*Service,classProb=0.3,methodProb=0.7,exception=java.lang.IllegalStateException" -jar myapp.jar
 */
public class ExploderAgent {
    private static ResettableClassFileTransformer lastInstallation;
    private static final AgentBuilder.RedefinitionStrategy STRATEGY =
            AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        install(inst, agentArgs);
        forceRetransform(inst);
    }

    private static void forceRetransform(Instrumentation inst) {
        try {
            Class<?>[] loadedClasses = inst.getAllLoadedClasses();
            for (Class<?> cls : loadedClasses) {
                if (inst.isModifiableClass(cls) && !cls.isInterface() && !cls.isArray() && !cls.isPrimitive()) {
                    try {
                        inst.retransformClasses(cls);
                    } catch (Exception e) {
                        // Ignore individual class transformation failures
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during force retransform: " + e.getMessage());
        }
    }

    private static void install(Instrumentation inst, String agentArgs) {
        if (agentArgs != null) {
            String trimmed = agentArgs.trim().toLowerCase();
            if ("help".equals(trimmed)) {
                printHelp();
                return;
            }
            if ("reset".equals(trimmed)) {
                if (lastInstallation != null) {
                    System.out.println("Resetting previous Byte Buddy installation...");
                    lastInstallation.reset(inst, STRATEGY);
                    lastInstallation = null;
                } else {
                    System.out.println("No installation to reset.");
                }
                return;
            }
        }

        // Parse arguments
        Map<String, String> args = parseArgs(agentArgs);
        String classGlob = args.getOrDefault("glob", "**");
        double classProb = Double.parseDouble(args.getOrDefault("classProb", "0.5"));
        double methodProb = Double.parseDouble(args.getOrDefault("methodProb", "0.5"));
        String exceptionClassName = args.getOrDefault("exception", "java.lang.RuntimeException");
        boolean verbose = Boolean.parseBoolean(args.getOrDefault("verbose", "false"));

        // Reset last installation automatically
        if (lastInstallation != null) {
            if (verbose) {
                System.out.println("Resetting previous Byte Buddy installation...");
            }
            lastInstallation.reset(inst, STRATEGY);
            lastInstallation = null;
        }

        Class<? extends Throwable> exceptionClass;
        try {
            exceptionClass = Class.forName(exceptionClassName).asSubclass(Throwable.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid exception class: " + exceptionClassName, e);
        }

        if (verbose) {
            System.out.printf("Exploder config: glob=%s, classProb=%.2f, methodProb=%.2f, exception=%s%n",
                    classGlob, classProb, methodProb, exceptionClass.getName());
        }

        SecureRandom random = new SecureRandom();
        String regex = GlobToRegex.toRegex(classGlob);

        AgentBuilder builder = new AgentBuilder.Default()
                .ignore(
                        ElementMatchers.nameStartsWith("net.bytebuddy.")
                                .or(ElementMatchers.nameStartsWith("java."))
                                .or(ElementMatchers.nameStartsWith("jdk."))
                                .or(ElementMatchers.nameStartsWith("sun."))
                                .or(ElementMatchers.nameStartsWith("javax"))
                                .or(ElementMatchers.nameStartsWith("com.sun."))
                                .or(ElementMatchers.nameStartsWith("me.bechberger.exploder")) // avoid self-instrumentation
                                .or(ElementMatchers.isSynthetic())
                )
                .with(STRATEGY)
                .type(ElementMatchers.nameMatches(regex))
                .transform((b, type, cl, module, domain) -> {
                    if (random.nextDouble() <= classProb) {
                        if (verbose) {
                            System.out.println("Exploding class: " + type.getName());
                        }
                        return b.method(
                                        ElementMatchers.isDeclaredBy(type)
                                                .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                                                .and(ElementMatchers.not(ElementMatchers.isAbstract()))
                                                .and(ElementMatchers.not(ElementMatchers.isNative()))
                                                .and(ElementMatchers.not(ElementMatchers.isTypeInitializer()))
                                                .and(ElementMatchers.not(ElementMatchers.isSynthetic()))
                                                .and(ElementMatchers.not(ElementMatchers.isBridge()))
                                                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("main"))) // avoid breaking main methods
                                )
                                .intercept(new ProbabilisticImplementation(random, methodProb, exceptionClass, verbose));
                    }
                    return b;
                });

        lastInstallation = builder.installOn(inst);
    }

    private static Map<String, String> parseArgs(String agentArgs) {
        Map<String, String> map = new HashMap<>();
        if (agentArgs == null || agentArgs.isBlank()) return map;
        for (String kv : agentArgs.split(",")) {
            String[] parts = kv.split("=", 2);
            if (parts.length == 1 && parts[0].trim().equalsIgnoreCase("verbose")) {
                map.put("verbose", "true");
            } else if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }
        return map;
    }

    private static void printHelp() {
        System.out.println("ExploderAgent - Byte Buddy chaos agent\n");
        System.out.println("Usage:");
        System.out.println("  -javaagent:exploder-agent.jar=\"glob=<pattern>,classProb=<0-1>,methodProb=<0-1>,exception=<ex>\"");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  glob       Glob pattern for class names (default=**)");
        System.out.println("  classProb  Probability [0-1] to modify a class (default=0.5)");
        System.out.println("  methodProb Probability [0-1] to modify a method (default=0.5)");
        System.out.println("  exception  Exception class to throw (default=java.lang.RuntimeException)");
        System.out.println("  verbose    If set, prints detailed info about each method decision (default=false)");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  help   Print this help and exit");
        System.out.println("  reset  Reset previous transformations and exit");
        System.out.println();
    }
}