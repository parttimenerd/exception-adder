package me.bechberger.exploder;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.ExceptionMethod;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.MethodVisitor;

import java.security.SecureRandom;

/**
 * Implementation that explodes methods with a given probability
 */
public class ProbabilisticImplementation implements Implementation {
    private final SecureRandom random;
    private final double probability;
    private final Class<? extends Throwable> exceptionClass;
    private final boolean verbose;

    ProbabilisticImplementation(SecureRandom random, double probability, Class<? extends Throwable> exceptionClass, boolean verbose) {
        this.random = random;
        this.probability = probability;
        this.exceptionClass = exceptionClass;
        this.verbose = verbose;
    }

    @Override
    public ByteCodeAppender appender(Target implementationTarget) {
        return (MethodVisitor mv, Context ctx, MethodDescription method) -> {
            boolean shouldExplode = random.nextDouble() <= probability;
            if (shouldExplode) {
                if (verbose) {
                    System.out.println("  -> Exploding method: " + method);
                }
                return ExceptionMethod.throwing(exceptionClass).appender(implementationTarget).apply(mv, ctx, method);
            } else {
                // Use SuperCall to preserve original method behavior
                return SuperMethodCall.INSTANCE.appender(implementationTarget).apply(mv, ctx, method);
            }
        };
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}