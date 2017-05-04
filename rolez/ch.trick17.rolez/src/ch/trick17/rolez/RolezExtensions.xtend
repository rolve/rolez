package ch.trick17.rolez

import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.generic.ParameterizedParam
import ch.trick17.rolez.rolez.Argumented
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BuiltInRole
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.InExecutable
import ch.trick17.rolez.rolez.Member
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Type
import org.eclipse.emf.ecore.EObject

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.RolezUtils.*

/**
 * Extension methods for the Rolez language elements
 */
class RolezExtensions {
    
    static def thisType(Constr it) {
        newRoleType(RolezFactory.eINSTANCE.createReadWrite, newClassRef(enclosingClass))
    }
    
    static def isObjectClass       (Class it) { qualifiedName ==        objectClassName }
    static def isSliceClass        (Class it) { qualifiedName ==         sliceClassName }
    static def isArrayClass        (Class it) { qualifiedName ==         arrayClassName }
    static def isVectorClass       (Class it) { qualifiedName ==        vectorClassName }
    static def isVectorBuilderClass(Class it) { qualifiedName == vectorBuilderClassName }
    static def isStringClass       (Class it) { qualifiedName ==        stringClassName }
    static def isTaskClass         (Class it) { qualifiedName ==          taskClassName }
    
    static def dispatch isSliceType(RoleType it) { base.clazz.isSliceClass }
    static def dispatch isSliceType(    Type it) { false }
    
    static def dispatch isArrayType(RoleType it) { base.clazz.isArrayClass }
    static def dispatch isArrayType(    Type it) { false }
    
    static def dispatch isVectorType(RoleType it) { base.clazz.isVectorClass }
    static def dispatch isVectorType(    Type it) { false }
    
    static def dispatch isVectorBuilderType(RoleType it) { base.clazz.isVectorBuilderClass }
    static def dispatch isVectorBuilderType(    Type it) { false }
    
    static def Iterable<Member> allMembers(Class it) {
        members +
            if(superclass === null) emptyList
            else superclass.allMembers.filter[m | !overrides(m)]
    }
    
    private static def overrides(Class it, Member m) {
        switch(m) {
            Field : false
            Method: methods.exists[equalErasedSignature(it, m)]
        }
    }
    
    static def Program enclosingProgram(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Program: container
            default: container?.enclosingProgram
        }
    }
    
    static def Class enclosingClass(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Class  : container
            default: container?.enclosingClass
        }
    }
    
    static def enclosingClass(Constr it) {
        (it as EObject).enclosingClass as NormalClass
    }
    
    static def Executable enclosingExecutable(InExecutable it) {
        val container = eContainer
        if(container instanceof Executable)
            container
        else if(container instanceof InExecutable)
            container.enclosingExecutable
        else
            null
    }
    
    def static Method enclosingMethod(InExecutable it) {
        val container = eContainer
        if(container instanceof Method)
            container
        else if(container instanceof InExecutable)
            container.enclosingMethod
        else
            null
    }
    
    static def isFieldWrite(MemberAccess it) {
        eContainer instanceof Assignment && it === (eContainer as Assignment).left
    }
    
    static def     destParam(Expr it) { (eContainer as Argumented).executable.params.get(argIndex) }
    static def destRoleParam(Role it) { (eContainer as MemberAccess).method.roleParams.get(roleArgIndex) }
    
    static def     paramIndex(    Param it) { enclosingExecutable.params.indexOf(it) }
    static def roleParamIndex(RoleParam it) { enclosingMethod.roleParams.indexOf(it) }
    static def       argIndex(     Expr it) { (eContainer as Argumented).args.indexOf(it) }
    static def   roleArgIndex(     Role it) { (eContainer as MemberAccess).roleArgs.indexOf(it) }
    
    static def jvmParam(Param it) { enclosingExecutable.jvmExecutable.parameters.get(paramIndex) }
                
    private static def dispatch jvmExecutable(Method it) { jvmMethod }
    private static def dispatch jvmExecutable(Constr it) { jvmConstr }
    
    static def <T> all(EObject it, java.lang.Class<T> c) {
        (#[it] + eAllContents.toIterable).filter(c)
    }
    
    static def Method original(Method it) { switch(it) {
        ParameterizedMethod: genericEObject.original
        default            : it  
    }}
    
    static def Param original(Param it) { switch(it) {
        ParameterizedParam: genericEObject.original
        default           : it  
    }}
    
    static def erased(Role it) { switch(it) {
        BuiltInRole : it
        RoleParamRef: param.upperBound
    }}
}