package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.rolez.Cast
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.ParameterizedBody
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Start
import ch.trick17.rolez.rolez.StringLiteral
import ch.trick17.rolez.rolez.Task
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.VarRef
import javax.inject.Inject

import static ch.trick17.rolez.rolez.Role.*

class RoleAnalysis {
    
    @Inject extension RolezExtensions
    
    def dispatch Role dynamicRole(MemberAccess it)  {
        if(isGlobal) READONLY else PURE
    }
    
    def dispatch Role dynamicRole(VarRef it) {
        if(variable instanceof Param && enclosingBody instanceof Task && !enclosingBody.mayStartTask)
            (variable.type as RoleType).role
        else
            PURE
    }
    
    def dispatch Role dynamicRole(This it) {
        if(enclosingBody instanceof Constr && !enclosingBody.mayStartTask)
            READWRITE
        else
            PURE
    }
    
    def dispatch Role dynamicRole(Cast it)          { expr.dynamicRole }
    
    def dispatch Role dynamicRole(Parenthesized it) { expr.dynamicRole }
    
    def dispatch Role dynamicRole(New _)            { READWRITE }
    
    def dispatch Role dynamicRole(The _)            { READONLY }
    
    def dispatch Role dynamicRole(StringLiteral _)  { READONLY }
    
    def dispatch Role dynamicRole(Expr _)           { PURE }
    
    def dynamicThisRoleAtExit(Constr it) {
        if(mayStartTask) PURE else READWRITE
    }
    
    private def dispatch boolean isGlobal(MemberAccess it) {
        isFieldAccess && target.isGlobal
    }
    
    private def dispatch boolean isGlobal(The _)  { true }
    
    private def dispatch boolean isGlobal(Expr _) { false }
    
    private def boolean mayStartTask(ParameterizedBody it) {
        body.eAllContents.exists[
            it instanceof Start
                || (it instanceof New && !(it as New).constr.isMapped)
                || (it instanceof MemberAccess
                    && (it as MemberAccess).isMethodInvoke
                    && !(it as MemberAccess).method.isMapped)
        ]
    }
}