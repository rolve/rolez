package ch.trick17.rolez.ui.labeling

import ch.trick17.rolez.rolez.Field
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.TypeParam
import ch.trick17.rolez.rolez.Var
import ch.trick17.rolez.typesystem.RolezSystem
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider

import static ch.trick17.rolez.rolez.VarKind.VAL
import static org.eclipse.jdt.ui.JavaElementImageDescriptor.*
import static org.eclipse.xtext.common.types.JvmVisibility.PUBLIC

class RolezLabelProvider extends DefaultEObjectLabelProvider {
    
    @Inject RolezSystem system
    @Inject RolezImages images

	@Inject
	new(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}
    
    /* Text */
    
    def String text(Var it)
        '''«name»: «varType»'''
    
    private def varType(Var it) {
        val result = system.varType(null, it)
        if(result.failed)
            "?"
        else
            result.value.toString
    }
	
	def text(EObject it) { toString }
    
    /* Images */
    
    def image(NormalClass it) {
        images.forClass(PUBLIC, 0)
    }
    
    def image(SingletonClass it) {
        images.forSingletonClass
    }
    
    def image(TypeParam it) {
        images.forTypeParameter(0)
    }
    
    def image(Field it) {
        images.forField(PUBLIC, if(kind == VAL) FINAL else 0)
    }
    
    def image(Method it) {
        images.forOperation(PUBLIC, if(isTask) TRANSIENT else 0) // abuse the "transient T" for tasks
    }
    
    def image(Var it) {
        images.forLocalVariable(if(kind == VAL) FINAL else 0)
    }
}
