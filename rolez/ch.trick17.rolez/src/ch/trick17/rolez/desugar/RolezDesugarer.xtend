package ch.trick17.rolez.desugar

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SuperConstrCall
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmVisibility

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*

import static extension ch.trick17.rolez.RolezExtensions.*

class RolezDesugarer extends AbstractDeclarativeDesugarer {

    @Inject extension RolezFactory

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
    def addUpperBound(RoleParam it) {
        if(upperBound == null)
            upperBound = createPure
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
}