package ch.trick17.rolez.generator

import ch.trick17.rolez.RolezExtensions
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Named
import javax.inject.Inject

class SafeJavaNames {
    
    @Inject extension RolezExtensions
    
    static val javaKeywords = #{
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", 
        "char", "class", "const", "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "package", "private", "protected", "public", "return",
        "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while"}
    
    def safeName(Named it) { safe(name) }
    
    def safeQualifiedName(Class it) {
        qualifiedName.segments.map[safe].join(".")
    }
    
    def safeSimpleName(Class it) {
        safe(qualifiedName.lastSegment)
    }
    
    def safePackage(Class it) {
        val segments = qualifiedName.segments
        segments.takeWhile[it != segments.last].map[safe].join(".")
    }
    
    private def safe(String name) {
        if(javaKeywords.contains(name))
            "£" + name
        else
            name
    }
}