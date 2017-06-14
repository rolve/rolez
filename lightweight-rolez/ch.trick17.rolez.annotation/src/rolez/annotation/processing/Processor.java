package rolez.annotation.processing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

import rolez.annotation.Roleztask;

@SupportedAnnotationTypes("rolez.annotation.Roleztask")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor {
	
	private Messager messager;
	
	public void init(ProcessingEnvironment env) {
    	messager = env.getMessager();
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Roleztask.class)) {
			
			if (!(annotatedElement instanceof ExecutableElement)) {
				messager.printMessage(Kind.ERROR, "Only Methods can use this Annotation.", annotatedElement);
			}
			ExecutableElement execElem = (ExecutableElement)annotatedElement;
			
			// Use sets, so double declarations inside readonly and readwrite are ignored
			HashSet<String> readonly = new HashSet<String>(Arrays.asList(execElem.getAnnotation(Roleztask.class).readonly()));
			HashSet<String> readwrite = new HashSet<String>(Arrays.asList(execElem.getAnnotation(Roleztask.class).readwrite()));
			
			if (hasDoubleDeclaration(readonly, readwrite)) {
				messager.printMessage(Kind.ERROR, "Parameters can't be declared as readonly AND readwrite.", annotatedElement);
			}
			HashSet<String> annotationParams = new HashSet<String>();
			
			String message = "ro: ";
			
			for (String s : readonly) {
				annotationParams.add(s);
				message += s;
				message += " ";
			}
			
			message += "\nrw: ";
			
			for (String s : readwrite) {
				annotationParams.add(s);
				message += s;
				message += " ";
			}

			message += "\nparams: ";
			
			checkMethodParameters(execElem.getParameters(), annotationParams, annotatedElement);
			
			for (VariableElement e : execElem.getParameters()) {				
				message += " ";
				message += e.asType().toString();
				message += " ";
				message += e.toString();
			}
			
			messager.printMessage(Kind.NOTE, message, execElem);
		}

		return true;
	}
	
	private void checkMethodParameters(List<? extends VariableElement> methodParams, Set<String> annotationParams, Element annotatedElement) {
		for (VariableElement e : methodParams) {
			if (!annotationParams.contains(e.toString())) {
				messager.printMessage(Kind.ERROR, "The Annotation must contain all method parameters (" + e.toString() + " is not included).", annotatedElement);
			}
		}

		for (String s : annotationParams) {
			boolean paramFound = false;
			for (VariableElement e : methodParams) {
				if (e.toString().equals(s)) {
					paramFound = true;
					break;
				}
			}
			if (!paramFound) {
				messager.printMessage(Kind.ERROR, "The method paramets must contain all annotation parameters (" + s + " is not included).", annotatedElement);
			}
		}
	}
	
	private boolean hasDoubleDeclaration(HashSet<String> ro, HashSet<String> rw) {
		for (String ros : ro) {
			for (String rws : rw) {
				if (ros.equals(rws)) return true;
			}
		}
		return false;
	}
	

}
