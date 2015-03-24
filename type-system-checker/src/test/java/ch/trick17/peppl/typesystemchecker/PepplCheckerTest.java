package ch.trick17.peppl.typesystemchecker;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
        
        final DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        final StandardJavaFileManager fileMgr = compiler
                .getStandardFileManager(collector, null, null);
        fileMgr.setLocation(StandardLocation.SOURCE_PATH, asList(new File(
                "src/test/java")));
        fileMgr.setLocation(StandardLocation.CLASS_OUTPUT, asList(TEMP_DIR));
        final JavaFileObject file = fileMgr
                .getJavaFileForInput(StandardLocation.SOURCE_PATH, getClass()
                        .getName(), Kind.SOURCE);
        
        final CompilationTask task = compiler.getTask(null, fileMgr, collector,
                asList("-source", "1.8"), null, asList(file));
        task.setProcessors(asList(new PepplChecker()));
        
        final Boolean success = task.call();
        
        assertEquals(asList(), collector.getDiagnostics());
        assertTrue(success);
        fileMgr.close();
    }
    
    @After
    public void cleanUpRepoDir() throws IOException {
        FileUtils.deleteDirectory(TEMP_DIR);
    }
}
