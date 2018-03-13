package ch.trick17.rolez.ui.buildpath

import org.apache.log4j.Logger
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

class RolezStdLib implements IClasspathContainer {
    
    static val ROLEZ_LIB_BUNDLE_ID = "ch.trick17.rolez.lib"
    static val SOURCE_SUFFIX = ".source"
    
    val logger = Logger.getLogger(RolezStdLib)
    
    val IPath containerPath
    var IClasspathEntry[] classpathEntries
    
    new(IPath containerPath) {
        this.containerPath = containerPath
    }
    
    override getClasspathEntries() {
        if(classpathEntries === null) {
            val libBundle = Platform.getBundle(ROLEZ_LIB_BUNDLE_ID)
            val libPath = new Path(FileLocator.getBundleFile(libBundle).absolutePath)
            val sourcePath =
                try {
                    val bundlesFolder = libPath.removeLastSegments(1)
                    val jarName = libPath.lastSegment
                    val sourceJarName = jarName.replace(libBundle.symbolicName,
                            libBundle.symbolicName + SOURCE_SUFFIX)
                    val sourceJar = bundlesFolder.append(sourceJarName)
                    if(sourceJar.toFile.exists)
                        sourceJar
                } catch(Exception e) {
                    logger.warn("Exception during source bundle investigation.", e)
                    null
                }
            classpathEntries = #[JavaCore.newLibraryEntry(libPath, sourcePath, null)]
        }
        classpathEntries
    }
    
    override getDescription() { "Rolez Standard Library" }
    
    override getKind() { K_APPLICATION }
    
    override getPath() { containerPath }
}
