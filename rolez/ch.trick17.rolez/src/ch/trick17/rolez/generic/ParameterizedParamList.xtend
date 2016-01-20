package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Param
import ch.trick17.rolez.rolez.ParameterizedBody
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.List
import java.util.Map

package class ParameterizedParamList extends ParameterizedEList<Param, ParameterizedBody> {
    
    new(List<Param> list, ParameterizedBody eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(list, eContainer, typeArgs, roleArgs)
    }
    
    private new(ParameterizedParamList base, int from, int to) {
        super(base, from, to)
    }
    
    override parameterize(Param e) { new ParameterizedParam(e, eContainer, typeArgs, roleArgs) }
    
    override subList(int fromIndex, int toIndex) {
        new ParameterizedParamList(this, fromIndex, toIndex)
    }
}