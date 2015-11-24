package ch.trick17.rolez.lang.typesystem

import it.xsemantics.runtime.RuleFailedException
import it.xsemantics.runtime.validation.XsemanticsValidatorFilter

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