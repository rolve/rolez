package ch.trick17.rolez.validation

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.rolez.Type
import ch.trick17.rolez.tests.RolezInjectorProvider
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith
import rolez.lang.GuardedArray
import rolez.lang.Task

import static extension org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class JavaMapperTest {
    
    @Inject extension JavaMapper
    @Inject extension RolezFactory
    @Inject extension RolezUtils
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtils
    @Inject IJvmTypeProvider.Factory jvmTypesFactory
    
    static interface Methods<T> {
        def                                 int            intMethod()
        def                              double         doubleMethod()
        def                             boolean        booleanMethod()
        def                                char           charMethod()
        def                                void           voidMethod()
        def                              Object         ObjectMethod()
        def                               int[]       intArrayMethod()
        def                           boolean[]   booleanArrayMethod()
        def                 GuardedArray<int[]>       IntArrayMethod()
        def                             int[][]  intArrayArrayMethod()
        def                            char[][] charArrayArrayMethod()
        def GuardedArray<GuardedArray<int[]>[]>  IntArrayArrayMethod()
        def                       Task<Integer>        intTaskMethod()
        def                                   T              TMethod()
    }
    
    @Test def mapsToPrimitiveTypes() {
        createInt    .mapsTo(jvmTypeRef("int"    )).assertTrue
        createDouble .mapsTo(jvmTypeRef("double" )).assertTrue
        createBoolean.mapsTo(jvmTypeRef("boolean")).assertTrue
        createChar   .mapsTo(jvmTypeRef("char"   )).assertTrue
        createVoid   .mapsTo(jvmTypeRef("void"   )).assertTrue
    }
    
    @Test def mapsToReferenceTypes() {
        var p = parse('''
            class rolez.lang.Object   mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            class rolez.lang.Task[V]  mapped to rolez.lang.Task
        ''')
        
        p.newRoleType(createReadWrite, "rolez.lang.Object").mapsTo(jvmTypeRef("Object")).assertTrue
        p.newRoleType(createReadOnly , "rolez.lang.Object").mapsTo(jvmTypeRef("Object")).assertTrue
        p.newRoleType(createPure     , "rolez.lang.Object").mapsTo(jvmTypeRef("Object")).assertTrue
        
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("intArray")).assertTrue
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("IntArray")).assertTrue
        
        p.newRoleType(createPure, "rolez.lang.Array", p.newRoleType(createReadOnly, "rolez.lang.Array", createInt))
            .mapsTo(jvmTypeRef("intArrayArray")).assertTrue
        p.newRoleType(createPure, "rolez.lang.Array", p.newRoleType(createReadOnly, "rolez.lang.Array", createInt))
            .mapsTo(jvmTypeRef("IntArrayArray")).assertTrue
        
        p.newRoleType(createPure, "rolez.lang.Task", createInt).mapsTo(jvmTypeRef("intTask")).assertTrue
    }
    
    @Test def mapsToTypeParamRef() {
        val ref = createTypeParamRef => [
            param = createTypeParam => [name = "T"]
        ]
        ref.mapsTo(jvmTypeRef("T")).assertTrue
    }
    
    @Test def mapsToNot() {
        createInt    .mapsTo(jvmTypeRef("boolean")).assertFalse
        createBoolean.mapsTo(jvmTypeRef("int"    )).assertFalse
        createInt    .mapsTo(jvmTypeRef("Object" )).assertFalse
        createInt    .mapsTo(jvmTypeRef("T"      )).assertFalse
        
        var p = parse('''
            class rolez.lang.Object   mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
            class rolez.lang.Task[V]  mapped to rolez.lang.Task
        ''')
        
        p.newRoleType(createReadWrite, "rolez.lang.Object").mapsTo(jvmTypeRef("int"     )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Object").mapsTo(jvmTypeRef("void"    )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Object").mapsTo(jvmTypeRef("T"       )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Object").mapsTo(jvmTypeRef("intArray")).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Object").mapsTo(jvmTypeRef("IntArray")).assertFalse
        
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("int"          )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("Object"       )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("T"            )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("booleanArray" )).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("intArrayArray")).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("IntArrayArray")).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("intTask"      )).assertFalse
        
        p.newRoleType(createReadWrite, "rolez.lang.Task", createInt).mapsTo(jvmTypeRef("intArray")).assertFalse
        p.newRoleType(createReadWrite, "rolez.lang.Task", createInt).mapsTo(jvmTypeRef("IntArray")).assertFalse
        
        p.newRoleType(createPure, "rolez.lang.Array", p.newRoleType(createReadOnly, "rolez.lang.Array", createInt))
            .mapsTo(jvmTypeRef("charArrayArray")).assertFalse
    }
    
    private def newRoleType(Program p, Role r, String c) {
        newRoleType(r, newClassRef(p.findNormalClass(c)))
    }
    
    private def newRoleType(Program p, Role r, String c, Type a) {
        newRoleType(r, newClassRef(p.findNormalClass(c), a))
    }
    
    private def jvmTypeRef(String name) {
        (jvmTypesFactory.findOrCreateTypeProvider(newResourceSet)
            .findTypeByName(Methods.name) as JvmDeclaredType)
            .method(name + "Method").returnType
    }
    
    private def method(JvmDeclaredType it, String name) {
        members.filter(JvmOperation).filter[simpleName == name].head
    }
}
