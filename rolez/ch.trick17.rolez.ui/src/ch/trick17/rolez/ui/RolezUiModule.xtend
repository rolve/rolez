package ch.trick17.rolez.ui

import ch.trick17.rolez.ui.syntaxcoloring.RolezHighlightingConfiguration
import ch.trick17.rolez.ui.syntaxcoloring.RolezSemanticHighlightCalculator
import ch.trick17.rolez.ui.syntaxcoloring.RolezTokenToAttributeIdMapper
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ide.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.ui.editor.hover.IEObjectHoverProvider
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration
import org.eclipse.xtext.documentation.IEObjectDocumentationProvider

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class RolezUiModule extends AbstractRolezUiModule {
    
    def Class<? extends IHighlightingConfiguration> bindIHighlightConfiguration() {
        RolezHighlightingConfiguration
    }
    
    def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntrlTokenToAttributeIdMapper() {
        RolezTokenToAttributeIdMapper
    }
    
    def Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
        RolezSemanticHighlightCalculator
    }
    
    def Class<? extends IEObjectHoverProvider> bindIEObjectHoverProvider() {
        RolezHoverProvider
    }
    
    def Class<? extends IEObjectDocumentationProvider> bindIEObjectDocumentationProvider() {
        RolezDocumentationProvider
    }
}
