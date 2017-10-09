package rolez.annotation.processing;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Checked;
import rolez.annotation.Pure;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Roleztask;
import rolez.checked.lang.Role;

@SupportedAnnotationTypes({"rolez.annotation.Roleztask",
						   "rolez.annotation.Pure", 
						   "rolez.annotation.Readonly",
						   "rolez.annotation.Readwrite",
						   "rolez.annotation.Checked"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor {
	
	private Messager messager;
	private Types types;
	private Elements elements;
	
	// Whitelist that contains java standard classes which are allowed to be used in rolez tasks
	public static final String[] WHITELIST = new String[] {
		"java.lang.String",
		"java.lang.Integer",
		"java.lang.Boolean",
		"java.lang.Byte",
		"java.lang.Character",
		"java.lang.Short",
		"java.lang.Long",
		"java.lang.Double",
		"java.lang.Float"
	};
	
	public static final String[] PRIMITIVE_TYPES = new String[] {
		"int",
		"boolean",
		"byte",
		"char",
		"short",
		"long",
		"double",
		"float"
	};
	
	public void init(ProcessingEnvironment env) {
	    super.init(env);
    	messager = env.getMessager();
    	types = env.getTypeUtils();
    	elements = env.getElementUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
		processTaskAnnotations(env);
		processCheckedAnnotations(env);
		
		return true;
	}
	
	/**
	 * Processes classes annotated with the <code>@Guareded</code> annotation, which indicates that
	 * this class will inherit from the Checked class of the Rolez runtime library.
	 * @param env
	 */
	private void processCheckedAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Checked.class)) {
			
			// Should actually never happen since @Checked has only ElementType as target
			if (!(annotatedElement instanceof TypeElement)) {
				Message message = new Message(Kind.ERROR, "The @Checked annotation is only legal on classes.", annotatedElement);
				message.print(messager);
			}
			
			TypeElement typeElement = (TypeElement)annotatedElement;
			TypeMirror type = typeElement.asType();
			TypeMirror supertype = getSupertype(type);
			TypeElement superTypeElement = elements.getTypeElement(supertype.toString());
			
			checkForObjectOrCheckedSupertype(typeElement, supertype, superTypeElement);
			
			enforceCheckedAnnotation(typeElement);
			
			checkOverridingTasksAreAnnotated(typeElement); 
		}
	}

	private void checkForObjectOrCheckedSupertype(TypeElement typeElement, TypeMirror supertype, TypeElement superTypeElement) {
		if(!(typeEqualsObject(supertype) || hasCheckedAnnotation(superTypeElement)) ) {
			Message message = new Message(Kind.ERROR, "The @Checked annotation is only legal on classes inheriting from @Checked"
					+ " annotated classes or classes inheriting from java.lang.Object.", typeElement);
			message.print(messager);
		}
	}

	private void enforceCheckedAnnotation(TypeElement typeElement) {
		List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
		boolean foundChecked = false;
		for (AnnotationMirror annotation : annotations) {				
			if (annotation.toString().equals("@" + Checked.class.getName())) {
				foundChecked = true;
				break;
			}
		}
		
		if (!foundChecked) {
			Message message = new Message(Kind.ERROR, "The @Checked annotation has to be present on this class.", typeElement);
			message.print(messager);
		}
	}
	
	private boolean typeEqualsObject(TypeMirror type) {
		return type.toString().equals(Object.class.getName());
	}
	
	private boolean hasCheckedAnnotation(TypeElement typeElement) {
		return typeElement.getAnnotation(Checked.class) != null; 
	}
	
	/**
	 * Processes the methods that are annotated with <code>@Roleztask</code> and checks whether all 
	 * method parameters have a role declared and are able to be checked.
	 * @param env
	 */
	private void processTaskAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Roleztask.class)) {
			
			if (!(annotatedElement instanceof ExecutableElement)) {
				Message message = new Message(Kind.ERROR, "Only methods can be annotated as Roleztask.", annotatedElement);
				message.print(messager);
			}
			
			if (!hasCheckedAnnotation((TypeElement)annotatedElement.getEnclosingElement())) {
				Message message = new Message(Kind.ERROR, "Tasks can only be defined in classes annotated with @Checked.", annotatedElement);
				message.print(messager);
			}
			
			ExecutableElement task = (ExecutableElement) annotatedElement;
			processTask(task);
		}
	}

	private void processTask(ExecutableElement task) {
		
		List<VariableElement> parameters = (List<VariableElement>) task.getParameters();
		
		// Check for $asTask parameter
		VariableElement lastParameter = parameters.get(parameters.size()-1);
		if (!lastParameter.toString().equals("$asTask") || !isOfBooleanType(lastParameter)) {
			Message message = new Message(Kind.ERROR, "A task needs a parameter $asTask and it needs to be a boolean. It has to be the last one in the list.", task);
			message.print(messager);
		}
		
		for (VariableElement parameter : parameters) {
			
			// No annotations needed for $asTask
			if(parameter.toString().equals("$asTask") && isOfBooleanType(parameter))
				continue;

			// No annotations needed for primitive types
			if (isPrimitiveType(parameter.asType()))
				continue;
			
			// Check type
			if (!isValidParameterType(parameter)) {
				Message message = new Message(Kind.ERROR, "Type is not checked or on the whitelist.", parameter); 
				message.print(messager);
			}
			
			// Check annotation
			Annotation pureAnnotation = parameter.getAnnotation(Pure.class);
			Annotation roAnnotation = parameter.getAnnotation(Readonly.class);
			Annotation rwAnnotation = parameter.getAnnotation(Readwrite.class);
			int roleAnnotationCount = 0;
			if (pureAnnotation != null) roleAnnotationCount++;
			if (roAnnotation != null) roleAnnotationCount++;
			if (rwAnnotation != null) roleAnnotationCount++;
			if (roleAnnotationCount == 0) {
				Message message = new Message(Kind.ERROR, "Type parameters have to be annotated with either @Pure, @Readonly or @Readwrite.", parameter);
				message.print(messager);
			}
			if (roleAnnotationCount > 1) {
				Message message = new Message(Kind.ERROR, "Only one role annotation is legal per task parameter.", parameter);
				message.print(messager);
			}
		}
	}

	private void checkOverridingTasksAreAnnotated(TypeElement clazz) {
		// Return when direct superclass is object, Object doesn't define tasks
		if (clazz.getSuperclass().toString().equals(Object.class.getName())) return;
		
		// Check every method in the class
		for (Element element : clazz.getEnclosedElements()) {
			if (element instanceof ExecutableElement) {
				ExecutableElement method = (ExecutableElement) element;
				ExecutableElement overriddenTask = getOverriddenTask(method);
				if (overriddenTask != null) {
					if (method.getAnnotation(Roleztask.class) == null) {
						Message message = new Message(Kind.ERROR, "Methods overriding tasks must have the @Roleztask annotation.", method);
						message.print(messager);
					}
					
					// If a method is overriding a task, it should have invariant role annotations
					checkInvariantRoleAnnotations(method, overriddenTask);
				}
			}
		}
	}
	
	private ExecutableElement getOverriddenTask(ExecutableElement method) {
		TypeElement methodClass = (TypeElement)method.getEnclosingElement();
		TypeElement currentClass = methodClass;
		while(!getSupertype(currentClass.asType()).toString().equals(Object.class.getName())) {
			DeclaredType declared = (DeclaredType) getSupertype(currentClass.asType());
			currentClass = (TypeElement) declared.asElement();
			for (Element superElement : currentClass.getEnclosedElements())
				if (superElement instanceof ExecutableElement) {
					ExecutableElement superMethod = (ExecutableElement) superElement;
					if(elements.overrides(method, superMethod, methodClass))
						if (superMethod.getAnnotation(Roleztask.class) != null)
							return superMethod;
				}
		}
		return null;
	}
	
	private void checkInvariantRoleAnnotations(ExecutableElement method, ExecutableElement overriddenMethod) {
		List<? extends VariableElement> parameters = method.getParameters();
		List<? extends VariableElement> superParameters = overriddenMethod.getParameters();
		for (int i=0; i<superParameters.size(); i++) {
			VariableElement parameter = parameters.get(i);
			VariableElement superParameter = superParameters.get(i);
			if (superParameter.getAnnotation(Pure.class) != null)
				if (parameter.getAnnotation(Pure.class) == null) {
					Element superElement = overriddenMethod.getEnclosingElement();
					Message message = new Message(Kind.ERROR, "Put the @Pure role here, since the overriden method in class " + superElement.toString() + " declares the @Pure "
							+ "role for this task parameter.", parameter);
					message.print(messager);
				}
			
			if (superParameter.getAnnotation(Readonly.class) != null)
				if (parameter.getAnnotation(Readonly.class) == null) {
					Element superElement = overriddenMethod.getEnclosingElement();
					Message message = new Message(Kind.ERROR, "Put the @Readonly role here, since the overriden method in class " + superElement.toString() + " declares the @Readonly "
							+ "role for this task parameter.", parameter);
					message.print(messager);
				}
			
			if (superParameter.getAnnotation(Readwrite.class) != null)
				if (parameter.getAnnotation(Readwrite.class) == null) {
					Element superElement = overriddenMethod.getEnclosingElement();
					Message message = new Message(Kind.ERROR, "Put the @Readwrite role here, since the overriden method in class " + superElement.toString() + " declares the @Readwrite "
							+ "role for this task parameter.", parameter);
					message.print(messager);
				}
		}
	}
	
	/*
	// Enforce invariant role annotations
	List<? extends VariableElement> parameters = method.getParameters();
	List<? extends VariableElement> superParameters = superMethod.getParameters();
	for (int i=0; i<superParameters.size(); i++) {
		VariableElement parameter = parameters.get(i);
		VariableElement superParameter = superParameters.get(i);
		if (superParameter.getAnnotation(Pure.class) != null) {
			if (parameter.getAnnotation(Pure.class) == null) {
				Message message = new Message(Kind.ERROR, "Put the @Pure role here, since the overriden method in class " + superElement.toString() + " declares the @Pure "
						+ "role for this task parameter as well.", parameter);
				message.print(messager);
			}
		}
		if (superParameter.getAnnotation(Readonly.class) != null) {
			if (parameter.getAnnotation(Readonly.class) == null) {
				Message message = new Message(Kind.ERROR, "Put the @Readonly role here, since the overriden method in class " + superElement.toString() + " declares the @Readonly "
						+ "role for this task parameter as well.", parameter);
				message.print(messager);
			}
		}
		if (superParameter.getAnnotation(Readwrite.class) != null) {
			if (parameter.getAnnotation(Readwrite.class) == null) {
				Message message = new Message(Kind.ERROR, "Put the @Readwrite role here, since the overriden method in class " + superElement.toString() + " declares the @Readwrite "
						+ "role for this task parameter as well.", parameter);
				message.print(messager);
			}
		}
	}*/
	
	private boolean isValidParameterType(VariableElement parameter) {
		TypeMirror parameterType = parameter.asType();
		if (isWhitelisted(parameterType)) 
			return true;
		if (isCheckedType(parameterType))
			return true;
		return false;
	}
	
	private boolean isPrimitiveType(TypeMirror parameterType) {
		for (String element : Processor.PRIMITIVE_TYPES)
			if (element.equals(parameterType.toString()))
				return true;
		return false;
	}
	
	private boolean isWhitelisted(TypeMirror parameterType) {
		for (String element : Processor.WHITELIST)
			if (element.equals(parameterType.toString()))
				return true;
		return false;
	}
	
	private boolean isCheckedType(TypeMirror type) {
		
		// Have to return here since Object has no supertype
		if (type.toString().equals(Object.class.getName()))
			return false;
		
		TypeMirror supertype = getSupertype(type);
		if(supertype.toString().equals(Object.class.getName())) {
			// If the super type is object, then the current type has to be annotated with @Checked.
			ParameterTypeVisitor typeVisitor = new ParameterTypeVisitor();
			return type.accept(typeVisitor, types);
		} else if (supertype.toString().equals(rolez.checked.lang.Checked.class.getCanonicalName())) {
			// If the super type is the Checked class, then we are done.
			return true;
		}
		// If nothing above is true, then we can further climb the inheritance tree to find an annotation
		// or the Checked class itself.
		return isCheckedType(supertype);
	}

	private boolean isOfBooleanType(Element element) {
		return element.asType().toString().equals("boolean");
	}
	
	private TypeMirror getSupertype(TypeMirror type) {
		// Get the super type of the current type (first in list is always a class, further are interfaces)
		List<? extends TypeMirror> supertypes = types.directSupertypes(type);
		return supertypes.get(0);
	}
	
}
