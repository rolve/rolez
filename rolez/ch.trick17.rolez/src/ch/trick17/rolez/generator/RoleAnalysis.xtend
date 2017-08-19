package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BuiltInRole
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.ThisParam
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.cfg.CfgProvider
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.dataflow.DataFlowAnalysis
import com.google.common.collect.ImmutableMap
import java.util.HashMap
import java.util.Map.Entry
import javax.inject.Inject
import org.eclipse.xtend.lib.annotations.Data

import static ch.trick17.rolez.rolez.VarKind.*
import static com.google.common.collect.ImmutableMap.copyOf

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension java.util.Objects.requireNonNull

/**
 * A dataflow analysis that approximates the dynamic role of local
 * variables and other expressions. Used together with the
 * {@link ChildTasksAnalysis} to determine where to insert guards in
 * the {@link InstrGenerator}.
 * <p>
 * Note that the role analysis is unaware of the different kinds of
 * methods generated from a single Rolez method (guarded, unguarded,
 * task). Only the results of the child tasks analysis are
 * method-kind-dependent.
 */
class RoleAnalysis extends DataFlowAnalysis<ImmutableMap<Var, RoleInfo>> {
    
    static class Provider {
        
        @Inject RolezSystem system
        @Inject RolezUtils utils
        @Inject extension CfgProvider
    
        def newRoleAnalysis(Executable executable) {
            new RoleAnalysis(executable, executable.code.controlFlowGraph, system, utils)
        }
    }
    
    val extension RolezFactory factory = RolezFactory.eINSTANCE
    val extension RolezUtils utils
    val RolezSystem system
    
    val Executable code
    
    private new(Executable code, ControlFlowGraph cfg, RolezSystem system, RolezUtils utils) {
        super(cfg, true)
        this.code = code
        
        this.system = system
        this.utils = utils
        
        analyze
    }
    
    protected override newFlow() { null } // represents a not-yet-computed flow
    
    protected override entryFlow() {
        val flow = new HashMap
        for(Param p : code.allParams.filter[type instanceof RoleType]) {
            val info =
                if(code instanceof Constr && p instanceof ThisParam)
                    RoleInfo.readWrite
                else
                    RoleInfo.pure
            flow.put(p, info)
        }
        copyOf(flow)
    }
    
    protected def dispatch flowThrough(LocalVarDecl d, ImmutableMap<Var, RoleInfo> in) {
        if(d.initializer !== null) flowThroughAssign(d.variable, d.initializer, in)
        else in
    }
    
    protected def dispatch flowThrough(Assignment a, ImmutableMap<Var, RoleInfo> in) {
        // TODO: support assignments to final fields (in constructors)
        if(a.left instanceof VarRef) flowThroughAssign((a.left as VarRef).variable, a.right, in)
        else in
    }
    
    private def flowThroughAssign(Var left, Expr right, ImmutableMap<Var, RoleInfo> in) {
        if(system.varType(left).value instanceof RoleType)
            in.replace(left, roleInfo(right, in))
        else
            in
    }
    
    protected def dispatch flowThrough(MemberAccess a, ImmutableMap<Var, RoleInfo> in) {
        if(a.isTaskStart || a.isMethodInvoke && a.method.isAsync)
            // after a task has (possibly) been started, reset all roles to pure
            ImmutableMap.of
        else if((a.isReadAccess || a.isWriteAccess) && a.target.isTracked) {
            // after a read/write access, we know the object is at least readonly/readwrite
            val role = if(a.isWriteAccess) createReadWrite else createReadOnly
            val accessSeq = a.target.asAccessSeq
            val prev = in.get(accessSeq.variable) ?: RoleInfo.pure
            in.replace(accessSeq.variable, prev.with(role, accessSeq.fieldSeq))
        }
        else
            in
    }
    
    protected def dispatch flowThrough(Instr _, ImmutableMap<Var, RoleInfo> in) { in }
    
    protected override merge(ImmutableMap<Var, RoleInfo> in1, ImmutableMap<Var, RoleInfo> in2) {
        // if one flow has not been computed yet, just return the other
        if(in1 === null)
            in2
        else if(in2 === null)
            in1
        else
            mergeMaps(in1, in2)
    }
    
    /**
     * checks if the expression is tracked by the analysis, i.e., if it is a var ref (to a
     * variable of a role type) or an access to a final field (of a role type) with a tracked
     * expression as the target
     */
    private def boolean isTracked(Expr it) { switch(it) {
        VarRef      : system.varType(variable).value instanceof RoleType
        MemberAccess: isFieldAccess && field.kind == VAL && field.type instanceof RoleType
                            && target.isTracked
        default     : false
    }}
    
    /**
     * returns the access sequence that corresponds to the given tracked (!) expression
     */
    private def AccessSeq asAccessSeq(Expr it) { switch(it) {
        VarRef      : new AccessSeq(variable, emptyList)
        MemberAccess: target.asAccessSeq.concat(field)
        default     : throw new AssertionError
    }}
    
    /**
     * Returns a role info object that is equivalent to the given one, except with the additional
     * information that the object denoted by the given sequence of field accesses (relative to the
     * object the role info applies to) has at least the given role. That is, if the field sequence
     * is empty, the role concerns the object's own role.
     */
    private def RoleInfo with(RoleInfo it, BuiltInRole role, Iterable<Field> fieldSeq) {
        if(fieldSeq.isEmpty)
            new RoleInfo(system.greatestCommonSubrole(ownRole, role) as BuiltInRole, fields)
        else {
            val field = fieldSeq.head
            val prev = fields.get(field) ?: RoleInfo.pure
            new RoleInfo(ownRole, fields.replace(field, prev.with(role, fieldSeq.tail)))
        }
    }
    
    private def <K> replace(ImmutableMap<K, RoleInfo> it, K key, RoleInfo value) {
        copyOf(new HashMap(it) => [put(key, value)])
    }
    
    private def <K> mergeMaps(ImmutableMap<K, RoleInfo> it, ImmutableMap<K, RoleInfo> other) {
        if(equals(other)) return it
        
        val merged = new HashMap
        for(Entry<K, RoleInfo> entry : entrySet)
            if(other.containsKey(entry.key))
                merged.put(entry.key, merge(entry.value, other.get(entry.key)))
        copyOf(merged)
    }
    
    private def RoleInfo merge(RoleInfo it, RoleInfo other) {
        val role = system.leastCommonSuperrole(ownRole, other.ownRole) as BuiltInRole
        new RoleInfo(role, mergeMaps(fields, other.fields))
    }
    
    private def RoleInfo roleInfo(Expr it, ImmutableMap<Var, RoleInfo> roles) { switch(it) {
        case isTracked: {
            val accessSeq = asAccessSeq
            var roleInfo = roles.get(accessSeq.variable) ?: RoleInfo.pure
            for(Field f : accessSeq.fieldSeq)
                roleInfo = roleInfo.fields.get(f) ?: RoleInfo.pure
            roleInfo
        }
        Cast, Parenthesized       : roleInfo(expr, roles)
        Assignment                : roleInfo(right, roles)
        Slicing                   : roleInfo(target, roles)
        New                       : RoleInfo.readWrite
        The, StringLiteral        : RoleInfo.readOnly
        MemberAccess case isGlobal: RoleInfo.readOnly
        default                   : RoleInfo.pure
    }}
    
    private def isReadAccess(MemberAccess it) {
        isMethodInvoke && method.isGuarded && method.erasedThisRole instanceof ReadOnly
            || isFieldAccess && field.kind == VAR && !isFieldWrite
    }
    
    private def isWriteAccess(MemberAccess it) {
        isMethodInvoke && method.isGuarded && method.erasedThisRole instanceof ReadWrite
            || isFieldWrite
    }
    
    private def erasedThisRole(Method it) {
        original.thisParam.type.role.erased
    }
    
    private def boolean isGlobal(Expr it) { switch(it) {
        The: true
        MemberAccess case isFieldAccess && target.isGlobal: true
        default: false
    }}
    
    /* Interface of the analysis */
    
    def dynamicRole(Expr it) {
        roleInfo(it, cfg.nodeOf(it).inFlow ?: ImmutableMap.of).ownRole
    }
}

package class RoleInfo {
    
    /* reuse the same "simple" instances */
    package static val pure      = new RoleInfo(RolezFactory.eINSTANCE.createPure)
    package static val readOnly  = new RoleInfo(RolezFactory.eINSTANCE.createReadOnly)
    package static val readWrite = new RoleInfo(RolezFactory.eINSTANCE.createReadWrite)
    
    package val BuiltInRole ownRole
    package val ImmutableMap<Field, RoleInfo> fields
    
    new(BuiltInRole ownRole, ImmutableMap<Field, RoleInfo> fields) {
        ownRole.requireNonNull
        fields.requireNonNull
        this.ownRole = ownRole
        this.fields = fields
    }
    
    new(BuiltInRole ownRole) {
        this(ownRole, ImmutableMap.of)
    }
    
    override hashCode() { 31 * (31 + fields.hashCode) + ownRole.hashCode }

    override equals(Object obj) {
        if(this === obj)        return true
        if(obj === null)        return false
        if(class !== obj.class) return false
        
        val other = obj as RoleInfo;
        if(fields  != other.fields ) return false
        if(ownRole != other.ownRole) return false
        true
    }
    
    override toString() {
        "RoleInfo[" + ownRole + ", {" + fields.entrySet.join(", ", [key.name + "=" + value]) + "}]"
    }
}

@Data package class AccessSeq {
    val Var variable
    val Iterable<Field> fieldSeq
    
    def concat(Field it) { new AccessSeq(variable, fieldSeq + #[it]) }
}