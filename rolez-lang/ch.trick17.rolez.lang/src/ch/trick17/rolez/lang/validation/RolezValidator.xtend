/*
 * generated by Xtext
 */
package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.cfg.CfgProvider
import ch.trick17.rolez.lang.cfg.InstrNode
import ch.trick17.rolez.lang.generator.RolezGenerator
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassLike
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.ExprStmt
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.FieldSelector
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.Int
import ch.trick17.rolez.lang.rolez.LocalVar
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.Null
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.ReturnExpr
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import ch.trick17.rolez.lang.rolez.This
import ch.trick17.rolez.lang.rolez.TypedBody
import ch.trick17.rolez.lang.rolez.Var
import ch.trick17.rolez.lang.rolez.VarRef
import ch.trick17.rolez.lang.rolez.Void
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
import static ch.trick17.rolez.lang.validation.ValFieldsInitializedAnalysis.*

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
    public static val CIRCULAR_INHERITANCE = "circular inheritance"
    public static val VAL_FIELD_NOT_INITIALIZED = "val field not initialized"
    public static val VAL_FIELD_OVERINITIALIZED = "val field overinitialized"
    public static val VAL_NOT_INITIALIZED = "val not initialized"
    public static val VAR_NOT_INITIALIZED = "var not initialized"
    public static val INCORRECT_SUPER_CONSTR_CALL = "incorrect super constructor call"
    public static val MISSING_SUPER_CONSTR_CALL = "missing super constructor call"
    public static val SUPER_CONSTR_CALL_FIRST = "super constructor call first"
    public static val THIS_BEFORE_SUPER_CONSTR_CALL = "'this' before super constructor call"
    public static val OUTER_EXPR_NO_SIDE_FX = "outer expr no side effects"
    public static val NULL_TYPE_USED = "null type used"
    public static val MAPPED_IN_NORMAL_CLASS = "mapped member in normal class"
    public static val MAPPED_WITH_BODY = "mapped with body"
    public static val MISSING_BODY = "missing body"
    public static val UNKNOWN_MAPPED_CLASS = "unknown mapped class"
    public static val CLASS_ACTUALLY_MAPPED = "class actually mapped"
    public static val INCORRECT_OBJECT_SUPERCLASS = "incorrect object superclass"
    public static val INCORRECT_ARRAY_SUPERCLASS = "incorrect array superclass"
    public static val INCORRECT_ARRAY_CONSTRS = "incorrect array constructors"
    public static val INCORRECT_LENGTH_FIELD = "incorrect length field"
    public static val INCORRECT_TASK_SUPERCLASS = "incorrect task superclass"
    
    @Inject extension RolezExtensions
    @Inject extension CfgProvider
    @Inject RolezSystem system
    @Inject RolezUtils utils
    
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
        if(body == null || type instanceof Void) return;
        
	    val cfg = controlFlowGraph
        for(p : cfg.exit.predecessors) {
            if(p instanceof InstrNode) {
                if(!(p.instr instanceof ReturnExpr))
                    error("Method must return a value of type " + type.string,
                        nonReturningNode(p).instr, null, MISSING_RETURN_EXPR)
            }
            else throw new AssertionError
	    }
	}
	
	private def InstrNode nonReturningNode(InstrNode n) {
	    val instr = n.instr
	    switch(instr) {
	        Block:
	           if(instr.stmts.isEmpty) n
	           else nonReturningNode(n.solePredecessor as InstrNode)
	        IfStmt:
	           if(n.isJoin || n.solePredecessor.isSplit) n
	           else nonReturningNode(n.solePredecessor as InstrNode)
	        default: n
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
    def checkValFieldInitialized(Field it) {
        if(kind == VAL && enclosingClass.constructors.isEmpty)
            error("Value field " + name + " is not initialized",
                it, null, VAL_FIELD_NOT_INITIALIZED)
    }
    
    @Check
    def checkValFieldsInitialized(Constr it) {
        if(body == null) return
        
        val cfg = controlFlowGraph
        val extension analysis = new ValFieldsInitializedAnalysis(cfg)
        for(f : enclosingClass.fields.filter[kind == VAL])
            if(!f.definitelyInitializedAfter(cfg.exit))
                error("Value field " + f.name + " may not have been initialized",
                    it, null, VAL_FIELD_NOT_INITIALIZED)
        
        for(a : all(Assignment).filter[isValFieldInit(it)]) {
            val f = assignedField(a)
            if(f.possiblyInitializedBefore(cfg.nodeOf(a)))
                error("Value field " + f.name + " may already have been initialized",
                    a, null, VAL_FIELD_OVERINITIALIZED)
        }
        
        for(a : all(MemberAccess).filter[isFieldAccess]) {
            val f = (a.selector as FieldSelector).field
            if(f.kind == VAL && !a.isAssignmentTarget
                    && !f.definitelyInitializedBefore(cfg.nodeOf(a)))
                error("Value field " + f.name + " may not have been initialized",
                    a, MEMBER_ACCESS__SELECTOR, VAL_FIELD_NOT_INITIALIZED)
        }
    }
    
    private def isAssignmentTarget(Expr e) {
        e.eContainer instanceof Assignment
            && (e.eContainer as Assignment).left == e
    }
    
    private def <T> all(ParameterizedBody it, java.lang.Class<T> c) {
        eAllContents.filter(c).toIterable
    }
    
    @Check
    def checkLocalValInitialized(LocalVarDecl it) {
        if(variable.kind == VAL && initializer == null)
            error("Value is not initialized", variable, null, VAL_NOT_INITIALIZED)
    }
    
    @Check
    def checkLocalVarsInitialized(ParameterizedBody it) {
        if(body == null) return
        
        val cfg = controlFlowGraph
        val extension analysis = new LocalVarsInitializedAnalysis(cfg)
        for(v : all(VarRef))
            if(v.variable instanceof LocalVar && !v.isAssignmentTarget
                    && !v.variable.isInitializedBefore(cfg.nodeOf(v)))
                error("Variable " + v.variable.name + " may not have been initialized",
                    v, VAR_REF__VARIABLE, VAR_NOT_INITIALIZED)
    }
    
    @Check
    def checkSuperConstrCall(SuperConstrCall it) {
        if(!(enclosingBody instanceof Constr))
            error("Cannot call a super constructor here", it,
                null, INCORRECT_SUPER_CONSTR_CALL)
    }
    
    @Check
    def checkSuperConstrCall(Constr it) {
        if(body == null) return
        
        val cfg = controlFlowGraph
        val extension analysis = new SuperConstrCallAnalysis(cfg)
        for(t : all(This))
            if(cfg.nodeOf(t).isBeforeSuperConstrCall)
                error("Cannot refer to 'this' before calling the super constructor",
                    t, null, THIS_BEFORE_SUPER_CONSTR_CALL)
        
        for(c : all(SuperConstrCall))
            if(body.stmts.head !== c)
                error("Super constructor call must be the first statement",
                    c, null, SUPER_CONSTR_CALL_FIRST)
        
        val superConstr = enclosingClass.actualSuperclass.allConstrs
        if(superConstr.filter[params.isEmpty].isEmpty && all(SuperConstrCall).isEmpty)
            error("Missing super constructor call",
                it, null, MISSING_SUPER_CONSTR_CALL)
    }
    
    @Check
    def checkSuperConstrCall(Class it) {
        val superConstr = actualSuperclass.allConstrs
        if(superConstr.filter[params.isEmpty].isEmpty && constructors.isEmpty)
            error("Missing super constructor call",
                it, NAMED__NAME, MISSING_SUPER_CONSTR_CALL)
    }
    
    @Check
    def checkExprStmt(ExprStmt it) {
        if(!utils.isSideFxExpr(expr))
            warning("Outermost expression has no side effects", expr, null,
                OUTER_EXPR_NO_SIDE_FX)
    }
    
    @Check
    def checkNullTypeUsed(Null it) {
        error("The null type cannot be used explicitly", it, null, NULL_TYPE_USED)
    }
    
    @Check
    def checkMappedField(Field it) {
        if(mapped) {
            if(!enclosingClass.mapped)
                error("mapped fields are allowed in mapped classes only",
                    it, NAMED__NAME, MAPPED_IN_NORMAL_CLASS)
        }
    }
    
    @Check
    def checkMappedMethod(Method it) {
        if(mapped) {
            if(!enclosingClass.mapped)
                error("mapped methods are allowed in mapped classes only",
                    it, NAMED__NAME, MAPPED_IN_NORMAL_CLASS)
            if(body != null)
                error("mapped methods cannot have a body", body, null, MAPPED_WITH_BODY)
        }
        else if(body == null) error("Missing body", it, NAMED__NAME, MISSING_BODY)
    }
    
    @Check
    def checkMappedConstr(Constr it) {
        if(mapped) {
            if(!enclosingClass.mapped)
                error("mapped constructors are allowed in mapped classes only",
                    it, null, MAPPED_IN_NORMAL_CLASS)
            if(body != null)
                error("mapped constructors cannot have a body", body, null, MAPPED_WITH_BODY)
        }
        else if(body == null) error("Missing body", it, null, MISSING_BODY)
    }
    
    @Check
    def checkMappedClass(Class it) {
        if(RolezGenerator.mappedClasses.containsKey(qualifiedName)) {
            if(!mapped)
                error("Class must be declared as mapped", it, NAMED__NAME, CLASS_ACTUALLY_MAPPED)
        }
        else if(mapped)
            error("Unknown mapped class " + qualifiedName, it, NAMED__NAME, UNKNOWN_MAPPED_CLASS)
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
            
            if(constructors.size != 1)
               error(qualifiedName + " must have exactly one constructor",
                   it, null, INCORRECT_ARRAY_CONSTRS)
            constructors.head => [
                if(!mapped)
                   error("This constructor must be declared as mapped",
                       it, null, INCORRECT_ARRAY_CONSTRS)
                else if(params.size != 1)
                   error("This constructor must have a single parameter",
                       it, null, INCORRECT_ARRAY_CONSTRS)
                else if(!(params.head.type instanceof Int))
                   error("The parameter of this constructor must be of type int",
                       params.head.type, null, INCORRECT_ARRAY_CONSTRS)
            ]
            
            fields.filter[name == "length"].forEach[
                if(!mapped)
                    error("length field must be declared as mapped", it,
                        NAMED__NAME, INCORRECT_LENGTH_FIELD)
                if(kind != VAL)
                    error("length field must be a value field", it,
                        FIELD__KIND, INCORRECT_LENGTH_FIELD)
                if(!(type instanceof Int))
                    error("the type of the length field must be int", type,
                        null, INCORRECT_LENGTH_FIELD)
            ]
        }
    }
    
    @Check
    def checkTaskClass(Class it) {
        if(qualifiedName == taskClassName) {
            if(actualSuperclass != utils.findClass(objectClassName, it))
               error("The superclass of " + qualifiedName + " must be " + objectClassName,
                   it, CLASS__SUPERCLASS, INCORRECT_TASK_SUPERCLASS)
        }
        // TODO: Check (mapped) members
    }
    
    // TODO: Check string class
    
	/*
	 * Delayed errors
	 */
	
	private val Set<Error> delayedErrors = new HashSet
	
	/**
	 * Can be called by other classes (e.g. the scope provider) to create
	 * errors before the actual validation phase. The validator will later
	 * report these errors.
	 */
    def delayedError(String message, EObject source, EStructuralFeature feature,
            String code, String... issueData) {
        delayedErrors.add(new Error(message, source, feature, code, issueData))
    }
    
    private static class Error {
        val String message
        val EObject source
        val EStructuralFeature feature
        val String code
        val String[] issueData
        
        new(String message, EObject source, EStructuralFeature feature,
                String code, String... issueData) {
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