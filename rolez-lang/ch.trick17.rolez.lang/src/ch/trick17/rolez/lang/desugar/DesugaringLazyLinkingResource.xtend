package ch.trick17.rolez.lang.desugar

import java.util.ArrayList
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.linking.lazy.LazyLinkingResource
import org.eclipse.xtext.linking.lazy.SyntheticLinkingSupport
import org.eclipse.xtext.util.Triple

class DesugaringLazyLinkingResource extends LazyLinkingResource {
    
    @Inject IDesugarer desugarer
    @Inject extension SyntheticLinkingSupport
    
    val desugarRefs = new ArrayList<Triple<EObject, EReference, String>>
    
    override protected doLinking() {
        super.doLinking()
        val newRefs = desugarer.desugar(this)
        
        for(ref : newRefs)
            ref.first.createAndSetProxy(ref.second, ref.third)
        
        for(var i = desugarRefs.iterator; i.hasNext;) {
            val ref = i.next
            if(ref.first.eResource === this)
                ref.first.createAndSetProxy(ref.second, ref.third)
            else
                i.remove
        }
        
        desugarRefs += newRefs
    }
}