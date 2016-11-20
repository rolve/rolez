package ch.trick17.rolez.ui

import org.eclipse.swt.graphics.RGB
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor

class RolezHighlightingConfiguration extends DefaultHighlightingConfiguration {
    
    public static val VARIABLE_ID = "variable"
    public static val FIELD_ID = "field"
    
    override configure(IHighlightingConfigurationAcceptor acceptor) {
        super.configure(acceptor)
        acceptor.acceptDefaultHighlighting(VARIABLE_ID, "Variable", variableTextStyle)
        acceptor.acceptDefaultHighlighting(FIELD_ID, "Field", fieldTextStyle)
    }
    
    def fieldTextStyle() { defaultTextStyle.copy => [color = new RGB(0, 0, 192)] }
    
    def variableTextStyle() { defaultTextStyle.copy => [color = new RGB(106, 62, 62)] }
}
