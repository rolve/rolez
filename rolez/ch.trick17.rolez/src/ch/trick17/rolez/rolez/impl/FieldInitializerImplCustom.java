package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.Expr;
import ch.trick17.rolez.rolez.Instr;
import ch.trick17.rolez.rolez.Param;

public class FieldInitializerImplCustom extends FieldInitializerImpl {
    
    @Override
    public boolean isMapped() {
        return false;
    }
    
    @Override
    public void setCode(Instr code) {
        if(!(code instanceof Expr))
            throw new IllegalArgumentException("field initializer code needs to be an expr");
        super.setCode(code);
    }
    
    @Override
    public Expr getExpr() {
        return (Expr) super.getCode();
    }
    
    @Override
    public Iterable<Param> getAllParams() {
        return getParams();
    }
}
