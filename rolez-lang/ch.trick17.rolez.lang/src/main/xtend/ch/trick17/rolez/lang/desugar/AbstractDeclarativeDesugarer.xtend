package ch.trick17.rolez.lang.desugar

import java.lang.reflect.Method
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.util.SimpleCache
import org.eclipse.emf.ecore.resource.Resource

abstract class AbstractDeclarativeDesugarer implements IDesugarer {
    
    val List<Method> ruleMethods
    
    val methodsForType = new SimpleCache<Class<?>, List<Method>>([type |
        ruleMethods.filter[parameterTypes.get(0).isAssignableFrom(type)].toList
    ])
    
    new() {
        ruleMethods = class.methods.filter[!isBridge && isAnnotationPresent(Rule)].toList
        ruleMethods.forEach[
            if(parameterTypes.size != 1 || !EObject.isAssignableFrom(parameterTypes.get(0)))
                throw new AssertionError("Invalid @Rule method: " + it)
        ]
    }
    
    override desugar(Resource it) {
        var contents = allContents.toList
        var prevContents = emptyList
        
        // Repeat as long as contents change
        while(contents != prevContents) {
            for(object : allContents.toIterable)
                methodsForType.get(object.class).forEach[invoke(this, object)]
            prevContents = contents
            contents = allContents.toList
        }
    }
}