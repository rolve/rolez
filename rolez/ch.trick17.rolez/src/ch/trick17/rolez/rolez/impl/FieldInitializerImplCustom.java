package ch.trick17.rolez.rolez.impl;

import ch.trick17.rolez.rolez.Expr;
import ch.trick17.rolez.rolez.Instr;

public class FieldInitializerImplCustom extends FieldInitializerImpl {
    
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
}
