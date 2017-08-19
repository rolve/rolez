package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.NormalClass
import ch.trick17.rolez.rolez.Program
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext

import static extension ch.trick17.rolez.generator.SafeJavaNames.*

/**
 * Generates Java source code files from Rolez classes. Uses
 * {@link ClassGenerator} to produce the actual Java code.
 */
class RolezGenerator extends AbstractGenerator {
    
    @Inject ClassGenerator classGenerator
    
    override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext _) {
        val program = resource.contents.head as Program
        for (c : program.classes.filter[!mapped || isSingleton]) {
            val baseName = c.qualifiedName.segments.map[safe].join(File.separator)
            fsa.generateFile(baseName + ".java", classGenerator.generate(c))
            if(c instanceof NormalClass)
                for(slice : c.slices) {
                    val name = baseName + "£" + slice.safeName
                    fsa.generateFile(name + ".java", classGenerator.generateSlice(slice))
                }
        }
    }
}