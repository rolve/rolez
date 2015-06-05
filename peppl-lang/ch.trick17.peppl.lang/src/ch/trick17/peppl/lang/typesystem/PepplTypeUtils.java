package ch.trick17.peppl.lang.typesystem;

import org.eclipse.xtext.naming.QualifiedName;

import ch.trick17.peppl.lang.peppl.Boolean;
import ch.trick17.peppl.lang.peppl.Char;
import ch.trick17.peppl.lang.peppl.Class;
import ch.trick17.peppl.lang.peppl.Int;
import ch.trick17.peppl.lang.peppl.Null;
import ch.trick17.peppl.lang.peppl.PepplFactory;
import ch.trick17.peppl.lang.peppl.Role;
import ch.trick17.peppl.lang.peppl.RoleType;
import ch.trick17.peppl.lang.peppl.Void;

/**
 * Utility functions for types
 * 
 * @author Michael Faes
 */
public class PepplTypeUtils {
    
    public RoleType roleType(Role role, Class base) {
        RoleType result = PepplFactory.eINSTANCE.createRoleType();
        result.setRole(role);
        result.setBase(base);
        return result;
    }
    
    public Int intType() {
        return PepplFactory.eINSTANCE.createInt();
    }
    
    public Boolean booleanType() {
        return PepplFactory.eINSTANCE.createBoolean();
    }
    
    public Char charType() {
        return PepplFactory.eINSTANCE.createChar();
    }
    
    public Void voidType() {
        return PepplFactory.eINSTANCE.createVoid();
    }
    
    public Null nullType() {
        return PepplFactory.eINSTANCE.createNull();
    }
    
    public QualifiedName objectClassName() {
        return QualifiedName.create("Object");
    }
    
    public QualifiedName stringClassName() {
        return QualifiedName.create("String");
    }
}
