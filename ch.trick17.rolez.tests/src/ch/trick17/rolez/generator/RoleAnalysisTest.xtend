package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

import static org.hamcrest.Matchers.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RoleAnalysisTest {
    
    @Inject extension RolezUtils
    @Inject extension TestUtils
    @Inject extension ParseHelper<Program>
    @Inject extension RoleAnalysisProvider
    
    @Test def testDataflowMerge() {
        var task = parse('''
            val a1 = new A;      // new instances are readwrite
            var a2: readwrite A;
            if(a)
                a2 = a1;
            else {
                a2 = this.getA;  // role of returned object is pure
                a2.i = 42;       // but after this access, it must be readwrite
            }
            a2;                  // a2 is known to be readwrite here
        '''.withFrame).task
        newRoleAnalysis(task).dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
        
        task = parse('''
            val a1 = new A; // new instances are readwrite
            for(var i = 0; i < 10; i++) {}
            a1;             // still readwrite
        '''.withFrame).task
        newRoleAnalysis(task).dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
        
        task = parse('''
            val a1 = this.getA; // pure
            for(var i = 0; i < "hi".hashCode; i++)
                a1.i += 42;
            a1;                 // still pure, since loop may have been skipped
        '''.withFrame).task
        newRoleAnalysis(task).dynamicRole(task.lastExpr).assertThat(instanceOf(Pure))
        
        task = parse('''
            val a1 = this.getA;                  // pure
            for(var i = 0; i < a1.hashCode; i++) // a1 is readonly after condition
                a1.i;                            // and should still be readonly here
        '''.withFrame).task
        val fieldAccess = task.all(MemberAccess).filter[isFieldAccess].map[target].head
        newRoleAnalysis(task).dynamicRole(fieldAccess).assertThat(instanceOf(ReadOnly))
    }

    @Test def testDataflowFinalFields() {
        var task = parse('''
            val obj = new B;
            obj.a.i = 42;
            obj.a; // since obj.a is final and already accessed above, it is readwrite here
        '''.withFrame).task
        newRoleAnalysis(task).dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))

        task = parse('''
            val obj1 = new B;
            obj1.a.i = 42;
            val obj2 = obj1; // information about obj1's final fields is preserved when assigning it
            val a1 = obj2.a; // information is also preserved when assigning the field to a var
            a1;
        '''.withFrame).task
        newRoleAnalysis(task).dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
        
        task = parse('''
            val a1 = this.getA;
            var sum = 0;
            for(var i = 0; i < a1.array.length; i++)
                sum += a1.array.get(i) * a1.array.get(i);
        '''.withFrame).task
        val analysis = newRoleAnalysis(task)
        val targets = task.all(MemberAccess).filter[isArrayGet].map[target].toList
        analysis.dynamicRole(targets.get(0)).assertThat(instanceOf(Pure))
        analysis.dynamicRole(targets.get(1)).assertThat(instanceOf(ReadOnly))
    }
    
    @Test def testDataflowConstructor() {
        var constr = parse('''
            class rolez.lang.Object mapped to java.lang.Object
            class A {
                new { this; }
            }
        ''').classes.filter(NormalClass).get(1).constrs.head
        
        newRoleAnalysis(constr).dynamicRole(constr.lastExpr).assertThat(instanceOf(ReadWrite))
    }
    
    @Test def testDataflowGuardedMethod() {
        // since Object.hashCode is guarded, it is known that the target is readonly afterwards
        var task = parse('''
            val a1 = this.getA;
            a1.hashCode;
            a1;
        '''.withFrame).task
        var analysis = newRoleAnalysis(task)
        var varRefs = task.all(Ref).filter[variable.name == "a1"].toList
        analysis.dynamicRole(varRefs.get(0)).assertThat(instanceOf(Pure))
        analysis.dynamicRole(varRefs.get(1)).assertThat(instanceOf(ReadOnly))
        
        // B overrides hashCode, making it non-guarded. A call to a non-guarded method does not
        // add any information, since method may not actually guard the object
        task = parse('''
            val b1 = this.getB;
            b1.hashCode;
            b1;
        '''.withFrame).task
        analysis = newRoleAnalysis(task)
        varRefs = task.all(Ref).filter[variable.name == "b1"].toList
        analysis.dynamicRole(varRefs.get(0)).assertThat(instanceOf(Pure))
        analysis.dynamicRole(varRefs.get(1)).assertThat(instanceOf(Pure))
    }
}
