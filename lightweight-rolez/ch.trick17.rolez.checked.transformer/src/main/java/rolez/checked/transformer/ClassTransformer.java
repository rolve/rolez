package rolez.checked.transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rolez.checked.lang.Checked;
import rolez.checked.transformer.checked.CheckedConstructor;
import rolez.checked.transformer.checked.GuardedRefsMethod;
import rolez.checked.transformer.util.JimpleWriter;
import soot.Printer;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator;
import soot.jimple.JasminClass;
import soot.options.Options;
import soot.tagkit.AnnotationTag;
import soot.tagkit.Tag;
import soot.tagkit.VisibilityAnnotationTag;
import soot.util.JasminOutputStream;

/**
 * A transformer that processes the user defined classes and outputs code, which
 * conforms Rolez.
 * 
 * @author Michael Giger
 *
 */
public class ClassTransformer extends SceneTransformer {

	static final Logger logger = LogManager.getLogger(ClassTransformer.class);
	
	static final SootClass CHECKED_CLASS = Scene.v().loadClassAndSupport(Checked.class.getCanonicalName());
	static final SootClass OBJECT_CLASS = Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
	
	static final String ROLEZTASK_ANNOTATION = "Lrolez/annotation/Roleztask;";
	static final String CHECKED_ANNOTATION = "Lrolez/annotation/Checked;";
	
	@Override
	protected void internalTransform(String phaseName, Map options) {		
		// Start transformation
		processClasses();
	}

	private void processClasses() {
		Set<SootClass> classesToProcess = findClassesToProcess();
		for (SootClass c : classesToProcess) {
			processClass(c);
		}
	}
	
	private Set<SootClass> findClassesToProcess() {
		SootClass mainClass = Scene.v().getMainClass();
		List<SootClass> foundClasses = new ArrayList<SootClass>();
		foundClasses.add(mainClass);

		Set<SootClass> classesToProcess = new HashSet<SootClass>();
		classesToProcess.add(mainClass);
		
		boolean change = true;
		while (change) {
			change = false;
			ArrayList<SootClass> refs = null;
			for (SootClass c : foundClasses) {
				refs = findRefs(c);
			}
			for (SootClass c : refs) {
				if (!classesToProcess.contains(c)) {
					foundClasses.add(c);
					classesToProcess.add(c);
					change = true;
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("\nClasses to Process\n\n");
		for (SootClass c : classesToProcess) {
			sb.append(c.toString() + "\n");
		}
		logger.debug(sb.toString());
		
		return classesToProcess;
	}
	
	//TODO: Not only include field references
	//TODO: Stop at library classes
	private ArrayList<SootClass> findRefs(SootClass c) {
		ArrayList<SootClass> ret = new ArrayList<SootClass>();
		for (SootField f : c.getFields()) {
			// TODO: Also allow arrays?
			if (f.getType() instanceof RefType) {
				SootClass classToAdd = Scene.v().loadClassAndSupport(f.getType().toString());
				logger.debug("Class to add: " + classToAdd);
				ret.add(classToAdd);
			}
		}
		return ret;
	}
	
	private void processClass(SootClass c) {
		logger.debug("Processing class: " + c.getName());
		
		if (hasCheckedAnnotation(c)) {
			c.setSuperclass(CHECKED_CLASS);
			
			// Replace constructors with one that calls the Checked constructor
			for (SootMethod m : c.getMethods()) {
				if (m.getName().equals("<init>")) {
					CheckedConstructor checkedConstructor = new CheckedConstructor(m);
					c.removeMethod(m);
					c.addMethod(checkedConstructor);
				}
			}
			
			// Generate the guardedRefs method
			GuardedRefsMethod guardedRefs = new GuardedRefsMethod(c);
			c.addMethod(guardedRefs);
		}
		
		// Search for methods which have the @Roleztask annotation
		processMethods(c);
		
		JimpleWriter.write(c);
	}
	
	private boolean hasCheckedAnnotation(SootClass c) {
		List<Tag> classTags = c.getTags();
		for (Tag t : classTags) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(CHECKED_ANNOTATION))
						return true;
		return false;
	}
	
	private void processMethods(SootClass c) {
		for (SootMethod m : c.getMethods()) {
			logger.debug("Processing method: " + c.getName() + ":" + m.getName());
			m.retrieveActiveBody();
			if (hasRoleztaskAnnotation(m)) {
				TaskGenerator taskGenerator = new TaskGenerator(c, m);
				taskGenerator.generateMethod();
			}
			
		}
	}
	
	private boolean hasRoleztaskAnnotation(SootMethod m) {
		for (Tag t : m.getTags()) 
			if (t instanceof VisibilityAnnotationTag) 
				for (AnnotationTag aTag : ((VisibilityAnnotationTag) t).getAnnotations()) 
					if (aTag.getType().equals(ROLEZTASK_ANNOTATION)) 
						return true;
		return false;
	}
	
	
	// TODO: Add generation of path if not available
	private void writeClass(SootClass c) {
		logger.debug("Writing class file");
		try {
			String fileName = SourceLocator.v().getFileNameFor(c, Options.output_format_class);
			OutputStream streamOut = new JasminOutputStream(new FileOutputStream(fileName));
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			JasminClass jasminClass = new soot.jimple.JasminClass(c);
			jasminClass.print(writerOut);
			writerOut.flush();
			streamOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
