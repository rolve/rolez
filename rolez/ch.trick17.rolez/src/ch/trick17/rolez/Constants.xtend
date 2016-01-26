package ch.trick17.rolez

import org.eclipse.xtext.naming.QualifiedName

class Constants {
    public static val objectClassName = QualifiedName.create("rolez", "lang", "Object");
    public static val  arrayClassName = QualifiedName.create("rolez", "lang", "Array")
    public static val  sliceClassName = QualifiedName.create("rolez", "lang", "Slice")
    public static val stringClassName = QualifiedName.create("rolez", "lang", "String")
    public static val   taskClassName = QualifiedName.create("rolez", "lang", "Task")
    
    public static val      jvmGuardedClassName = "rolez.lang.Guarded"
    public static val jvmGuardedArrayClassName = "rolez.lang.GuardedArray"
    public static val   jvmTaskSystemClassName = "rolez.lang.TaskSystem"
}