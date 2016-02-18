package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.rolez.Program
import com.google.inject.Injector
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

class RolezGenerator extends AbstractGenerator {
    
    @Inject extension RolezExtensions
    @Inject Injector injector
    
    override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext _) {
        val program = resource.contents.head as Program
        for (c : program.classes.filter[!mapped || isSingleton]) {
            val generator = new ClassGenerator
            injector.injectMembers(generator)
            
            val name = c.qualifiedName.segments.join(File.separator) + ".java"
            fsa.generateFile(name, generator.generate(c))
        }
    }
}