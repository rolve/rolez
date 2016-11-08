package ch.trick17.rolez

import ch.trick17.rolez.desugar.DesugaringLazyLinkingResource
import ch.trick17.rolez.generic.ParameterizedEObject
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.typesystem.RolezSystem
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.naming.IQualifiedNameProvider

class RolezResource extends DesugaringLazyLinkingResource {
    
    @Inject RolezSystem system
    @Inject IQualifiedNameProvider nameProvider
    
    /**
     * Allows {@link ParameterizedEObject}s to get access to the {@link RolezSystem}, without the
     * need to carry around references to it themselves.
     */
    def rolezSystem() { system }
    
    /**
     * Allows {@link Class}es to get access to the {@link IQualifiedNameProvider} for their
     * <code>toString</code> method.
     */
    def qualifiedNameProvider() { nameProvider }
    
    /**
     * Enables crosslinking of {@link ParameterizedEObject}s in the Eclipse editor
     */
    override getURIFragment(EObject object) {
        if(object instanceof ParameterizedEObject<?>)
            super.getURIFragment(object.genericEObject)
        else
            super.getURIFragment(object)
    }
}