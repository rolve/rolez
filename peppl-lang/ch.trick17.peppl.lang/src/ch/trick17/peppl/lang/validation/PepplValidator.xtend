/*
 * generated by Xtext
 */
package ch.trick17.peppl.lang.validation

import ch.trick17.peppl.lang.peppl.Class
import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.peppl.PepplPackage.Literals
import ch.trick17.peppl.lang.typesystem.PepplSystem
import ch.trick17.peppl.lang.typesystem.PepplTypeUtils
import ch.trick17.peppl.lang.typesystem.validation.PepplSystemValidator
import javax.inject.Inject
import org.eclipse.xtext.validation.Check
import ch.trick17.peppl.lang.peppl.Field
import ch.trick17.peppl.lang.peppl.Variable

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class PepplValidator extends PepplSystemValidator {

    public static val INVALID_NAME = "invalid name"
    public static val OBJECT_CLASS_NOT_DEFINED = "object class not defined"
    public static val DUPLICATE_CLASS = "duplicate class"
    public static val DUPLICATE_METHOD = "duplicate method"
    public static val DUPLICATE_FIELD = "duplicate field"
    public static val DUPLICATE_VARIABLE = "duplicate variable"
    public static val MISSING_OVERRIDE = "missing override"
    public static val INCORRECT_OVERRIDE = "incorrect override"
    public static val INCOMPATIBLE_RETURN_TYPE = "incompatible return type"

    @Inject private extension PepplSystem
    @Inject private extension PepplTypeUtils

	@Check
    def checkClassNameStartsWithCapital(Class clazz) {
        if(!Character.isUpperCase(clazz.name.charAt(0)))
            warning("Name should start with a capital",
                Literals.NAMED__NAME, INVALID_NAME)
    }
	
	@Check
	def checkObjectExists(Class c) {
	    if(c.superclass == null && findClass(objectClassName, c) == null)
	       error("Object class is not defined",
	           Literals.NAMED__NAME,  OBJECT_CLASS_NOT_DEFINED)
	}
	
	@Check
	def checkNoDuplicateClasses(Class c) {
	    val matching = c.enclosingProgram.classes.filter[name.equals(c.name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate class " + c.name, Literals.NAMED__NAME, DUPLICATE_CLASS)
	}
	
    @Check
    def checkNoDuplicateMethods(Method m) {
        val matching = m.enclosingClass.methods.filter[equalSignature(it, m)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate method " + m.name + "("+ m.params.join(",") + ")",
               Literals.NAMED__NAME, DUPLICATE_METHOD)
    }
    
    @Check
    def checkNoDuplicateFields(Field f) {
        val matching = f.enclosingClass.fields.filter[name.equals(f.name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate field " + f.name, Literals.NAMED__NAME, DUPLICATE_FIELD)
    }
    
    @Check
    def checkNoDuplicateVars(Variable v) {
        val matching = v.enclosingMethod.variables.filter[name.equals(v.name)]
        if(matching.size < 1)
           throw new AssertionError
        if(matching.size > 1)
           error("Duplicate variable " + v.name, Literals.NAMED__NAME, DUPLICATE_VARIABLE)
    }
	
	@Check
	def checkOverrides(Method m) {
	    val superMethods = m.enclosingClass.actualSuperclass
	           .allMembers.filter(Method)
        val matching = superMethods.filter[equalSignature(it, m)]
	    
	    if(matching.size > 0) {
	        if(m.overriding) {
                for(match : matching)
                    if(subtype(envFor(m), m.type, match.type).failed)
                        error("The return type is incompatible with " + match,
                            Literals.TYPED__TYPE, INCOMPATIBLE_RETURN_TYPE)
            }
            else
                error("Method must be declared with \"override\" since it
                        actually overrides a superclass method",
                    Literals.NAMED__NAME, MISSING_OVERRIDE)
        }
        else if(m.overriding)
           error("Method must override a superclass method",
               Literals.NAMED__NAME, INCORRECT_OVERRIDE)
	}
}