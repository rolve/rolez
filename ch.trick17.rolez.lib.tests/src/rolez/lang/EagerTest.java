package rolez.lang;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static rolez.lang.Eager.*;

import javax.management.RuntimeErrorException;

public class EagerTest {
	
	@Rule
	public final ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testInterferenceExceptions() {
		GuardedArray arr0 = GuardedArray.wrap(new Object[] {});
		GuardedArray arr1 = GuardedArray.wrap(new Object[] {arr0});
		
		exception.expect(ConcurrentInterferenceException.class);
		collectAndCheck(new Object[][]{
				new Object[] {arr0},
				new Object[] {},
				new Object[] {},
				new Object[] {arr1},
			} , 1L);	
	}
	
	@Test
	public void testInterferenceExceptionsGuarded() {
		GuardedArray arr0 = GuardedArray.wrap(new Object[] {});
		GuardedArray arr1 = GuardedArray.wrap(new Object[] {arr0});
		
		exception.expect(ConcurrentInterferenceException.class);
		collectAndCheckGuarded(new Object[][]{
				new Object[] {arr0},
				new Object[] {},
				new Object[] {},
				new Object[] {arr1},
			} , 1L);	
	}
	
	@Test
	public void testSliceInterferenceException1() {

		GuardedArray arr0 = GuardedArray.wrap(new Object[] {});
		GuardedArray arr1 = GuardedArray.wrap(new Object[] {arr0});
		GuardedSlice<Object> slice = GuardedArray.wrap(new Object[] {0, arr0, 2, 3, 4, 5, arr1, 7, 8, 9 });
		
		GuardedSlice<Object> part1 = slice.slice(0,  3);
		GuardedSlice<Object> part2 = slice.slice(4, 10);
		
		
		exception.expect(ConcurrentInterferenceException.class);
		collectAndCheck(new Object[][]{
				new Object[] {part1},
				new Object[] {},
				new Object[] {},
				new Object[] {part2},
			} , 1L);	
		
	}
	
}
