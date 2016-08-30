package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.OpAssignment
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.VarRef

class RoleAnalysis {
    
    extension RolezFactory = RolezFactory.eINSTANCE
    
    val Instr code
    val CodeKind codeKind
    
    new(Instr code, CodeKind codeKind) {
        this.code = code
        this.codeKind = codeKind
    }
    
    def dispatch Role dynamicRole(MemberAccess it)  {
        if(isGlobal) createReadOnly else createPure
    }
    
    def dispatch Role dynamicRole(VarRef it) {
        if(variable instanceof Param && codeKind == CodeKind.TASK && !code.mayStartTask)
            (variable.type as RoleType).role
        else
            createPure
    }
    
    def dispatch Role dynamicRole(This _) {
        if(codeKind == CodeKind.CONSTR && !thisMayEscapeTask)
            createReadWrite
        else
            createPure
    }
    
    def dispatch Role dynamicRole(Cast it)          { expr.dynamicRole }
    
    def dispatch Role dynamicRole(Parenthesized it) { expr.dynamicRole }
    
    def dispatch Role dynamicRole(New _)            { createReadWrite }
    
    def dispatch Role dynamicRole(The _)            { createReadOnly }
    
    def dispatch Role dynamicRole(StringLiteral _)  { createReadOnly }
    
    def dispatch Role dynamicRole(Expr _)           { createPure }
    
    def dynamicThisRoleAtExit() {
        if(codeKind != CodeKind.CONSTR)
            throw new IllegalStateException("Only applicable if codeKind is CONSTR")
        if(thisMayEscapeTask) createPure else createReadWrite
    }
    
    private def dispatch boolean isGlobal(MemberAccess it) {
        isFieldAccess && target.isGlobal
    }
    
    private def dispatch boolean isGlobal(The _)  { true }
    
    private def dispatch boolean isGlobal(Expr _) { false }
    
    private def mayStartTask(Instr it) {
        eAllContents.exists[switch(it) {
            New: !constr.isMapped
            MemberAccess: isTaskStart || isMethodInvoke && !method.isMapped
            default: false
        }]
    }
    
    /**
     * Very simple escape analysis to avoid guarding "this" in constructors when some methods are
     * called before val fields have been initialized.
     * 
     * TODO: In addition to this, change code generation s.t. it does not generate the problematic
     * "guard...(this).field = ..." pattern.
     */
    private def thisMayEscapeTask() {
        code.eAllContents.exists[switch(it) {
            Assignment: right.isThis
            MemberAccess case isMethodInvoke: target.isThis || args.exists[isThis]
            New: args.exists[isThis]
            default: false
        }]
    }
    
    private def boolean isThis(Expr it) { switch(it) {
        This: true
        Parenthesized: expr.isThis
        Cast: expr.isThis
        Assignment: op == OpAssignment.ASSIGN && right.isThis
        default: false
    }}
}