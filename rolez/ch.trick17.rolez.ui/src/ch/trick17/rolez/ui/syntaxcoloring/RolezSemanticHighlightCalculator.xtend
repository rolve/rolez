package ch.trick17.rolez.ui.syntaxcoloring

import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.RolezPackage
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.rolez.VarRef
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.CancelIndicator

import static ch.trick17.rolez.ui.syntaxcoloring.RolezHighlightingConfiguration.*
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.*

class RolezSemanticHighlightCalculator implements ISemanticHighlightingCalculator {
    
    static val rolez = RolezPackage.eINSTANCE
    
    override provideHighlightingFor(XtextResource res, IHighlightedPositionAcceptor acceptor, CancelIndicator canceler) {
        if(res?.parseResult == null) return
        
        val iter = EcoreUtil.getAllContents(res, true)
        while(iter.hasNext) {
            val object = iter.next
            switch(object) {
                Var: {
                    val node = findNodesForFeature(object, rolez.named_Name).head
                    acceptor.addPosition(node.offset, node.length, VARIABLE_ID)
                }
                VarRef: {
                    val node = findActualNodeFor(object)
                    acceptor.addPosition(node.offset, node.length, VARIABLE_ID)
                }
                Field: {
                    val node = findNodesForFeature(object, rolez.named_Name).head
                    acceptor.addPosition(node.offset, node.length, FIELD_ID)
                }
                MemberAccess case object.isFieldAccess: {
                    val node = findNodesForFeature(object, rolez.memberAccess_Member).head
                    acceptor.addPosition(node.offset, node.length, FIELD_ID)
                }
            }
        }
    }
}
