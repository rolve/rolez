package ch.trick17.rolez.lang.typesystem

import ch.trick17.rolez.lang.rolez.Boolean
import ch.trick17.rolez.lang.rolez.Char
import ch.trick17.rolez.lang.rolez.Double
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.Int
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import ch.trick17.rolez.lang.rolez.Unit
import it.xsemantics.runtime.StringRepresentation

class RolezStringRepresentation extends StringRepresentation {
    
    protected def stringRep(Int _)     { "int" }
    protected def stringRep(Double _)  { "double" }
    protected def stringRep(Boolean _) { "boolean" }
    protected def stringRep(Char _)    { "char" }
    protected def stringRep(Unit _)    { "unit" }
        
    protected def stringRep(RoleType t) {
        // Strangely, we need to call "string" in here, not "stringRep"
        t.role + " " + t.base.string
    }
    
    protected def stringRep(SimpleClassRef r) { r.clazz.name }
    protected def stringRep(GenericClassRef r) {
        r.clazz.name + "[" + r.typeArg.string + "]"
    }
}