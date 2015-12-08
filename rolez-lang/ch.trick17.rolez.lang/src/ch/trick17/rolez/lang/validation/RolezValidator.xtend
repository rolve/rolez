package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.RolezUtils
import ch.trick17.rolez.lang.cfg.CfgProvider
import ch.trick17.rolez.lang.cfg.InstrNode
import ch.trick17.rolez.lang.rolez.Assignment
import ch.trick17.rolez.lang.rolez.Block
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.ClassLike
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.Expr
import ch.trick17.rolez.lang.rolez.ExprStmt
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.IfStmt
import ch.trick17.rolez.lang.rolez.Int
import ch.trick17.rolez.lang.rolez.LocalVar
import ch.trick17.rolez.lang.rolez.LocalVarDecl
import ch.trick17.rolez.lang.rolez.MemberAccess
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.New
import ch.trick17.rolez.lang.rolez.NormalClass
import ch.trick17.rolez.lang.rolez.Null
import ch.trick17.rolez.lang.rolez.ParameterizedBody
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.ReturnExpr
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.SuperConstrCall
import ch.trick17.rolez.lang.rolez.Task
import ch.trick17.rolez.lang.rolez.This
import ch.trick17.rolez.lang.rolez.Type
import ch.trick17.rolez.lang.rolez.TypeParamRef
import ch.trick17.rolez.lang.rolez.TypedBody
import ch.trick17.rolez.lang.rolez.VarRef
import ch.trick17.rolez.lang.rolez.Void
import ch.trick17.rolez.lang.typesystem.RolezSystem
import ch.trick17.rolez.lang.typesystem.validation.RolezSystemValidator
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.validation.Check

import static ch.trick17.rolez.lang.Constants.*
import static ch.trick17.rolez.lang.rolez.Role.*
import static ch.trick17.rolez.lang.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.lang.rolez.VarKind.*

class RolezValidator extends RolezSystemValidator {

    public static val INVALID_NAME = "invalid name"
    public static val DUPLICATE_TOP_LEVEL_ELEMENT = "duplicate top-level element"
    public static val CIRCULAR_INHERITANCE = "circular inheritance"
    public static val INCORRECT_MAIN_TASK = "incorrect main task"
    public static val INCORRECT_TYPE_PARAM = "incorrect type variable"
    public static val MISSING_TYPE_PARAM = "missing type parameter"
    public static val DUPLICATE_FIELD = "duplicate field"
    public static val DUPLICATE_METHOD = "duplicate method"
    public static val FIELD_WITH_SAME_NAME = "field with same name"
    public static val DUPLICATE_CONSTR = "duplicate constructor"
    public static val DUPLICATE_VARIABLE = "duplicate variable"
    public static val MISSING_OVERRIDE = "missing override"
    public static val INCORRECT_OVERRIDE = "incorrect override"
    public static val INCOMPATIBLE_RETURN_TYPE = "incompatible return type"
    public static val INCOMPATIBLE_THIS_ROLE = "incompatible \"this\" role"
    public static val MISSING_RETURN_EXPR = "missing return statement"
    public static val AMBIGUOUS_CALL = "ambiguous call"
    public static val MISSING_TYPE_ARG = "missing type argument"
    public static val INCORRECT_TYPE_ARG = "incorrect type argument"
    public static val VAL_FIELD_NOT_INITIALIZED = "val field not initialized"
    public static val VAL_FIELD_OVERINITIALIZED = "val field overinitialized"
    public static val THIS_IN_FIELD_INIT = "'this' in field initializer"
    public static val MAPPED_FIELD_WITH_INIT = "mapped with with initializer"
    public static val VAR_FIELD_IN_SINGLETON_CLASS = "var field in singleton class"
    public static val INEFFECTIVE_FIELD_ROLE = "ineffective field role"
    public static val VAL_NOT_INITIALIZED = "val not initialized"
    public static val VAR_NOT_INITIALIZED = "var not initialized"
    public static val INCORRECT_SUPER_CONSTR_CALL = "incorrect super constructor call"
    public static val MISSING_SUPER_CONSTR_CALL = "missing super constructor call"
    public static val SUPER_CONSTR_CALL_FIRST = "super constructor call first"
    public static val THIS_BEFORE_SUPER_CONSTR_CALL = "'this' before super constructor call"
    public static val UNCATCHABLE_CHECKED_EXCEPTION = "uncatchable checked exception"
    public static val OUTER_EXPR_NO_SIDE_FX = "outer expr no side effects"
    public static val NULL_TYPE_USED = "null type used"
    public static val MAPPED_IN_NORMAL_CLASS = "mapped member in normal class"
    public static val NON_MAPPED_FIELD = "non-mapped field"
    public static val NON_MAPPED_METHOD = "non-mapped method"
    public static val NON_MAPPED_CONSTR = "non-mapped constructor"
    public static val MAPPED_WITH_BODY = "mapped with body"
    public static val MISSING_BODY = "missing body"
    public static val UNKNOWN_MAPPED_CLASS = "unknown mapped class"
    public static val UNKNOWN_MAPPED_FIELD = "unknown mapped field"
    public static val UNKNOWN_MAPPED_METHOD = "unknown mapped method"
    public static val UNKNOWN_MAPPED_CONSTR = "unknown mapped constructor"
    public static val CLASS_ACTUALLY_MAPPED = "class actually mapped"
    public static val INCORRECT_MAPPED_SUPERCLASS = "incorrect mapped superclass"
    public static val INCORRECT_MAPPED_CLASS_KIND = "incorrect mapped class kind"
    public static val INCORRECT_MAPPED_FIELD = "incorrect mapped field"
    public static val INCORRECT_MAPPED_METHOD = "incorrect mapped method"
    public static val INCORRECT_MAPPED_CONSTR = "incorrect mapped constructor"
    
    @Inject extension RolezExtensions
    @Inject extension CfgProvider
    @Inject extension JavaMapper javaMapper
    @Inject ValFieldsInitializedAnalysis.Provider valFieldsAnalysis
    @Inject RolezSystem system
    @Inject RolezUtils utils
    
	@Check
    def checkClassNameStartsWithCapital(Class it) {
        if(!Character.isUpperCase(qualifiedName.lastSegment.charAt(0)))
            warning("Name should start with a capital",
                NAMED__NAME, INVALID_NAME)
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
            error("Circular inheritance", CLASS__SUPERCLASS, CIRCULAR_INHERITANCE)
    }
    
    @Check
    def checkMainTask(Task it) {
        if(!isMain) return;
        
        if(!(type instanceof Void))
            error("A main task must have a void return type", type, null, INCORRECT_MAIN_TASK)
        if(params.size > 1)
            error("A main task must have zero or one parameter", PARAMETERIZED_BODY__PARAMS, INCORRECT_MAIN_TASK)
        else if(params.size == 1 && !params.head.type.isStringArray)
            error("The parameter of a main must must be of type readonly Array[String]",
                params.head.type, null, INCORRECT_MAIN_TASK)
    }
    
    private def isStringArray(Type it) {
        switch(it) {
            RoleType case role == READONLY && base.clazz.isArrayClass: {
                val arg = (base as GenericClassRef).typeArg
                switch(arg) {
                    RoleType: arg.base.clazz.isStringClass
                    default: false
                }
            }
            default: false
        }
    }
    
    @Check
    def checkTypeParam(NormalClass it) {
        if(!isArrayClass && typeParam != null)
            error("Only " + arrayClassName + " may declare a type parameter",
                NORMAL_CLASS__TYPE_PARAM, INCORRECT_TYPE_PARAM)
    }
    
    private def boolean findSuperclass(Class it, Class c) {
        switch(superclass) {
            case null: false
            case c: true
            default:
                findSuperclass(superclass, c)
        }
    }
    
    @Check
    def checkNoDuplicateFields(Field it) {
        val matching = enclosingClass.fields.filter[f | f.name.equals(name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate field " + name, NAMED__NAME, DUPLICATE_FIELD)
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
           error("Duplicate method " + name + "("+ params.map[type.string].join(", ") + ")",
               NAMED__NAME, DUPLICATE_METHOD)
    }
    
    @Check
    def checkFieldWithSameName(Method it) {
        if(params.isEmpty && !enclosingClass.allMembers.filter(Field)
                .filter[f | f.name == name].isEmpty)
            error("Field with same name: method cannot be called",
                NAMED__NAME, FIELD_WITH_SAME_NAME)
    }
    
    @Check
    def checkNoDuplicateConstrs(Constr it) {
        val matching = enclosingClass.constrs.filter[c | utils.equalParams(c, it)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate constructor", null, DUPLICATE_CONSTR)
    }
    
    @Check
    def checkNoDuplicateVars(LocalVar it) {
        val vars = utils.varsAbove(enclosingStmt.eContainer, enclosingStmt)
        if(vars.exists[v | v.name == name])
            error("Duplicate variable " + name, NAMED__NAME, DUPLICATE_VARIABLE)
    }
    
    @Check
    def checkNoDuplicateParams(ParameterizedBody it) {
        for(p : params)
            if(params.exists[p !== it && p.name == name])
                error("Duplicate parameter " + p.name, p, NAMED__NAME, DUPLICATE_VARIABLE)
    }
	
	/**
	 * Checks that overriding methods actually override a method in a super
	 * class and that the return type is co- and the <code>this</code> role is
	 * contravariant. Note that covariance for the <code>this</code> role would
	 * be unsafe.
	 */
	@Check
	def checkOverrides(Method it) {
	    val superMethods = enclosingClass.superclass
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
        if(clazz instanceof NormalClass && (clazz as NormalClass).typeParam != null)
            error("Class " + clazz.name + " takes a type argument",
                SIMPLE_CLASS_REF__CLAZZ, MISSING_TYPE_ARG)
    }
    
    @Check
    def checkGenericClassRef(GenericClassRef it) {
        if(clazz.typeParam == null)
            error("Class " + clazz.name + " does not take a type argument",
                GENERIC_CLASS_REF__TYPE_ARG, INCORRECT_TYPE_ARG)
    }
    
    @Check
    def checkValFieldsInitialized(Constr it) {
        if(body == null) return
        
        val cfg = controlFlowGraph
        val extension analysis = valFieldsAnalysis.analyze(cfg, enclosingClass)
        for(f : enclosingClass.fields.filter[kind == VAL])
            if(!f.definitelyInitializedAfter(cfg.exit))
                error("Value field " + f.name + " may not have been initialized",
                    null, VAL_FIELD_NOT_INITIALIZED)
        
        for(a : all(Assignment).filter[utils.isValFieldInit(it)]) {
            val f = utils.assignedField(a)
            if(f.possiblyInitializedBefore(cfg.nodeOf(a)))
                error("Value field " + f.name + " may already have been initialized",
                    a, null, VAL_FIELD_OVERINITIALIZED)
        }
        
        for(a : all(MemberAccess).filter[isFieldAccess]) {
            val f = a.field
            if(f.kind == VAL && !a.isAssignmentTarget && a.target instanceof This
                    && !f.definitelyInitializedBefore(cfg.nodeOf(a)))
                error("Value field " + f.name + " may not have been initialized",
                    a, MEMBER_ACCESS__MEMBER, VAL_FIELD_NOT_INITIALIZED)
        }
    }
    
    @Check
    def checkValFieldInitialized(Field it) {
        if(enclosingClass.isSingleton && !isMapped && initializer == null)
            error("Value field " + name + " is not being initialized",
                    NAMED__NAME, VAL_FIELD_NOT_INITIALIZED)
    }
    
    private def isAssignmentTarget(Expr e) {
        e.eContainer instanceof Assignment
            && (e.eContainer as Assignment).left == e
    }
    
    private def <T> all(EObject it, java.lang.Class<T> c) {
        eAllContents.filter(c).toIterable
    }
    
    @Check
    def checkFieldInitializer(Field it) {
        if(initializer == null) return;
        
        if(mapped)
            error("Mapped fields cannot have an initializer", initializer,
                null, MAPPED_FIELD_WITH_INIT)
        for(t : initializer.all(This))
            error("Cannot refer to 'this' in a field initializer", t, null,
                THIS_IN_FIELD_INIT)
    }
    
    @Check
    def checkSingletonClassField(Field it) {
        if(!enclosingClass.isSingleton) return;
        
        if(kind != VAL)
            error("Fields of singleton classes must be val", FIELD__KIND,
                VAR_FIELD_IN_SINGLETON_CLASS)
        
        val type = type
        if(type instanceof RoleType)
            if(system.subrole(READONLY, type.role).failed) {
                val effectiveRole = system.leastCommonSuperrole(READONLY, type.role);
                warning("Singleton objects are always readonly, therefore this field's effective role is " + effectiveRole,
                    type, ROLE_TYPE__ROLE, INEFFECTIVE_FIELD_ROLE)
            }
    }
    
    @Check
    def checkLocalValInitialized(LocalVarDecl it) {
        if(variable.kind == VAL && initializer == null)
            error("Value is not initialized", variable, NAMED__NAME, VAL_NOT_INITIALIZED)
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
                    v, null, VAR_NOT_INITIALIZED)
    }
    
    @Check
    def checkSuperConstrCall(SuperConstrCall it) {
        if(!(enclosingBody instanceof Constr))
            error("Cannot call a super constructor here", null, INCORRECT_SUPER_CONSTR_CALL)
        
        if(!constr.checkedExceptionTypes.isEmpty)
            error("Cannot call a mapped super constructor that throws checked exceptions",
                null, UNCATCHABLE_CHECKED_EXCEPTION)
    }
    
    @Check
    def checkSuperConstrCall(Constr it) {
        if(body == null || enclosingClass.isObjectClass) return
        
        val cfg = controlFlowGraph
        val extension analysis = new SuperConstrCallAnalysis(cfg)
        for(t : all(This))
            if(cfg.nodeOf(t).isBeforeSuperConstrCall)
                error("Cannot refer to 'this' before calling the super constructor",
                    t, null, THIS_BEFORE_SUPER_CONSTR_CALL)
        for(n: all(New))
            if(cfg.nodeOf(n).isBeforeSuperConstrCall
                    && !(n.constr.checkedExceptionTypes.isEmpty))
                error("Cannot call a mapped constructor that throws checked exceptions before callig the super constructor",
                    n, null, UNCATCHABLE_CHECKED_EXCEPTION)
        
        for(c : all(SuperConstrCall))
            if(body.stmts.head !== c)
                error("Super constructor call must be the first statement",
                    c, null, SUPER_CONSTR_CALL_FIRST)
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
                    FIELD__MAPPED, MAPPED_IN_NORMAL_CLASS)
            
            val javaField =
                try javaMapper.javaField(it)
                catch(NoSuchFieldException _) {
                    error("Unknown mapped field", NAMED__NAME, UNKNOWN_MAPPED_FIELD)
                    return
                }
            if(!type.mapsTo(javaField.genericType))
                error("Incorrect type for mapped field: should map to "
                    + javaField.genericType, type, null, INCORRECT_MAPPED_FIELD)
        }
        else if(enclosingClass.mapped)
            error("Fields of mapped classes must be mapped",
                NAMED__NAME, NON_MAPPED_FIELD)
    }
    
    @Check
    def checkMappedMethod(Method it) {
        if(mapped) {
            if(!enclosingClass.mapped)
                error("mapped methods are allowed in mapped classes only",
                    METHOD__MAPPED, MAPPED_IN_NORMAL_CLASS)
            if(body != null)
                error("mapped methods cannot have a body", body, null, MAPPED_WITH_BODY)
            
            val javaMethod = 
                try javaMapper.javaMethod(it)
                catch(NoSuchMethodException _) {
                    error("Unknown mapped method", NAMED__NAME, UNKNOWN_MAPPED_METHOD)
                    return
                }
            if(!type.mapsTo(javaMethod.genericReturnType))
                error("Incorrect type for mapped method: should map to "
                    + javaMethod.genericReturnType, type, null, INCORRECT_MAPPED_METHOD)
        }
        else {
            if(enclosingClass.mapped)
                error("Methods of mapped classes must be mapped",
                    NAMED__NAME, NON_MAPPED_METHOD)
            if(body == null)
                error("Missing body", NAMED__NAME, MISSING_BODY)
        }
    }
    
    @Check
    def checkMappedConstr(Constr it) {
        if(mapped) {
            if(!enclosingClass.mapped)
                error("mapped constructors are allowed in mapped classes only",
                    CONSTR__MAPPED, MAPPED_IN_NORMAL_CLASS)
            if(body != null)
                error("mapped constructors cannot have a body", body, null, MAPPED_WITH_BODY)
            
            try javaMapper.javaConstr(it)
            catch(NoSuchMethodException _) {
                error("Unknown mapped constr", null, UNKNOWN_MAPPED_CONSTR)
                return
            }
        }
        else {
            if(enclosingClass.mapped)
                error("Constructors of mapped classes must be mapped",
                    null, NON_MAPPED_CONSTR)
            if(body == null)
                error("Missing body", null, MISSING_BODY)
        }
    }
    
    @Check
    def checkMappedClass(Class it) {
        if(mappedClasses.contains(qualifiedName)) {
            if(!mapped)
                error("Class must be declared as mapped", NAMED__NAME, CLASS_ACTUALLY_MAPPED)
        }
        else if(mapped)
            error("Unknown mapped class " + qualifiedName, NAMED__NAME, UNKNOWN_MAPPED_CLASS)
    }
    
    @Check
    def checkObjectClass(Class it) {
        if(!isObjectClass) return;
        
        checkClassKind(false)
        if(superclass != null)
           error(qualifiedName + " must not have a superclass",
               CLASS__SUPERCLASS, INCORRECT_MAPPED_SUPERCLASS)
    }
    
    @Check
    def checkStringClass(Class it) {
        if(!isStringClass) return;
        
        checkClassKind(false)
        checkSuperclass(objectClassName)
    }
    
    @Check
    def checkArrayClass(Class it) {
        if(!isArrayClass) return;
        
        checkClassKind(false)
        checkSuperclass(objectClassName)
        
        if(it instanceof NormalClass) {
            if(typeParam == null)
                error(qualifiedName + " must declare a type parameter",
                    NAMED__NAME, MISSING_TYPE_PARAM)
            
            if(constrs.size != 1)
               error(qualifiedName + " must have exactly one constructor",
                   null, INCORRECT_MAPPED_CONSTR)
            constrs.head => [
                if(!mapped)
                   error("Constructor must be declared as mapped",
                       it, null, INCORRECT_MAPPED_CONSTR)
                else if(params.size != 1)
                   error("Constructor must have a single parameter",
                       it, null, INCORRECT_MAPPED_CONSTR)
                else if(!(params.head.type instanceof Int))
                   error("Constructor parameter must be of type int",
                       params.head.type, null, INCORRECT_MAPPED_CONSTR)
            ]
            
            for(f : fields.filter[name == "length"]) {
                if(!f.mapped)
                    error("This field must be declared as mapped", f,
                        NAMED__NAME, INCORRECT_MAPPED_FIELD)
                if(f.kind != VAL)
                    error("This field must be a value field", f,
                        FIELD__KIND, INCORRECT_MAPPED_FIELD)
                if(!(f.type instanceof Int))
                    error("The type of this field must be int", f.type,
                        null, INCORRECT_MAPPED_FIELD)
            }
            for(f : fields.filter[isMapped && name != "length"])
                error("Unknown mapped field " + f.name, f,
                    NAMED__NAME, UNKNOWN_MAPPED_FIELD)
            
            for(m : methods.filter[name == "get"]) {
                if(!m.mapped)
                    error("This method must be declared as mapped", m,
                        NAMED__NAME, INCORRECT_MAPPED_METHOD)
                val type = m.type
                switch(type) {
                    TypeParamRef case type.param == typeParam: {}
                    default: error("The return type of this method must be "
                        + typeParam.name, m, NAMED__NAME, INCORRECT_MAPPED_METHOD)
                }
                if(m.params.size != 1)
                    error("This method must have a single int parameter", m,
                        NAMED__NAME, INCORRECT_MAPPED_METHOD)
                else if(!(m.params.head.type instanceof Int))
                    error("This parameter's type must be int", m.params.head.type,
                        null, INCORRECT_MAPPED_METHOD)
            }
            for(m : methods.filter[name == "set"]) {
                if(!m.mapped)
                    error("This method must be declared as mapped", m,
                        NAMED__NAME, INCORRECT_MAPPED_METHOD)
                if(!(m.type instanceof Void))
                    error("The return type of this method must be void", m.type,
                        null, INCORRECT_MAPPED_METHOD)
                
                if(m.params.size != 2)
                    error("This method must have two parameters", m,
                        NAMED__NAME, INCORRECT_MAPPED_METHOD)
                else {
                    if(!(m.params.get(0).type instanceof Int))
                        error("This parameter's type must be int", m.params.get(0).type,
                            null, INCORRECT_MAPPED_METHOD)
                    if(!(m.params.get(1).type instanceof TypeParamRef))
                        error("This parameter's type must be " + typeParam.name,
                            m.params.get(1).type, null, INCORRECT_MAPPED_METHOD)
                }
            }
            for(m : methods.filter[isMapped && name != "get" && name != "set"])
                error("unknown mapped method " + m.name, m,
                    NAMED__NAME, UNKNOWN_MAPPED_METHOD)
        }
    }
    
    @Check
    def checkTaskClass(Class it) {
        if(!isTaskClass) return;
        
        checkClassKind(false)
        checkSuperclass(objectClassName)
    }
    
    @Check
    def checkSystemClass(Class it) {
        if(qualifiedName != systemClassName) return;
        
        checkClassKind(true)
        checkSuperclass(objectClassName)
    }
    
    private def checkClassKind(Class it, boolean expectSingleton) {
        if(isSingleton != expectSingleton)
            error(qualifiedName + " must " + (if(expectSingleton) "" else "not ")
                + "be a singleton class", NAMED__NAME, INCORRECT_MAPPED_CLASS_KIND)
    }
    
    private def checkSuperclass(Class it, QualifiedName expected) {
        if(superclass.qualifiedName != expected)
           error("The superclass of " + qualifiedName + " must be " + expected,
               CLASS__SUPERCLASS, INCORRECT_MAPPED_SUPERCLASS)
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