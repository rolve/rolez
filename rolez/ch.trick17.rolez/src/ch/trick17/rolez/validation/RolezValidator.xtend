package ch.trick17.rolez.validation

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.ClassRef
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.LocalVar
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.ReturnExpr
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SimpleClassRef
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypedExecutable
import ch.trick17.rolez.rolez.VarKind
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.rolez.Void
import ch.trick17.rolez.scoping.RolezScopeProvider
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.typesystem.validation.RolezSystemValidator
import ch.trick17.rolez.validation.cfg.CfgProvider
import ch.trick17.rolez.validation.cfg.InstrNode
import ch.trick17.rolez.validation.dataflow.LocalVarsInitializedAnalysis
import ch.trick17.rolez.validation.dataflow.SuperConstrCallAnalysis
import ch.trick17.rolez.validation.dataflow.ValFieldsInitializedAnalysis
import java.util.HashSet
import java.util.Set
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmType
import org.eclipse.xtext.util.Exceptions
import org.eclipse.xtext.validation.AbstractDeclarativeValidator
import org.eclipse.xtext.validation.Check

import static ch.trick17.rolez.Constants.*
import static ch.trick17.rolez.rolez.RolezPackage.Literals.*
import static ch.trick17.rolez.rolez.VarKind.*

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.RolezUtils.*

class RolezValidator extends RolezSystemValidator {

    public static val INVALID_NAME = "invalid name"
    public static val CIRCULAR_INHERITANCE = "circular inheritance"
    public static val SINGLETON_SUPERCLASS = "singleton superclass"
    public static val INCORRECT_SUPERCLASS_PURITY = "incorrect superclass purity"
    public static val INCORRECT_TYPE_PARAM = "incorrect type parameter"
    public static val MISSING_TYPE_PARAM = "missing type parameter"
    public static val DUPLICATE_FIELD = "duplicate field"
    public static val DUPLICATE_METHOD = "duplicate method"
    public static val FIELD_WITH_SAME_NAME = "field with same name"
    public static val DUPLICATE_CONSTR = "duplicate constructor"
    public static val DUPLICATE_VAR = "duplicate var"
    public static val MISSING_OVERRIDE = "missing override"
    public static val INCORRECT_OVERRIDE = "incorrect override"
    public static val INCOMPATIBLE_RETURN_TYPE = "incompatible return type"
    public static val INCOMPATIBLE_THIS_ROLE = "incompatible \"this\" role"
    public static val INCOMPATIBLE_PARAM_TYPE = "incompatible param type"
    public static val MISSING_RETURN_EXPR = "missing return statement"
    public static val MISSING_TYPE_ARG = "missing type argument"
    public static val INCORRECT_TYPE_ARG = "incorrect type argument"
    public static val INCORRECT_MAIN = "incorrect main"
    public static val INCORRECT_MAIN_CLASS = "incorrect main class"
    public static val VAL_FIELD_NOT_INITIALIZED = "val field not initialized"
    public static val VAL_FIELD_OVERINITIALIZED = "val field overinitialized"
    public static val FIELD_INIT_TYPE_MISMATCH = "field initializer type mismatch"
    public static val MAPPED_FIELD_WITH_INIT = "mapped with with initializer"
    public static val THIS_IN_FIELD_INIT = "'this' in field initializer"
    public static val VAR_FIELD_IN_SINGLETON_CLASS = "var field in singleton class"
    public static val INEFFECTIVE_FIELD_ROLE = "ineffective field role"
    public static val VAR_FIELD_IN_PURE_CLASS = "var field in pure class"
    public static val NON_PURE_FIELD_IN_PURE_CLASS = "non-pure field in pure class"
    public static val UNCALLABLE_METHOD = "uncallable method"
    public static val VAL_NOT_INITIALIZED = "val not initialized"
    public static val VAR_NOT_INITIALIZED = "var not initialized"
    public static val INCORRECT_SUPER_CONSTR_CALL = "incorrect super constructor call"
    public static val MISSING_SUPER_CONSTR_CALL = "missing super constructor call"
    public static val SUPER_CONSTR_CALL_FIRST = "super constructor call first"
    public static val THIS_BEFORE_SUPER_CONSTR_CALL = "'this' before super constructor call"
    public static val UNCATCHABLE_CHECKED_EXCEPTION = "uncatchable checked exception"
    public static val OUTER_EXPR_NO_SIDE_FX = "outer expr no side effects"
    public static val NULL_TYPE_USED = "null type used"
    public static val VOID_NOT_RETURN_TYPE = "void not return type"
    public static val MAPPED_IN_NORMAL_CLASS = "mapped member in normal class"
    public static val NON_MAPPED_FIELD = "non-mapped field"
    public static val NON_MAPPED_METHOD = "non-mapped method"
    public static val NON_MAPPED_CONSTR = "non-mapped constructor"
    public static val MAPPED_WITH_BODY = "mapped with body"
    public static val MISSING_BODY = "missing body"
    public static val CLASS_ACTUALLY_MAPPED = "class actually mapped"
    public static val INCORRECT_MAPPED_CLASS = "incorrect mapped class"
    public static val INCORRECT_MAPPED_CLASS_KIND = "incorrect mapped class kind"
    public static val INCORRECT_MAPPED_CLASS_PURITY = "incorrect mapped class purity"
    public static val INCORRECT_MAPPED_FIELD = "incorrect mapped field"
    public static val NON_GUARDED_MAPPED_VAR_FIELD = "non-guarded mapped var field"
    public static val INCORRECT_MAPPED_METHOD = "incorrect mapped method"
    public static val INCORRECT_MAPPED_TASK = "incorrect mapped task"
    public static val INCORRECT_MAPPED_CONSTR = "incorrect mapped constructor"
    
    @Inject extension RolezFactory
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
    def checkCircularInheritance(Class it) {
        if(findSuperclass(it))
            error("Circular inheritance", CLASS__SUPERCLASS_REF, CIRCULAR_INHERITANCE)
    }
    
    private def boolean findSuperclass(Class it, Class c) {
        switch(superclass) {
            case null: false
            case c   : true
            default  : findSuperclass(superclass, c)
        }
    }
    
    @Check
    def checkSingletonSuperclass(Class it) {
        if(superclassRef != null && superclassRef.clazz instanceof SingletonClass)
            error("Singleton classes cannot be extended", CLASS__SUPERCLASS_REF, SINGLETON_SUPERCLASS)
    }
    
    @Check
    def checkPureSuperclass(Class it) {
        /* Pure classes must extend pure classes or Object; otherwise, they could inherit mutable
         * parts. Singletons are an exception: since there can never be a readwrite reference to
         * them, even mutable parts they inherit from superclasses are effectively immutable. */
        if(isPure && !singleton && !superclass.isPure && !superclass.isObjectClass)
            error("Pure classes cannot extend non-pure classes", CLASS__SUPERCLASS_REF, INCORRECT_SUPERCLASS_PURITY)
        else if(!isPure && !isObjectClass && superclass.isPure)
            error("Non-pure classes cannot extend pure classes", CLASS__SUPERCLASS_REF, INCORRECT_SUPERCLASS_PURITY)
    }
    
    @Check
    def checkMainTask(Method it) {
        if(!isMain) return;
        
        if(!isTask)
            error("Illegal method name: only tasks can be named main", NAMED__NAME, INCORRECT_MAIN)
        if(!(type instanceof Void))
            error("A main task must have a void return type", type, null, INCORRECT_MAIN)
        if(params.size > 1)
            error("A main task must have zero or one parameter", EXECUTABLE__PARAMS, INCORRECT_MAIN)
        else if(params.size == 1 && !params.head.type.isStringArray)
            error("The parameter of a main must must be of type readonly Array[String]",
                params.head.type, null, INCORRECT_MAIN)
    }
    
    @Check
    def checkMainClassInstantiable(Class it) {
        if(!methods.exists[isMain]) return;
        
        if(it instanceof NormalClass) if(!constrs.exists[params.isEmpty])
            error("Class with a main task must have a no-args constructor", NAMED__NAME,
                INCORRECT_MAIN_CLASS)
    }
    
    private def isStringArray(Type it) {
        switch(it) {
            RoleType case role instanceof ReadOnly && base.clazz.isArrayClass: {
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
        if(typeParam != null && !isMapped)
            error("Only mapped classes may declare a type parameter",
                NORMAL_CLASS__TYPE_PARAM, INCORRECT_TYPE_PARAM)
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
	 * Checks that there are no other methods in the same class with the same signature. The
	 * signature does not comprise roles, therefore <code>this</code>-role-based overloading is not
	 * possible, which greatly simplifies code generation.
	 * <p>
	 * An additional reason for ignoring the "this" role is that it might be surprising to
	 * programmers if methods are virtually dispatched based on the class of the target, but
	 * statically dispatched based on its role.
	 */
    @Check
    def checkNoDuplicateMethods(Method it) {
        // TODO: Shouldn't we also ignore type args while comparing?
        val matching = enclosingClass.methods.filter[m |
            it !== m && equalSignatureWithoutRoles(m, it)
        ]
        if(matching.size > 0)
           error("Duplicate method " + name + "("+ params.map[type.toStringWithoutRoles].join(", ") + ")",
               NAMED__NAME, DUPLICATE_METHOD)
    }
    
    private def String toStringWithoutRoles(Type it) { switch(it) {
        RoleType: base.toStringWithoutRoles
        default : toString
    }}
    
    private def toStringWithoutRoles(ClassRef it) { switch(it) {
        GenericClassRef: clazz.qualifiedName + "[" + typeArg.toStringWithoutRoles + "]"
        default        : toString
    }}
    
    @Check
    def checkFieldWithSameName(Method it) {
        if(params.isEmpty && !enclosingClass.allMembers.filter(Field).filter[f | f.name == name].isEmpty) {
            if(isOverriding)
                info("Field with same name: method cannot be called on targets with static type " + enclosingClass.name,
                    NAMED__NAME, FIELD_WITH_SAME_NAME)
            else
                error("Field with same name: method cannot be called",
                    NAMED__NAME, FIELD_WITH_SAME_NAME)
        }
    }
    
    @Check
    def checkNoDuplicateConstrs(Constr it) {
        val matching = enclosingClass.constrs.filter[c | equalParamsWithoutRoles(c, it)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate constructor", null, DUPLICATE_CONSTR)
    }
    
    @Check
    def checkNoDuplicateVars(LocalVarDecl it) {
        val vars = varsAbove(eContainer, it)
        if(vars.exists[v | v.name == variable.name])
            error("Duplicate variable " + variable.name, variable, NAMED__NAME, DUPLICATE_VAR)
    }
    
    @Check
    def checkNoDuplicateParams(Executable it) {
        for(p : params)
            if(params.exists[p !== it && p.name == name])
                error("Duplicate parameter " + p.name, p, NAMED__NAME, DUPLICATE_VAR)
    }
	
	/**
	 * For overriding methods, checks that the return type is co- and the parameter types and the
	 * "this" role are contravariant. Note that the parameter types are already guaranteed to be
	 * equal to the corresponding types in the overridden method, except for the roles (including
	 * roles of type arguments).
	 */
	@Check
	def checkValidOverride(Method it) {
        if(!overriding) return;
        
        if(system.subtype(createEnv(it), type, superMethod.type).failed)
            error("The return type " + type + " is incompatible with overridden method "
                + superMethod, TYPED__TYPE, INCOMPATIBLE_RETURN_TYPE)
        if(system.subrole(superMethod.thisRole, thisRole).failed)
            error("The role of \"this\" is incompatible with overridden method " + superMethod,
                METHOD__THIS_ROLE, RolezValidator.INCOMPATIBLE_THIS_ROLE)
        
        for(p : params) {
            val superParamType = superMethod.params.get(p.paramIndex).type
            if(system.subtype(createEnv(it), superParamType, p.type).failed)
                error("This parameter type is incompatible with overridden method "
                    + superMethod, p, TYPED__TYPE, RolezValidator.INCOMPATIBLE_PARAM_TYPE)
        }
	}
    
    /* To reuse the code that finds overridden methods */
    @Inject RolezScopeProvider scopeProvider
    
	/**
     * For non-overriding methods, checks that they are not actually overriding a method from the
     * superclass.
	 */
	@Check
	def checkMissingOverride(Method it) {
	    if(overriding) return
	    
        val scope = scopeProvider.scope_Method_superMethod(it, METHOD__SUPER_METHOD)
        if(!scope.allElements.isEmpty)
            error("Method must be declared with \"override\" since it "
                    + "actually overrides a superclass method",
                NAMED__NAME, MISSING_OVERRIDE)
	}
	
	@Check
	def checkReturnExpr(TypedExecutable it) {
        if(body == null || type instanceof Void) return;
        
	    val cfg = body.controlFlowGraph
        for(p : cfg.exit.predecessors) {
            if(p instanceof InstrNode) {
                if(!(p.instr instanceof ReturnExpr))
                    error("Method must return a value of type " + type,
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
        
        val cfg = body.controlFlowGraph
        val extension analysis = valFieldsAnalysis.analyze(cfg, enclosingClass)
        for(f : enclosingClass.fields.filter[kind == VAL])
            if(!f.definitelyInitializedAfter(cfg.exit))
                error("Value field " + f.name + " may not have been initialized",
                    null, VAL_FIELD_NOT_INITIALIZED)
        
        for(a : all(Assignment).filter[isValFieldInit(it)]) {
            val f = assignedField(a)
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
        
        val subtypeResult = system.subtypeExpr(initializer, type)
        if(subtypeResult.failed)
            error(subtypeResult.ruleFailedException.message, initializer, null, FIELD_INIT_TYPE_MISMATCH)
        if(isMapped)
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
            if(system.subrole(createReadOnly, type.role).failed) {
                val effectiveRole = system.leastCommonSuperrole(createReadOnly, type.role);
                warning("Singleton objects are always readonly, therefore this field's effective role is " + effectiveRole,
                    type.role, null, INEFFECTIVE_FIELD_ROLE)
            }
    }
    
    @Check
    def checkPureClassFields(Field it) {
        if(!enclosingClass.isPure || enclosingClass.isSingleton) return; // Singletons are checked separately
        
        if(kind == VarKind.VAR)
            error("Var-field in pure class", FIELD__KIND, VAR_FIELD_IN_PURE_CLASS)
        if(type instanceof RoleType && !((type as RoleType).base.clazz.isPure))
            error("Non-pure field in pure class", TYPED__TYPE, NON_PURE_FIELD_IN_PURE_CLASS)
    }
    
    @Check
    def checkSingletonClassMethod(Method it) {
        if(!enclosingClass.isSingleton) return;
        
        if(thisRole instanceof ReadWrite || thisRole instanceof RoleParamRef
                && (thisRole as RoleParamRef).param.upperBound instanceof ReadWrite) {
            warning("Singleton objects are always readonly, therefore this "
                + if(isTask) "task can never be started" else "method can never be called",
                thisRole, null, UNCALLABLE_METHOD)
        }
    }
    
    @Check
    def checkLocalValInitialized(LocalVarDecl it) {
        if(variable.kind == VAL && initializer == null)
            error("Value is not initialized", variable, NAMED__NAME, VAL_NOT_INITIALIZED)
    }
    
    @Check
    def checkLocalVarsInitialized(Executable it) {
        if(body == null) return
        
        val cfg = body.controlFlowGraph
        val extension analysis = new LocalVarsInitializedAnalysis(cfg)
        for(v : all(VarRef))
            if(v.variable instanceof LocalVar && !v.isAssignmentTarget
                    && !v.variable.isInitializedBefore(cfg.nodeOf(v)))
                error("Variable " + v.variable.name + " may not have been initialized",
                    v, null, VAR_NOT_INITIALIZED)
    }
    
    @Check
    def checkSuperConstrCall(SuperConstrCall it) {
        if(!(enclosingExecutable instanceof Constr))
            error("Cannot call a super constructor here", null, INCORRECT_SUPER_CONSTR_CALL)
        
        if(!constr.checkedExceptionTypes.isEmpty)
            error("Cannot call a mapped super constructor that throws checked exceptions",
                null, UNCATCHABLE_CHECKED_EXCEPTION)
    }
    
    @Check
    def checkSuperConstrCall(Constr it) {
        if(body == null || enclosingClass.isObjectClass) return
        
        val cfg = body.controlFlowGraph
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
    def checkNull(Null it) {
        error("The null type cannot be used explicitly", null, NULL_TYPE_USED)
    }
    
    @Check
    def checkVoid(Void it) {
        if(!(eContainer instanceof TypedExecutable) || it !== (eContainer as TypedExecutable).type)
            error("The void type can only be used as a return type", null, VOID_NOT_RETURN_TYPE)
    }
    
    @Check
    def checkMappedClass(Class it) {
        if(!isMapped) return;
        
        if(it instanceof NormalClass) {
            if(!jvmClass.typeParameters.isEmpty) {
                if(jvmClass.typeParameters.size > 1)
                    error("Cannot map to a class with multiple type parameters",
                        CLASS__JVM_CLASS, INCORRECT_MAPPED_CLASS)
                else if(typeParam == null)
                    error("Missing type parameter for mapped class",
                        NAMED__NAME, MISSING_TYPE_PARAM)
                else if(typeParam.name != jvmClass.typeParameters.head.name)
                    error("Incorrect type parameter name for mapped class: expected " + jvmClass.typeParameters.head.name,
                        typeParam, NAMED__NAME, INCORRECT_TYPE_PARAM)
            }
            else if(typeParam != null)
                error(qualifiedName + " must not declare a type parameter",
                    typeParam, NAMED__NAME, INCORRECT_TYPE_PARAM)
            
            if(superclass != null) {
                if(!superclass.isMapped)
                    error("A mapped class cannot extend a non-mapped class",
                        CLASS__SUPERCLASS_REF, INCORRECT_MAPPED_CLASS)
                else if(!jvmClass.isSubclassOf(superclass.jvmClass))
                    error("Incorrect superclass for mapped class: expected one of "
                        + jvmClass.superclasses.join(", "), CLASS__SUPERCLASS_REF, INCORRECT_MAPPED_CLASS)
            }
        }
    }
    
    private def Iterable<JvmType> superclasses(JvmDeclaredType it) {
        if(extendedClass == null) #[]
        else (extendedClass.type as JvmDeclaredType).superclasses + #[extendedClass.type]
    }
    
    @Check
    def checkMappedField(Field it) {
        if(isMapped) {
            if(!enclosingClass.isMapped)
                error("Mapped fields are allowed in mapped classes only",
                    FIELD__JVM_FIELD, MAPPED_IN_NORMAL_CLASS)
            
            if((kind == VAL) != jvmField.isFinal)
                error("Incorrect field kind for mapped field: expected " + if(jvmField.isFinal) "val" else "var",
                    FIELD__KIND, INCORRECT_MAPPED_FIELD)
            if(!type.mapsTo(jvmField.type))
                error("Incorrect type for mapped field: should map to "
                    + jvmField.type, type, null, INCORRECT_MAPPED_FIELD)
            if(kind == VarKind.VAR && !enclosingClass.jvmClass.isSubclassOf(jvmGuardedClassName, it))
                error("Cannot map to non-final field of non-Guarded class",
                    null, NON_GUARDED_MAPPED_VAR_FIELD)
        }
        else if(enclosingClass.isMapped)
            error("Fields of mapped classes must be mapped",
                NAMED__NAME, NON_MAPPED_FIELD)
    }
    
    @Check
    def checkMappedMethod(Method it) {
        if(isMapped) {
            if(isTask)
                error("Tasks cannot be mapped", METHOD__JVM_METHOD, INCORRECT_MAPPED_TASK)
            if(!enclosingClass.isMapped)
                error("Mapped methods are allowed in mapped classes only",
                    METHOD__JVM_METHOD, MAPPED_IN_NORMAL_CLASS)
            if(body != null)
                error("Mapped methods cannot have a body", body, null, MAPPED_WITH_BODY)
            
            if(!type.mapsTo(jvmMethod.returnType))
                error("Incorrect type for mapped method: should map to "
                    + jvmMethod.returnType, type, null, INCORRECT_MAPPED_METHOD)
        }
        else {
            if(enclosingClass.isMapped)
                error("Methods of mapped classes must be mapped",
                    NAMED__NAME, NON_MAPPED_METHOD)
            if(body == null)
                error("Missing body", NAMED__NAME, MISSING_BODY)
        }
    }
    
    @Check
    def checkMappedConstr(Constr it) {
        if(isMapped) {
            if(!enclosingClass.isMapped)
                error("Mapped constructors are allowed in mapped classes only",
                    CONSTR__JVM_CONSTR, MAPPED_IN_NORMAL_CLASS)
            if(body != null)
                error("Mapped constructors cannot have a body", body, null, MAPPED_WITH_BODY)
        }
        else {
            if(enclosingClass.isMapped)
                error("Constructors of mapped classes must be mapped",
                    null, NON_MAPPED_CONSTR)
            if(body == null)
                error("Missing body", null, MISSING_BODY)
        }
    }
    
    @Check
    def checkObjectClass(Class it) {
        if(!isObjectClass) return;
        
        checkClassKind(false)
        checkPurity(false)
        checkMapped
        if(superclass != null)
           error(qualifiedName + " must not have a superclass",
               CLASS__SUPERCLASS_REF, INCORRECT_MAPPED_CLASS)
    }
    
    @Check
    def checkSliceClass(Class it) {
        if(!isSliceClass) return;
        
        checkClassKind(false)
        checkPurity(false)
        checkMapped
    }
    
    @Check
    def checkArrayClass(Class it) {
        if(!isArrayClass) return;
        
        checkClassKind(false)
        checkPurity(false)
        checkMapped
    }
    
    @Check
    def checkVectorClass(Class it) {
        if(!isVectorClass) return;
        
        checkClassKind(false)
        checkPurity(true)
        checkMapped
    }
    
    @Check
    def checkVectorBuilderClass(Class it) {
        if(!isVectorBuilderClass) return;
        
        checkClassKind(false)
        checkPurity(false)
        checkMapped
    }
        
    // TODO: Introduce final classes and make array, slice, etc. final, so that they cannot be 
    // extended (although, it would be cool if the array class could be extended...)
    
    @Check
    def checkStringClass(Class it) {
        if(!isStringClass) return;
        
        checkClassKind(false)
        checkPurity(true)
        checkMapped
    }
    
    private def checkClassKind(Class it, boolean expectSingleton) {
        if(isSingleton != expectSingleton)
            error(qualifiedName + " must " + (if(expectSingleton) "" else "not ")
                + "be a singleton class", NAMED__NAME, INCORRECT_MAPPED_CLASS_KIND)
    }
    
    private def checkPurity(Class it, boolean expectPure) {
        if(isPure != expectPure)
            error(qualifiedName + " must " + (if(expectPure) "" else "not ")
                + "be a pure class", NAMED__NAME, INCORRECT_MAPPED_CLASS_PURITY)
    }
    
    private def checkMapped(Class it) {
        if(!mapped)
            error("Class must be declared as mapped", NAMED__NAME, CLASS_ACTUALLY_MAPPED)
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
    
    /*
     * Custom MethodWrapper
     */
    
    override protected createMethodWrapper(AbstractDeclarativeValidator validator, java.lang.reflect.Method m) {
        super.createMethodWrapper(validator, m)
    }
    
    private static class MethodWrapper extends AbstractDeclarativeValidator.MethodWrapper {
        new(AbstractDeclarativeValidator instance, java.lang.reflect.Method m) {
            super(instance, m)
        }
        
        override protected handleInvocationTargetException(Throwable e, State state) {
            Exceptions.throwUncheckedException(e)
        }
    }
}