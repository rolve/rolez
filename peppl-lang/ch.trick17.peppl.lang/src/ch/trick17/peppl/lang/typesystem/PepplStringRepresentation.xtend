package ch.trick17.peppl.lang.typesystem

import ch.trick17.peppl.lang.peppl.GenericClassRef
import ch.trick17.peppl.lang.peppl.RoleType
import it.xsemantics.runtime.StringRepresentation
import ch.trick17.peppl.lang.peppl.SimpleClassRef
import ch.trick17.peppl.lang.peppl.Int
import ch.trick17.peppl.lang.peppl.Boolean
import ch.trick17.peppl.lang.peppl.Char
import ch.trick17.peppl.lang.peppl.Unit

class PepplStringRepresentation extends StringRepresentation {
    
    protected def stringRep(Int _)     { "int" }
    protected def stringRep(Boolean _) { "boolean" }
    protected def stringRep(Char _)    { "char" }
    protected def stringRep(Unit _)    { "unit" }
        
    protected def stringRep(RoleType t) {
        // Strangely, we need to call "string" in here, not "stringRep"
        t.role + " " + t.base.string
    }
    
    protected def stringRep(SimpleClassRef r) {
        r.clazz.name
    }
    
    protected def stringRep(GenericClassRef r) {
        r.clazz.name + "[" + r.typeArg.string + "]"
    }
}