package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.LocalVar
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.VarRef

import static ch.trick17.rolez.generator.CodeKind.*
import static ch.trick17.rolez.rolez.VarKind.*

class RoleAnalysis {
    
    extension RolezFactory = RolezFactory.eINSTANCE
    
    val Instr code
    public val CodeKind codeKind
    
    new(Instr code, CodeKind codeKind) {
        this.code = code
        this.codeKind = codeKind
    }
    
    def dispatch Role dynamicRole(MemberAccess it)  {
        if(isGlobal) createReadOnly else createPure
    }
    
    def dispatch Role dynamicRole(VarRef it) {
        // TODO: dataflow analysis
        if(!code.mayStartTask) {
            val variable = variable
            switch(variable) {
                Param case codeKind == TASK: (variable.type as RoleType).role
                LocalVar case variable.kind == VAL: variable.decl.initializer.dynamicRole
                default: createPure
            }
        }
        else
            createPure
    }
    
    def dispatch Role dynamicRole(This _) {
        if(codeKind == CONSTR && !code.mayStartTask)
            createReadWrite
        else
            createPure
    }
    
    private def mayStartTask(Instr it) {
        eAllContents.filter(MemberAccess).exists[isTaskStart || isMethodInvoke && method.isAsync]
    }
    
    def dispatch Role dynamicRole(Cast it)          { expr.dynamicRole }
    
    def dispatch Role dynamicRole(Parenthesized it) { expr.dynamicRole }
    
    def dispatch Role dynamicRole(New _)            { createReadWrite }
    
    def dispatch Role dynamicRole(The _)            { createReadOnly }
    
    def dispatch Role dynamicRole(StringLiteral _)  { createReadOnly }
    
    def dispatch Role dynamicRole(Expr _)           { createPure }
    
    private def dispatch boolean isGlobal(MemberAccess it) {
        isFieldAccess && target.isGlobal
    }
    
    private def dispatch boolean isGlobal(The _)  { true }
    
    private def dispatch boolean isGlobal(Expr _) { false }
}