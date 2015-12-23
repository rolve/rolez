package ch.trick17.rolez.scoping

import ch.trick17.rolez.rolez.Program
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.scoping.impl.ImportNormalizer
import org.eclipse.xtext.scoping.impl.ImportedNamespaceAwareLocalScopeProvider

import static extension org.eclipse.xtext.EcoreUtil2.getContainerOfType

class RolezImportedNamespaceScopeProvider extends ImportedNamespaceAwareLocalScopeProvider {
    
    // automatically import all types from the package we are in
    override List<ImportNormalizer> internalGetImportedNamespaceResolvers(
            EObject context, boolean ignoreCase) {
        val ns = context.getContainerOfType(Program).name + ".*";
        val result = super.internalGetImportedNamespaceResolvers(context, ignoreCase)
        result += createImportedNamespaceResolver(ns, ignoreCase)
        result += createImportedNamespaceResolver("rolez.lang.*", ignoreCase)
        result
    }
}