package ch.trick17.rolez.lang.desugar

import java.lang.reflect.Method
import java.util.List
import java.util.Optional
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EObjectEList
import org.eclipse.xtext.util.SimpleCache

abstract class AbstractDeclarativeDesugarer implements IDesugarer {
    
    val List<Method> ruleMethods
    
    val methodsForType = new SimpleCache<Class<?>, List<Method>>([type |
        ruleMethods.filter[parameterTypes.get(0).isAssignableFrom(type)].toList
    ])
    
    new() {
        ruleMethods = class.methods.filter[!isBridge && isAnnotationPresent(Rule)].toList
        ruleMethods.forEach[
            if(parameterTypes.size != 1)
                throw new AssertionError("Invalid @Rule method " + it + ": must have exactly one parameter")
            if(!EObject.isAssignableFrom(parameterTypes.get(0)))
                throw new AssertionError("Invalid @Rule method " + it + ": parameter type must be a subtype of EObject")
            if(returnType != void && !EObject.isAssignableFrom(returnType))
                throw new AssertionError("Invalid @Rule method " + it + ": return type must be void or a subtype of EObject")
        ]
    }
    
    override desugar(Resource it) {
        var prevContents = emptyList
        
        // Repeat as long as contents change
        while(allContents.toList != prevContents) {
            prevContents = allContents.toList
            for(var i = 0; i < contents.size; i++) {
                val replacement = contents.get(i).desugar
                if(replacement.isPresent)
                    contents.set(i, replacement.get)
                contents.get(i).desugarChildren
            }
        }
    }
    
    private def Optional<EObject> desugar(EObject it) {
        for(m : methodsForType.get(class))
            if(m.returnType == void)
                m.invoke(this, it)
            else {
                val replacement = m.invoke(this, it) as EObject
                if(replacement == null)
                    throw new AssertionError("A @Rule method must not return null")
                return Optional.of(replacement)
            }
        Optional.empty
    }
    
    private def void desugarChildren(EObject it) {
        for(ref : eClass.EAllContainments) {
            val value = eGet(ref)
            if(value instanceof EObject) {
                val replacement = value.desugar
                if(replacement.isPresent)
                    eSet(ref, replacement.get)
                (eGet(ref) as EObject).desugarChildren
            }
            else if(value instanceof EObjectEList<?>) {
                for(var i = 0; i < value.size; i++) {
                    val child = value.get(i) as EObject
                    val replacement = child.desugar
                    if(replacement.isPresent)
                        (value as EObjectEList<EObject>).set(i, replacement.get)
                    (value.get(i) as EObject).desugarChildren
                }
            }
        }
    }
}