package ch.trick17.rolez.lang.generator

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.Class
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.Param
import ch.trick17.rolez.lang.rolez.PrimitiveType
import ch.trick17.rolez.lang.rolez.Program
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.SimpleClassRef
import java.io.File
import javax.inject.Inject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.xtext.generator.IGenerator

import static ch.trick17.rolez.lang.Constants.*

class RolezGenerator implements IGenerator {
    
    @Inject extension RolezExtensions
    
    override void doGenerate(Resource resource, IFileSystemAccess fsa) {
        val program = resource.contents.head as Program
        for (c : program.classes) {
            val name = c.qualifiedName.segments.join(File.separator) + ".java"
            fsa.generateFile(name, generate(c, program))
        }
    }
    
    private def generate(Class it, Program p) {'''
        «if(!package.isEmpty) '''package «package»;'''»
        
        «p.imports.map[importedNamespace].join('''
        ''')»
        public class «simpleName» extends «actualSuperclass?.generate?:"java.lang.Object"» {
            
            «fields.map[generate].join»
            
            «constructors.map[generate].join("\n")»
            
            «methods.map[generate].join("\n")»
        }
    '''}
    
    private def generate(Class it) { name } // Qualified name is not easy to get, apparently...
    
    private def generate(Field it) {'''
        public «type.generate» «name»;
    '''}
    
    private def generate(Constr it) {'''
        public «enclosingClass.simpleName»(«params.map[generate].join(", ")») {
            // TODO
            throw new Error();
        }
    '''}
    
    private def generate(Method it) {'''
        public «type.generate» «name»(«params.map[generate].join(", ")») {
            // TODO
            throw new Error();
        }
    '''}
    
    private def generate(Param it) {'''«type.generate» «name»'''}
    
    private def dispatch String generate(PrimitiveType it) { string }
    
    private def dispatch String generate(RoleType it) { base.generate }
    
    private def dispatch String generate(SimpleClassRef it) { clazz.generate }
    
    private def dispatch String generate(GenericClassRef it) {
        if(clazz.qualifiedName == arrayClassName)
            '''«typeArg.generate»[]'''
        else
            '''«clazz.generate»<«typeArg.generate»>'''
    }
}
