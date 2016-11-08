package ch.trick17.rolez.ui.contentassist

import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.typesystem.RolezSystem
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor

import static ch.trick17.rolez.RolezUtils.*

import static extension ch.trick17.rolez.RolezExtensions.*

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class RolezProposalProvider extends AbstractRolezProposalProvider {
    
    @Inject RolezSystem system
    
    override completeMemberAccess_Member(EObject model, Assignment a, ContentAssistContext context,
        ICompletionProposalAcceptor acceptor) {
        var Expr target

        // Find target. Why the heck is this so complicated???
        if (model instanceof Expr) {
            target = model;
            if (model instanceof MemberAccess) {
                target = model.target
                if (model.eContainer instanceof MemberAccess)
                    target = (model.eContainer as MemberAccess).target as MemberAccess
            }
        }
        
        if (target != null) {
            val targetType = system.type(createEnv(target), target).value
            if (targetType instanceof RoleType) {
                val factory = getProposalFactory("MemberAccess", context)
                val scope = Scopes.scopeFor(targetType.base.clazz.allMembers)
                for (e : scope.allElements) {
                    if (!acceptor.canAcceptMoreProposals)
                        return;
                    acceptor.accept(factory.apply(e))
                }
            }
        }
    }
}
