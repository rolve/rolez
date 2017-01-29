package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezUtils
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
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParamRef
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
import java.util.Map
import java.util.Map.Entry
import javax.inject.Inject

import static ch.trick17.rolez.generator.CodeKind.*
import static ch.trick17.rolez.rolez.VarKind.*
import static com.google.common.collect.ImmutableMap.copyOf

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension com.google.common.collect.Maps.toMap

class RoleAnalysis extends DataFlowAnalysis<ImmutableMap<Var, BuiltInRole>> {
    
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
        
        analyze
    }
    
    protected override newFlow()   { ImmutableMap.of }
    
    protected override entryFlow() {
        copyOf(code.allParams.filter[type instanceof RoleType].toMap[
            if(codeKind == CONSTR && it instanceof ThisParam)
                createReadWrite
            else if(codeKind == TASK)
                (type as RoleType).role.erased
            else
                createPure
        ])
    }
    
    private def erased(Role it) { switch(it) {
        BuiltInRole : it
        RoleParamRef: param.upperBound  
    }}
    
    protected def dispatch flowThrough(MemberAccess a, ImmutableMap<Var, BuiltInRole> in) {
        if(a.isTaskStart || a.isMethodInvoke && a.method.isAsync)
            // after a task has (potentially) been started, reset all roles to pure
            copyOf(in.mapValues[createPure])
        else if(a.target instanceof VarRef && a.isWriteAccess)
            // after a successful write access, we know the object is readwrite
            in.with((a.target as VarRef).variable, createReadWrite)
        else if(a.target instanceof VarRef && a.isReadAccess) {
            // after a successful read access, we know the object is at least readonly
            val variable = (a.target as VarRef).variable
            in.with(variable, system.greatestCommonSubrole(in.get(variable), createReadOnly) as BuiltInRole)
        }
        else
            in
    }
    
    protected def dispatch flowThrough(LocalVarDecl d, ImmutableMap<Var, BuiltInRole> in) {
        in.with(d.variable, d.initializer?.dynamicRole(in) ?: createPure)
    }
    
    protected def dispatch flowThrough(Assignment a, ImmutableMap<Var, BuiltInRole> in) {
        if(a.left instanceof VarRef && (a.left as VarRef).variable.type instanceof RoleType)
            in.with((a.left as VarRef).variable, a.right.dynamicRole(in))
        else
            in
    }
    
    protected def dispatch flowThrough(Instr _, ImmutableMap<Var, BuiltInRole> in) { in }
    
    protected override merge(ImmutableMap<Var, BuiltInRole> in1,
            ImmutableMap<Var, BuiltInRole> in2) {
        val merged = new HashMap(in1)
        for(Entry<Var, BuiltInRole> entry : in2.entrySet) {
            val mergedRole = system.leastCommonSuperrole(entry.value,
                    merged.get(entry.key) ?: createReadWrite) as BuiltInRole
            merged.put(entry.key, mergedRole)
        }
        copyOf(merged)
    }
    
    private def BuiltInRole dynamicRole(Expr it, Map<Var, BuiltInRole> roles) { switch(it) {
        New                       : createReadWrite
        The, StringLiteral        : createReadOnly
        MemberAccess case isGlobal: createReadOnly
        VarRef                    : roles.get(variable)
        Cast, Parenthesized       : expr.dynamicRole(roles)
        Assignment                : right.dynamicRole(roles)
        default: createPure
    }}
    
    private def isReadAccess(MemberAccess it) {
        isFieldAccess && field.kind == VAR || isSliceGet || isArrayGet || isVectorBuilderGet
    }
    
    private def isWriteAccess(MemberAccess it) {
        isFieldWrite || isSliceSet || isArraySet || isVectorBuilderSet
    }
    
    private def boolean isGlobal(Expr it) { switch(it) {
        The: true
        MemberAccess case isFieldAccess && target.isGlobal: true
        default: false
    }}
    
    private def with(Map<Var, BuiltInRole> map, Var variable, BuiltInRole role) {
        copyOf(new HashMap(map) => [put(variable, role)])
    }
    
    /* Interface of the analysis */
    
    def dynamicRole(Expr it) { switch(it) {
        VarRef : cfg.nodeOf(it).inFlow.get(variable)
        default: dynamicRole(ImmutableMap.of)
    }}
}