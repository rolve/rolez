package ch.trick17.rolez.typesystem

import org.eclipse.xsemantics.runtime.RuleFailedException
import org.eclipse.xsemantics.runtime.validation.XsemanticsValidatorFilter

/**
 * Filters out failures that are due to unresolved cross references.
 */
class RolezValidatorFilter extends XsemanticsValidatorFilter {
    
    override filterRuleFailedExceptions(RuleFailedException e) {
        val exceptions = super.filterRuleFailedExceptions(e)
        exceptions.filter[!filterErrorInformation.isEmpty]
    }
    
    override filterErrorInformation(RuleFailedException e) {
        super.filterErrorInformation(e).filter[
            source.eCrossReferences.forall[!eIsProxy]
        ]
    }
}