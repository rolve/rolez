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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;

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
	 * method parameters have a role declared and are able to be guarded.
	 * @param env
	 */
	private void processTaskAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Roleztask.class)) {
			
			if (!(annotatedElement instanceof ExecutableElement)) {
				Message message = new Message(Kind.ERROR, "Only methods can be annotated as Rolez tasks", annotatedElement);
				message.print(messager);
			}
			
			ExecutableElement task = (ExecutableElement) annotatedElement;
			
			boolean hasAsTaskParameter = false;
			for (VariableElement parameter : task.getParameters()) {
				
				if(parameter.toString().equals("$asTask") && isOfBooleanType(parameter)) {
					hasAsTaskParameter = true;
					continue;
				}
				
				checkClassIsGuarded(parameter);
				
				Annotation roAnnotation = parameter.getAnnotation(Readonly.class);
				Annotation rwAnnotation = parameter.getAnnotation(Readwrite.class);
				
				// Check whether parameter is annotated properly
				if (roAnnotation == null && rwAnnotation == null) {
					Message noRoleDeclared = new Message(Kind.ERROR, "Method parameters have to be annotated with either @Readonly or @Readwrite", task);
					noRoleDeclared.print(messager);
				}
				
				// TODO: What happens when parameters are annotated?

			}
			
			if (!hasAsTaskParameter) {
				Message message = new Message(Kind.ERROR, "A task needs a parameter $asTask and it needs to be a final boolean.", task);
				message.print(messager);
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
	
	//TODO: This method doesn't work since final modifiers on method parameters are erased after compilation
	//      and therefore element.getModifiers() cannot find the final modifier.
	@SuppressWarnings(value = {"unused"})
	private boolean hasFinalModifier(Element element) {
		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.size() > 1) return false;
		return modifiers.contains(Modifier.FINAL);
	}
	
	private boolean isOfBooleanType(Element element) {
		return element.asType().toString().equals("boolean");
	}
}
