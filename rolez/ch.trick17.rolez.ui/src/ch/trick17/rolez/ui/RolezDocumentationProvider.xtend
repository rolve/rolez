package ch.trick17.rolez.ui

import org.eclipse.xtext.documentation.impl.MultiLineCommentDocumentationProvider

class RolezDocumentationProvider extends MultiLineCommentDocumentationProvider {
    
    override injectProperties(MultiLineCommentProviderProperties properties) {
        ruleName = "DOCUMENTATION"
        super.injectProperties(properties)
    }
}