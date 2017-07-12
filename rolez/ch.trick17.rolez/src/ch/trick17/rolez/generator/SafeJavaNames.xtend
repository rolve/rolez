package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Named

class SafeJavaNames {
    
    static val javaKeywords = #{
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", 
        "char", "class", "const", "continue", "default", "do", "double", "else",
        "enum", "extends", "final", "finally", "float", "for", "goto", "if",
        "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "package", "private", "protected", "public", "return",
        "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while"}
    
    static def safeName(Named it) { safe(name) }
    
    static def safeQualifiedName(Class it) {
        qualifiedName.segments.map[safe].join(".")
    }
    
    static def safeSimpleName(Class it) {
        safe(qualifiedName.lastSegment)
    }
    
    static def safePackage(Class it) {
        val segments = qualifiedName.segments
        segments.subList(0, segments.size-1).map[safe].join(".")
    }
    
    static def safe(String name) {
        if(javaKeywords.contains(name))
            "£" + name
        else
            name
    }
}