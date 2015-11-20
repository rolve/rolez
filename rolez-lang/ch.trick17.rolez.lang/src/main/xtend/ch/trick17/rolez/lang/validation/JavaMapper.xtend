package ch.trick17.rolez.lang.validation

import ch.trick17.rolez.lang.RolezExtensions
import ch.trick17.rolez.lang.rolez.Constr
import ch.trick17.rolez.lang.rolez.Field
import ch.trick17.rolez.lang.rolez.GenericClassRef
import ch.trick17.rolez.lang.rolez.Method
import ch.trick17.rolez.lang.rolez.PrimitiveType
import ch.trick17.rolez.lang.rolez.RoleType
import ch.trick17.rolez.lang.rolez.Type
import java.lang.reflect.Executable
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
    
    def javaField(Field it) throws NoSuchFieldException {
        enclosingClass.javaClass.getField(name)
    }
    
    def javaMethod(Method it) throws NoSuchMethodException {
        val matching = enclosingClass.javaClass.methods.filter[m |
            val javaParamTypes = m.genericParameterTypes.iterator
            name == m.name
                && params.size == m.parameterTypes.length
                && params.forall[type.mapsTo(javaParamTypes.next)]
        ]
        switch(matching.size) {
            case 0 : throw new NoSuchMethodException
            case 1 : return matching.head
            default: throw new AssertionError("So, this can happen...")
        }
    }
    
    def javaConstr(Constr it) throws NoSuchMethodException {
        val matching = enclosingClass.javaClass.constructors.filter[c |
            val javaParamTypes = c.genericParameterTypes.iterator
            params.size == c.parameterTypes.length
                && params.forall[type.mapsTo(javaParamTypes.next)]
        ]
        switch(matching.size) {
            case 0 : throw new NoSuchMethodException
            case 1 : return matching.head
            default: throw new AssertionError("So, this can happen...")
        }
    }
    
    def checkedExceptionTypes(Method it) {
        if(enclosingClass.isArrayClass) emptyList
        else if(!isMapped) emptyList
        else javaMethod.checkedExceptionTypes
    }
    
    def checkedExceptionTypes(Constr it) {
        if(enclosingClass.isArrayClass) emptyList
        else if(!isMapped) emptyList
        else javaConstr.checkedExceptionTypes
    }
    
    private def checkedExceptionTypes(Executable it) {
        exceptionTypes.filter[isCheckedException].map[it as Class<? extends Exception>]
    }
    
    private def isCheckedException(Class<?> it) {
        Exception.isAssignableFrom(it) && !RuntimeException.isAssignableFrom(it)
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
