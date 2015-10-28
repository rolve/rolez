package ch.trick17.rolez.lang

import org.eclipse.xtext.naming.QualifiedName

class Constants {
    public static val objectClassName = QualifiedName.create("rolez", "lang", "Object");
    public static val stringClassName = QualifiedName.create("rolez", "lang", "String")
    public static val arrayClassName  = QualifiedName.create("rolez", "lang", "Array")
    public static val taskClassName   = QualifiedName.create("rolez", "lang", "Task")
    public static val systemClassName = QualifiedName.create("rolez", "lang", "System")
}