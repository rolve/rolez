package rolez.annotation.processing;

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
		for (Element element : env.getElementsAnnotatedWith(Roleztask.class)) {
			
			if (!(element instanceof ExecutableElement)) {
				messager.printMessage(Kind.ERROR, "Only Methods can use this Annotation.", element);
			}
			
			ExecutableElement execElem = (ExecutableElement)element;
			String[] readonly = execElem.getAnnotation(Roleztask.class).readonly();
			String[] readwrite = execElem.getAnnotation(Roleztask.class).readwrite();

			String message = "ro: ";
			
			for (String s : readonly) {
				message += s;
				message += " ";
			}
			
			message += "\nrw: ";
			
			for (String s : readwrite) {
				message += s;
				message += " ";
			}

			message += "\nparams: ";
			
			for (VariableElement e : execElem.getParameters()) {
				message += " ";
				message += e.asType().toString();
				message += " ";
				message += e.toString();
			}
			
			messager.printMessage(Kind.WARNING, message, execElem);
			
			/*
			if (element.getSimpleName().toString().startsWith("Silly")) {
				// We don't want generate new silly classes 
				// for auto-generated silly classes
				continue;
			}

			if (element.getSimpleName().toString().startsWith("T")) {
				messager.printMessage(Kind.WARNING, 
					"This class name starts with a T!", 
					element);	
			}

			String sillyClassName = "Silly" + element.getSimpleName();
			String sillyClassContent = 
					"package silly;\n" 
				+	"public class " + sillyClassName + " {\n"
				+	"	public String foobar;\n"
				+	"}";

			JavaFileObject file = null;

			try {
				file = filer.createSourceFile(
						"silly/" + sillyClassName, 
						element);
				file.openWriter()
					.append(sillyClassContent)
					.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
		}

		return true;
	}
	
}
