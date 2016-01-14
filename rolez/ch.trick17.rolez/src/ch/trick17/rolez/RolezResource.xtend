package ch.trick17.rolez

import ch.trick17.rolez.desugar.DesugaringLazyLinkingResource
import org.eclipse.emf.ecore.EObject
import ch.trick17.rolez.generic.ParameterizedEObject

class RolezResource extends DesugaringLazyLinkingResource {
    
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