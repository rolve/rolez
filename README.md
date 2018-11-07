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
We recommend to read at least Sections 1, 2, and 4 of the paper to understand
the basics of Rolez.
(Sections 5 and 6 may be of interest too, as they describe two language features
that are important for programs with data parallelism.)
Another paper, titled *Efficient VM-Independent Runtime Checks for Parallel
Programming*, describes the implementation of Rolez, in particular two
optimization techniques that helped achieving good guarding performance.
The paper is available from the [author's website][mfaes].

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

### Using Maven

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

### Using the Rolez SDK for Eclipse

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

You can import any of the
[Rolez example projects](https://github.com/rolve/rolez/tree/master/examples/)
in the same way.


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


## Implementation Details

This section provides some additional details about the Rolez language
implementation, for people that are interested in studying or modifying the
compiler or runtime system.

### Overview

Rolez is build on top of the Java platform (currently Java 8). The runtime
system, which performs guarding and the role transitions, is implemented as a
Java library. This library also contains implementations of the built-in Rolez
classes like `Array` and `Slice`. The compiler is implemented with the
[Xtext framework](http://www.eclipse.org/Xtext/) and transforms Rolez source
code into Java source code, inserting role transition and guarding operations as
calls to the runtime library where necessary. The generated Java code is
compiled using a standard Java compiler and executed on a standard Java Virtual
Machine (JVM).

The Xtext framework is also based on the Java platform, and more specifically on
the [Eclipse](https://www.eclipse.org/) platform. Hence, the compiler is 
implemented in Java and in other languages that can be compiled into Java
bytecode. In addition, the Rolez implementation uses the Java-based
[Maven][mvn] build tool.

According to Xtext conventions, the Rolez language infrastructure in divided
into several modules, which correspond to directories inside `~/rolez`. The most
important are the following:

- `ch.trick17.rolez`: the compiler
- `ch.trick17.rolez.lib`: the runtime system and Rolez standard library

For both of these modules, there exists a `.test` module that contains the
respective unit and integration tests. The rest of the modules mostly concern
the Eclipse IDE support and are not discussed here.

### Compiler

The compiler is located in the `ch.trick17.rolez` directory. Most of its parts
are implemented using [Xtend](http://www.eclipse.org/xtend/), a language very
similar to Java, while some parts are implemented using a range of
domain-specific languages (DSLs).

**Syntax**:
The syntax of Rolez is described using the "Xtext" language, in the
`Rolez.xtext` file in `src/ch/trick17/rolez`. In addition to the Rolez syntax,
this file also defines how the parsed elements are mapped to objects of the
intermediate representation (IR) of the compiler. The Xtext framework generates
an [Antlr](http://www.antlr.org/) grammar from the `Rolez.xtext` file, which in
turn is used to generate a lexer and parser.

**IR**:
The structure of the IR (or "model" in Eclipse terminology) is defined in the
`Rolez.xcore` file in the `model` directory. This is an
[Xcore](https://wiki.eclipse.org/Xcore) file, which defines the IR classes,
their properties, their methods, and their relationship to each other.

**Type System and Semantic Checks**:
The Rolez type system is implemented using the
[Xsemantics](https://projects.eclipse.org/projects/modeling.xsemantics)
language, a specific DSL for type systems. All typing rules are defined in the
`Rolez.xsemantics` file in the `src/ch/trick17/rolez/typesystem` directory.
Additional semantic checks are implemented using plain Xtend in
`src/ch/trick17/rolez/validation/RolezValidator.xtend`.

Like in Java, some semantic checks are based on dataflow analysis (e.g. whether
a variable has been initialized on all paths to some statement), which in turn
is based on control flow. The `cfg` subdirectory of the `validation` directory
contains both the classes of the control flow graph (CFG) itself, as well as the
code to construct it, while the `dataflow` subdirectory contains various
concrete dataflow analyses.

**Scoping**:
In Xtext, scoping is handled somewhat separately from the other semantic checks.
The scoping rules, including method overloading resolution, are implemented in
the `src/ch/trick17/rolez/scoping/RolezScopeProvider.xtend` file. Apart from
standard scoping rules similar to Java, this file also implements scoping for
class slices, which are explained in the paper.

**Code Generation**:
Finally, the implementation of the Java code generation is located in the
`src/ch/trick17/rolez/generator` directory. While the entry point for the code
generator is in the `RolezGenerator.xtend` file, the `generator` directory
contains various other relevant files, including two static analyses,
`RoleAnalysis.xtend` and `ChildTasksAnalysis.xtend`, which are used to generate
code with less redundant guarding.

### Runtime System & Standard Library

The Rolez runtime and standard library are in the `ch.trick17.rolez.lib`
directory. The runtime system, which takes care of guarding and role
transitions, is implemented in Java. The (minimal) standard library, which
contains classes like `Array` and `String` is implemented using a mix of Rolez
and Java.

**Guarding**:
Guarding is implemented in the `rolez.lang.Guarded` class defined in the
`src/rolez/lang/Guarded.java` file. The Java classes generated from most Rolez
classes extend the `Guarded` class and thus inherit its methods and fields,
which implement guarding and individual role transitions.

**Arrays and Slices**:
Rolez arrays and (array) slices are implemented in the `rolez.lang.GuardedArray`
and `rolez.lang.GuardedSlice` Java classes defined in the `GuardedArray.java`
and `GuardedSlice.java` files in `src/rolez/lang` directory. Additional
functionality related to slicing is found in the `SliceRange.java` and
`partitioners.rz` files.

**Tasks**:
The implementation of Rolez tasks can be found in the `src/rolez/lang/Task.java`
file. When tasks start and finish, they perform all the required role
transitions. This includes collecting the objects that are reachable from the
directly shared objects, to perform "joint role transitions", as described in
the paper.

**Standard Library**:
The Rolez standard library so far only contains a few basic classes like
`Array`, `String`, and `Math`. Most of these classes are implemented in Java
or directly map to classes from the Java standard library. This is achieved
using `mapped` classes. Such classes have no Rolez implementation, but only
contain the field declarations and method signatures (including role
declarations!) required to compile against them. The actual implementations
of the methods is provided by the Java class that a Rolez class is mapped to.

Most of the mapped Rolez standard library classes are defined in the following
files:

- `src/rolez/lang/lang.rz`: Contains basic classes such as `Array`, `String`,
  and `Math`. Also includes the `Object` class, the base class for all Rolez
  classes.
- `src/rolez/lang/primitives.rz`: Contains classes related to the primitive
  types `int`, `boolean`, etc.
- `src/rolez/io/io.rz`: Contains a minimal set of classes to perform I/O.
- `src/rolez/util/util.rz`: Contains a few utility classes like `Scanner` and
  `Random`.

In addition, there is a file `src/rolez/lang/partitioners.rz`, which contains
the implementation of the three built-in partitioning schemes described in the
paper.


[threads]: http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-1.pdf
[deterministic]: https://www.usenix.org/legacy/event/hotpar09/tech/full_papers/bocchino/bocchino_html/
[pr]: https://doi.org/10.1145/3276500
[mfaes]: http://people.inf.ethz.ch/mfaes/
[xtext]: http://www.eclipse.org/Xtext/
[mvn]: https://maven.apache.org/
[eclipse]: https://www.eclipse.org/ide/
[eclipse-java]: 
https://www.eclipse.org/downloads/packages/release/2018-09/r/eclipse-ide-java-developers
[m2eclipse]: http://www.eclipse.org/m2e/
[jpf]: https://github.com/javapathfinder/jpf-core
