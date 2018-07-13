package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.tests.RolezInjectorProvider
import java.util.List
import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import static java.util.Arrays.asList
import static org.hamcrest.Matchers.*

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class ParameterizedParamListTest {
    
    @Inject extension RolezFactory
    
    var List<Param> orig
    
    @Before def void createOrig() {
        orig = asList(
            createParam => [name = "1"],
            createParam => [name = "2"],
            createParam => [name = "3"],
            createParam => [name = "4"],
            createParam => [name = "5"]
        )
    }
    
    @Test def testStuff() {
        val list = new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap)
        
        list.size.assertThat(is(5))
        list.isEmpty.assertThat(is(false))
        val parameterizedParam = list.get(0)
        parameterizedParam.assertThat(instanceOf(ParameterizedParam))
        parameterizedParam.name.assertThat(is("1"))
        
        // The following will test the (list)iterator:
        list.listIterator.map[(it as ParameterizedParam).eObject].toList.assertThat(is(equalTo(orig)))
        list.listIterator(2).map[(it as ParameterizedParam).eObject].toList.assertThat(is(equalTo(orig.subList(2, 5))))
        
        list.subList(1, 4).map[(it as ParameterizedParam).eObject].toList.assertThat(equalTo(orig.subList(1, 4)))
    }
    
    @Test(expected=AssertionError) def void testSet() {
        new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap).set(0, null)
    }
    
    @Test(expected=AssertionError) def void testAdd() {
        new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap).add(null)
    }
    
    @Test(expected=AssertionError) def void testRemove() {
        new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap).remove(0)
    }
    
    @Test(expected=AssertionError) def testListIteratorSet() {
        new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap).listIterator.set(null)
    }
    
    @Test(expected=AssertionError) def testListIteratorAdd() {
        new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap).listIterator.add(null)
    }
    
    @Test(expected=AssertionError) def testListIteratorRemove() {
        new ParameterizedParamList(orig, createMethod, emptyMap, emptyMap).listIterator.remove
    }
}