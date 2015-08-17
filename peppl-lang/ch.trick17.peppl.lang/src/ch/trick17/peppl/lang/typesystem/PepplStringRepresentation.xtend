package ch.trick17.peppl.lang.typesystem

import it.xsemantics.runtime.StringRepresentation
import ch.trick17.peppl.lang.peppl.RoleType

class PepplStringRepresentation extends StringRepresentation {
        
    protected def String stringRep(RoleType t) {
        t.role + " " + t.base.name
    }
}