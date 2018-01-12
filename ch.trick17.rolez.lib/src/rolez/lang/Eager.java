package rolez.lang;

import static java.util.Collections.newSetFromMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
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
	
	private static void error(String err){
		throw new RuntimeException("This parallel construct can't be executed concurrently due to interference. (" + err + ")");
	}
	
	
	/**
	 * @param receivers the receivers of each call. will be checked but it's object graph will not be in the returned sets
	 * @param receiverRole True: passed, False: shared, Null: pure
	 * @param args in form passedT1, sharedT1, passedT2, sharedT2, ... 
	 * returns the collected sets as follows:
	 * 0: passedT1
	 * 1: passedReachable T1
	 * 2: sharedReachable T1
	 * 3: passedT2
	 * 4: passedReachable T2
	 * 5: sharedReachable T2
	 * ...
	 * @return sets collected for potentially 
	 */
	public static Set<Guarded>[] collectAndCheckGuarded(Object[][] args, long idBits){
		// IMPROVE: collect all slices that could interfere, store them seperately
		// IMPROVE: combine collecting and guarding with checking (probably minor impact though)
		@SuppressWarnings("unchecked")
		Set<Guarded>[] out = new Set[args.length/2*3];
		
		for(int i = 0; i < args.length; i += 2){
			Object[] passedObjects = args[i];
			Object[] sharedObjects = args[i+1];
			
			Set<Guarded> passed = new HashSet<>();
	        for(Object g : passedObjects)
	            if(g instanceof Guarded)
	                passed.add((Guarded) g);
	        
	        Set<Guarded> passedReachable = newIdentitySet();
	        for(Guarded g : passed)
	            g.guardReadWriteReachable(passedReachable, idBits);
	        
	        // Objects that are reachable both from a passed and a shared object are effectively *passed*
	        Set<Guarded> sharedReachable = newIdentitySet();
	        sharedReachable.addAll(passedReachable);
	        for(Object g : sharedObjects)
	            if(g instanceof Guarded)
	                ((Guarded) g).guardReadOnlyReachable(sharedReachable, idBits);
	        sharedReachable.removeAll(passedReachable);
	        
			int taskInd = (i/2)*3;
			out[taskInd] = passed;
			out[taskInd+1] = passedReachable;
			out[taskInd+2] = sharedReachable;
		}
		
		check(out);
		
		return out;
	}
	
	public static Set<Guarded>[] collectAndCheck(Object[][] args, long idBits){
		// IMPROVE: collect all slices that could interfere
		@SuppressWarnings("unchecked")
		Set<Guarded>[] out = new Set[args.length/2*3];

		for(int i = 0; i < args.length; i += 2){
			Object[] passedObjects = args[i];
			Object[] sharedObjects = args[i+1];

			Set<Guarded> passed = new HashSet<>();
			for(Object g : passedObjects)
				if(g instanceof Guarded)
					passed.add((Guarded) g);

			Set<Guarded> passedReachable = newIdentitySet();
			for(Guarded g : passed)
				collectReachables(g, passedReachable);

			// Objects that are reachable both from a passed and a shared object are effectively *passed*
			Set<Guarded> sharedReachable = newIdentitySet();
			sharedReachable.addAll(passedReachable);
			for(Object g : sharedObjects)
				if(g instanceof Guarded)
					collectReachables((Guarded)g, sharedReachable);
			sharedReachable.removeAll(passedReachable);

			int taskInd = (i/2)*3;
			out[taskInd] = passed;
			out[taskInd+1] = passedReachable;
			out[taskInd+2] = sharedReachable;
		}

		check(out);

		return out;
	}
	
	
	private static void check(Set<Guarded>[] sets){
		
		List<Set<Guarded>> accumulatedPassed = new ArrayList<>(sets.length / 2);
		List<Set<Guarded>> accumulatedShared = new ArrayList<>(sets.length / 2);
		for(int i = 0; i < sets.length; i += 3){
			Set<Guarded> passed = sets[i+1];
			Set<Guarded> shared = sets[i+2];
			
			// check interference
			if(interferesWith(passed, accumulatedShared))
				error(passed.toString() + " (passed objects) interferes with " + accumulatedShared.toString() + " (other shared objects)");
			if(interferesWith(passed, accumulatedPassed))
				error(passed.toString() + " (passed objects) interferes with " + accumulatedPassed.toString() + " (other passed objects)");
			if(interferesWith(shared, accumulatedPassed))
				error(shared.toString() + " (shared objects) interferes with " + accumulatedPassed.toString() + " (other passed objects)");
			
			// add these sets to the list to be checked against
			accumulatedPassed.add(passed);
			accumulatedShared.add(shared);
		}
		
	}
	
	// add all guarded objects to the set that are reachable from 'from' to processed
	private static void collectReachables(Guarded from, Set<Guarded> processed){
		if(processed.add(from)) {
            for(Object g : from.guardedRefs())
                if(g instanceof Guarded)
                    collectReachables((Guarded)g, processed);
        }
	}
	
	// check if any object in 'set' interferes with an object in one of the 'with' sets
	private static boolean interferesWith(Set<Guarded> set, List<Set<Guarded>> with){
		for(Set<Guarded> w : with){
			for(Guarded g : set){
				if(checkInterference(g, w))
					return true;
			}
		}
		return false;
	}
	
	
	// returns true iff g is contained in with or is a slice overlapping with a slice in with
	private static boolean checkInterference(Guarded g, Set<Guarded> with){
		if(with.contains(g))
			return true;
		if(g instanceof GuardedSlice<?>){
			GuardedSlice<?> gs = ((GuardedSlice<?>)g);
			// is it faster to loop over the overlapping slices or the other guarded objects?
			if(gs.overlappingSlices.size() <= with.size()){
				for(GuardedSlice<?> s : gs.overlappingSlices)
					if(with.contains(s))
						return true;
			} else {
				for(Guarded gu : with)
					if(gu instanceof GuardedSlice<?> && ((GuardedSlice<?>)gu).overlappingSlices.contains(gs))
						return true;
				
			}
		}
		return false;
	}
	
	private static Set<Guarded> newIdentitySet() {
        return newSetFromMap(new IdentityHashMap<Guarded, java.lang.Boolean>());
    }
	
}
