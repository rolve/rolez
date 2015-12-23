package ch.trick17.rolez

import ch.trick17.rolez.rolez.SuperConstrCall
import it.xsemantics.runtime.StringRepresentation

class StringRepresentationFix extends StringRepresentation {

    /**
     * This custom implementation prevents a CyclicLinkingException when
     * resolving the target of SuperConstrCalls
     */
    protected def String stringRep(SuperConstrCall it) {
        "super" + args.string
    }
}