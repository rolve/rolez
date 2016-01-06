package ch.trick17.rolez.tests

import ch.trick17.rolez.RolezUtils
import ch.trick17.rolez.rolez.Program
import ch.trick17.rolez.rolez.RolezFactory
import ch.trick17.rolez.validation.JavaMapper
import javax.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith
import rolez.lang.IntArray
import rolez.lang.ObjectArray
import rolez.lang.Task

import static ch.trick17.rolez.rolez.Role.*

import static extension org.junit.Assert.*
import ch.trick17.rolez.rolez.Role
import ch.trick17.rolez.rolez.Type

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class JavaMapperTest {
    
    @Inject extension JavaMapper
    @Inject extension RolezFactory
    @Inject extension RolezUtils
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtilz
    @Inject IJvmTypeProvider.Factory jvmTypesFactory
    
    static interface Methods<T> {
        def                   int            intMethod()
        def                double         doubleMethod()
        def               boolean        booleanMethod()
        def                  char           charMethod()
        def                  void           voidMethod()
        def                Object         ObjectMethod()
        def                 int[]       intArrayMethod()
        def             boolean[]   booleanArrayMethod()
        def              IntArray       IntArrayMethod()
        def               int[][]  intArrayArrayMethod()
        def              char[][] charArrayArrayMethod()
        def ObjectArray<IntArray>  IntArrayArrayMethod()
        def         Task<Integer>        intTaskMethod()
        def                     T              TMethod()
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
        
        p.newRoleType(READWRITE, "rolez.lang.Object").mapsTo(jvmTypeRef("Object")).assertTrue
        p.newRoleType(READONLY , "rolez.lang.Object").mapsTo(jvmTypeRef("Object")).assertTrue
        p.newRoleType(PURE     , "rolez.lang.Object").mapsTo(jvmTypeRef("Object")).assertTrue
        
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("intArray")).assertTrue
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("IntArray")).assertTrue
        
        p.newRoleType(PURE, "rolez.lang.Array", p.newRoleType(READONLY, "rolez.lang.Array", createInt))
            .mapsTo(jvmTypeRef("intArrayArray")).assertTrue
        p.newRoleType(PURE, "rolez.lang.Array", p.newRoleType(READONLY, "rolez.lang.Array", createInt))
            .mapsTo(jvmTypeRef("IntArrayArray")).assertTrue
        
        p.newRoleType(PURE, "rolez.lang.Task", createInt).mapsTo(jvmTypeRef("intTask")).assertTrue
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
        
        p.newRoleType(READWRITE, "rolez.lang.Object").mapsTo(jvmTypeRef("int"     )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Object").mapsTo(jvmTypeRef("void"    )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Object").mapsTo(jvmTypeRef("T"       )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Object").mapsTo(jvmTypeRef("intArray")).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Object").mapsTo(jvmTypeRef("IntArray")).assertFalse
        
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("int"          )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("Object"       )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("T"            )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("booleanArray" )).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("intArrayArray")).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("IntArrayArray")).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Array", createInt).mapsTo(jvmTypeRef("intTask"      )).assertFalse
        
        p.newRoleType(READWRITE, "rolez.lang.Task", createInt).mapsTo(jvmTypeRef("intArray")).assertFalse
        p.newRoleType(READWRITE, "rolez.lang.Task", createInt).mapsTo(jvmTypeRef("IntArray")).assertFalse
        
        p.newRoleType(PURE, "rolez.lang.Array", p.newRoleType(READONLY, "rolez.lang.Array", createInt))
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
            .findTypeByName("ch.trick17.rolez.tests.JavaMapperTest$Methods") as JvmDeclaredType)
            .method(name + "Method").returnType
    }
    
    private def method(JvmDeclaredType it, String name) {
        members.filter(JvmOperation).filter[simpleName == name].head
    }
}