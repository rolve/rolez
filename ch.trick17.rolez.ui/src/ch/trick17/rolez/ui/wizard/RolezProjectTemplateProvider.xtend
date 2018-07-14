package ch.trick17.rolez.ui.wizard

import java.util.LinkedHashMap
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.JavaCore
import org.eclipse.swt.widgets.Shell
import org.eclipse.xtext.builder.impl.XtextBuilder
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.util.JavaProjectFactory
import org.eclipse.xtext.ui.wizard.template.AbstractProjectTemplate
import org.eclipse.xtext.ui.wizard.template.IProjectGenerator
import org.eclipse.xtext.ui.wizard.template.IProjectTemplateProvider

import static ch.trick17.rolez.ui.buildpath.RolezStdLibInitializer.ROLEZ_STD_LIB_PATH
import static org.eclipse.jdt.core.JavaCore.*

/**
 * Create a list with all project templates to be shown in the template new project wizard.
 * 
 * Each template is able to generate one or more projects. Each project can be configured such that any number of files are included.
 */
class RolezProjectTemplateProvider implements IProjectTemplateProvider {
    override getProjectTemplates() {
        #[new HelloWorldProject]
    }
}

final class HelloWorldProject extends AbstractProjectTemplate {
    
    override getLabel() { "Hello World" }
    
    override getDescription() '''
        <p><b>Hello World</b></p>
        <p>This is the classic \"Hello World!\" in Rolez.</p>
    '''
    
    override getIcon() { "project_template.png" }
    
    override generateProjects(IProjectGenerator generator) {
        generator.generate(new BetterJavaProjectFactory => [
            projectName = projectInfo.projectName
            location = projectInfo.locationPath
            projectNatures += #[JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID]
            builderIds += JavaCore.BUILDER_ID
            builderIds += XtextBuilder.BUILDER_ID
            folders += "src"
            folders += "src-gen"
            folderAttributes.put("src-gen", #[newClasspathAttribute("ignore_optional_problems", "true")])
            extraClasspathEntries += newContainerEntry(ROLEZ_STD_LIB_PATH)
            
            addFile('''src/hello/HelloWorld.rz''', '''
                package hello
                
                object HelloWorld {
                    task pure main: {
                        System.out.println("Hello World!");
                    }
                }
            ''')
        ])
    }
}

class BetterJavaProjectFactory extends JavaProjectFactory {
    
    package val folderAttributes = new LinkedHashMap<String, IClasspathAttribute[]>
    
    override protected enhanceProject(IProject project, SubMonitor monitor, Shell shell) throws CoreException {
        folders -= folderAttributes.keySet
        val entries = folderAttributes.entrySet
                .map[newSourceEntry(project.getFolder(key).fullPath, #[], #[], null, value)]
        extraClasspathEntries.addAll(0, entries.toList)
        super.enhanceProject(project, monitor, shell)
    }
    
}