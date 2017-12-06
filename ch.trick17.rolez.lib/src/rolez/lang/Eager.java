package rolez.lang;

import static java.util.Collections.newSetFromMap;

import java.util.IdentityHashMap;
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
	
	private static void error(){
		throw new RuntimeException("This parallel construct can't be executed concurrently due to interference");
	}
	
	/**
	 * collects all objects reachable from the passed arrays and checks if they interfere. returns the collected sets as follows:
	 * 0: passedT1
	 * 1: passedReachable T1
	 * 2: sharedReachable T1
	 * 3: passedT2
	 * 4: passedReachable T2
	 * 5: sharedReachable T2
	 * 
	 * @param passedT1 all objects passed to T1
	 * @param sharedT1 all objects shared with T1
	 * @param passedT2 all objects passed to T2
	 * @param sharedT2 all objects shared with T2
	 * @return the collected reachable objects
	 * 	
	 */
	@SuppressWarnings("unchecked")
	public static Set<Guarded>[] collectAndCheck(Object[] passedArrayT1, Object[] sharedArrayT1, Object[] passedArrayT2, Object[] sharedArrayT2){
//		println(passedArrayT1, "passedT1");
//		println(sharedArrayT1, "sharedT1");
//		println(passedArrayT2, "passedT2");
//		println(sharedArrayT2, "sharedT2");

        Set<Guarded> passedReachableT1 = newIdentitySet();
        Set<Guarded> passedReachableT2 = newIdentitySet();
        Set<Guarded> sharedReachableT1 = newIdentitySet();
        Set<Guarded> sharedReachableT2 = newIdentitySet();
        
        Set<Guarded> passedT1 = newIdentitySet();
        Set<Guarded> passedT2 = newIdentitySet();
        
        Set<Guarded> combinedT1 = newIdentitySet();
        
        // collect all object passed to T1
        for(Object g : passedArrayT1)
        	if(g instanceof Guarded){
        		passedT1.add((Guarded)g);
        		collectReachables((Guarded)g, combinedT1, passedReachableT1);
        	}
        // collect all objects shared with T1
        for(Object g : sharedArrayT1)
        	if(g instanceof Guarded)
        		collectReachables((Guarded)g, combinedT1, sharedReachableT1);
        		// objects that are shared and passed are effectively passed, so what's already in combined will not be put in shared
        
        
//        println(passedReachableT1, "passedReachableT1");
//        println(sharedReachableT1, "sharedReahableT1");
        
        // do any objects passed to T2 interfere with those passed to T1 or shared with T1?
        for(Object g : passedArrayT2){
            if(g instanceof Guarded){
            	passedT2.add((Guarded)g);
            	if(interferesWith((Guarded)g, combinedT1, passedReachableT2))
            		error();
            }
        }
        
        // do any objects shared with T2 interfere with those passed to T1?
        for(Object g : sharedArrayT2)
        	if(g instanceof Guarded)
        		if(interferesWith((Guarded)g, passedReachableT1, sharedReachableT2))
            		error();

        // now all sets have been collected
        		
        // objects that are shared and passed are effectively passed
        for(Guarded g : passedReachableT2)
        	sharedReachableT2.remove(g);
       
        return new Set[] {passedT1, passedReachableT1, sharedReachableT1, passedT2, passedReachableT2, sharedReachableT2};
	}
	
	// add all guarded objects to the set that are reachable from 'from' to processed, and alsoAdd if it is not null
	private static void collectReachables(Guarded from, Set<Guarded> processed, Set<Guarded> alsoAdd){
		if(processed.add(from)) {
			if(alsoAdd != null)
    			alsoAdd.add(from);
            for(Object g : from.guardedRefs())
                if(g instanceof Guarded)
                    collectReachables((Guarded)g, processed, alsoAdd);
        }
	}
	
	// returns true iff 'g' or an object reachable from 'g' is contained in 'with' or can interfere with it (slices)
	// accumulates in processed all guarded objects reachable from g
	private static boolean interferesWith(Guarded g, Set<Guarded> with, Set<Guarded> processed){
		if(!processed.add(g))
			return false;
		
		if((g instanceof GuardedSlice<?> && ((GuardedSlice<?>)g).overlappingSlices.contains(g)) || with.contains(g))
			return true;
		
		for(Object gu : g.guardedRefs())
			if(gu instanceof Guarded)
				if(interferesWith((Guarded)gu, with, processed))
					return true;
		
		return false;
	}
	
	private static Set<Guarded> newIdentitySet() {
        return newSetFromMap(new IdentityHashMap<Guarded, java.lang.Boolean>());
    }
	
}
