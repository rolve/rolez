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
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;

@SupportedAnnotationTypes({"rolez.annotation.Roleztask","rolez.annotation.Readonly","rolez.annotation.Readwrite"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor {
	
	private Messager messager;
	
	public void init(ProcessingEnvironment env) {
    	messager = env.getMessager();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		return processTaskAnnotations(env);
	}
	
	private boolean processTaskAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Roleztask.class)) {
			if (!(annotatedElement instanceof ExecutableElement)) {
				messager.printMessage(Kind.ERROR, "Only methods can be annotated as Rolez tasks", annotatedElement);
			}
			
			ExecutableElement task = (ExecutableElement) annotatedElement;
			
			String message = "";
			for (VariableElement parameter : task.getParameters()) {
				Annotation roAnnotation = parameter.getAnnotation(Readonly.class);
				Annotation rwAnnotation = parameter.getAnnotation(Readwrite.class);
				
				// Check whether parameters are annotated properly
				if (roAnnotation == null && rwAnnotation == null) {
					messager.printMessage(Kind.ERROR, "Method parameters have to be annotated with either @Readonly or @Readwrite", task);
				}
				
				message += parameter.asType().toString() + "/" + parameter.toString();
				message += ", ";
			}
			messager.printMessage(Kind.WARNING, message, task);
		}
		
		return true;
	}
}
