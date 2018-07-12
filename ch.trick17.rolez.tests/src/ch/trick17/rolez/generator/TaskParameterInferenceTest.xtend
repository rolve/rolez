package ch.trick17.rolez.generator

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.tests.RolezInjectorProvider
import com.google.inject.Injector
import java.util.Collection
import java.util.HashSet
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith

import static extension org.junit.Assert.*
import ch.trick17.rolez.rolez.Block

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class TaskParameterInferenceTest {
    
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtils
    
    @Inject extension Injector
    
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
        '''.withFrameA1, testResources).testParallelStmtNoSolution()
    }
    
    @Test def testParallelVar5() {
        parse('''
            parallel {
            	objA.mRW();
            }
            and {
                objA.mRW();
            }
        '''.withFrameA1, testResources).testParallelStmtNoSolution()
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
        '''.withFrameB1, testResources).testParallelStmt(#["objB"], #["objB"])
    }
    
    @Test def testParallelField3() {
        parse('''
            parallel {
            	objB.field1.mRW();
            }
            and {
                objB.field1.mRW();
            }
        '''.withFrameB1, testResources).testParallelStmtNoSolution()
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
        '''.withFrameC1, testResources).testParallelStmt(#["objC.fieldC"], #["objC.fieldC"]) //TODO: does this make sense?
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
        '''.withFrameD1, testResources).testParallelStmtNoSolution()
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
        '''.withFrameE1, testResources).testParallelStmtNoSolution()
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
        '''.withFrameA1, testResources).testParforStmtNoSolution()
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
        '''.withFrameB1, testResources).testParforStmtNoSolution()
    }
    
    @Test def testParforField3() {
        parse('''
            parfor(var i = 0; i < 10; i++) {
            	objD.field = 3;
            }
        '''.withFrameD1, testResources).testParallelStmtNoSolution()
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
        '''.withFrameE1, testResources).testParforStmtNoSolution()
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
    
    private def testParallelStmt(Program p, Collection<String> params1, Collection<String> params2) {
    	//TODO: better exception handling
    	val tpi = TPIResult.selectParameters(findParallelStmt(p), newNodeBuilder())
    	
    	val tpiSet1 = new HashSet(tpi.get(0).selectedParams.map[toString])
    	val tpiSet2 = new HashSet(tpi.get(1).selectedParams.map[toString])
    	
    	val expectedSet1 = new HashSet(params1)
    	val expectedSet2 = new HashSet(params2)
    	
    	expectedSet1.assertEquals(tpiSet1)
    	expectedSet2.assertEquals(tpiSet2)
    }
    
    private def testParallelStmtNoSolution(Program p) {
    	try {
    		TPIResult.selectParameters(findParallelStmt(p), newNodeBuilder())
    	}
    	catch (Exception e) {
    		//TODO: better exception handling
    		return
    	}
    	fail()
    }
    
    private def testParforStmt(Program p, Collection<String> params) {
    	//TODO: better exception handling
    	val tpi = TPIResult.selectParameters(findParforStmt(p), newNodeBuilder())
    	
    	val tpiSet = new HashSet(tpi.selectedParams.map[toString])
    	val expectedSet = new HashSet(params)
    	expectedSet.assertEquals(tpiSet)
    }
    
    private def testParforStmtNoSolution(Program p) {
    	try {
    		TPIResult.selectParameters(findParforStmt(p), newNodeBuilder())
    	}
    	catch (Exception e) {
    		//TODO: better exception handling
    		return
    	}
    	fail()
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
    
    private def newNodeBuilder() {
        new TPINodeBuilder() => [injectMembers]
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
        ''')
    }
    
}