package me.bechberger.exploder;

import me.bechberger.test.ExplodeTestClass;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExplodeTest {

    @Test
    public void testExplode() throws InterruptedException {
        ExceptionAdder.explode(new ExplodeArgs()
                .glob("me.bechberger.test.ExplodeTestClass")
                .classProbability(1.0)
                .methodProbability(1.0)
                .exception("java.lang.RuntimeException")
        );
        assertThrows(RuntimeException.class, () -> {
            ExplodeTestClass testClass = new ExplodeTestClass();
            testClass.executeAll();
        });
        ExceptionAdder.reset();
    }

    @Test
    public void testReset() {
        ExceptionAdder.reset();
        ExplodeTestClass testClass = new ExplodeTestClass();
        assertDoesNotThrow(testClass::executeAll);
    }

    @Test
    public void testWith() throws Exception {
        ExceptionAdder.with(new ExplodeArgs()
                .glob("me.bechberger.test.ExplodeTestClass")
                .classProbability(1.0)
                .methodProbability(1.0)
                .exception("java.lang.RuntimeException")
        , () -> {
            assertThrows(RuntimeException.class, () -> {
                ExplodeTestClass testClass = new ExplodeTestClass();
                testClass.executeAll();
            });
        });
    }
}