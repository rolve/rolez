package ch.trick17.rolez.lang.desugar

import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import javax.inject.Inject

class DesugaringLazyLinkingResource extends LazyLinkingResource {
    
    @Inject IDesugarer desugarer
    
    override protected doLinking() {
        super.doLinking()
        desugarer.desugar(this)
    }
}