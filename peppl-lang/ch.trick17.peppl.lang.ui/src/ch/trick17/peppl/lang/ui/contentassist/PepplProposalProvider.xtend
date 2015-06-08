/*
 * generated by Xtext
 */
package ch.trick17.peppl.lang.ui.contentassist

import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.Member
import ch.trick17.peppl.lang.peppl.MemberAccess
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.RoleType
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import org.eclipse.xtext.Assignment
import ch.trick17.peppl.lang.peppl.Expression

class PepplProposalProvider extends AbstractPepplProposalProvider {

    @Inject private extension PepplSystem system
    @Inject private extension PepplTypeUtils __

    override completeFieldSelector_Field(EObject model, Assignment a, ContentAssistContext context,
        ICompletionProposalAcceptor acceptor) {
        completeMemberAccess(model, context, acceptor, Field)
    }

    override completeMethodSelector_Method(EObject model, Assignment a, ContentAssistContext context,
        ICompletionProposalAcceptor acceptor) {
        completeMemberAccess(model, context, acceptor, Method)
    }

    private def completeMemberAccess(EObject model, ContentAssistContext context, ICompletionProposalAcceptor acceptor,
        Class<? extends Member> kind) {
        var Expression target

        // Find target. Why the heck is this so complicated???
        if (model instanceof Expression) {
            target = model;
            if (model instanceof MemberAccess) {
                target = model.target
                if (model.eContainer instanceof MemberAccess)
                    target = (model.eContainer as MemberAccess).target as MemberAccess
            }
        }

        if (target != null) {
            val targetType = system.type(envFor(model), target).value
            if (targetType instanceof RoleType) {
                val factory = getProposalFactory("MemberAccess", context)
                val scope = Scopes.scopeFor(targetType.base.allMembers.filter(kind))
                for (e : scope.allElements) {
                    if (!acceptor.canAcceptMoreProposals)
                        return;
                    acceptor.accept(factory.apply(e))
                }
            }
        }
    }
}
