package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.List
import java.util.Map

package class ParameterizedConstrList extends ParameterizedEList<Constr, NormalClass>  {
    
    new(List<Constr> list, NormalClass eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(list, eContainer, typeArgs, roleArgs)
    }
    
    private new(ParameterizedConstrList base, int from, int to) {
        super(base, from, to)
    }
    
    override parameterize(Constr e) { new ParameterizedConstr(e, eContainer, typeArgs, roleArgs) }
    
    override subList(int fromIndex, int toIndex) {
        new ParameterizedConstrList(this, fromIndex, toIndex)
    }
}