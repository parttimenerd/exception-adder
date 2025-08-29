Exception Adder
===============

[![Build JAR](https://github.com/parttimenerd/exploder-agent/actions/workflows/build.yml/badge.svg)](https://github.com/parttimenerd/exploder-agent/actions/workflows/build.yml)

This agent adds `throw new SomeException()` at random places in your application (or agent).
This is useful to test the resilience of your application (or agent) against unexpected exceptions.
It is primarily meant to test that a failing agent doesn't affect the application.

__This project is just a rough prototype so use at your own risk.__

Build
-----
```sh
git clone https://github.com/parttimenerd/exception-adder
cd exception-adder
mvn package
```

Usage
-----
Attach the agent either later or at the beginning:
```sh
java -javaagent:target/exploder-agent.jar=classProb=1,methodProb=0.5 Sample.java
```

This will replace methods with `throw new RuntimeException()` with a probability of 50%.

Help:
```sh
> java -javaagent:target/exploder-agent.jar=help
Usage:
  -javaagent:exploder-agent.jar="glob=<pattern>,classProb=<0-1>,methodProb=<0-1>,exception=<ex>"

Arguments:
  glob       Glob pattern for class names (default=**)
  classProb  Probability [0-1] to modify a class (default=0.5)
  methodProb Probability [0-1] to modify a method (default=0.5)
  exception  Exception class to throw (default=java.lang.RuntimeException)
  verbose    If set, prints detailed info about each method decision (default=false)

Commands:
  help   Print this help and exit
  reset  Reset previous transformations and exit
```

License
-------
MIT, Copyright 2025 SAP SE or an SAP affiliate company, Johannes Bechberger
and exception-adder contributors


*This project is a prototype of the [SapMachine](https://sapmachine.io) team
at [SAP SE](https://sap.com)*