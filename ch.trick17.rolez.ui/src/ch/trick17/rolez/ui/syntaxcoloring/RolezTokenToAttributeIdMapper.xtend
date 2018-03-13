package ch.trick17.rolez.ui.syntaxcoloring

import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper

class RolezTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
    
    override protected calculateId(String tokenName, int tokenType) { switch(tokenName) {
        case "RULE_DOUBLE":
            RolezHighlightingConfiguration.NUMBER_ID
        case "RULE_DOCUMENTATION":
            RolezHighlightingConfiguration.DOCUMENTATION_ID
        default:
            super.calculateId(tokenName, tokenType)
    }}
}