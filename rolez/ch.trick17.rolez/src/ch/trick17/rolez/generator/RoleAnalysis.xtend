package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BuiltInRole
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.FieldInitializer
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.dataflow.DataFlowAnalysis
import com.google.common.collect.ImmutableMap
import java.util.HashMap
import java.util.Map
import java.util.Map.Entry

import static ch.trick17.rolez.generator.CodeKind.*
import static ch.trick17.rolez.rolez.VarKind.*
import static com.google.common.collect.ImmutableMap.copyOf

class RoleAnalysis extends DataFlowAnalysis<ImmutableMap<String, BuiltInRole>> {
    
    extension RolezFactory = RolezFactory.eINSTANCE
    
    val Executable code
    public val CodeKind codeKind
    
    new(FieldInitializer code, ControlFlowGraph cfg) {
        this(code, cfg, FIELD_INITIALIZER)
    }
    
    new(Constr code, ControlFlowGraph cfg) {
        this(code, cfg, CONSTR)
    }
    
    new(Method code, ControlFlowGraph cfg, CodeKind codeKind) {
        this(code as Executable, cfg, codeKind)
        if(!#[METHOD, TASK].contains(codeKind))
            throw new IllegalArgumentException
    }
    
    private new(Executable code, ControlFlowGraph cfg, CodeKind codeKind) {
        super(cfg, true)
        this.code = code
        this.codeKind = codeKind
        
        analyze
    }
    
    protected override newFlow()   { ImmutableMap.of }
    
    protected override entryFlow() {
        val paramRoles = code.params.filter[type instanceof RoleType].toMap[name].mapValues[
            if(codeKind == TASK) (type as RoleType).role.erased else createPure
        ]
        val thisRole = switch(codeKind) {
            case CONSTR: createReadWrite
            case TASK  : (code as Method).thisRole.erased
            default    : createPure
        }
        paramRoles.with("$this", thisRole)
    }
    
    private def erased(Role it) { switch(it) {
        BuiltInRole : it
        RoleParamRef: param.upperBound  
    }}
    
    protected def dispatch flowThrough(MemberAccess a, ImmutableMap<String, BuiltInRole> in) {
        if(a.isTaskStart || a.isMethodInvoke && a.method.isAsync)
            copyOf(in.mapValues[createPure])
        else if(a.target instanceof Var && a.isFieldAccess && a.field.kind == VAR) {
            // after a successful field read, we know the object is at least readonly!
            val name = (a.target as Var).name
            in.with(name, greatestCommonSubrole(in.get(name), createReadOnly))
        }
        else
            in
    }
    
    protected def dispatch flowThrough(LocalVarDecl d, ImmutableMap<String, BuiltInRole> in) {
        in.with(d.variable.name, d.initializer?.dynamicRole(in) ?: createPure)
    }
    
    protected def dispatch flowThrough(Assignment a, ImmutableMap<String, BuiltInRole> in) {
        if(a.left instanceof Var)
            in.with((a.left as Var).name, a.right.dynamicRole(in))
        else
            in
    }
    
    protected def dispatch flowThrough(Instr _, ImmutableMap<String, BuiltInRole> in) { in }
    
    protected override merge(ImmutableMap<String, BuiltInRole> in1,
            ImmutableMap<String, BuiltInRole> in2) {
        val merged = new HashMap(in1)
        for(Entry<String, BuiltInRole> entry : in2.entrySet)
            merged.put(entry.key,
                    leastCommonSuperrole(entry.value, merged.get(entry.key) ?: createReadWrite))
        copyOf(merged)
    }
    
    private def leastCommonSuperrole(BuiltInRole r1, BuiltInRole r2) {
        if(r1.isSubroleOf(r2)) r2 else r1
    }
    
    private def greatestCommonSubrole(BuiltInRole r1, BuiltInRole r2) {
        if(r1.isSubroleOf(r2)) r1 else r2
    }
    
    private def isSubroleOf(BuiltInRole it, BuiltInRole other) {
        it instanceof ReadWrite || other instanceof Pure
            || it instanceof ReadOnly && other instanceof ReadOnly
    }
    
    private def BuiltInRole dynamicRole(Expr it, Map<String, BuiltInRole> roles) { switch(it) {
        New                       : createReadWrite
        The, StringLiteral        : createReadOnly
        MemberAccess case isGlobal: createReadOnly
        VarRef                    : roles.get(variable.name)
        This                      : roles.get("$this")
        Cast, Parenthesized       : expr.dynamicRole(roles)
        default: createPure
    }}
        
    private def boolean isGlobal(Expr it) { switch(it) {
        The: true
        MemberAccess case isFieldAccess && target.isGlobal: true
        default: false
    }}
    
    private def with(Map<String, BuiltInRole> map, String varName, BuiltInRole role) {
        copyOf(new HashMap(map) => [put(varName, role)])
    }
    
    def dynamicRole(Expr it) { switch(it) {
        This   : cfg.nodeOf(it).inFlow.get("$this")
        VarRef : cfg.nodeOf(it).inFlow.get(variable.name)
        default: dynamicRole(ImmutableMap.of)
    }}
}