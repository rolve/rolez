package ch.trick17.rolez.scoping

import ch.trick17.rolez.rolez.Argumented
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Executable
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.GenericClassRef
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.SuperConstrCall
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.typesystem.RolezSystem
import ch.trick17.rolez.validation.JavaMapper
import ch.trick17.rolez.validation.RolezValidator
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.InternalEObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.resource.EObjectDescription
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider
import org.eclipse.xtext.xbase.lib.Functions.Function1

import static ch.trick17.rolez.RolezUtils.*
import static org.eclipse.xtext.scoping.Scopes.scopeFor

import static extension ch.trick17.rolez.RolezExtensions.*
import static extension ch.trick17.rolez.generic.Parameterized.parameterizedWith
import static extension com.google.common.collect.Maps.toMap

class RolezScopeProvider extends AbstractDeclarativeScopeProvider {
    
    public static val AMBIGUOUS_CALL = "ambiguous call"
    
    @Inject extension JavaMapper
    @Inject extension RolezFactory
    @Inject RolezSystem system
    @Inject RolezValidator validator
    
    def scope_GenericClassRef_clazz(GenericClassRef it, EReference ref) {
        delegateGetScope(it, ref).map(NormalClass, [c |
            val resolvedClass = EcoreUtil.resolve(c, it) as NormalClass
            resolvedClass.parameterizedWith(#{resolvedClass.typeParam -> typeArg})
        ])
    }
    
    def scope_RoleType_slice(RoleType it, EReference ref) {
        val clazz = base.clazz
        switch(clazz) {
            NormalClass: scopeFor(clazz.slices)
            default    : IScope.NULLSCOPE
        }
    }
    
    def scope_Slicing_slice(Slicing it, EReference ref) {
        val targetType = system.type(target).value
        if(targetType instanceof RoleType) {
            val clazz = targetType.base.clazz
            switch(clazz) {
                NormalClass: scopeFor(clazz.slices)
                default    : IScope.NULLSCOPE
            }
        }
        else
            IScope.NULLSCOPE
    }
    
    def scope_MemberAccess_member(MemberAccess it, EReference ref) {
        val targetType = system.type(target).value
        val memberName = crossRefText(ref)
        
        if(targetType instanceof RoleType) {
            val members = if(targetType.isSliced) targetType.slice.members
                          else targetType.base.clazz.allMembers
            val fields = members.filter(Field).filter[f | f.name == memberName]
            if(args.isEmpty && roleArgs.isEmpty && !isTaskStart && !forceInvoke && !fields.isEmpty)
                scopeFor(fields)
            else {
                val candidates = members.filter(Method)
                    .filter[m | m.name == memberName && (!isTaskStart || m.isTask)]
                    .map[m |
                        val roleArgs = system.inferRoleArgs(it, m)
                        if(roleArgs.size == m.roleParams.size) m.parameterizedWith(roleArgs)
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
        val clazz = classRef.clazz
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
        val maxSpecific = maxSpecific(enclosingClass.superclass.constrs, it).toList
        
        if(maxSpecific.size > 1)
            validator.delayedError("Constructor call is ambiguous", it, ref, AMBIGUOUS_CALL)
        
        // IMPROVE: Better error message if no constructor is found
        scopeFor(maxSpecific, [QualifiedName.create("super")], IScope.NULLSCOPE)
    }
    
    def scope_Method_superMethod(Method it, EReference ref) {
        if(enclosingClass.superclass === null)
            return IScope.NULLSCOPE
        
        val allMethods = enclosingClass.superclass.allMembers.filter(Method)
        val matching = allMethods.filter[m | equalErasedSignature(m, it)]
            .filter[m | m.roleParams.size >= roleParams.size]
            .map[m |
                // Parameterize the super method with references to this method's role parameters
                // IMPROVE: This could be problematic if the super method has more role params than
                // this method, since the parameterization is position-based
                val roleArgs = m.roleParams.toMap[p |
                    // If the super method has more role params than this, the args corresponding to
                    // the "excess" role params are just their upper bounds.
                    // TODO: Does this make any sense?
                    if(p.roleParamIndex >= roleParams.size)
                        p.upperBound
                    else
                        createRoleParamRef => [r | r.param = roleParams.get(p.roleParamIndex)]
                ]
                m.parameterizedWith(roleArgs)
            ].toList
        
        // IMPROVE: Better error message if no matching method found
        scopeFor(matching, [QualifiedName.create("override")], IScope.NULLSCOPE)
    }
    
    def scope_Field_jvmField(Field it, EReference ref) {
        if(enclosingClass.jvmClass === null)
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
        if(enclosingClass.jvmClass === null)
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
        if(enclosingClass.jvmClass === null)
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
        scopeFor(varsAbove(eContainer, it))
    }
    
    /**
     * Finds the maximally specific methods/constructors for the given argument list, following
     * <a href="http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2">
     * http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.12.2
     * </a>.
     */
    private def maxSpecific(Iterable<? extends Executable> candidates, Argumented args) {
        val applicable = candidates.filter[
            system.validArgsSucceeded(args, it)
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
        target.params.forall[system.subtypeSucceeded(it.type, i.next.type)]
    }
    
    // IMPROVE: can replace the following with LinkingHelper.getCrossRefNodeAsString()?
    private def crossRefText(EObject it, EReference ref) {
        val proxy = eGet(ref, false) as InternalEObject
        val fragment = proxy.eProxyURI.fragment
        val node = (eResource as LazyLinkingResource).encoder.decode(eResource, fragment).third
        node.text.trim
    }
    
    private def <T extends EObject> map(IScope original, Class<T> expectedClass, Function1<T, T> transformation) {
        new IScope {
            override getAllElements()                     { original.allElements.map[transform] }
            override getElements(QualifiedName name)      { original.getElements(name).map[transform] }
            override getElements(EObject object)          { original.getElements(object).map[transform] }
            override getSingleElement(QualifiedName name) { original.getSingleElement(name)?.transform }
            override getSingleElement(EObject object)     { original.getSingleElement(object)?.transform }
            
            private def transform(IEObjectDescription desc) {
                new EObjectDescription(
                    desc.qualifiedName,
                    transformation.apply(expectedClass.cast(desc.EObjectOrProxy)),
                    desc.userDataKeys.toMap[desc.getUserData(it)]
                )
            }
        }
    }
}
