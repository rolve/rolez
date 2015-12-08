package ch.trick17.rolez.lang.ui.buildpath

import org.eclipse.jdt.core.IClasspathContainer
import org.eclipse.core.runtime.IPath

class RolezStdLib implements IClasspathContainer {
    
    val IPath containerPath
    
    new(IPath containerPath) {
        this.containerPath = containerPath
    }
    
    override getClasspathEntries() {
        #[] // TODO
    }
    
    override getDescription() { "Rolez Standard Library" }
    
    override getKind() { K_APPLICATION }
    
    override getPath() { containerPath }
}