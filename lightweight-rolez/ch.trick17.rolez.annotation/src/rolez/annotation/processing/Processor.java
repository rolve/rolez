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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
			
			
			String message = "";
			for (Element e : element.getEnclosedElements()) {
				message += " ";
				message += e.toString();
			}
			
			Element enclosing = element.getEnclosingElement();
			message += " || " + enclosing;

			message += " || ";
			for (Modifier modifier : element.getModifiers()) {
				message += modifier.toString();
			}
			
			messager.printMessage(Kind.WARNING, message, element);
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
