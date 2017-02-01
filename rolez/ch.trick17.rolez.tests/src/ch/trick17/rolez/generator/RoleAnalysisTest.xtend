package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.tests.RolezInjectorProvider
import ch.trick17.rolez.validation.cfg.CfgProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

import static ch.trick17.rolez.generator.CodeKind.*
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
    @Inject extension CfgProvider
    @Inject extension RoleAnalysis.Provider
    
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
        newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
                .dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
        
        task = parse('''
            val a1 = new A; // new instances are readwrite
            for(var i = 0; i < 10; i += 1) {}
            a1;             // still readwrite
        '''.withFrame).task
        newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
                .dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
        
        task = parse('''
            val a1 = this.getA; // pure
            for(var i = 0; i < "hi".hashCode; i += 1)
                a1.i += 42;
            a1;                 // still pure, since loop may have been skipped
        '''.withFrame).task
        newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
                .dynamicRole(task.lastExpr).assertThat(instanceOf(Pure))
    }

    @Test def testDataflowFinalFields() {
        var task = parse('''
            val obj = new B;
            obj.a.i = 42;
            obj.a; // since obj.a is final and already accessed above, it is readwrite here
        '''.withFrame).task
        newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
                .dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))

        task = parse('''
            val obj1 = new B;
            obj1.a.i = 42;
            val obj2 = obj1; // information about obj1's final fields is preserved when assigning it
            val a1 = obj2.a; // information is also preserved when assigning the field to a var
            a1;
        '''.withFrame).task
        newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
                .dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
        
        task = parse('''
            val a1 = this.getA;
            var sum = 0;
            for(var i = 0; i < a1.array.length; i += 1)
                sum += a1.array.get(i) * a1.array.get(i);
        '''.withFrame).task
        val analysis = newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
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
        
        newRoleAnalysis(constr, constr.body.controlFlowGraph)
                .dynamicRole(constr.lastExpr).assertThat(instanceOf(ReadWrite))
    }
}
