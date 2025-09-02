package me.bechberger.exploder;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.security.SecureRandom;

/**
 * Add exceptions to methods probabilistically.
 */
public class ExceptionAdder {
    private static final Instrumentation inst = ByteBuddyAgent.install();
    private static ResettableClassFileTransformer installedTransformer;
    private static final AgentBuilder.RedefinitionStrategy STRATEGY =
            AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;

    /**
     * Explode methods according to the given args.
     * If there is already an active transformation, it will be reset first.
     */
    public static void explode(ExplodeArgs args) {
        // Reset last installation automatically
        if (installedTransformer != null) {
            installedTransformer.reset(inst, STRATEGY);
            installedTransformer = null;
        }
        Class<? extends Throwable> exceptionClass;
        try {
            exceptionClass = Class.forName(args.exception()).asSubclass(Throwable.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid exception class: " + args.exception(), e);
        }
        SecureRandom random = new SecureRandom();
        String regex = GlobToRegex.toRegex(args.glob());
        AgentBuilder builder = new AgentBuilder.Default()
                .ignore(
                        ElementMatchers.nameStartsWith("net.bytebuddy.")
                                .or(ElementMatchers.nameStartsWith("me.bechberger.exploder")) // avoid self-instrumentation
                                .or(ElementMatchers.isSynthetic())
                )
                .disableClassFormatChanges()
                .with(STRATEGY)
                .type(ElementMatchers.nameMatches(regex))
                .transform((b, type, cl, module, domain) -> {
                    if (random.nextDouble() <= args.classProbability()) {
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
                                .intercept(new ProbabilisticImplementation(random, args.methodProbability(), exceptionClass));
                    }
                    return b;
                });
        installedTransformer = builder.installOnByteBuddyAgent();
    }

    /**
     * Reset all transformations done by the last call to {@link #explode(ExplodeArgs)}.
     * If no transformations are active, this method does nothing.
     */
    public static void reset() {
        if (installedTransformer != null) {
            installedTransformer.reset(inst, STRATEGY);
            installedTransformer = null;
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Explode methods according to the given args, run the given runnable, and then reset the transformations.
     * This is useful for testing.
     *
     * @param args     Exploder arguments
     * @param runnable Runnable to execute while the methods are exploded
     * @throws Exception if the runnable throws an exception
     */
    public static void with(ExplodeArgs args, ThrowingRunnable runnable) throws Exception {
        explode(args);
        try {
            runnable.run();
        } finally {
            reset();
        }
    }
}