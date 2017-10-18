package rolez.annotation.processing;

import java.lang.annotation.Annotation;
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
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Checked;
import rolez.annotation.Pure;
import rolez.annotation.Readonly;
import rolez.annotation.Readwrite;
import rolez.annotation.Task;

@SupportedAnnotationTypes({"rolez.annotation.Task",
						   "rolez.annotation.Pure", 
						   "rolez.annotation.Readonly",
						   "rolez.annotation.Readwrite",
						   "rolez.annotation.Checked"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor {
	
	private Messager messager;
	private Types types;
	private Elements elements;
	
	// Whitelist that contains java standard classes which are allowed to be used as task parameters
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
	
	// List of java primitive types
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
		processCheckedAnnotations(env);
		processTaskAnnotations(env);
		processRoleAnnotations(env);
		return true;
	}
	
	private void processRoleAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Pure.class)) {
			checkRoleAnnotation(annotatedElement, Pure.class);
		}
		for (Element annotatedElement : env.getElementsAnnotatedWith(Readonly.class)) {
			checkRoleAnnotation(annotatedElement, Readonly.class);
		}
		for (Element annotatedElement : env.getElementsAnnotatedWith(Readwrite.class)) {
			checkRoleAnnotation(annotatedElement, Readwrite.class);
		}
	}
	
	private void checkRoleAnnotation(Element element, Class<?> annotationClass) {
		if (element instanceof ExecutableElement) {
			if (element.getAnnotation(Task.class) == null) {
				Message message = new Message(Kind.ERROR, "The @" + annotationClass.getSimpleName() + " annotation can only be placed on methods annotated with @Task.", element);
				message.print(messager);
			}
		}
		if (element instanceof VariableElement) {
			if (element.getEnclosingElement().getAnnotation(Task.class) == null) {
				Message message = new Message(Kind.ERROR, "The @" + annotationClass.getSimpleName() + " annotation can only be placed on parameter of methods annotated with @Task.", element);
				message.print(messager);
			}
		}
	}
	
	/**
	 * Processes classes annotated with the <code>@Guareded</code> annotation, which indicates that
	 * this class will inherit from the Checked class of the Rolez runtime library.
	 * @param env
	 */
	private void processCheckedAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Checked.class)) {
			// Cast is safe because @Checked is only valid on classes
			TypeElement typeElement = (TypeElement)annotatedElement;
			enforceCheckedAnnotation(typeElement);
			checkForObjectOrCheckedSupertype(typeElement);
			checkOverridingTasksAreAnnotated(typeElement); 
			checkRefsAreCheckedTypes(typeElement);
		}
	}
	
	/**
	 * This method checks whether the Checked annotation is present on a class or not.
	 * If not, an error message is displayed at the class.
	 * @param typeElement
	 */
	private void enforceCheckedAnnotation(TypeElement typeElement) {
		List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
		boolean foundChecked = false;
		for (AnnotationMirror annotation : annotations) {				
			if (isCheckedAnnotation(annotation)) {
				foundChecked = true;
				break;
			}
		}
		
		if (!foundChecked) {
			Message message = new Message(Kind.ERROR, "The @Checked annotation has to be present on this class.", typeElement);
			message.print(messager);
		}
	}

	/**
	 * This method checks whether a class has java.lang.Object or a class annotated with @Checked as superclass.
	 * If none of the two checks are true, the method displays an error message on the class declaration.
	 * @param typeElement
	 */
	private void checkForObjectOrCheckedSupertype(TypeElement typeElement) {
		TypeMirror type = typeElement.asType();
		TypeMirror supertype = getSupertype(type);
		TypeElement superTypeElement = elements.getTypeElement(supertype.toString());
		if(!(typeEqualsObject(supertype) || hasCheckedAnnotation(superTypeElement)) ) {
			Message message = new Message(Kind.ERROR, "The @Checked annotation is only legal on classes inheriting from @Checked"
					+ " annotated classes or classes inheriting from java.lang.Object.", typeElement);
			message.print(messager);
		}
	}
	
	/**
	 * This method checks if methods inside the class are overriding tasks and
	 * if so, that they are providing the same annotations as the overridden task.
	 * @param typeElement
	 */
	private void checkOverridingTasksAreAnnotated(TypeElement typeElement) {
		// Return when direct superclass is Object, Object doesn't define tasks
		if (typeElement.getSuperclass().toString().equals(Object.class.getName())) return;
		
		// Check every method in the class
		List<ExecutableElement> methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
		for (ExecutableElement method : methods) {
			ExecutableElement overriddenTask = getOverriddenTask(method);
			if (overriddenTask != null) {
				if (method.getAnnotation(Task.class) == null) {
					Message message = new Message(Kind.ERROR, "Methods overriding tasks must have the @Task annotation.", method);
					message.print(messager);
				}
				
				// If a method is overriding a task, it should have invariant role annotations
				checkInvariantRoleAnnotations(method, overriddenTask);
			}
		}
	}
	
	/**
	 * This method ensures that a @Checked class does not contain references to unchecked classes.
	 * @param typeElement
	 */
	private void checkRefsAreCheckedTypes(TypeElement typeElement) {
		List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
		for (VariableElement f : fields) {
			if (!isPrimitiveType(f.asType())) {
				if (!isCheckedType(f.asType()) && !isWhitelisted(f.asType())) {
					Message message = new Message(Kind.ERROR, "Only references to checked types are allowed.", f);
					message.print(messager);
				}
			}
		}
	}
	
	/**
	 * Processes the methods that are annotated with <code>@Task</code> and checks whether all 
	 * method parameters have a role declared and are able to be checked.
	 * @param env
	 */
	private void processTaskAnnotations(RoundEnvironment env) {
		for (Element annotatedElement : env.getElementsAnnotatedWith(Task.class)) {
			
			if (!(annotatedElement instanceof ExecutableElement)) {
				Message message = new Message(Kind.ERROR, "Only methods can be annotated as task.", annotatedElement);
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

	/**
	 * This method processes methods annotated with the @Task annotation.
	 * @param task
	 */
	private void processTask(ExecutableElement task) {
		
		// Check for void return type
		if (!typeEqualsVoid(task.getReturnType())) {
			Message message = new Message(Kind.ERROR, "Tasks cannot have non-void return types. Replace return type with void.", task);
			message.print(messager);
		}
		
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
			if (!isValidTaskParameter(parameter)) {
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

	
	
	/**
	 * Returns the overridden task or null if there is none.
	 * @param method
	 * @return
	 */
	private ExecutableElement getOverriddenTask(ExecutableElement method) {
		TypeElement methodClass = (TypeElement)method.getEnclosingElement();
		TypeElement currentClass = methodClass;
		while(!getSupertype(currentClass.asType()).toString().equals(Object.class.getName())) {
			DeclaredType declared = (DeclaredType) getSupertype(currentClass.asType());
			currentClass = (TypeElement) declared.asElement();

			List<ExecutableElement> superMethods = ElementFilter.methodsIn(currentClass.getEnclosedElements());
			for (ExecutableElement superMethod : superMethods)
				if(elements.overrides(method, superMethod, methodClass))
					if (superMethod.getAnnotation(Task.class) != null)
						return superMethod;
		}
		return null;
	}
	
	/**
	 * For a task overriding another task, this method checks whether the role annotations for 
	 * the parameters are invariant or not.
	 * @param overrider
	 * @param overriddenMethod
	 */
	private void checkInvariantRoleAnnotations(ExecutableElement overrider, ExecutableElement overriddenMethod) {
		if (overriddenMethod.getAnnotation(Pure.class) != null) {
			if (overrider.getAnnotation(Readwrite.class) != null || overrider.getAnnotation(Readonly.class) != null) {
				Message message = new Message(Kind.ERROR, "Put the @Pure role here, since the overriden method in class " + overriddenMethod.toString() + " declares the @Pure "
						+ "role for the method.", overrider);
				message.print(messager);
			}
		}
		if (overriddenMethod.getAnnotation(Readonly.class) != null) {
			if (overrider.getAnnotation(Readonly.class) == null) {
				Message message = new Message(Kind.ERROR, "Put the @Readonly role here, since the overriden method in class " + overriddenMethod.toString() + " declares the @Readonly "
						+ "role for the method.", overrider);
				message.print(messager);
			}
		}
		if (overriddenMethod.getAnnotation(Readwrite.class) != null) {
			if (overrider.getAnnotation(Readwrite.class) == null) {
				Message message = new Message(Kind.ERROR, "Put the @Readwrite role here, since the overriden method in class " + overriddenMethod.toString() + " declares the @Readonly "
						+ "role for the method.", overrider);
				message.print(messager);
			}
		}
		List<? extends VariableElement> parameters = overrider.getParameters();
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
	
	/**
	 * This method checks whether a parameter is a valid task parameter or not.
	 * I.e. if the parameter is either on the whitelist or is a checked type.
	 * @param variableElement
	 * @return
	 */
	private boolean isValidTaskParameter(VariableElement variableElement) {
		TypeMirror parameterType = variableElement.asType();
		if (isWhitelisted(parameterType)) 
			return true;
		if (isCheckedType(parameterType))
			return true;
		return false;
	}
	
	/**
	 * Checks whether a parameter is a primitive type or not.
	 * @param parameterType
	 * @return
	 */
	private boolean isPrimitiveType(TypeMirror parameterType) {
		for (String element : Processor.PRIMITIVE_TYPES)
			if (element.equals(parameterType.toString()))
				return true;
		return false;
	}
	
	/**
	 * Checks whether a parameter is on the whitelist or not.
	 * @param type
	 * @return
	 */
	private boolean isWhitelisted(TypeMirror type) {
		for (String element : Processor.WHITELIST)
			if (element.equals(type.toString()))
				return true;
		return false;
	}
	
	/**
	 * Checks whether a parameter is checked or not. A parameter is checked if one of the following
	 * statements holds:
	 * - The class of the parameter type is annotated with @Checked.
	 * - The class of the parameter type is extending the class rolez.checked.lang.Checked.
	 * @param type
	 * @return
	 */
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
	
	private boolean typeEqualsVoid(TypeMirror type) {
		return type.toString().equals("void");
	}
	
	private boolean typeEqualsObject(TypeMirror type) {
		return type.toString().equals(Object.class.getName());
	}
	
	private boolean isCheckedAnnotation(AnnotationMirror annotation) {
		return annotation.getAnnotationType().toString().equals(Checked.class.getName());
	}
	
	private boolean hasCheckedAnnotation(TypeElement typeElement) {
		return typeElement.getAnnotation(Checked.class) != null; 
	}
}
