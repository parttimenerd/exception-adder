package me.bechberger.exploder;

/**
 * Arguments for the {@link ExceptionAdder}
 *
 * @param glob              Glob pattern for class names (default="**")
 * @param classProbability  Probability [0-1] to modify a class (default=0.5)
 * @param methodProbability Probability [0-1] to modify a method (default=0.5)
 * @param exception         Exception class to throw (default="java.lang.RuntimeException")
 */
public record ExplodeArgs(
        String glob,
        double classProbability,
        double methodProbability,
        String exception

) {
    public ExplodeArgs() {
        this("**", 0.5, 0.5, "java.lang.RuntimeException");
    }

    public ExplodeArgs glob(String glob) {
        return new ExplodeArgs(glob, this.classProbability, this.methodProbability, this.exception);
    }
    public ExplodeArgs classProbability(double classProbability) {
        return new ExplodeArgs(this.glob, classProbability, this.methodProbability, this.exception);
    }
    public ExplodeArgs methodProbability(double methodProbability) {
        return new ExplodeArgs(this.glob, this.classProbability, methodProbability, this.exception);
    }
    public ExplodeArgs exception(String exception) {
        return new ExplodeArgs(this.glob, this.classProbability, this.methodProbability, exception);
    }
}