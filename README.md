# The Rolez Programming Language

*Rolez* is a research programming language that offers *deterministic parallel
programming* in a Java-like language.
This means that parallel programs written in Rolez are *guaranteed* to produce
the same results every time they are executed (they actually produce the same
results as if executed sequentially).
In particular, notorious PP problems such as data races and deadlocks are
impossible in Rolez, which makes writing parallel programs as safe as writing
sequential programs.

Rolez is based on the *Parallel Roles* programming model, which is described in
[this research paper][pr].
Until this mini-tutorial is complete, we recommend to read Sections 1&ndash;3
of the paper to understand the basics of Rolez.
For more background about deterministic parallel programming, see
[this][threads] and [this article][deterministic].


## Hello World!

Rolez is very similar to statically-typed, object-oriented languages like Java
or Scala.
The main differences all have to do with how parallelism is expressed and
controlled.
For example, there are no explicit threads in Rolez; instead, you can declare
a method as a *task*, to make it execute in parallel to the invoking code.

The execution of every Rolez program starts in the *main task*.
For example, *Hello World!* in Rolez looks like this:

<pre>
<strong>object</strong> App {
    <b>task pure</b> main: <b>void</b> {
        System.out.println("Hello World!");
    }
}
</pre>

Like Scala, Rolez has no static fields or methods; instead, you can define
globally accessible singleton objects, like the `App` object above.
The `System` class is a singleton object too.
The meaning of the `pure` keyword is explained later.


## Compiling and Running a Rolez Program

Rolez runs on the Java Virtual Machine (JVM).
It uses a source-to-source compiler based on [Xtext][xtext] to translate Rolez
code into Java code, which can be compiled to bytecode using a standard Java
compiler.

The simplest way to use the Rolez compiler is using [Maven][mvn].
Create a new Maven project and add `rolez-parent` as the parent project to your
`pom.xml` file, as follows.
This will pull in all the necessary dependencies and configuration to compile a
Rolez program.

```xml
<parent>
    <groupId>ch.trick17.rolez</groupId>
    <artifactId>rolez-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>
```

In addition, you need to add the Rolez Maven repository, which always contains
the most up-to-date version of the Rolez compiler and runtime library.

```xml
<repositories>
    <repository>
        <id>rolez-maven-repo</id>
        <url>https://rolve.gitlab.io/rolez/maven/</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

Now, create an `App.rz` file in your project's `src/main/java` folder and paste
the *Hello World!* code from above. Run `mvn compile` in the project root to
compile the program and `mvn exec:java -q` to execute it. Note that the first
time you run these commands, Maven automatically downloads the Rolez compiler
and the runtime library (and many other dependencies), which can take some time.

You can find a complete *Hello World!* Maven project, including more detailed build
instructions, in the examples directory:
[examples/helloworld](https://github.com/rolve/rolez/tree/master/examples/helloworld).


## Using Rolez in Eclipse

*TODO*


## Declaring and Starting Tasks

To see how we can parallelize a program in Rolez, let's start with this simple
program that estimates π using the Monte Carlo method:

<pre>
<b>import</b> rolez.util.Random
<b>object</b> Pi {
    <b>task pure</b> main: <b>void</b> {
        <b>val</b> random = <b>new</b> Random;
        <b>val</b> n = 1000000000;
        <b>var</b> hits = 0;
        <b>for</b>(<b>var</b> i = 0; i < n ; i++) {
            <b>val</b> x = random.nextDouble;              
            <b>val</b> y = random.nextDouble;
            <b>if</b>(x*x + y*y <= 1)
                hits++;
        }
        System.out.println("π = " + hits / (0.25 * n));
    }
}
</pre>

To speed this program up on computers with multiple cores, we can wrap the main
computation into a task and start that task multiple times, making each task
perform only a part of the computation:

<pre>
<b>import</b> rolez.util.Random
<b>object</b> Pi {
    <b>task pure</b> main: <b>void</b> {
        <b>val</b> n = 1000000000;
        <b>val</b> cores = 4;
        
        <b>val</b> tasks = <b>new</b> Array[Task[<b>int</b>]](cores);
        <b>for</b>(<b>var</b> i = 0; i < cores; i++)
            tasks.set(i, <b>this start</b> simulate(n/cores));
        
        <b>var</b> totalHits = 0;
        <b>for</b>(<b>var</b> i = 0; i < cores; i++)
            totalHits += tasks.get(i).get;
        
        System.out.println("π = " + totalHits / (0.25 * n));
    }
    <b>task pure</b> simulate(n: <b>int</b>): <b>int</b> {
        <b>val</b> random = <b>new</b> Random;
        <b>var</b> hits = 0;
        <b>for</b>(<b>var</b> i = 0; i < n ; i++) {
            <b>val</b> x = random.nextDouble;              
            <b>val</b> y = random.nextDouble;
            <b>if</b>(x*x + y*y <= 1)
                hits++;
        }
        <b>return</b> hits;
    }
}
</pre>

To start a task, we use the `start` keyword instead of the dot that usually
separates the receiver from the member name (as in `System.out`).
Note that, at the moment, Rolez's syntax does not allow you to leave away
the `this` when accessing members or starting tasks with the same receiver as
the enclosing method.

A `start` expression returns an instance of the built-in `rolez.lang.Task`
class, with a type argument that corresponds to the return type of the task.
To get the result of a task, you can invoke the `get` method on the `Task`
instance, which blocks until the task has finished.

The above code uses a loop to start multiple tasks and stores all task instances
in an array.
After all tasks have been started, the main task continues to combine the
task's partial results into the final result.

You can find the complete *Pi* Maven project in the examples directory:
[examples/pi](https://github.com/rolve/rolez/tree/master/examples/pi).


## Object Sharing

*TODO*.


## Compiling the Rolez Infrastructure

*TODO*, but basically `mvn install -DskipTests` in the root directory.


[threads]: http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-1.pdf
[deterministic]: https://www.usenix.org/legacy/event/hotpar09/tech/full_papers/bocchino/bocchino_html/
[pr]: http://people.inf.ethz.ch/mfaes/publications/parallel-roles-corrected.pdf
[xtext]: http://www.eclipse.org/Xtext/
[mvn]: https://maven.apache.org/
