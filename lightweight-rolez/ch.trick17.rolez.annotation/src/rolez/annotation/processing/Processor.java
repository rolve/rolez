package rolez.annotation.processing;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;
import rolez.annotation.Guarded;

@SupportedAnnotationTypes({"rolez.annotation.Roleztask","rolez.annotation.Readonly","rolez.annotation.Readwrite","rolez.annotation.Guarded"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor {
	
	private Messager messager;
	
	public void init(ProcessingEnvironment env) {
    	messager = env.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		processTaskAnnotations(env);
		processGuardedAnnotations(env);
		return true;
	}
	
	/**
	 * Processes classes annotated with the <code>@Guareded</code> annotation, which indicates that
	 * this class will inherit from the Guarded class of the Rolez runtime library.
	 * @param env
	 */
	private void processGuardedAnnotations(RoundEnvironment env) {
		//TODO: Process classes annotated with Guarded if necessary.
	}
	
	/**
	 * Processes the methods that are annotated with <code>@Roleztask</code> and checks whether all 
	 * method parameters have a role declared.
	 * @param env
	 */
	private void processTaskAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Roleztask.class)) {
			if (!(annotatedElement instanceof ExecutableElement)) {
				Message message = new Message(Kind.ERROR, "Only methods can be annotated as Rolez tasks", annotatedElement);
				message.print(messager);
			}
			
			ExecutableElement task = (ExecutableElement) annotatedElement;
			
			for (VariableElement parameter : task.getParameters()) {
				Annotation roAnnotation = parameter.getAnnotation(Readonly.class);
				Annotation rwAnnotation = parameter.getAnnotation(Readwrite.class);
				
				// Check whether parameter is annotated properly
				if (roAnnotation == null && rwAnnotation == null) {
					Message message = new Message(Kind.ERROR, "Method parameters have to be annotated with either @Readonly or @Readwrite", task);
					message.print(messager);
				}
				
				checkClassIsGuarded(parameter);
				
				// TODO: What happens when parameters are annotated?

			}
		}
	}
	
	private void checkClassIsGuarded(VariableElement parameter) {
		TypeMirror parameterType = parameter.asType();
		ParameterTypeVisitor visitor = new ParameterTypeVisitor();
		Message message = parameterType.accept(visitor, null);
		if (message != null) {
			message.setElement(parameter);
			message.print(messager);
		}
	}
	
	private class ParameterTypeVisitor implements TypeVisitor<Message,Void> {
		
		@Override
		public Message visit(TypeMirror t, Void p) {
			return null;
		}

		@Override
		public Message visit(TypeMirror t) {
			return null;
		}

		@Override
		public Message visitPrimitive(PrimitiveType t, Void p) {
			return null;
		}

		@Override
		public Message visitNull(NullType t, Void p) {
			return null;
		}

		@Override
		public Message visitArray(ArrayType t, Void p) {
			return null;
		}

		@Override
		public Message visitDeclared(DeclaredType t, Void p) {
			Element declaredType = t.asElement();
			Guarded annotation = declaredType.getAnnotation(Guarded.class);
			if (annotation == null) {
				return new Message(Kind.ERROR, "Parameter has to be a guarded Type (i.e. annotated with @Guarded).");
			}
			
			return null;
		}

		@Override
		public Message visitError(ErrorType t, Void p) {
			return null;
		}

		@Override
		public Message visitTypeVariable(TypeVariable t, Void p) {
			return null;
		}

		@Override
		public Message visitWildcard(WildcardType t, Void p) {
			return null;
		}

		@Override
		public Message visitExecutable(ExecutableType t, Void p) {
			return null;
		}

		@Override
		public Message visitNoType(NoType t, Void p) {
			return null;
		}

		@Override
		public Message visitUnknown(TypeMirror t, Void p) {
			return null;
		}

		@Override
		public Message visitUnion(UnionType t, Void p) {
			return null;
		}
		
	}
}
