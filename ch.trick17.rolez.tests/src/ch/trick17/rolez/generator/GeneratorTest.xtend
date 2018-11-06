package ch.trick17.rolez.generator

import ch.trick17.rolez.TestUtils
import ch.trick17.rolez.rolez.Program
import com.google.common.io.ByteStreams
import com.google.common.io.CharStreams
import java.net.URI
import java.util.regex.Pattern
import javax.inject.Inject
import javax.tools.Diagnostic
import javax.tools.DiagnosticListener
import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.ForwardingJavaFileObject
import javax.tools.JavaFileManager.Location
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import rolez.lang.Guarded

import static org.hamcrest.Matchers.*

import static extension org.junit.Assert.*

abstract class GeneratorTest {
    
    @Inject extension ValidationTestHelper
    @Inject extension TestUtils
    
    protected def someClasses() {
        newResourceSet.with('''
            class rolez.lang.Object mapped to java.lang.Object {
                mapped def readonly equals(o: readonly Object): boolean
                mapped def readonly hashCode: int
                mapped def readonly toString: pure String
            }
            class rolez.lang.Slice[T] mapped to rolez.lang.Slice {
                mapped def pure arrayLength: int
                mapped def readonly  get(index: int): T
                mapped def readwrite set(index: int, component: T):
                mapped def r slice[r](begin: int, end: int, step: int): r Slice[T]
            }
            class rolez.lang.Array[T] mapped to rolez.lang.Array extends Slice[T] {
                mapped val length: int
                mapped new(i: int)
            }
            pure class rolez.lang.Vector[T] mapped to rolez.lang.Vector {
                mapped val length: int
                mapped def readonly get(index: int): T
            }
            class rolez.lang.VectorBuilder[T] mapped to rolez.lang.VectorBuilder {
                mapped new(length: int)
                mapped def readonly get(index: int): T
                mapped def readwrite set(index: int, component: T): readwrite VectorBuilder[T]
                mapped def readonly build: readonly Vector[T]
            }
            pure class rolez.lang.String mapped to java.lang.String {
                mapped new(chars: readonly Array[char])
                mapped def pure length: int
                mapped def pure substring(b: int, e: int): pure String
            }
            class rolez.lang.Task[V] mapped to rolez.lang.Task {
                mapped def pure get: V
            }
            object rolez.lang.System mapped to java.lang.System {
                mapped val out: readonly rolez.io.PrintStream
                mapped def readonly exit(code: int):
            }
            class rolez.io.PrintStream mapped to java.io.PrintStream {
                mapped new(file: readonly String)
                mapped def readonly println:
                mapped def readonly println(i: int):
                mapped def readonly println(s: pure String):
            }
            class Base {
                var foo: int
                def pure bar: {}
            }
            pure class PureBase {
                val foo: int = 42
            }
            class foo.bar.Base {
                new {}
                new(i: int) {}
                new(i: int, j: int) {}
            }
            class S {
                slice a { var i: int }
                slice b { def pure foo(i: int): {} }
            }
            class Container[E] mapped to «Container.canonicalName» {
                mapped var e: E
                mapped new
                mapped def readonly  get: E
                mapped def readwrite set(e: E):
            }
            object Tasks {
                task pure foo: {}
                task pure bar(o: readwrite Object): {}
                task pure sum(i: int, j: int): int { return i + j; }
            }
            object Constants {
                val answer: int = 42
            }
            object Asyncer {
                async def pure foo: {}
            }
        ''')
    }
    
    static class Container<E> extends Guarded {
        public var E e
        new() {}
        def E get() { e }
        def void set(E e) { this.e = e }
    }
    
    protected def onlyClass(Program it) {
        assertNoErrors
        classes.size.assertThat(is(1))
        classes.head
    }
    
    @Inject RolezGenerator generator
    
    protected def someClassesCompiled() {
        val fsa = new InMemoryFileSystemAccess
        generator.doGenerate(someClasses.resources.head, fsa, null)
        fsa.textFiles.values
    }
    
    protected def throwExceptionWrapper(String e) {
        '''throw new java.lang.RuntimeException("ROLEZ EXCEPTION WRAPPER", «e»);'''
    }
    
    protected def assertEqualsJava(CharSequence it, CharSequence javaCode, CharSequence... moreJavaCode) {
        assertCompilable(#[javaCode] + moreJavaCode + someClassesCompiled)
        javaCode.toString.assertEquals(toString)
    }
    
    static val className = Pattern.compile("public (final )?(class|interface) ([A-Za-z0-9_£]+) ")

    protected def assertCompilable(Iterable<CharSequence> sources) {
        val compilationUnits = sources.map[code |
            val matcher = className.matcher(code)
            matcher.find.assertTrue
            
            val uri = URI.create("string:///" + matcher.group(3) + ".java")
            new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
                override getCharContent(boolean _) { code }
            }
        ]
        
        // Collect errors
        val errors = new StringBuilder
        val listener = new DiagnosticListener<JavaFileObject> {
            override report(Diagnostic<? extends JavaFileObject> it) {
                // IMPROVE: Emit code without issues, not even "notes"
                if(kind === Diagnostic.Kind.ERROR)
                    errors.append(it).append("\n\n")
            }
        }
        
        val compiler = ToolProvider.systemJavaCompiler
        val stdFileMgr = compiler.getStandardFileManager(null, null, null)
        
        val fileMgr = new ForwardingJavaFileManager(stdFileMgr) {
            override getJavaFileForOutput(Location l, String c, JavaFileObject.Kind k, FileObject s) {
                new ForwardingJavaFileObject(super.getJavaFileForOutput(l, c, k, s)) {
                    override openOutputStream() { ByteStreams.nullOutputStream }
                    override openWriter()       { CharStreams.nullWriter }
                }
            }
        }
        
        compiler.getTask(null, fileMgr, listener, null, null, compilationUnits).call
        "".assertEquals(errors.toString)
    }
}