package ch.trick17.rolez.generator

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
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

import static extension org.hamcrest.MatcherAssert.assertThat

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RoleAnalysisTest {
    
    @Inject extension TestUtils
    @Inject extension ParseHelper<Program>
    @Inject extension CfgProvider
    @Inject extension RoleAnalysis.Provider
    
    @Test def testDataflow() {
        val task = parse('''
            val a1 = new A; // new instances are readwrite
            var a2: readwrite A;
            if(a)
                a2 = a1;
            else {
                a2 = this.getA; // role of returned object is unknown
                a2.i = 42;      // but after this access, it must be readwrite
            }
            a2; // a2 is known to be readwrite here
        '''.withFrame).task
        
        val analysis = newRoleAnalysis(task, task.body.controlFlowGraph, TASK)
        analysis.dynamicRole(task.lastExpr).assertThat(instanceOf(ReadWrite))
    }
}