package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.Instr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
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

class RoleAnalysis {
    
    extension RolezFactory = RolezFactory.eINSTANCE
    
    val Instr code
    val Container container
    
    new(Instr code, Container container) {
        this.code = code
        this.container = container
    }
    
    def dispatch Role dynamicRole(MemberAccess it)  {
        if(isGlobal) createReadOnly else createPure
    }
    
    def dispatch Role dynamicRole(VarRef it) {
        if(variable instanceof Param && enclosingExecutable instanceof Method
                && enclosingMethod.isTask && !enclosingExecutable.body.mayStartTask)
            (variable.type as RoleType).role
        else
            createPure
    }
    
    def dispatch Role dynamicRole(This it) {
        if(enclosingExecutable instanceof Constr && !enclosingExecutable.body.mayStartTask)
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
        if(container != Container.CONSTR)
            throw new IllegalStateException("Only applicable if container is CONSTR")
        if(code.mayStartTask) createPure else createReadWrite
    }
    
    private def dispatch boolean isGlobal(MemberAccess it) {
        isFieldAccess && target.isGlobal
    }
    
    private def dispatch boolean isGlobal(The _)  { true }
    
    private def dispatch boolean isGlobal(Expr _) { false }
    
    private def boolean mayStartTask(Instr it) {
        eAllContents.exists[
            it instanceof New && !(it as New).constr.isMapped
                || it instanceof MemberAccess && (it as MemberAccess).isTaskStart
                || it instanceof MemberAccess
                    && (it as MemberAccess).isMethodInvoke
                    && !(it as MemberAccess).method.isMapped
        ]
    }
    
    static enum Container {
        CONSTR, METHOD, TASK, FIELD_INITIALIZER
    }
}