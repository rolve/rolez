package ch.trick17.rolez

import org.eclipse.xtext.naming.QualifiedName

class Constants {
    public static val        objectClassName = QualifiedName.create("rolez", "lang", "Object");
    public static val         arrayClassName = QualifiedName.create("rolez", "lang", "Array")
    public static val         sliceClassName = QualifiedName.create("rolez", "lang", "Slice")
    public static val        vectorClassName = QualifiedName.create("rolez", "lang", "Vector")
    public static val vectorBuilderClassName = QualifiedName.create("rolez", "lang", "VectorBuilder")
    public static val        stringClassName = QualifiedName.create("rolez", "lang", "String")
    public static val          taskClassName = QualifiedName.create("rolez", "lang", "Task")
    
    public static val              jvmGuardedClassName = "rolez.lang.Guarded"
    public static val         jvmGuardedSliceClassName = "rolez.lang.GuardedSlice"
    public static val         jvmGuardedArrayClassName = "rolez.lang.GuardedArray"
    public static val jvmGuardedVectorBuilderClassName = "rolez.lang.GuardedVectorBuilder"
    public static val           jvmTaskSystemClassName = "rolez.lang.TaskSystem"
    public static val                jvmTasksClassName = "rolez.internal.Tasks"
    
    public static val safeAnnotationName = "rolez.lang.Safe"
    
    public static val noRoleAnalysis       = "noRoleAnalysis"
    public static val noChildTasksAnalysis = "noChildTasksAnalysis"
}