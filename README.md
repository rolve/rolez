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

There are two ways to use the Rolez compiler, either from the command line,
using [Maven][mvn], or using the [Eclipse IDE][eclipse].
If you already have Maven installed on your system, using Maven from the
command line is the quickest way to try out Rolez.
Otherwise, or if you want to do more than some quick experiments, then we
recommend to use Eclipse, as the Rolez SDK for Eclipse gives you some nice
features, like syntax highlighting, tooltips, simple code completion, and
automatic compilation including error markers.
Plus, the Rolez SDK for Eclipse has Maven support too, so you can get the best
of both worlds.

### Maven

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
the most up-to-date version of the Rolez compiler and runtime library (and the
`rolez-parent` project above).

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
compile the program and `mvn exec:java -Dexec.mainClass=App -q` to execute it.
Note that the first time you run these commands, Maven automatically downloads
the Rolez compiler and the runtime library (and many other dependencies), which
can take some time.

You can find a complete *Hello World!* Maven project, including more detailed build
instructions, in the examples directory:
[examples/helloworld](https://github.com/rolve/rolez/tree/master/examples/helloworld).

### Rolez SDK for Eclipse

To use Rolez inside Eclipse, first you need to download and install the
[Eclipse IDE for Java Developers][eclipse-java], if you haven't already.
Rolez requires at least Eclipse Photon.

You can install the Rolez SDK for Eclipse by going to the *Help* menu and selecting
*Install New Software*.
In the field that says "type or select a site", enter the Rolez update site:

    http://rolve.gitlab.io/rolez/eclipse/

and press Enter.
Select the *Rolez SDK* item below and follow the dialog to finish the installation.

After a restart of Eclipse, you are ready to create a Rolez project.
In the menu, go to *File → New → Project* (**not** *Java Project*!) and then select
*Rolez → Rolez Project*.
Enter a name and press *Finish*; this will generate a Java project with Rolez support.
The project already contains a `HelloWorld.rz` file that contains a Rolez program
similar to the one above.
In addition, you will find a `src-gen` folder, which contains the Java code that is
generated from the Rolez code inside `HelloWorld.rz`.

Modify the Rolez file and save it.
If you did not introduce any compile errors, the file will be recompiled and the Java
file will be updated immediately.
You can have both the Rolez and Java file open in two editors next to each other, to
see the changes happen right away.

To execute the program, you simply open or select the generated Java file and press
the green Run button.
You should see the output of the program in the *Console* view.

### Eclipse Maven Integration

The Rolez SDK for Eclipse supports Maven projects, via the [M2Eclipse][m2eclipse]
plugin (already part of Eclipse for Java Devs).

To use the Eclipse Maven integration with Rolez, create a Rolez Maven project, as
explained above, and then import it into Eclipse as follows:
Select *File → Import*, then *Maven → Existing Maven Projects*, then browse to the
directory that contains the `pom.xml` file and press *Finish*.
After a short while, the project should be completely built and you should find the
generated Java files under `target/generated-sources/rolez`.


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


## Building the Rolez Infrastructure

The Rolez compiler, runtime system, and standard library can be built using
the following command:

    mvn install -DskipTests

Rolez uses the [Maven][mvn] build tool.
The `-DskipTests` flag skips the testing phase.
The tests of the Rolez runtime use a software model checker
([Java PathFinder][jpf]) that checks all possible thread interleavings, which
is why these tests take very long (hours).
To execute the tests anyway, change to one of the following directories and
run `mvn test`:

- `ch.trick17.rolez.tests`: Compiler tests
- `ch.trick17.rolez.lib.tests`: Runtime system and standard library tests


[threads]: http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-1.pdf
[deterministic]: https://www.usenix.org/legacy/event/hotpar09/tech/full_papers/bocchino/bocchino_html/
[pr]: http://people.inf.ethz.ch/mfaes/publications/parallel-roles-corrected.pdf
[xtext]: http://www.eclipse.org/Xtext/
[mvn]: https://maven.apache.org/
[eclipse]: https://www.eclipse.org/ide/
[eclipse-java]: 
https://www.eclipse.org/downloads/packages/release/2018-09/r/eclipse-ide-java-developers
[m2eclipse]: http://www.eclipse.org/m2e/
[jpf]: https://github.com/javapathfinder/jpf-core
