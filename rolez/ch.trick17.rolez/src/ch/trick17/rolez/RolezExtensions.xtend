package ch.trick17.rolez

import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.generic.ParameterizedNormalClass
import ch.trick17.rolez.rolez.Argumented
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.Member
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.typesystem.RolezSystem
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.util.OnChangeEvictingCache

import static ch.trick17.rolez.Constants.*

import static extension ch.trick17.rolez.generic.Parameterized.*

/**
 * Extension methods for the Rolez language elements
 */
class RolezExtensions {
    
    @Inject IQualifiedNameProvider nameProvider
    @Inject RolezSystem system
    @Inject RolezUtils utils
    
    def QualifiedName qualifiedName(Class it) { switch(it) {
        ParameterizedNormalClass: nameProvider.getFullyQualifiedName(genericEObject)
        default                  :nameProvider.getFullyQualifiedName(it)
    }}
    
    def QualifiedName qualifiedName(   Member it) { switch(it) {
        ParameterizedMethod: nameProvider.getFullyQualifiedName(genericEObject)
        default            : nameProvider.getFullyQualifiedName(it)
    }}
    
    def thisType(Constr it) {
        utils.newRoleType(RolezFactory.eINSTANCE.createReadWrite, utils.newClassRef(enclosingClass))
    }
    def thisType(Method it) {
        utils.newRoleType(thisRole, utils.newClassRef(enclosingClass))
    }
    
    def isObjectClass(Class it) { qualifiedName == objectClassName }
    def isSliceClass (Class it) { qualifiedName ==  sliceClassName }
    def isArrayClass (Class it) { qualifiedName ==  arrayClassName }
    def isStringClass(Class it) { qualifiedName == stringClassName }
    def isTaskClass  (Class it) { qualifiedName ==   taskClassName }
    
    def dispatch isSliceType(RoleType it) { base.clazz.isSliceClass }
    def dispatch isSliceType(    Type it) { false }
    
    def dispatch isArrayType(RoleType it) { base.clazz.isArrayClass }
    def dispatch isArrayType(    Type it) { false }
    
    def Iterable<Member> allMembers(Class it) {
        members +
            if(superclass == null) emptyList
            else parameterizedSuperclass.allMembers.filter[m | !overrides(m)]
    }
    
    def parameterizedSuperclass(Class it) { superclassRef?.parameterizedClass as NormalClass }
    
    val superclassesCache = new OnChangeEvictingCache
    
    def strictSuperclasses(Class it) {
        superclassesCache.get(it, eResource, [
            val result = new HashSet
            collectSuperclasses(result)
            
            val object = utils.findClass(objectClassName, it)
            if(it != object && object != null)
                result += object
            result
        ])
    }
    
    private def void collectSuperclasses(Class it, Set<Class> classes) {
        if(classes += it)
            superclass?.collectSuperclasses(classes)
    }
    
    def dispatch parameterizedClass( SimpleClassRef it) { clazz }
    def dispatch parameterizedClass(GenericClassRef it) {
        clazz.parameterizedWith(#{clazz.typeParam -> typeArg})
    }
    
    private def overrides(Class it, Member m) {
        switch(m) {
            Field: false
            Method: methods.exists[utils.equalSignatureWithoutRoles(it, m)]
        }
    }
    
    def Method enclosingMethod(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Method: container
            default: container?.enclosingMethod
        }
    }
    
    def Class enclosingClass(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Class: container
            default: container?.enclosingClass
        }
    }
    
    def enclosingClass(Constr it) {
        (it as EObject).enclosingClass as NormalClass
    }
    
    def Program enclosingProgram(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Program: container
            default: container?.enclosingProgram
        }
    }
    
    def Executable enclosingExecutable(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Executable: container
            default: container?.enclosingExecutable
        }
    }
    
    def isSliceGet(MemberAccess it) {
        isMethodInvoke && method.name == "get"
            && system.type(utils.createEnv(it), target).value.isSliceType
    }
    
    def isSliceSet(MemberAccess it) {
        isMethodInvoke && method.name == "set"
            && system.type(utils.createEnv(it), target).value.isSliceType
    }
    
    def isArrayGet(MemberAccess it) {
        isMethodInvoke && method.name == "get"
            && system.type(utils.createEnv(it), target).value.isArrayType
    }
    
    def isArraySet(MemberAccess it) {
        isMethodInvoke && method.name == "set"
            && system.type(utils.createEnv(it), target).value.isArrayType
    }
    
    def isArrayLength(MemberAccess it) {
        isFieldAccess && field.name == "length" && field.enclosingClass.qualifiedName == arrayClassName
    }
    
    def     destParam(Expr it) { (eContainer as Argumented).executable.params.get(argIndex) }
    def destRoleParam(Role it) { (eContainer as MemberAccess).method.roleParams.get(roleArgIndex) }
    
    def     paramIndex(    Param it) { enclosingExecutable.params.indexOf(it) }
    def roleParamIndex(RoleParam it) { enclosingMethod.roleParams.indexOf(it) }
    def       argIndex(     Expr it) { (eContainer as Argumented).args.indexOf(it) }
    def   roleArgIndex(     Role it) { (eContainer as MemberAccess).roleArgs.indexOf(it) }
    
    /*
     * toString() replacements:
     */
    
    def string(Member it) { switch(it) {
        Method: thisRole.string + " " + qualifiedName
            + roleParams.join("[", ",", "]", [string])
            + params.join("(", ",", ")", [type.string])
            + ": " + type.string
        Field: qualifiedName + ": " + type.string
    }}
    
    def String string(Type it) { switch(it) {
        PrimitiveType: name
        Null         : "null"
        RoleType     : role.string + " " + base.string
        TypeParamRef : param.name + if(restrictingRole != null) " with " + restrictingRole else ""
    }}
    
    def String stringWithoutRoles(Type it) { switch(it) {
        RoleType: base.stringWithoutRoles
        default : string
    }}
    
    def string(ClassRef it) { switch(it) {
        SimpleClassRef : clazz.qualifiedName.toString
        GenericClassRef: clazz.qualifiedName + "[" + typeArg.string + "]"
    }}
    
    def string(Role it) { name }
    
    def string(RoleParam it) {
        name + " includes " + upperBound.string
    }
    
    def stringWithoutRoles(ClassRef it) { switch(it) {
        GenericClassRef: clazz.qualifiedName + "[" + typeArg.stringWithoutRoles + "]"
        default        : string
    }}
    
    def string(Instr it) {
        // IMPROVE: Not a good idea for synthetic instructions
        NodeModelUtils.findActualNodeFor(it).text.trim.replaceAll("\\s+", " ")
    }
}