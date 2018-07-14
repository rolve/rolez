package ch.trick17.rolez.ui.wizard

import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtext.builder.impl.XtextBuilder
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.util.JavaProjectFactory
import org.eclipse.xtext.ui.wizard.template.AbstractProjectTemplate
import org.eclipse.xtext.ui.wizard.template.IProjectGenerator
import org.eclipse.xtext.ui.wizard.template.IProjectTemplateProvider

import static ch.trick17.rolez.ui.buildpath.RolezStdLibInitializer.ROLEZ_STD_LIB_PATH

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
        generator.generate(new JavaProjectFactory => [
            projectName = projectInfo.projectName
            location = projectInfo.locationPath
            projectNatures += #[JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID]
            builderIds += JavaCore.BUILDER_ID
            builderIds += XtextBuilder.BUILDER_ID
            folders += "src"
            folders += "src-gen"
            extraClasspathEntries += JavaCore.newContainerEntry(ROLEZ_STD_LIB_PATH)
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
