package ch.trick17.peppl.typesystemchecker;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class PepplCheckerTest {
    
    private static final File TEMP_DIR = new File("target/test-temp");
    
    private final Class<?> myClass = getClass();
    
    @Before
    public void initializeRepoDir() throws IOException {
        if(TEMP_DIR.exists() && !TEMP_DIR.isDirectory())
            throw new AssertionError(
                    "File 'testing-repo' interferes with this test. Delete it.");
        
        if(TEMP_DIR.exists())
            FileUtils.deleteDirectory(TEMP_DIR);
        
        TEMP_DIR.mkdir();
    }
    
    @Test
    public void testChecker() throws IOException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        StandardJavaFileManager fileMgr = compiler.getStandardFileManager(
                collector, null, null);
        fileMgr.setLocation(StandardLocation.SOURCE_PATH, asList(new File(
                "src/test/java")));
        fileMgr.setLocation(StandardLocation.CLASS_OUTPUT, asList(TEMP_DIR));
        
        JavaFileObject file = fileMgr.getJavaFileForInput(
                StandardLocation.SOURCE_PATH, myClass.getName(), Kind.SOURCE);
        CompilationTask task = compiler.getTask(null, fileMgr, collector,
                asList("-source", "1.8", "-AprintErrorStack"), null,
                asList(file));
        task.setProcessors(asList(new PepplChecker()));
        
        Boolean success = task.call();
        
        collector.getDiagnostics().stream().forEach(System.out::println);
        if(hasDeclaredTypeErrors()) {
            Set<Long> lines = collector.getDiagnostics().stream().map(
                    Diagnostic::getLineNumber).collect(Collectors.toSet());
            assertEquals(typeErrorLines(), lines);
        }
        else {
            assertEquals(asList(), collector.getDiagnostics());
            assertTrue(success);
        }
        fileMgr.close();
    }
    
    private boolean hasDeclaredTypeErrors() {
        return myClass.getAnnotation(TypeErrors.class) != null;
    }
    
    private Set<Long> typeErrorLines() {
        long[] lines = myClass.getAnnotation(TypeErrors.class).lines();
        return LongStream.of(lines).boxed().collect(Collectors.toSet());
    }
    
    @After
    public void cleanUpRepoDir() throws IOException {
        FileUtils.deleteDirectory(TEMP_DIR);
    }
}
