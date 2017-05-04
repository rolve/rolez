package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BuiltInRole
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.FieldInitializer
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
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.ThisParam
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.cfg.ControlFlowGraph
import ch.trick17.rolez.validation.dataflow.DataFlowAnalysis
import com.google.common.collect.ImmutableMap
import java.util.HashMap
import java.util.Map.Entry
import javax.inject.Inject
import org.eclipse.xtend.lib.annotations.Data

import static ch.trick17.rolez.generator.CodeKind.*
import static ch.trick17.rolez.rolez.VarKind.*
import static com.google.common.collect.ImmutableMap.copyOf

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension java.util.Objects.requireNonNull

class RoleAnalysis extends DataFlowAnalysis<ImmutableMap<Var, RoleData>> {
    
    static class Provider {
        
        @Inject RolezSystem system
        @Inject RolezUtils utils
    
        def newRoleAnalysis(FieldInitializer code, ControlFlowGraph cfg) {
            newRoleAnalysis(code, cfg, FIELD_INITIALIZER)
        }
        
        def newRoleAnalysis(Constr code, ControlFlowGraph cfg) {
            newRoleAnalysis(code, cfg, CONSTR)
        }
        
        def newRoleAnalysis(Method code, ControlFlowGraph cfg, CodeKind codeKind) {
            if(!#[METHOD, TASK].contains(codeKind))
                throw new IllegalArgumentException
            newRoleAnalysis(code as Executable, cfg, codeKind)
        }
        
        private def newRoleAnalysis(Executable code, ControlFlowGraph cfg, CodeKind codeKind) {
            new RoleAnalysis(code, cfg, codeKind, system, utils)
        }
    }
    
    val extension RolezFactory factory = RolezFactory.eINSTANCE
    val extension RolezUtils utils
    val RolezSystem system
    
    val Executable code
    public val CodeKind codeKind
    
    private new(Executable code, ControlFlowGraph cfg, CodeKind codeKind,
            RolezSystem system, RolezUtils utils) {
        super(cfg, true)
        this.code = code
        this.codeKind = codeKind
        
        this.system = system
        this.utils = utils
        
        if(childTasksMayExist)
            analyze
    }
    
    private def childTasksMayExist() {
        codeKind != TASK || code.all(MemberAccess).exists[
            isTaskStart || isMethodInvoke && method.isAsync
        ]
    }
    
    protected override newFlow() { null } // represents a not-yet-computed flow
    
    protected override entryFlow() {
        val flow = new HashMap
        for(Param p : code.allParams.filter[type instanceof RoleType]) {
            val data =
                if(codeKind == CONSTR && p instanceof ThisParam)
                    RoleData.readWrite
                else if(codeKind == TASK)
                    new RoleData((p.type as RoleType).role.erased)
                else
                    RoleData.pure
            flow.put(p, data)
        }
        copyOf(flow)
    }
    
    protected def dispatch flowThrough(LocalVarDecl d, ImmutableMap<Var, RoleData> in) {
        if(d.initializer !== null) flowThroughAssign(d.variable, d.initializer, in)
        else in
    }
    
    protected def dispatch flowThrough(Assignment a, ImmutableMap<Var, RoleData> in) {
        // TODO: support assignments to final fields (in constructors)
        if(a.left instanceof VarRef) flowThroughAssign((a.left as VarRef).variable, a.right, in)
        else in
    }
    
    private def flowThroughAssign(Var left, Expr right, ImmutableMap<Var, RoleData> in) {
        if(system.varType(left).value instanceof RoleType)
            in.replace(left, roleData(right, in))
        else
            in
    }
    
    protected def dispatch flowThrough(MemberAccess a, ImmutableMap<Var, RoleData> in) {
        if(a.isTaskStart || a.isMethodInvoke && a.method.isAsync)
            // after a task has (possibly) been started, reset all roles to pure
            ImmutableMap.of
        else if((a.isReadAccess || a.isWriteAccess) && a.target.isTracked) {
            // after a read/write access, we know the object is at least readonly/readwrite
            val role = if(a.isWriteAccess) createReadWrite else createReadOnly
            val accessSeq = a.target.asAccessSeq
            val prev = in.get(accessSeq.variable) ?: RoleData.pure
            in.replace(accessSeq.variable, prev.with(role, accessSeq.fieldSeq))
        }
        else
            in
    }
    
    protected def dispatch flowThrough(Instr _, ImmutableMap<Var, RoleData> in) { in }
    
    protected override merge(ImmutableMap<Var, RoleData> in1, ImmutableMap<Var, RoleData> in2) {
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
     * Returns a role data object that is equivalent to the given one, except with the additional
     * information that the object denoted by the given sequence of field accesses (relative to the
     * object the role data applies to) has at least the given role. That is, if the field sequence
     * is empty, the role concerns the object's own role.
     */
    private def RoleData with(RoleData it, BuiltInRole role, Iterable<Field> fieldSeq) {
        if(fieldSeq.isEmpty)
            new RoleData(system.greatestCommonSubrole(ownRole, role) as BuiltInRole, fields)
        else {
            val field = fieldSeq.head
            val prev = fields.get(field) ?: RoleData.pure
            new RoleData(ownRole, fields.replace(field, prev.with(role, fieldSeq.tail)))
        }
    }
    
    private def <K> replace(ImmutableMap<K, RoleData> it, K key, RoleData value) {
        copyOf(new HashMap(it) => [put(key, value)])
    }
    
    private def <K> mergeMaps(ImmutableMap<K, RoleData> it, ImmutableMap<K, RoleData> other) {
        if(equals(other)) return it
        
        val merged = new HashMap
        for(Entry<K, RoleData> entry : entrySet)
            if(other.containsKey(entry.key))
                merged.put(entry.key, merge(entry.value, other.get(entry.key)))
        copyOf(merged)
    }
    
    private def RoleData merge(RoleData it, RoleData other) {
        val role = system.leastCommonSuperrole(ownRole, other.ownRole) as BuiltInRole
        new RoleData(role, mergeMaps(fields, other.fields))
    }
    
    private def RoleData roleData(Expr it, ImmutableMap<Var, RoleData> roles) { switch(it) {
        case isTracked: {
            val accessSeq = asAccessSeq
            var roleData = roles.get(accessSeq.variable) ?: RoleData.pure
            for(Field f : accessSeq.fieldSeq)
                roleData = roleData.fields.get(f) ?: RoleData.pure
            roleData
        }
        Cast, Parenthesized       : roleData(expr, roles)
        Assignment                : roleData(right, roles)
        New                       : RoleData.readWrite
        The, StringLiteral        : RoleData.readOnly
        MemberAccess case isGlobal: RoleData.readOnly
        default                   : RoleData.pure
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
        if(childTasksMayExist)
            roleData(it, cfg.nodeOf(it).inFlow ?: ImmutableMap.of).ownRole
        else {
            // Special case: if we know that no child task may run in parallel to statements
            // in this piece of code, no guarding is required. So just return the static role.
            val type = system.type(it).value
            if(type instanceof RoleType)
                type.role
            else
                RolezFactory.eINSTANCE.createPure
            // TODO: Why are non-roletype instances passed to this method?...
        }
    }
}

package class RoleData {
    
    /* reuse the same "simple" instances */
    package static val pure      = new RoleData(RolezFactory.eINSTANCE.createPure)
    package static val readOnly  = new RoleData(RolezFactory.eINSTANCE.createReadOnly)
    package static val readWrite = new RoleData(RolezFactory.eINSTANCE.createReadWrite)
    
    package val BuiltInRole ownRole
    package val ImmutableMap<Field, RoleData> fields
    
    new(BuiltInRole ownRole, ImmutableMap<Field, RoleData> fields) {
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
        
        val other = obj as RoleData;
        if(fields  != other.fields ) return false
        if(ownRole != other.ownRole) return false
        true
    }
    
    override toString() {
        "RoleData[" + ownRole + ", {" + fields.entrySet.join(", ", [key.name + "=" + value]) + "}]"
    }
}

@Data package class AccessSeq {
    val Var variable
    val Iterable<Field> fieldSeq
    
    def concat(Field it) { new AccessSeq(variable, fieldSeq + #[it]) }
}