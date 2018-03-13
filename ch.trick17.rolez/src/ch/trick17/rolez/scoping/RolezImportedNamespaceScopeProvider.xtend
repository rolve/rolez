package ch.trick17.rolez.scoping

import ch.trick17.rolez.rolez.Program
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.scoping.impl.ImportedNamespaceAwareLocalScopeProvider

/**
 * Imports all types from the same package and from rolez.lang
 */
class RolezImportedNamespaceScopeProvider extends ImportedNamespaceAwareLocalScopeProvider {
    
    override internalGetImportedNamespaceResolvers(EObject context, boolean ignoreCase) {
        val result = super.internalGetImportedNamespaceResolvers(context, ignoreCase)
        if(context instanceof Program) {
            if(context.name !== null)
                result += createImportedNamespaceResolver(context.name + ".*", ignoreCase)
            result += createImportedNamespaceResolver("rolez.lang.*", ignoreCase)
        }
        result
    }
}