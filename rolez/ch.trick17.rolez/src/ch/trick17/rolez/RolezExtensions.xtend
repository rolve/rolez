package ch.trick17.rolez

import ch.trick17.rolez.rolez.Boolean
import ch.trick17.rolez.rolez.Char
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassLike
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Double
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.Int
import ch.trick17.rolez.rolez.LocalVar
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.Member
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.ParameterizedBody
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.Task
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.VarKind
import ch.trick17.rolez.rolez.Void
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

import static ch.trick17.rolez.Constants.*

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
    
    def qualifiedName(ClassLike it) { nameProvider.getFullyQualifiedName(it) }
    def qualifiedName(   Member it) { nameProvider.getFullyQualifiedName(it) }
    
    def isSingleton(Class it) { it instanceof SingletonClass }
    
    def isObjectClass(Class it) { qualifiedName == objectClassName }
    def isArrayClass (Class it) { qualifiedName ==  arrayClassName }
    def isStringClass(Class it) { qualifiedName == stringClassName }
    def isTaskClass  (Class it) { qualifiedName ==   taskClassName }
    
    def Iterable<Member> allMembers(Class it) {
        members +
            if(superclass == null) emptyList
            else superclass.allMembers.filter[m | !overrides(m)]
    }
    
    def dispatch clazz( SimpleClassRef it) { clazz }
    def dispatch clazz(GenericClassRef it) { clazz }
    
    private def overrides(Class it, Member m) {
        switch(m) {
            Field: false
            Method: methods.exists[utils.equalSignature(it, m)]
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
    
    def isFieldAccess (MemberAccess it) { member instanceof Field  }
    def isMethodInvoke(MemberAccess it) { member instanceof Method }
    
    def field (MemberAccess it) { member as Field  }
    def method(MemberAccess it) { member as Method }
    
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
    
    /*
     * toString() replacements:
     */
    
    def string(Member it)   { memberString }
    def string(Type it)     { typeString }
    def string(Role it)     { literal }
    def string(ClassRef it) { classRefString }
    def string(Instr it)    {
        // IMPROVE: Not a good idea for synthetic instructions
        NodeModelUtils.findActualNodeFor(it).text.trim.replaceAll("\\s+", " ")
    }
    
    private def dispatch memberString(Method it) {
        thisRole.string + " " + qualifiedName
            + "(" + params.map[type.string].join(",") + ")"
            + ": " + type.string
    }
    
    private def dispatch memberString(Field it) {
        qualifiedName + ": " + type.string
    }
    
    private def dispatch typeString(PrimitiveType it) { name }
    
    private def dispatch typeString(Null _)    { "null" }
    
    private def dispatch String typeString(RoleType it) {
        role.string + " " + base.string
    }
    
    private def dispatch typeString(TypeParamRef it) { param.name }
    
    private def dispatch classRefString(SimpleClassRef r) { r.clazz.name }
    
    private def dispatch classRefString(GenericClassRef r) {
        r.clazz.qualifiedName + "[" + r.typeArg.string + "]"
    }
    
    def dispatch name(    Int _) { "int"     }
    def dispatch name( Double _) { "double"  }
    def dispatch name(Boolean _) { "boolean" }
    def dispatch name(   Char _) { "char"    }
    def dispatch name(   Void _) { "void"    }
}