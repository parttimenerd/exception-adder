package me.bechberger.exploder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class GlobToRegexTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "com.example.MyClass, com\\.example\\.MyClass",
            "com.*.MyClass, com\\.[^.]*\\.MyClass",
            "com.**.MyClass, com\\..*\\.MyClass",
            "*.MyClass, [^.]*\\.MyClass",
            "**.MyClass, .*\\.MyClass",
            "com.example.?, com\\.example\\..",
            "com.example.*Service, com\\.example\\.[^.]*Service",
            "com.example.**Service, com\\.example\\..*Service",
            "com.example.My?lass, com\\.example\\.My.lass",
            "com.example.My\\*class, com\\.example\\.My\\\\\\*class",
            "com.example.My.class, com\\.example\\.My\\.class"
    })
    public void testToRegex(String glob, String expectedRegex) {
        String actualRegex = GlobToRegex.toRegex(glob);
        assertEquals(expectedRegex, actualRegex);
    }
}