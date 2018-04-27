package ch.trick17.rolez.generator

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

import static ch.trick17.rolez.generator.MethodKind.*

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ChildTasksAnalysisTest {
    
    @Inject extension TestUtils
    @Inject extension ParseHelper<Program>
    @Inject extension ChildTasksAnalysisProvider
    
    @Test def testGuardedMethod() {
        var task = parse('''
            this;
            this start frameTask(true, false);
            this;
        '''.withFrame).task
        var analysis = newChildTasksAnalysis(task, GUARDED_METHOD)
        analysis.childTasksMayExist(task.firstExpr).assertTrue
        analysis.childTasksMayExist(task.lastExpr).assertTrue
    }
    
    @Test def testUnguardedMethod() {
        var task = parse('''
            this;
            this.somethingAsync;
            this;
        '''.withFrame).task
        var analysis = newChildTasksAnalysis(task, UNGUARDED_METHOD)
        analysis.childTasksMayExist(task.firstExpr).assertFalse
        analysis.childTasksMayExist(task.lastExpr).assertTrue
        
        task = parse('''
            this;
            this start frameTask(true, false);
            this;
        '''.withFrame).task
        analysis = newChildTasksAnalysis(task, UNGUARDED_METHOD)
        analysis.childTasksMayExist(task.firstExpr).assertFalse
        analysis.childTasksMayExist(task.lastExpr).assertTrue
    }
    
    @Test def testTask() {
        var task = parse('''
            this;
            this.somethingAsync;
            this;
        '''.withFrame).task
        var analysis = newChildTasksAnalysis(task, TASK)
        analysis.childTasksMayExist(task.firstExpr).assertFalse
        analysis.childTasksMayExist(task.lastExpr).assertTrue
        
        task = parse('''
            this;
            this start frameTask(true, false);
            this;
        '''.withFrame).task
        analysis = newChildTasksAnalysis(task, TASK)
        analysis.childTasksMayExist(task.firstExpr).assertFalse
        analysis.childTasksMayExist(task.lastExpr).assertTrue
    }
}