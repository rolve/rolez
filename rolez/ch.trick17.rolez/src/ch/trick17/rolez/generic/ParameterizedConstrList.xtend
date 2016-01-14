package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.List
import java.util.Map

package class ParameterizedConstrList extends ParameterizedEList<Constr, NormalClass>  {
    
    new(List<Constr> list, NormalClass eContainer, Map<TypeParam, Type> typeArgs) {
        super(list, eContainer, typeArgs)
    }
    
    private new(ParameterizedConstrList base, int from, int to) {
        super(base, from, to)
    }
    
    override parameterize(Constr e) { new ParameterizedConstr(e, eContainer, typeArgs) }
    
    override subList(int fromIndex, int toIndex) {
        new ParameterizedConstrList(this, fromIndex, toIndex)
    }
}