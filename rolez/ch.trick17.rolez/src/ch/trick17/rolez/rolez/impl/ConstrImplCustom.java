package ch.trick17.rolez.rolez.impl;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

import ch.trick17.rolez.rolez.Block;
import ch.trick17.rolez.rolez.Instr;
import ch.trick17.rolez.rolez.Param;

public class ConstrImplCustom extends ConstrImpl {
    
    @Override
    public boolean isMapped() {
        return getJvmConstr() != null;
    }
    
    @Override
    public void setCode(Instr code) {
        if(!(code instanceof Block))
            throw new IllegalArgumentException("constr code needs to be a block");
        super.setCode(code);
    }
    
    @Override
    public Block getBody() {
        return (Block) getCode();
    }
    
    @Override
    public Iterable<Param> getAllParams() {
        return concat(asList(getThisParam()), getParams());
    }
}
