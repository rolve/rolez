package ch.trick17.rolez.ui

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Constr
import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Method
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.common.types.xtext.ui.JdtHoverDocumentationProvider
import org.eclipse.xtext.documentation.impl.MultiLineCommentDocumentationProvider

class RolezDocumentationProvider extends MultiLineCommentDocumentationProvider {
    
    @Inject
    JdtHoverDocumentationProvider jdtDocProvider
    
    override getDocumentation(EObject object) {
        super.getDocumentation(object) ?: mappedDocumentation(object)
    }
    
    private def mappedDocumentation(EObject it) { switch(it) {
        Class  case isMapped: jdtDocProvider.getDocumentation(jvmClass)
        Field  case isMapped: jdtDocProvider.getDocumentation(jvmField)
        Method case isMapped: jdtDocProvider.getDocumentation(jvmMethod)
        Constr case isMapped: jdtDocProvider.getDocumentation(jvmConstr) // cannot hover of "new" yet...
    }}
    
    override injectProperties(MultiLineCommentProviderProperties properties) {
        ruleName = "DOCUMENTATION"
        super.injectProperties(properties)
    }
}