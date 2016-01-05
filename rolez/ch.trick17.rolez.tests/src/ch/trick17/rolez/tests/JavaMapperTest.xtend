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

@RunWith(XtextRunner)
@InjectWith(RolezInjectorProvider)
class JavaMapperTest {
    
    @Inject extension JavaMapper
    @Inject extension RolezFactory
    @Inject extension RolezUtils
    @Inject extension ParseHelper<Program>
    @Inject extension TestUtilz
    @Inject IJvmTypeProvider.Factory jvmTypesFactory
    
    static interface Methods {
        def                   int           intMethod()
        def                double        doubleMethod()
        def               boolean       booleanMethod()
        def                  char          charMethod()
        def                  void          voidMethod()
        def                Object        ObjectMethod()
        def                 int[]      intArrayMethod()
        def              IntArray      IntArrayMethod()
        def               int[][] intArrayArrayMethod()
        def ObjectArray<IntArray> IntArrayArrayMethod()
        def         Task<Integer>       intTaskMethod()
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
            class rolez.lang.Object mapped to java.lang.Object
            class rolez.lang.Array[T] mapped to rolez.lang.Array
        ''')
        
        newRoleType(READWRITE, newClassRef(p.findClass("rolez.lang.Object")))
            .mapsTo(jvmTypeRef("Object")).assertTrue
        newRoleType(READONLY, newClassRef(p.findClass("rolez.lang.Object")))
            .mapsTo(jvmTypeRef("Object")).assertTrue
        newRoleType(PURE, newClassRef(p.findClass("rolez.lang.Object")))
            .mapsTo(jvmTypeRef("Object")).assertTrue
        
        newRoleType(READWRITE, newClassRef(p.findNormalClass("rolez.lang.Array"), createInt))
            .mapsTo(jvmTypeRef("intArray")).assertTrue
        newRoleType(READWRITE, newClassRef(p.findNormalClass("rolez.lang.Array"), createInt))
            .mapsTo(jvmTypeRef("IntArray")).assertTrue
        
        newRoleType(PURE, newClassRef(p.findNormalClass("rolez.lang.Array"),
            newRoleType(READONLY, newClassRef(p.findNormalClass("rolez.lang.Array"), createInt))))
            .mapsTo(jvmTypeRef("intArrayArray")).assertTrue
        newRoleType(PURE, newClassRef(p.findNormalClass("rolez.lang.Array"),
            newRoleType(READONLY, newClassRef(p.findNormalClass("rolez.lang.Array"), createInt))))
            .mapsTo(jvmTypeRef("IntArrayArray")).assertTrue
        
        newRoleType(PURE, newClassRef(p.findNormalClass("rolez.lang.Task"), createInt))
            .mapsTo(jvmTypeRef("IntTask")).assertTrue
    }
    
    // TODO: Negative tests for mapsTo
    
    private def jvmTypeRef(String name) {
        (jvmTypesFactory.findOrCreateTypeProvider(newResourceSet)
            .findTypeByName("ch.trick17.rolez.tests.JavaMapperTest$Methods") as JvmDeclaredType)
            .method(name + "Method").returnType
    }
    
    private def method(JvmDeclaredType it, String name) {
        members.filter(JvmOperation).filter[simpleName == name].head
    }
}