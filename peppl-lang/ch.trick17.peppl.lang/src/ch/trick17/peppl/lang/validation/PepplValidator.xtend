/*
 * generated by Xtext
 */
package ch.trick17.peppl.lang.validation

import org.eclipse.xtext.validation.Check
import ch.trick17.peppl.lang.peppl.PepplPackage
import ch.trick17.peppl.lang.typesystem.validation.PepplSystemValidator
import ch.trick17.peppl.lang.peppl.Class

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class PepplValidator extends PepplSystemValidator {

  public static val INVALID_NAME = "invalid Name"

	@Check
	def checkGreetingStartsWithCapital(Class clazz) {
		if (!Character.isUpperCase(clazz.name.charAt(0))) {
			warning("Name should start with a capital", 
					PepplPackage.Literals.NAMED__NAME,
					INVALID_NAME)
		}
	}
}
