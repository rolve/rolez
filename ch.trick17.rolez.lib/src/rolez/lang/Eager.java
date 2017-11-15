package rolez.lang;

import java.util.HashSet;
import java.util.Set;

public class Eager {

	private static void println(String str){
		java.lang.System.out.println(str);
	}
	
	private static void println(Iterable<?> objs, String str){
		java.lang.System.out.println(str);
		for(Object o : objs)
			println(o.toString());
	}
	
	private static void println(Object[] objs, String str){
		java.lang.System.out.println(str);
		for(Object o : objs)
			println(o.toString());
	}
	
	/**
	 * 
	 * @param passedT1 all objects passed to T1
	 * @param sharedT1 all objects shared with T1
	 * @param passedT2 all objects passed to T2
	 * @param sharedT2 all objects shared with T2
	 * @return true iff the passed objects of the tasks overlap so that troublesome interleaving can happen
	 */
	public static boolean checkInterference(Object[] passedArrayT1, Object[] sharedArrayT1, Object[] passedArrayT2, Object[] sharedArrayT2){
		println(passedArrayT1, "passedT1");
		
		println(sharedArrayT1, "sharedT1");
		
		println(passedArrayT2, "passedT2");
		
		println(sharedArrayT2, "sharedT2");
		
		
        // collect all object passed to T1
        Set<Guarded> passedReachableT1 = new HashSet<Guarded>();
        for(Object g : passedArrayT1)
        	if(g instanceof Guarded)
        		collectReachables(passedReachableT1, (Guarded)g);
        
        println(passedReachableT1, "passedReachableT1");
        
        // do any objects passed to T2 interfere with those passed to T1?
        Set<Guarded> processed = new HashSet<Guarded>();
        for(Object g : passedArrayT2)
            if(g instanceof Guarded)
            	if(interferesWith((Guarded)g, passedReachableT1, new HashSet<Guarded>()))
            		throw new RuntimeException("This parallel construct can't be executed concurrently due to interference");//return true;
        
        // do any objects shared with T2 interfere with those passed to T1?
        for(Object g : sharedArrayT2)
            if(g instanceof Guarded)
            	if(interferesWith((Guarded)g, passedReachableT1, new HashSet<Guarded>()))
            		throw new RuntimeException("This parallel construct can't be executed concurrently due to interference");//return true;
        
        // collect all objects shared with T1
        Set<Guarded> sharedReachableT1 = passedReachableT1;
        for(Object g : sharedArrayT1)
        	if(g instanceof Guarded)
        		collectReachables(sharedReachableT1, (Guarded)g);
        
        println(sharedReachableT1, "sharedReahableT1");
        
        // do any objects passed to T2 interfere with those shared with to T1?
        processed = new HashSet<Guarded>();
        for(Object g : sharedArrayT2)
            if(g instanceof Guarded)
            	if(interferesWith((Guarded)g, sharedReachableT1, processed))
            		throw new RuntimeException("This parallel construct can't be executed concurrently due to interference");//return true;
        
		return false;
	}
	
	// add all guarded objects to the set that are reachable from 'from'
	private static void collectReachables(Set<Guarded> processed, Guarded from){
		if(processed.add(from)) {
            for(Object g : from.guardedRefs())
                if(g instanceof Guarded)
                    collectReachables(processed, (Guarded)g);
        }
	}
	
	// returns true iff 'g' or an object reachable from 'g' is contained in 'with'
	private static boolean interferesWith(Guarded g, Set<Guarded> with, Set<Guarded> processed){
		if(!processed.add(g))
			return false;
		
		if(with.contains(g))
			return true;
		for(Object gu : g.guardedRefs())
			if(gu instanceof Guarded)
				if(interferesWith((Guarded)gu, with, processed))
					return true;
		return false;
	}
	
	
}
