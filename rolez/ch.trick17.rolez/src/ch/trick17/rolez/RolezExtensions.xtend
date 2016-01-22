package ch.trick17.rolez

import ch.trick17.rolez.generic.ParameterizedMethod
import ch.trick17.rolez.generic.ParameterizedNormalClass
import ch.trick17.rolez.rolez.Argumented
import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Char
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassLike
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Double
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.LocalVar
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.Member
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.ParameterizedBody
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.Start
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.Task
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.VarKind
import ch.trick17.rolez.rolez.Void
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.util.OnChangeEvictingCache

import static ch.trick17.rolez.Constants.*

import static extension ch.trick17.rolez.generic.Parameterized.*
import org.eclipse.xtext.naming.QualifiedName

/**
 * Extension methods for the Rolez language elements
 */
class RolezExtensions {
    
    @Inject IQualifiedNameProvider nameProvider
    @Inject RolezUtils utils
    
    def Iterable<Class> classes(Program it) { elements.filter(Class) }
    
    def Iterable<Task> tasks(Program it) { elements.filter(Task) }
    
    def Iterable<Method> methods(Class it) { members.filter(Method) }
    
    def Iterable<Field> fields(Class it) { members.filter(Field) }
    
    def QualifiedName qualifiedName(ClassLike it) { switch(it) {
        ParameterizedNormalClass: nameProvider.getFullyQualifiedName(genericEObject)
        default                  :nameProvider.getFullyQualifiedName(it)
    }}
    
    def QualifiedName qualifiedName(   Member it) { switch(it) {
        ParameterizedMethod: nameProvider.getFullyQualifiedName(genericEObject)
        default            : nameProvider.getFullyQualifiedName(it)
    }}
    
    def isSingleton(Class it) { it instanceof SingletonClass }
    
    def isObjectClass(Class it) { qualifiedName == objectClassName }
    def isArrayClass (Class it) { qualifiedName ==  arrayClassName }
    def isStringClass(Class it) { qualifiedName == stringClassName }
    def isTaskClass  (Class it) { qualifiedName ==   taskClassName }
    
    def dispatch isArrayType(RoleType it) { base.clazz.isArrayClass }
    def dispatch isArrayType(    Type it) { false }
    
    def Iterable<Member> allMembers(Class it) {
        members +
            if(superclass == null) emptyList
            else parameterizedSuperclass.allMembers.filter[m | !overrides(m)]
    }
    
    val superclassesCache = new OnChangeEvictingCache
    
    def superclass(Class it) {
        val clazz = superclassRef?.clazz
        if(clazz instanceof NormalClass) clazz else null
    }
    
    def parameterizedSuperclass(Class it) { superclassRef?.parameterizedClass as NormalClass }
    
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
    
    def dispatch clazz( SimpleClassRef it) { clazz }
    def dispatch clazz(GenericClassRef it) { clazz }
    
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
    
    def dispatch kind(LocalVar it) { kind }
    def dispatch kind(   Param it) { VarKind.VAL }
    
    def Stmt enclosingStmt(EObject it) {
        val container = it?.eContainer
        switch(container) {
            Stmt: container
            default: container?.enclosingStmt
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
    
    def ParameterizedBody enclosingBody(EObject it) {
        val container = it?.eContainer
        switch(container) {
            ParameterizedBody: container
            default: container?.enclosingBody
        }
    }
    
    def decl(LocalVar it) { enclosingStmt as LocalVarDecl }
    
    def jvmParam(Param it) { enclosingBody.jvmBody.parameters.get(paramIndex) }
    
    def dispatch jvmBody(Method it) { jvmMethod }
    def dispatch jvmBody(Constr it) { jvmConstr }
    
    def isFieldAccess (MemberAccess it) { member instanceof Field  }
    def isMethodInvoke(MemberAccess it) { member instanceof Method }
    
    def field (MemberAccess it) { member as Field  }
    def method(MemberAccess it) { member as Method }
    
    def parameterizedMethod(MemberAccess it) {
        method.parameterizedWith(roleArgs.toMap[destRoleParam])
    }
    
    def body(Argumented it) { switch(it) {
        MemberAccess: method
        New         : constr
        Start       : taskRef.task
    }}
    
    def isArrayGet(Method it) {
        enclosingClass.qualifiedName == arrayClassName && name == "get" && mapped
    }
    
    def isArraySet(Method it) {
        enclosingClass.qualifiedName == arrayClassName && name == "set" && mapped
    }
    
    def isArrayLength(Field it) {
        enclosingClass.qualifiedName == arrayClassName && name == "length" && mapped
    }
    
    def isMapped(Constr it) { jvmConstr != null }
    def isMapped( Field it) { jvmField  != null }
    def isMapped(Method it) { jvmMethod != null }
    
    def isOverriding(Method it) {
        superMethod != null && !superMethod.eIsProxy    // If superMethod could not be resolved,
    }                                                   // don't do any overriding checks
    
    def     destParam(Expr it) { (eContainer as Argumented).body.params.get(argIndex) }
    def destRoleParam(Role it) { (eContainer as MemberAccess).method.roleParams.get(roleArgIndex) }
    
    def     paramIndex(    Param it) { enclosingBody.params.indexOf(it) }
    def roleParamIndex(RoleParam it) { enclosingMethod.roleParams.indexOf(it) }
    def       argIndex(     Expr it) { (eContainer as Argumented).args.indexOf(it) }
    def   roleArgIndex(     Role it) { (eContainer as MemberAccess).roleArgs.indexOf(it) }
    
    /*
     * toString() replacements:
     */
    
    def string(Member it) { switch(it) {
        Method: thisRole.string + " " + qualifiedName
            + "[" + roleParams.map[]
            + "(" + params.map[type.string].join(",") + ")"
            + ": " + type.string
        Field: qualifiedName + ": " + type.string
    }}
    
    def String string(Type it) { switch(it) {
        PrimitiveType: name
        Null         : "null"
        RoleType     : role.string + " " + base.string
        TypeParamRef : param.name
    }}
    
    def String stringWithoutRoles(Type it) { switch(it) {
        RoleType: base.stringWithoutRoles
        default : string
    }}
    
    def string(ClassRef it) { switch(it) {
        SimpleClassRef : clazz.name
        GenericClassRef: clazz.qualifiedName + "[" + typeArg.string + "]"
    }}
    
    def string(Role it) { name }
    
    def name(Role it) { switch(it) {
        ReadWrite   : "readwrite"
        ReadOnly    : "readonly"
        Pure        : "pure"
        RoleParamRef: param.name
    }}
    
    def string(RoleParam it) {
        name + if(upperBound != null) " includes " + upperBound.string else ""
    }
    
    def stringWithoutRoles(ClassRef it) { switch(it) {
        GenericClassRef: clazz.qualifiedName + "[" + typeArg.stringWithoutRoles + "]"
        default        : string
    }}
    
    def string(Instr it) {
        // IMPROVE: Not a good idea for synthetic instructions
        NodeModelUtils.findActualNodeFor(it).text.trim.replaceAll("\\s+", " ")
    }
    
    def name(PrimitiveType it) { switch(it) {
        Int:     "int"
        Double:  "double"
        Boolean: "boolean"
        Char:    "char"
        Void:    "void"
    }}
}