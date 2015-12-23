package ch.trick17.rolez.desugar

import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.util.Triple

interface IDesugarer {
    
    /**
     * Desugars the given resource and returns a list of references to be linked.
     */
    def Iterable<Triple<EObject, EReference, String>> desugar(Resource it)
}