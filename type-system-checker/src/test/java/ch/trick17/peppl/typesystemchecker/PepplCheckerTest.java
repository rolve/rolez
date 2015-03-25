package ch.trick17.peppl.typesystemchecker;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.checkerframework.framework.test.CheckerTest;
import org.junit.Test;

public abstract class PepplCheckerTest extends CheckerTest {
    
    private static final File TESTS_DIR = new File("tests");
    
    private final Class<?> myClass = getClass();
    
    public PepplCheckerTest() {
        super(PepplChecker.class, "<not used>", "-Anomsgtext");
    }
    
    static {
        /* CheckerTest uses the hard-coded "tests" directory. So make sure it is
         * deleted after all tests are over. */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(TESTS_DIR);
            } catch(IOException e) {}
        }));
    }
    
    @Test
    public void testChecker() {
        /* The following method does not use the "checkerDir" argument of the
         * constructor to find source files. Lucky us! */
        String srcFile = myClass.getName().replace('.', '/') + ".java";
        test(new File("src/test/java", srcFile));
    }
}
