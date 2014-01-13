package ch.trick17.simplejpf;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.GETFIELD;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.LockSupport;

import ch.trick17.simplejpf.test.JpfTest;

public class JpfSearchTest {
    
    private static final String DFSEARCH = "gov.nasa.jpf.search.DFSearch";
    private static final String INTERLEAVING = "gov.nasa.jpf.search.heuristic.Interleaving";
    
    public static void main(final String[] args) throws IOException {
        Config.enableLogging(true);
        final Config config = JPF.createConfig(new String[0]);
        config.load(JpfTest.class.getResourceAsStream("jpf.properties"));
        config.setProperty("classpath", System.getProperty("java.class.path"));
        config.setTarget(Test.class.getName());
        
        final Map<String, String> results = new HashMap<>();
        for(final String search : new String[]{DFSEARCH, INTERLEAVING}) {
            config.setProperty("search.class", search);
            config.printEntries();
            
            final JPF jpf = new JPF(config);
            jpf.addListener(new TestListener());
            jpf.run();
            if(jpf.foundErrors())
                results.put(search, jpf.getLastError().getDescription());
            else
                results.put(search, "no errors");
        }
        
        System.out.println();
        for(final Entry<String, String> result : results.entrySet())
            System.out.println(result.getKey() + ": " + result.getValue());
    }
    
    public static class Test {
        
        public static void main(final String[] args) {
            final Thread main = Thread.currentThread();
            final Int i = new Int();
            
            i.blocked = true;
            new Thread() {
                @Override
                public void run() {
                    i.value++;
                    i.blocked = false;
                    LockSupport.unpark(main);
                }
            }.start();
            
            while(i.blocked)
                LockSupport.park();
            
            i.blocked = true;
            new Thread() {
                @Override
                public void run() {
                    i.value++;
                    i.blocked = false;
                    LockSupport.unpark(main);
                }
            }.start();
            
            while(i.blocked)
                LockSupport.park();
            
            if(i.value != 2)
                throw new AssertionError();
        }
        
        static class Int {
            volatile Boolean blocked = false;
            int value;
        }
    }
    
    static class TestListener extends ListenerAdapter {
        @Override
        public void instructionExecuted(final VM vm, final ThreadInfo thread,
                final Instruction next, final Instruction instr) {
            final MethodInfo method = thread.getTopFrameMethodInfo();
            if(method != null) {
                final String pkg = method.getClassInfo().getPackageName();
                if(pkg.startsWith("ch.trick17.simplejpf")) {
                    System.out.println(method.getFullName() + ": " + next);
                    if(next.toString().contains("new java.lang.AssertionError"))
                        System.currentTimeMillis();
                }
            }
        }
        
        @Override
        public void choiceGeneratorSet(final VM vm,
                final ChoiceGenerator<?> newCG) {
            if(newCG instanceof ThreadChoiceFromSet) {
                final ThreadChoiceFromSet threadChoice = (ThreadChoiceFromSet) newCG;
                if(threadChoice.getId().contains("SHARED_FIELD_ACCESS")) {
                    final Instruction insn = threadChoice.getInsn();
                    if(insn instanceof GETFIELD) {
                        final FieldInfo field = ((GETFIELD) insn)
                                .getFieldInfo();
                        if(field.getName().contains("blocked"))
                            System.out.println("-- " + threadChoice);
                    }
                }
            }
        }
        
        @Override
        public void stateBacktracked(final Search search) {
            System.out.println("------- Backtracked -------");
        }
    }
}
