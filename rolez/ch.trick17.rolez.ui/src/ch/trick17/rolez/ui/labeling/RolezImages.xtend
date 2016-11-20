package ch.trick17.rolez.ui.labeling

import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider
import org.eclipse.xtext.xbase.ui.labeling.XbaseImages2

class RolezImages extends XbaseImages2 {
    
    def forSingletonClass() { "object.gif" }
    
    override protected imagesSize() {
        JavaElementImageProvider.SMALL_SIZE
    }
}