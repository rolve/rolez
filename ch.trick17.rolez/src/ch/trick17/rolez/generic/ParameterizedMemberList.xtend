package ch.trick17.rolez.generic

import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Member
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RoleParam
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.rolez.TypeParam
import java.util.List
import java.util.Map

package class ParameterizedMemberList extends ParameterizedEList<Member, NormalClass>  {
    
    new(List<Member> list, NormalClass eContainer, Map<TypeParam, Type> typeArgs, Map<RoleParam, Role> roleArgs) {
        super(list, eContainer, typeArgs, roleArgs)
    }
    
    private new(ParameterizedMemberList base, int from, int to) {
        super(base, from, to)
    }
    
    override parameterize(Member e) { switch(e) {
        Field : new ParameterizedField (e, eContainer, typeArgs, roleArgs)
        Method: new ParameterizedMethod(e, eContainer, typeArgs, roleArgs)
    }}
    
    override subList(int fromIndex, int toIndex) {
        new ParameterizedMemberList(this, fromIndex, toIndex)
    }
}