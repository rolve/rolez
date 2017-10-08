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
For now, we recommend to read Sections 1&ndash;3 of the paper before playing
around with *Rolez*.
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
        <b>the</b> System.out.println("Hello World!");
    }
}
</pre>

Like Scala, Rolez has no static fields or methods; instead, you can define
globally accessible singleton objects, like the `App` object above.
Rolez uses a slightly different syntax to refer these objects:
Instead of just `System`, you have to write `the System` in Rolez.
The meaning of the `pure` keyword is explained later.


## Compiling and Running a Rolez Program

Rolez runs on the Java Virtual Machine (JVM).
It uses a source-to-source compiler based on [Xtext][xtext] to translate Rolez
code into Java code, which can be compiled to bytecode using a standard Java
compiler.

The simplest way to use the Rolez compiler is using [Maven][mvn].
Create a new Maven project and add the following configuration to your
`pom.xml` file:

```xml
<properties>
    <rolezOutputDir>${project.build.directory}/generated-sources/rolez</rolezOutputDir>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.eclipse.xtext</groupId>
            <artifactId>xtext-maven-plugin</artifactId>
            <version>2.9.1</version>
            <executions>
                <execution>
                    <goals><goal>generate</goal></goals>
                </execution>
            </executions>
            <configuration>
                <languages>
                    <language>
                        <setup>ch.trick17.rolez.RolezStandaloneSetup</setup>
                        <outputConfigurations>
                            <outputConfiguration>
                                <outputDirectory>${rolezOutputDir}</outputDirectory>
                            </outputConfiguration>
                        </outputConfigurations>
                    </language>
                </languages>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>ch.trick17.rolez</groupId>
                    <artifactId>ch.trick17.rolez</artifactId>
                    <version>1.0.0-SNAPSHOT</version>
                </dependency>
            </dependencies>
        </plugin>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <version>1.10</version>
            <executions>
                <execution>
                    <goals><goal>add-source</goal></goals>
                    <configuration>
                        <sources><source>${rolezOutputDir}</source></sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

<pluginRepositories>
    <pluginRepository>
        <id>rolez-mvn-repo</id>
        <url>https://raw.github.com/rolve/rolez/mvn-repo/</url>
        <snapshots><enabled>true</enabled></snapshots>
    </pluginRepository>
</pluginRepositories>
```

In addition to the Rolez compiler, you'll need the Rolez runtime library.
Add it as a dependency to your project:

```xml
<dependencies>
    <dependency>
        <groupId>ch.trick17.rolez</groupId>
        <artifactId>ch.trick17.rolez.lib</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>rolez-mvn-repo</id>
        <url>https://raw.github.com/rolve/rolez/mvn-repo/</url>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>
```

Now, create an `App.rz` file in your project's `src/main/java` folder and paste
the *Hello World!* code from above. Run `mvn install` to compile the program
and then run `mvn exec:java -Dexec.mainClass=App` to execute it.

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
        <b>the</b> System.out.println("π = " + hits / (0.25 * n));
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
        
        <b>val</b> tasks = <b>new</b> Array[<b>pure</b> Task[<b>int</b>]](cores);
        <b>for</b>(<b>var</b> i = 0; i < cores; i++)
            tasks.set(i, <b>this start</b> simulate(n/cores));
        
        <b>var</b> totalHits = 0;
        <b>for</b>(<b>var</b> i = 0; i < cores; i++)
            totalHits += tasks.get(i).get;
        
        <b>the</b> System.out.println("π = " + totalHits / (0.25 * n));
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

[threads]: http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-1.pdf
[deterministic]: https://www.usenix.org/legacy/event/hotpar09/tech/full_papers/bocchino/bocchino_html/
[pr]: http://people.inf.ethz.ch/mfaes/publications/parallel-roles.pdf
[xtext]: http://www.eclipse.org/Xtext/
[mvn]: https://maven.apache.org/
