package ch.trick17.rolez.lang.desugar

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import javax.inject.Inject
import org.eclipse.xtext.linking.lazy.SyntheticLinkingSupport
import ch.trick17.rolez.lang.rolez.ForLoop
import ch.trick17.rolez.lang.rolez.Block

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
}