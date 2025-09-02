ExceptionAdder
==============

[![Build](https://github.com/parttimenerd/exception-adder/actions/workflows/build.yml/badge.svg)](https://github.com/parttimenerd/exception-adder/actions/workflows/build.yml)
![Maven Central Version](https://img.shields.io/maven-central/v/me.bechberger/exception-adder)


A tiny library that adds `throw new SomeException()` at random places in your application (or agent).
This is useful to test the resilience of your application (or agent) against unexpected exceptions.
It is primarily meant to test that a failing agent doesn't affect the application
and that an agent itself is resilient against exceptions and can recover from them.

Installation
------------

It requires Java 17 or later. With Maven, just add the dependency:

```xml
<dependency>
    <groupId>me.bechberger</groupId>
    <artifactId>exception-adder</artifactId>
    <version>0.0.1</version>
</dependency>
```

And allow the use [bytebuddy](https://bytebuddy.net) agent to be attached:

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.3</version>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading -Djdk.attach.allowAttachSelf=true</argLine>
    </configuration>
</plugin>
```

Usage
-----

Now you can use the agent in your tests like this:

```java
import me.bechberger.exploder.ExceptionAdder;

@Test
public void testWith() throws Exception {
    ExceptionAdder.with(new ExploderArgs()
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
```

Build
-----

```sh
git clone https://github.com/parttimenerd/exception-adder
cd exception-adder
mvn package
```

Test
----
There are minimal unit tests included. Run them with:

```sh
mvn test
```

License
-------
MIT, Copyright 2025 SAP SE or an SAP affiliate company, Johannes Bechberger
and exception-adder contributors


*This project is a prototype of the [SapMachine](https://sapmachine.io) team
at [SAP SE](https://sap.com)*