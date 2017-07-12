package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Program
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import static extension ch.trick17.rolez.generator.SafeJavaNames.*

class RolezGenerator extends AbstractGenerator {
    
    @Inject ClassGenerator classGenerator
    
    override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext _) {
        val program = resource.contents.head as Program
        for (c : program.classes.filter[!mapped || isSingleton]) {
            val name = c.qualifiedName.segments.map[safe].join(File.separator) + ".java"
            fsa.generateFile(name, classGenerator.generate(c))
        }
    }
}