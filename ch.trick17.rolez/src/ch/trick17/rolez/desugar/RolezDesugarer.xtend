package ch.trick17.rolez.desugar

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Super
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.ThisParam
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
            else c.code = createBlock
        }
    }
    
    private def hasNoArgConstr(JvmGenericType it) {
        declaredConstructors.exists[visibility == JvmVisibility.PUBLIC && parameters.isEmpty]
    }
    
    @Rule
    def addSuperClassRef(Class it) {
        if(superclassRef === null && !isObjectClass)
            superclassRef = createSimpleClassRef => [
                createReference(SIMPLE_CLASS_REF__CLAZZ, objectClassName.toString)
            ]
    }
    
    @Rule
    def addUpperBound(RoleParam it) {
        if(upperBound === null)
            upperBound = createPure
    }
    
    @Rule
    def addSuperConstrCall(Constr it) {
        if(body !== null && !(body.stmts.head instanceof SuperConstrCall)
                && !enclosingClass.isObjectClass) {
            val supr = createSuperConstrCall
            body.stmts.add(0, supr)
            supr.createReference(SUPER_CONSTR_CALL__CONSTR, "super")
        }
    }
    
    @Rule
    def addThisParam(Constr it) {
        if(thisParam === null)
            thisParam = createThisParam => [
                rawType = createRoleType => [
                    role = createReadWrite
                ]
            ]
    }
    
    @Rule
    def completeThisParam(ThisParam it) {
        if(name === null) {
            name = "this"
            val clazz = enclosingClass
            if(clazz instanceof NormalClass && (clazz as NormalClass).typeParam !== null) {
                type.base = createGenericClassRef => [
                    rawTypeArg = createTypeParamRef => [
                        param = (clazz as NormalClass).typeParam
                    ]
                ]
                type.base.createReference(GENERIC_CLASS_REF__CLAZZ, clazz.qualifiedName.toString)
            }
            else {
                type.base = createSimpleClassRef
                type.base.createReference(SIMPLE_CLASS_REF__CLAZZ, clazz.qualifiedName.toString)
            }
            if(enclosingSlice !== null)
                type.createReference(ROLE_TYPE__SLICE, enclosingSlice.name)
        }
    }
    
    @Rule
    def completeRoleType(RoleType it) {
        if(role === null) {
            role = createPure
        }
    }
    
    @Rule
    def modifySuperRef(Super it) {
        createReference(REF__REFEREE, "this")
    }
}