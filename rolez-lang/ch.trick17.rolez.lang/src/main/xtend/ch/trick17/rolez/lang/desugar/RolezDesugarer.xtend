package ch.trick17.rolez.lang.desugar

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.ForLoop
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.OpArithmetic
import ch.trick17.rolez.lang.rolez.OpLogical
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import javax.inject.Inject
import org.eclipse.xtext.linking.lazy.SyntheticLinkingSupport

import static ch.trick17.rolez.lang.rolez.OpArithmetic.*
import static ch.trick17.rolez.lang.rolez.OpAssignment.*
import static ch.trick17.rolez.lang.rolez.OpLogical.*

import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

class RolezDesugarer extends AbstractDeclarativeDesugarer {

    extension RolezFactory = RolezFactory.eINSTANCE
    @Inject extension RolezExtensions
    @Inject extension SyntheticLinkingSupport

    @Rule
    def void addDefaultConstr(NormalClass it) {
        if(constrs.isEmpty) {
            val c = createConstr
            if(isMapped) c.mapped = true
            else c.body = createBlock
            constrs += c
        }
    }
    
    @Rule
    def addSuperConstrCall(Constr it) {
        if(body != null && !(body.stmts.head instanceof SuperConstrCall)
                && !enclosingClass.isObjectClass) {
            val supr = createSuperConstrCall
            body.stmts.add(0, supr)
            supr.createAndSetProxy(rolezPackage.superConstrCall_Target, "super")
        }
    }
    
    @Rule
    def addSuperClass(Class it) {
        if(superclass == null && !isObjectClass)
            createAndSetProxy(rolezPackage.class_Superclass, "rolez.lang.Object")
    }
    
    @Rule
    def addElsePart(IfStmt it) {
        if(elsePart == null)
            elsePart = createBlock
    }
    
    @Rule
    def desugarForLoop(ForLoop orig) {
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