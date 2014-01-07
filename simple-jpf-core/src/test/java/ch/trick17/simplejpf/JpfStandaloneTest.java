package ch.trick17.simplejpf;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.File;
import java.io.IOException;

public class JpfStandaloneTest {
    
    public static void main(final String[] args) throws IOException {
        final Config config = JPF.createConfig(new String[]{"-show"});
        config.load(JpfStandaloneTest.class
                .getResourceAsStream("jpf.properties"));
        
        final String classpath = "lib/jpf-classes.jar" + File.pathSeparator
                + "target/test-classes";
        
        config.setProperty("classpath", classpath);
        config.setTarget(ThreadTest.class.getName());
        
        final JPF jpf = new JPF(config);
        jpf.run();
    }
    
    public static class ThreadTest {
        
        private static int counter = 0;
        
        public static void main(final String[] args) {
            new Thread(new Runnable() {
                public void run() {
                    for(int i = 0; i < 3000; i++)
                        counter = i;
                }
            }).start();
            
            if(counter == 2999)
                throw new RuntimeException("Hello World");
        }
    }
}
