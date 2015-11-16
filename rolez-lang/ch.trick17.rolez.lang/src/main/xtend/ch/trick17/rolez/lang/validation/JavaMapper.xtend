package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.PrimitiveType
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.Type
import javax.inject.Inject

import static ch.trick17.rolez.lang.Constants.*
import static java.util.Collections.unmodifiableSet

class JavaMapper {
    
    @Inject extension RolezExtensions
    
    static val mappedClasses = #{
        objectClassName      -> "java.lang.Object",
        stringClassName      -> "java.lang.String",
        arrayClassName       -> null,
        systemClassName      -> "java.lang.System",
        printStreamClassName -> "java.io.PrintStream"
    }
    
    def mappedClasses() { unmodifiableSet(mappedClasses.keySet) }
    
    def javaClassName(ch.trick17.rolez.lang.rolez.Class it) {
        mappedClasses.get(qualifiedName)
    }
    
    def javaClass(ch.trick17.rolez.lang.rolez.Class it) {
        Class.forName(javaClassName)
    }
    
    def dispatch boolean mapsTo(PrimitiveType it, Class<?> javaType) {
        javaType.isPrimitive && name == javaType.name
    }
    
    def dispatch boolean mapsTo(RoleType it, Class<?> javaType) {
        val base = base
        if(base instanceof GenericClassRef)
            javaType.isArray && base.typeArg.mapsTo(javaType.componentType)
        else
            base.clazz.javaClassName == javaType.name
    }
    
    def dispatch boolean mapsTo(Type it, java.lang.reflect.Type _) { false }
}
