package ch.trick17.rolez.tpi

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import ch.trick17.rolez.tpi.TPIException
import ch.trick17.rolez.tpi.TPIProvider
import java.util.Collection
import java.util.HashSet
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.validation.RolezValidator.*

import static extension org.junit.Assert.*
import org.eclipse.xtext.junit4.validation.ValidationTestHelper

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class TaskParameterInferenceTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtils
    @Inject extension TPIProvider
    @Inject extension ValidationTestHelper
    
    @Test def testEmptyParallel() {
    	val res = testResources
        parse('''
            parallel {
            	
            }
            and {
            	
            }
        '''.withFrame0, res).testParallelStmt(#[], #[])
    }
    
    @Test def testParallelVar1() {
        parse('''
            parallel {
            	objA.mPure();
            }
            and {
                objA.mPure();
            }
        '''.withFrameA1, testResources).testParallelStmt(#["objA"], #["objA"])
    }
    
    @Test def testParallelVar2() {
        parse('''
            parallel {
            	objA.mRO();
            }
            and {
                objA.mRO();
            }
        '''.withFrameA1, testResources).testParallelStmt(#["objA"], #["objA"])
    }
    
    @Test def testParallelVar3() {
        parse('''
            parallel {
            	objA.mPure();
            }
            and {
                objA.mRW();
            }
        '''.withFrameA1, testResources).testParallelStmt(#["objA"], #["objA"])
    }
    
    @Test def testParallelVar4() {
        parse('''
            parallel {
            	objA.mRO();
            }
            and {
                objA.mRW();
            }
        '''.withFrameA1, testResources).assertError(PARALLEL_STMT, NO_TPI)
    }
    
    @Test def testParallelVar5() {
        parse('''
            parallel {
            	objA.mRW();
            }
            and {
                objA.mRW();
            }
        '''.withFrameA1, testResources).assertError(PARALLEL_STMT, NO_TPI)
    }
    
    @Test def testParallelVar6() {
        parse('''
            parallel {
            	objA1.mRW();
            }
            and {
                objA2.mRW();
            }
        '''.withFrameA2, testResources).testParallelStmt(#["objA1"], #["objA2"])
    }
    
    @Test def testParallelVar7() {
        parse('''
            parallel {
            	objA1.mRO();
            	objA2.mRO();
            }
            and {
            	objA1.mRO();
            	objA2.mRO();
            }
        '''.withFrameA2, testResources).testParallelStmt(#["objA1", "objA2"], #["objA1", "objA2"])
    }
    
    @Test def testParallelField1() {
        parse('''
            parallel {
            	objB.field1.mRO();
            }
            and {
                objB.field1.mRO();
            }
        '''.withFrameB1, testResources).testParallelStmt(#["objB"], #["objB"])
    }
    
    @Test def testParallelField2() {
        parse('''
            parallel {
            	objB.field1.mPure();
            }
            and {
                objB.field1.mRW();
            }
        '''.withFrameB1, testResources).testParallelStmt(#["objB.field1"], #["objB.field1"])
    }
    
    @Test def testParallelField3() {
        parse('''
            parallel {
            	objB.field1.mRW();
            }
            and {
                objB.field1.mRW();
            }
        '''.withFrameB1, testResources).assertError(PARALLEL_STMT, NO_TPI)
    }
    
    @Test def testParallelField4() {
        parse('''
            parallel {
            	objB.field1.mRW();
            }
            and {
                objB.field2.mRW();
            }
        '''.withFrameB1, testResources).testParallelStmt(#["objB.field1"], #["objB.field2"])
    }
    
    @Test def testParallelField5() {
        parse('''
            parallel {
            	objB.field1.mRW();
            	objB.field2.mPure();
            }
            and {
                objB.field1.mPure();
                objB.field2.mRW();
            }
        '''.withFrameB1, testResources).testParallelStmt(#["objB.field1", "objB.field2"], #["objB.field1", "objB.field2"])
    }
    
    @Test def testParallelField6() {
        parse('''
            parallel {
            	objC.fieldA.mRW();
            	objC.fieldB.field1.mRW();
            }
            and {
                objC.fieldB.field2.mRW();
            }
        '''.withFrameC1, testResources).testParallelStmt(#["objC.fieldA", "objC.fieldB.field1"], #["objC.fieldB.field2"])
    }
    
    @Test def testParallelField7() {
        parse('''
            parallel {
            	objC.fieldC.mRO();
            }
            and {
            	objC.fieldC.fieldC.mRW();
            }
        '''.withFrameC1, testResources).testParallelStmt(#["objC.fieldC.mRO()"], #["objC.fieldC.fieldC"]) //TODO: does this make sense?
    }
    
    @Test def testParallelField8() {
        parse('''
            parallel {
            	objD.field = 3;
            }
            and {
            	objD.mPure();
            }
        '''.withFrameD1, testResources).testParallelStmt(#["objD"], #["objD"])
    }
    
    @Test def testParallelField9() {
        parse('''
            parallel {
            	objD.field = 3;
            }
            and {
            	objD.mRO();
            }
        '''.withFrameD1, testResources).assertError(PARALLEL_STMT, NO_TPI)
    }
    
    @Test def testParallelField10() {
        parse('''
            parallel {
            	val a = objC.fieldA;
            	val c: readonly C = objC.fieldC;
            	a.mRW();
            	c.mRO();
            }
            and {
            	val b = objC.fieldB;
            	val c: readonly C = objC.fieldC;
            	b.mRW();
            	c.mRO();
            }
        '''.withFrameC1, testResources).testParallelStmt(#["objC.fieldA", "objC.fieldC"], #["objC.fieldB", "objC.fieldC"]) //TODO: does this make sense?
    }
    
    @Test def testParallelNAM1() {
        parse('''
            parallel {
            	objB.getF1().mRO();
            }
            and {
                objB.getF1().mRO();
            }
        '''.withFrameB1, testResources).testParallelStmt(#["objB"], #["objB"])
    }
    
    @Test def testParallelSlicing1() {
        parse('''
            parallel {
            	(objE slice a).fieldA = 5;
            }
            and {
            	(objE slice b).fieldB = 2;
            }
        '''.withFrameE1, testResources).testParallelStmt(#["objE slice a"], #["objE slice b"])
    }
    
    @Test def testParallelSlicing2() {
        parse('''
            parallel {
            	(objE slice a).fieldA = 5;
            }
            and {
            	objE.mRW();
            }
        '''.withFrameE1, testResources).assertError(PARALLEL_STMT, NO_TPI)
    }
    
    @Test def testParallelSlicing3() {
        parse('''
            parallel {
            	(objE slice a).fieldA = 5;
            }
            and {
            	(objE slice b).fieldB = objE.fieldA;
            }
        '''.withFrameE1, testResources).testParallelStmt(#["objE slice a"], #["objE slice b", "objE.fieldA"])
    }
    
    @Test def testEmptyParfor() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	
            }
        '''.withFrame0, testResources).testParforStmt(#[])
    }
    
    @Test def testParforVar1() {
        parse('''
        	parfor(var i = 0; i < 10; i++) {
        		objA.mRO();
        	}
        '''.withFrameA1, testResources).testParforStmt(#["objA"])
    }
    
    @Test def testParforVar2() {
        parse('''
        	parfor(var i = 0; i < 10; i++) {
        		objA.mRW();
        	}
        '''.withFrameA1, testResources).assertError(PARFOR, NO_TPI)
    }
    
    @Test def testParforField1() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objB.field1.mRO();
            }
        '''.withFrameB1, testResources).testParforStmt(#["objB"])
    }
    
    @Test def testParforField2() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objB.field1.mRW();
            }
        '''.withFrameB1, testResources).assertError(PARFOR, NO_TPI)
    }
    
    @Test def testParforField3() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objD.field = 3;
            }
        '''.withFrameD1, testResources).assertError(PARFOR, NO_TPI)
    }
    
    @Test def testParforNAM1() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objB.getF1().mRO();
            }
        '''.withFrameB1, testResources).testParforStmt(#["objB"])
    }
    
    @Test def testParforSlicing1() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	val j = (objE slice a).fieldA + 2;
            }
        '''.withFrameE1, testResources).testParforStmt(#["objE"])
    }
    
    @Test def testParforSlicing2() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	(objE slice a).fieldA = 5;
            }
        '''.withFrameE1, testResources).assertError(PARFOR, NO_TPI)
    }
    
    @Test def testParforStepVar1() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objF.array.get(i).mRO();
            }
        '''.withFrameF1, testResources).testParforStmt(#["i", "objF"])
    }
    
    @Test def testParforStepVar2() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objF.array.get(i).mRW();
            }
        '''.withFrameF1, testResources).testParforStmt(#["objF.array.get(i)"])
    }
    
    /* Test infrastructure */
    
    /** Wraps the given Rolez code in a m() method and in a class. */
    private def withFrame0(CharSequence it) '''
        class M {
            def readwrite m(): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(A) method and in a class. */
    private def withFrameA1(CharSequence it) '''
        class M {
            def readwrite m(objA: readwrite A): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(A) method and in a class. */
    private def withFrameA2(CharSequence it) '''
        class M {
            def readwrite m(objA1: readwrite A, objA2: readwrite A): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(B) method and in a class. */
    private def withFrameB1(CharSequence it) '''
        class M {
            def readwrite m(objB: readwrite B): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(C) method and in a class. */
    private def withFrameC1(CharSequence it) '''
        class M {
            def readwrite m(objC: readwrite C): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(D) method and in a class. */
    private def withFrameD1(CharSequence it) '''
        class M {
            def readwrite m(objD: readwrite D): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(E) method and in a class. */
    private def withFrameE1(CharSequence it) '''
        class M {
            def readwrite m(objE: readwrite E): {
                «it»
            }
        }
    '''
    
    /** Wraps the given Rolez code in a m(F) method and in a class. */
    private def withFrameF1(CharSequence it) '''
        class M {
            def readwrite m(objF: readwrite F): {
                «it»
            }
        }
    '''
    
    private def testParallelStmt(Program p, Collection<String> params1, Collection<String> params2) {
    	val tpi = findParallelStmt(p).tpi
    	
    	val tpiSet1 = new HashSet(tpi.get(0).selectedParams.map[toString])
    	val tpiSet2 = new HashSet(tpi.get(1).selectedParams.map[toString])
    	
    	val expectedSet1 = new HashSet(params1)
    	val expectedSet2 = new HashSet(params2)
    	
    	expectedSet1.assertEquals(tpiSet1)
    	expectedSet2.assertEquals(tpiSet2)
    }
    
    private def testParforStmt(Program p, Collection<String> params) {
    	val tpi = findParforStmt(p).tpi
    	
    	val tpiSet = new HashSet(tpi.selectedParams.map[toString])
    	val expectedSet = new HashSet(params)
    	expectedSet.assertEquals(tpiSet)
    }
    
    private def ParallelStmt findParallelStmt(Program p) {
    	val body = p.findClass("M").findMethod("m").body
    	findParallelStmt(body)
    }
    
    private def ParallelStmt findParallelStmt(Block b) {
    	for (stmt : b.stmts) {
    		if (stmt instanceof ParallelStmt)
    			return stmt;
    	}
    	return null;
    }
    
    private def Parfor findParforStmt(Program p) {
    	val body = p.findClass("M").findMethod("m").body
    	findParforStmt(body)
    }
    
    private def Parfor findParforStmt(Block b) {
    	for (stmt : b.stmts) {
    		if (stmt instanceof Parfor)
    			return stmt;
    	}
    	return null;
    }
    
    private def testResources() {
    	newResourceSet.with('''
    		class rolez.lang.Object mapped to java.lang.Object {
    			mapped def readonly equals(o: readonly Object): boolean
    			mapped def readonly hashCode: int
    			mapped def readonly toString: pure String
    		}
    		class rolez.lang.Slice[T] mapped to rolez.lang.Slice {
    			mapped def pure arrayLength: int
    			mapped def readonly  get(index: int): T
    			mapped def readwrite set(index: int, component: T):
    			mapped def r slice[r](begin: int, end: int, step: int): r Slice[T]
    		}
    		class rolez.lang.Array[T] mapped to rolez.lang.Array extends Slice[T] {
    			mapped val length: int
    			mapped new(i: int)
    		}
    		pure class rolez.lang.String mapped to java.lang.String {
    			mapped new(chars: readonly Array[char])
    			mapped def pure length: int
    			mapped def pure substring(b: int, e: int): pure String
    		}
    		class A {
    			def pure mPure(): {}
    			def readonly mRO(): {}
    			def readwrite mRW(): {}
    		}
    		class B extends A {
    			var field1: readwrite A
    			var field2: readwrite A
    			
    			def readonly getF1(): readonly A {
    				return this.field1;
    			}
    			    			
    			def readonly getF2(): readonly A {
    				return this.field2;
    			}
    		}
    		class C extends A {
    			var fieldA: readwrite A
    			var fieldB: readwrite B
    			var fieldC: readwrite C
    		}
    		class D extends A {
    			var field: int
    		}
    		class E extends A {
    			slice a { var fieldA: int }
    			slice b { var fieldB: int }
    		}
    		class F extends A {
    			var array: readwrite Slice[readwrite A]
    		}
        ''')
    }
    
}