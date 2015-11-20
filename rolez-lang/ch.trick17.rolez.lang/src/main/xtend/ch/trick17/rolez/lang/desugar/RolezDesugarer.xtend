package ch.trick17.rolez.lang.desugar

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import javax.inject.Inject
import org.eclipse.xtext.linking.lazy.SyntheticLinkingSupport

import static ch.trick17.rolez.lang.Constants.*

class RolezDesugarer extends AbstractDeclarativeDesugarer {

    extension RolezFactory = RolezFactory.eINSTANCE
    @Inject extension RolezExtensions
    @Inject extension SyntheticLinkingSupport

    @Rule
    def addDefaultConstr(NormalClass it) {
        if(constrs.isEmpty) {
            val c = createConstr
            if(isMapped) c.mapped = true
            else c.body = createBlock
            // eResource.contents += c
            constrs += c
        }
    }
    
    @Rule
    def addSuperConstrCall(Constr it) {
        if(body != null && !(body.stmts.head instanceof SuperConstrCall)
                && enclosingClass.qualifiedName != objectClassName) {
            val call = createSuperConstrCall
            // eResource.contents += call
            body.stmts.add(0, call)
            call.createAndSetProxy(rolezPackage.superConstrCall_Target, "super")
            call
        }
    }
}