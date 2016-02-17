package ch.trick17.rolez.scoping

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Argumented
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import ch.trick17.rolez.validation.RolezValidator
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

import static org.eclipse.xtext.scoping.Scopes.scopeFor

import static extension ch.trick17.rolez.generic.Parameterized.parameterizedWith

class RolezScopeProvider extends AbstractDeclarativeScopeProvider {
    
    public static val AMBIGUOUS_CALL = "ambiguous call"
    
    @Inject extension RolezExtensions
    @Inject extension JavaMapper
    @Inject extension RolezFactory
    @Inject RolezSystem system
    @Inject RolezValidator validator
    @Inject RolezUtils utils
    
    def scope_MemberAccess_member(MemberAccess it, EReference ref) {
        val targetType = system.type(utils.createEnv(it), target).value
        val memberName = crossRefText(ref)
        
        if(targetType instanceof RoleType) {
            val fields = targetType.base.parameterizedClass.allMembers.filter(Field)
                .filter[f | f.name == memberName]
            if(args.isEmpty && roleArgs.isEmpty && !isTaskStart && !fields.isEmpty)
                scopeFor(fields)
            else {
                val candidates = targetType.base.parameterizedClass.allMembers.filter(Method)
                    .filter[m | m.name == memberName && (!isTaskStart || m.isTask)]
                    .map[m |
                        val roleArgs = system.inferRoleArgs(utils.createEnv(it), it, m)
                        if(roleArgs.size == m.roleParams.size) m.parameterizedWith(roleArgs)
                        else null
                    ].filterNull
                val maxSpecific = maxSpecific(candidates, it).toList
                
                if(maxSpecific.size <= 1)
                    scopeFor(maxSpecific)
                else {
                    validator.delayedError("Method invoke is ambiguous", it, ref, AMBIGUOUS_CALL)
                    scopeFor(maxSpecific)
                }
            }
        }
        else
            IScope.NULLSCOPE;
    }
    
    def scope_New_constr(New it, EReference ref) {
        val clazz = classRef.parameterizedClass
        if(clazz instanceof NormalClass) {
            val maxSpecific = maxSpecific(clazz.constrs, it).toList
            
            if(maxSpecific.size <= 1)
                scopeFor(maxSpecific, [QualifiedName.create("new")], IScope.NULLSCOPE)
            else {
                validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
                scopeFor(maxSpecific)
            }
        }
        else
            IScope.NULLSCOPE
    }
    
    def scope_SuperConstrCall_constr(SuperConstrCall it, EReference ref) {
        val maxSpecific = maxSpecific(enclosingClass.parameterizedSuperclass.constrs, it).toList
        
        if(maxSpecific.size > 1)
            validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
        
        // IMPROVE: Better error message if no constructor is found
        scopeFor(maxSpecific, [QualifiedName.create("super")], IScope.NULLSCOPE)
    }
    
    def scope_Method_superMethod(Method it, EReference ref) {
        val allMethods = enclosingClass.parameterizedSuperclass.allMembers.filter(Method)
        val matching = allMethods.filter[m | utils.equalSignatureWithoutRoles(m, it)]
            .filter[m | m.roleParams.size >= roleParams.size]
            .map[m |
                // Parameterize the super method with references to this method's role parameters
                // IMPROVE: This could be problematic if the super method has more role params than
                // this method, since the parameterization is position-based
                val roleArgs = roleParams
                    .toMap[m.roleParams.get(roleParamIndex)]
                    .mapValues[p | createRoleParamRef => [param = p]]
                m.parameterizedWith(roleArgs)
            ].toList
        
        // IMPROVE: Better error message if no matching method found
        scopeFor(matching, [QualifiedName.create("override")], IScope.NULLSCOPE)
    }
    
    def scope_Field_jvmField(Field it, EReference ref) {
        if(enclosingClass.jvmClass == null)
            return IScope.NULLSCOPE
        
        // TODO: Allow to map to inherited fields, like with methods
        val candidates = enclosingClass.jvmClass.declaredFields.filter[f |
            f.simpleName == name && f.visibility == JvmVisibility.PUBLIC
                && f.isStatic == enclosingClass.isSingleton
        ]
        
        // IMPROVE: Better error message if no field is found
        scopeFor(candidates, [QualifiedName.create("mapped")], IScope.NULLSCOPE)
    }
    
    def scope_Method_jvmMethod(Method it, EReference ref) {
        if(enclosingClass.jvmClass == null)
            return IScope.NULLSCOPE
        
        val filter = [JvmOperation m |
            val javaParams = m.parameters.iterator
            m.simpleName == name
                && m.visibility == JvmVisibility.PUBLIC
                && m.isStatic == enclosingClass.isSingleton
                && params.size == m.parameters.size
                && params.forall[type.mapsTo(javaParams.next.parameterType)]
        ]
        val candidates = enclosingClass.jvmClass.allMethods(filter)
        
        // IMPROVE: Better error message if no method is found
        scopeFor(candidates, [QualifiedName.create("mapped")], IScope.NULLSCOPE)
    }
    
    private def Iterable<JvmOperation> allMethods(JvmDeclaredType it, (JvmOperation) => boolean filter) {
        declaredOperations.filter(filter)
            + ((extendedClass?.type as JvmDeclaredType)?.allMethods(filter) ?: #[])
    }
    
    def scope_Constr_jvmConstr(Constr it, EReference ref) {
        if(enclosingClass.jvmClass == null)
            return IScope.NULLSCOPE
        
        val candidates = enclosingClass.jvmClass.declaredConstructors.filter[c |
            val javaParams = c.parameters.iterator
            c.visibility == JvmVisibility.PUBLIC
                && params.size == c.parameters.size
                && params.forall[type.mapsTo(javaParams.next.parameterType)]
        ]
        
        // IMPROVE: Better error message if no constructor is found
        scopeFor(candidates, [QualifiedName.create("mapped")], IScope.NULLSCOPE)
    }
    
    def IScope scope_VarRef_variable(VarRef it, EReference eRef) {
        scopeFor(utils.varsAbove(eContainer, it))
    }
    
    /**
     * Finds the maximally specific methods/constructors for the given argument list, following
     * <a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2">
     * http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2
     * </a>.
     */
    private def maxSpecific(Iterable<? extends Executable> candidates, Argumented args) {
        val applicable = candidates.filter[
            system.validArgsSucceeded(utils.createEnv(args), args, it)
        ].toList
        
        applicable.filter[p |
            applicable.forall[p === it || !strictlyMoreSpecificThan(p)]
        ]
    }
    
    private def strictlyMoreSpecificThan(Executable target, Executable other) {
        target.moreSpecificThan(other) && !other.moreSpecificThan(target)
    }
    
    private def moreSpecificThan(Executable target, Executable other) {
        // Assume both targets have the same number of parameters
        val i = other.params.iterator
        target.params.forall[system.subtypeSucceeded(utils.createEnv(target), it.type, i.next.type)]
    }
    
    private def crossRefText(EObject it, EReference ref) {
        val proxy = eGet(ref, false) as InternalEObject
        val fragment = proxy.eProxyURI.fragment
        val node = (eResource as LazyLinkingResource).encoder.decode(eResource, fragment).third
        node.text.trim
    }
}
