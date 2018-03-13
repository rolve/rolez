package ch.trick17.rolez.ui.buildpath

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.ClasspathContainerInitializer
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.JavaModelException

class RolezStdLibInitializer extends ClasspathContainerInitializer {
    
    public static val ROLEZ_STD_LIB_PATH = new Path("ch.trick17.rolez.ROLEZ_CONTAINER")
    
    override initialize(IPath containerPath, IJavaProject project) throws JavaModelException {
        if(ROLEZ_STD_LIB_PATH.equals(containerPath))
            JavaCore.setClasspathContainer(containerPath, #[project], #[new RolezStdLib(containerPath)], null)
    }
}