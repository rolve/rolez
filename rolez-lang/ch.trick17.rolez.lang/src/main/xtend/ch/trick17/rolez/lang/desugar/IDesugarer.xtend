package ch.trick17.rolez.lang.desugar

import org.eclipse.emf.ecore.resource.Resource

interface IDesugarer {
    
    def void desugar(Resource it)
    
}