package ch.trick17.rolez.ui.syntaxcoloring

import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ide.editor.syntaxcoloring.HighlightingStyles

class RolezTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
    
    override protected calculateId(String tokenName, int tokenType) {
        if("RULE_DOUBLE".equals(tokenName))
            HighlightingStyles.NUMBER_ID
        else
            super.calculateId(tokenName, tokenType)
    }
}