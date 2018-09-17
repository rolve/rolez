package ch.trick17.rolez.ui.syntaxcoloring

import org.eclipse.swt.graphics.RGB
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor

class RolezHighlightingConfiguration extends DefaultHighlightingConfiguration {
    
    public static val DOCUMENTATION_ID = "documentation"
    public static val VARIABLE_ID = "variable"
    public static val FIELD_ID = "field"
    public static val TASK_PARAM_ID = "taskParam"
    
    override configure(IHighlightingConfigurationAcceptor acceptor) {
        super.configure(acceptor)
        acceptor.acceptDefaultHighlighting(DOCUMENTATION_ID, "Documentation", documentationTextStyle)
        acceptor.acceptDefaultHighlighting(VARIABLE_ID, "Variable", variableTextStyle)
        acceptor.acceptDefaultHighlighting(FIELD_ID, "Field", fieldTextStyle)
        acceptor.acceptDefaultHighlighting(TASK_PARAM_ID, "Task Parameter", taskParamTextStyle)
    }
    
    def documentationTextStyle() { commentTextStyle.copy => [color = new RGB(63, 95, 191)]}
    
    def fieldTextStyle() { defaultTextStyle.copy => [color = new RGB(0, 0, 192)] }
    
    def variableTextStyle() { defaultTextStyle.copy => [color = new RGB(106, 62, 62)] }
    
    def taskParamTextStyle() { defaultTextStyle.copy => [backgroundColor = new RGB(230, 230, 0)] }
}
