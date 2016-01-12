package ch.trick17.rolez.desugar

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.OpArithmetic
import ch.trick17.rolez.rolez.OpLogical
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SuperConstrCall
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.OpArithmetic.*
import static ch.trick17.rolez.rolez.OpAssignment.*
import static ch.trick17.rolez.rolez.OpLogical.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

class RolezDesugarer extends AbstractDeclarativeDesugarer {

    @Inject extension RolezFactory
    @Inject extension RolezExtensions

    @Rule
    def void addDefaultConstr(NormalClass it) {
        if(constrs.isEmpty && (!isMapped || jvmClass.hasNoArgConstr)) {
            val c = createConstr
            constrs += c
            if(mapped) c.createReference(CONSTR__JVM_CONSTR, "mapped")
            else c.body = createBlock
        }
    }
    
    private def hasNoArgConstr(JvmGenericType it) {
        declaredConstructors.exists[visibility == JvmVisibility.PUBLIC && parameters.isEmpty]
    }
    
    @Rule
    def addSuperClassRef(Class it) {
        if(superclassRef == null && !isObjectClass)
            superclassRef = createSimpleClassRef => [
                createReference(SIMPLE_CLASS_REF__CLAZZ, objectClassName.toString)
            ]
    }
    
    @Rule
    def addSuperConstrCall(Constr it) {
        if(body != null && !(body.stmts.head instanceof SuperConstrCall)
                && !enclosingClass.isObjectClass) {
            val supr = createSuperConstrCall
            body.stmts.add(0, supr)
            supr.createReference(SUPER_CONSTR_CALL__CONSTR, "super")
        }
    }
    
    @Rule
    def addElsePart(IfStmt it) {
        if(elsePart == null)
            elsePart = createBlock
    }
    
    @Rule
    def desugarForLoop(ForLoop orig) {
        // Don't attempt to desugar if there are syntax errors
        if(!orig.eResource.errors.isEmpty) return createBlock
        
        createBlock => [
            stmts += orig.initializer
            stmts += createWhileLoop => [
                condition = orig.condition
                body = createBlock => [
                    val origBody = orig.body
                    stmts += switch(origBody) {
                        Block: origBody.stmts
                        default: #[origBody]
                    }
                    stmts += createExprStmt => [
                        expr = orig.step
                    ]
                ]
            ]
        ]
    }
    
    @Rule
    def desugarAssignment(Assignment orig) {
        switch(orig.op) {
            case            ASSIGN:                  orig
            case         OR_ASSIGN:    logicalAssign(orig,         OR)
            case        AND_ASSIGN:    logicalAssign(orig,        AND)
            case       PLUS_ASSIGN: arithmeticAssign(orig,       PLUS)
            case      MINUS_ASSIGN: arithmeticAssign(orig,      MINUS)
            case      TIMES_ASSIGN: arithmeticAssign(orig,      TIMES)
            case DIVIDED_BY_ASSIGN: arithmeticAssign(orig, DIVIDED_BY)
            case     MODULO_ASSIGN: arithmeticAssign(orig,     MODULO)
        }
    }
    
    private def logicalAssign(Assignment orig, OpLogical theOp) {
        createAssignment => [
            op = ASSIGN
            left = orig.left.copy
            right = createLogicalExpr => [
                op = theOp
                left = orig.left
                right = orig.right
            ]
        ]
    }
    
    private def arithmeticAssign(Assignment orig, OpArithmetic theOp) {
        createAssignment => [
            op = ASSIGN
            left = orig.left.copy
            right = createArithmeticBinaryExpr => [
                op = theOp
                left = orig.left
                right = orig.right
            ]
        ]
    }
}