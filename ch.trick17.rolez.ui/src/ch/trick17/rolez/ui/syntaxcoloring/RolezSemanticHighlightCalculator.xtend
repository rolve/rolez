package ch.trick17.rolez.ui.syntaxcoloring

import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.rolez.RolezPackage
import ch.trick17.rolez.rolez.Super
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Var
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.CancelIndicator

import static ch.trick17.rolez.ui.syntaxcoloring.RolezHighlightingConfiguration.*
import static org.eclipse.xtext.nodemodel.util.NodeModelUtils.*
import static org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration.*
import org.apache.log4j.Logger

class RolezSemanticHighlightCalculator implements ISemanticHighlightingCalculator {
    
    static val logger = Logger.getLogger(RolezSemanticHighlightCalculator)
    static val rolez = RolezPackage.eINSTANCE
    
    override provideHighlightingFor(XtextResource res, IHighlightedPositionAcceptor acceptor, CancelIndicator canceler) {
        if(res?.parseResult === null) return
        
        val iter = EcoreUtil.getAllContents(res, true)
        while(iter.hasNext) {
            val object = iter.next
            try {
                switch(object) {
                    // local variables
                    Var: {
                        val node = findNodesForFeature(object, rolez.named_Name).head
                        if(node !== null) // skip synthetic vars like the "this" parameter
                            acceptor.addPosition(node.offset, node.length, VARIABLE_ID)
                    }
                    This, Super: {} // skip this and super (they're VarRefs too)
                    Ref case object.isVarRef: {
                        val node = findActualNodeFor(object)
                        acceptor.addPosition(node.offset, node.length, VARIABLE_ID)
                    }
                    // fields
                    Field: {
                        val node = findNodesForFeature(object, rolez.named_Name).head
                        acceptor.addPosition(node.offset, node.length, FIELD_ID)
                    }
                    MemberAccess case object.isFieldAccess: {
                        val node = findNodesForFeature(object, rolez.memberAccess_Member).head
                        acceptor.addPosition(node.offset, node.length, FIELD_ID)
                    }
                    // methods (which may have the same name as a keyword)
                    Method: {
                        val node = findNodesForFeature(object, rolez.named_Name).head
                        acceptor.addPosition(node.offset, node.length, DEFAULT_ID)
                    }
                    MemberAccess case object.isMethodInvoke || object.isTaskStart: {
                        val node = findNodesForFeature(object, rolez.memberAccess_Member).head
                        acceptor.addPosition(node.offset, node.length, DEFAULT_ID)
                    }
                }
            } catch(Exception e) {
                // some thing above may not work if there are errors in the resource
                logger.warn("Could not provide highlighting for " + object, e)
            }
        }
    }
}
