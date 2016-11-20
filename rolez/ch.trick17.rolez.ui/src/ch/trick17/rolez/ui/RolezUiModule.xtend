package ch.trick17.rolez.ui

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class RolezUiModule extends AbstractRolezUiModule {
    
    def Class<? extends IHighlightingConfiguration> bindIHighlightConfiguration() {
        RolezHighlightingConfiguration;
    }
    
    def Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
        RolezSemanticHighlightCalculator;
    }    
}
