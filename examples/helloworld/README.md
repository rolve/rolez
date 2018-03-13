# Hello World! in Rolez

This is the Rolez *Hello World!* program. To build and run it,
you'll need [Maven](https://maven.apache.org/).
If you're using an OS with `apt`, follow the instructions below.
Otherwise, download and install Maven (and Java) manually and skip
the first step.


## Step-by-step instructions

1. Install Maven:

   ```
   sudo apt-get install maven
   ```

2. Clone the Rolez repository and enter the `examples/helloworld` directory:
   
   ```
   git clone https://github.com/rolve/rolez.git
   cd rolez/examples/helloworld
   ```

3. Use Maven to compile the *Hello World!* program:
   
   ```
   mvn compile
   ```
   
   Since Maven downloads most of its code on the fly, this command will take
   some time when executed for the first time.
   Don't worry, the next time you compile a Rolez program, this will be much
   faster!

4. Execute the program. Since a Rolez program requires the Rolez runtime
   library on the classpath, the simplest way to do this is using Maven:
   
   ```
   mvn exec:java -Dexec.mainClass=App
   ```
   
   After Maven has downloaded some more libraries, you should see the
   `Hello World!` output.
   To suppress the Maven output, add the `-q` flag:
   
   ```
   mvn -q exec:java -Dexec.mainClass=App
   ```
