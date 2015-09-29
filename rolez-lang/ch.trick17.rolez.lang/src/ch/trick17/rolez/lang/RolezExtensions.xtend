package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.rolez.Boolean
import ch.trick17.rolez.lang.rolez.Char
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassLike
import ch.trick17.rolez.lang.rolez.ClassRef
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.Double
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.FieldSelector
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.Instr
import ch.trick17.rolez.lang.rolez.Int
import ch.trick17.rolez.lang.rolez.LocalVar
import ch.trick17.rolez.lang.rolez.Void
import ch.trick17.rolez.lang.rolez.Member
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.MethodSelector
import ch.trick17.rolez.lang.rolez.Null
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.Role
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.Stmt
import ch.trick17.rolez.lang.rolez.Task
import ch.trick17.rolez.lang.rolez.Type
import ch.trick17.rolez.lang.typesystem.RolezUtils
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

import static ch.trick17.rolez.lang.Constants.*

/**
 * Extension methods for the Rolez language elements
 */
class RolezExtensions {
    
    @Inject private IQualifiedNameProvider nameProvider
    @Inject private RolezUtils utils
    
    def Iterable<Class> classes(Program it) {
        elements.filter(Class)
    }
    
    def Iterable<Method> methods(Class it) {
        members.filter(Method)
    }
    
    def Iterable<Field> fields(Class it) {
        members.filter(Field)
    }
    
    def qualifiedName(ClassLike it) { nameProvider.getFullyQualifiedName(it) }
    
    def getPackage(ClassLike it) {
        val segments = qualifiedName.segments
        segments.takeWhile[it != segments.last].join(".")
    }
    
    def simpleName(ClassLike it) {
        nameProvider.getFullyQualifiedName(it).lastSegment
    }
    
    def Iterable<Constr> allConstrs(Class it) {
        if(!constructors.isEmpty) constructors
        else #[RolezFactory.eINSTANCE.createConstr]
    }
    
    def Iterable<Member> allMembers(Class it) {
        members + if(actualSuperclass != null) actualSuperclass.allMembers else emptyList
    }
    
    def actualSuperclass(Class it) {
        val objectClass = utils.findClass(objectClassName, it)
        if(it == objectClass)
            null
        else if(superclass == null)
            objectClass
        else
            superclass
    }
    
    def qualifiedName(Member it) { nameProvider.getFullyQualifiedName(it) }
    
    def variables(ParameterizedBody it) {
        body.eAllContents.filter(LocalVar).toList + params
    }
    
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
    
    def dispatch kind(Method _) { "method" }
    def dispatch kind(Constr _) { "constructor" }
    def dispatch kind(Task _)   { "task" }
    
    def isFieldAccess(MemberAccess it) { selector instanceof FieldSelector }
    def isMethodInvoke(MemberAccess it) { selector instanceof MethodSelector }
    
    /*
     * toString() replacements:
     */
    
    def string(Member it)   { memberString }
    def string(Type it)     { typeString }
    def string(Role it)     { literal }
    def string(ClassRef it) { classRefString }
    def string(Instr it)    {
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

    private def dispatch typeString(Int _)     { "int" }
    private def dispatch typeString(Double _)  { "double" }
    private def dispatch typeString(Boolean _) { "boolean" }
    private def dispatch typeString(Char _)    { "char" }
    private def dispatch typeString(Void _)    { "void" }
    private def dispatch typeString(Null _)    { "null" }
    
    private def dispatch String typeString(RoleType it) {
        role.string + " " + base.string
    }
    
    private def dispatch classRefString(SimpleClassRef r) { r.clazz.name }
    
    private def dispatch classRefString(GenericClassRef r) {
        r.clazz.qualifiedName + "[" + r.typeArg.string + "]"
    }
}