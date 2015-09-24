/*
 * generated by Xtext
 */
package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.cfg.CfgBuilder
import ch.trick17.rolez.lang.cfg.StmtNode
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassLike
import ch.trick17.rolez.lang.rolez.Constructor
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.ReturnExpr
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.TypedBody
import ch.trick17.rolez.lang.rolez.Unit
import ch.trick17.rolez.lang.rolez.Var
import ch.trick17.rolez.lang.typesystem.RolezSystem
import ch.trick17.rolez.lang.typesystem.RolezUtils
import ch.trick17.rolez.lang.typesystem.validation.RolezSystemValidator
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.validation.Check

import static ch.trick17.rolez.lang.Constants.*
import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.lang.rolez.VarKind.*

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class RolezValidator extends RolezSystemValidator {

    public static val INVALID_NAME = "invalid name"
    public static val OBJECT_CLASS_NOT_DEFINED = "object class not defined"
    public static val DUPLICATE_TOP_LEVEL_ELEMENT = "duplicate top-level element"
    public static val DUPLICATE_METHOD = "duplicate method"
    public static val DUPLICATE_FIELD = "duplicate field"
    public static val DUPLICATE_VARIABLE = "duplicate variable"
    public static val MISSING_OVERRIDE = "missing override"
    public static val INCORRECT_OVERRIDE = "incorrect override"
    public static val INCOMPATIBLE_RETURN_TYPE = "incompatible return type"
    public static val INCOMPATIBLE_THIS_ROLE = "incompatible \"this\" role"
    public static val MISSING_RETURN_EXPR = "missing return statement"
    public static val AMBIGUOUS_CALL = "ambiguous call"
    public static val MISSING_TYPE_ARGS = "missing type arguments"
    public static val INCORRECT_TYPE_ARGS = "incorrect type arguments"
    public static val INCORRECT_OBJECT_SUPERCLASS = "incorrect object superclass"
    public static val INCORRECT_ARRAY_SUPERCLASS = "incorrect array superclass"
    public static val INCORRECT_TASK_SUPERCLASS = "incorrect task superclass"
    public static val CIRCULAR_INHERITANCE = "circular inheritance"
    public static val VAL_FIELD_NOT_INITIALIZED = "val field not initialized"
    public static val VAL_FIELD_OVERINITIALIZED = "val field overinitialized"
    public static val VAL_NOT_INITIALIZED = "val not initialized"
    public static val VAR_NOT_INITIALIZED = "var not initialized"
    
    @Inject private extension RolezExtensions
    @Inject private RolezSystem system
    @Inject private CfgBuilder builder
    @Inject private RolezUtils utils
    
	@Check
    def checkClassNameStartsWithCapital(Class it) {
        if(!Character.isUpperCase(qualifiedName.lastSegment.charAt(0)))
            warning("Name should start with a capital",
                NAMED__NAME, INVALID_NAME)
    }
	
	@Check
	def checkObjectExists(Class it) {
	    if(superclass == null && utils.findClass(objectClassName, it) == null)
	       error("Object class is not defined",
	           NAMED__NAME,  OBJECT_CLASS_NOT_DEFINED)
	}
	
	@Check
	def checkNoDuplicateTopLevelElem(ClassLike it) {
	    val matching = enclosingProgram.elements.filter[e | e.name.equals(name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate top-level element " + name, NAMED__NAME, DUPLICATE_TOP_LEVEL_ELEMENT)
	}
	
    @Check
    def checkObjectClass(Class it) {
        if(qualifiedName == objectClassName) {
            if(superclass != null)
               error(qualifiedName + " must not have a superclass",
                   it, CLASS__SUPERCLASS, INCORRECT_OBJECT_SUPERCLASS)
        }
    }
    
    @Check
    def checkArrayClass(Class it) {
        if(qualifiedName == arrayClassName) {
            if(actualSuperclass != utils.findClass(objectClassName, it))
               error("The superclass of " + qualifiedName + " must be "+ objectClassName,
                   it, CLASS__SUPERCLASS, INCORRECT_ARRAY_SUPERCLASS)
        }
        // TODO: Check (built-in) members
    }
    
    @Check
    def checkTaskClass(Class it) {
        if(qualifiedName == taskClassName) {
            if(actualSuperclass != utils.findClass(objectClassName, it))
               error("The superclass of " + qualifiedName + " must be " + objectClassName,
                   it, CLASS__SUPERCLASS, INCORRECT_TASK_SUPERCLASS)
        }
        // TODO: Check (built-in) members
    }
    
    // TODO: Check string class
    
    @Check
    def checkCircularInheritance(Class it) {
        if(findSuperclass(it))
            error("Circular inheritance", it, CLASS__SUPERCLASS, CIRCULAR_INHERITANCE)
    }
    
    private def boolean findSuperclass(Class it, Class c) {
        switch(actualSuperclass) {
            case null: false
            case c: true
            default:
                findSuperclass(actualSuperclass, c)
        }
    }
	
	/**
	 * Checks that there are no other methods in the same class with the same
	 * signature. The signature does not comprise the role of <code>this</code>,
	 * therefore <code>this</code>-role-based overloading is not possible.
	 * <p>
	 * The reason for this is that it might be surprising to programmers if
	 * methods are virtually dispatched based on the class of the target, but
	 * statically dispatched based on its role.
	 */
    @Check
    def checkNoDuplicateMethods(Method it) {
        val matching = enclosingClass.methods.filter[m | utils.equalSignature(m, it)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate method " + name + "("+ params.join(",") + ")",
               NAMED__NAME, DUPLICATE_METHOD)
    }
    
    @Check
    def checkNoDuplicateFields(Field it) {
        val matching = enclosingClass.fields.filter[f | f.name.equals(name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate field " + name, NAMED__NAME, DUPLICATE_FIELD)
    }
    
    @Check
    def checkNoDuplicateVars(Var it) {
        val matching = enclosingBody.variables.filter[v | v.name.equals(name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate variable " + name, NAMED__NAME, DUPLICATE_VARIABLE)
    }
	
	/**
	 * Checks that overriding methods actually override a method in a super
	 * class and that the return type is co- and the <code>this</code> role is
	 * contravariant. Note that covariance for the <code>this</code> role would
	 * be unsafe.
	 */
	@Check
	def checkOverrides(Method it) {
	    val superMethods = enclosingClass.actualSuperclass
	           .allMembers.filter(Method)
        val matching = superMethods.filter[m | utils.equalSignature(m, it)]
	    
	    if(matching.size > 0) {
	        if(overriding) {
                for(match : matching) {
                    if(system.subtype(utils.envFor(it), type, match.type).failed)
                        error("The return type is incompatible with overridden method" + match,
                            TYPED__TYPE, INCOMPATIBLE_RETURN_TYPE)
                    if(system.subrole(match.thisRole, thisRole).failed)
                        error("This role of \"this\" is incompatible with overridden method" + match,
                            TYPED__TYPE, INCOMPATIBLE_THIS_ROLE)
                }
            }
            else
                error("Method must be declared with \"override\" since it
                        actually overrides a superclass method",
                    NAMED__NAME, MISSING_OVERRIDE)
        }
        else if(overriding)
           error("Method must override a superclass method",
               NAMED__NAME, INCORRECT_OVERRIDE)
	}
	
	@Check
	def checkReturnExpr(TypedBody it) {
        if(type instanceof Unit)
            return;
	    val cfg = builder.controlFlowGraph(it)
	    
	    if(cfg.exit === cfg.entry)
            error("Method must return a value of type " + type.string,
                body, null, MISSING_RETURN_EXPR)
        for(p : cfg.exit.predecessors) {
            if(p instanceof StmtNode) {
                if(!(p.stmt instanceof ReturnExpr))
                    error("Method must return a value of type " + type.string,
                        p.stmt, null, MISSING_RETURN_EXPR)
            }
            else throw new AssertionError
	    }
	}
    
    @Check
    def checkSimpleClassRef(SimpleClassRef it) {
        if(clazz == utils.findClass(arrayClassName, it))
            error("Class " + clazz.name + " takes type arguments",
                it, CLASS_REF__CLAZZ, MISSING_TYPE_ARGS)
    }
    
    @Check
    def checkGenericClassRef(GenericClassRef it) {
        if(clazz != utils.findClass(arrayClassName, it))
            error("Class " + clazz.name + " does not take type arguments",
                it, GENERIC_CLASS_REF__TYPE_ARG, INCORRECT_TYPE_ARGS)
    }
    
    @Check
    def checkValFieldsInitialized(Constructor it) {
        // TODO: This requires a data flow analysis of sorts...
    }
    
    @Check
    def checkLocalValInitialized(LocalVarDecl it) {
        if(variable.kind == VAL && initializer == null)
            error("Uninitialized value", variable, null, VAL_NOT_INITIALIZED)
    }
    
    @Check
    def checkLocalVarsInitialized(ParameterizedBody it) {
        // TODO
    }
    
	/*
	 * Delayed errors
	 */
	
	private val Set<Error> delayedErrors = new HashSet
	
	/**
	 * Can be called by other classes (e.g. the scope provider) to create
	 * errors before the actual validation phase. The validator will later
	 * report these errors.
	 */
    def delayedError(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
        delayedErrors.add(new Error(message, source, feature, code, issueData))
    }
    
    private static class Error {
        val String message
        val EObject source
        val EStructuralFeature feature
        val String code
        val String[] issueData
        
        new(String message, EObject source, EStructuralFeature feature, String code, String... issueData) {
            this.message = message
            this.source = source
            this.feature = feature
            this.code = code
            this.issueData = issueData
        }
    }
    
    @Check
    def reportDelayedErrors(Program p) {
        delayedErrors.filter[source.enclosingProgram == p].forEach[
            error(message, source, feature, code, issueData)
        ]
    }
}