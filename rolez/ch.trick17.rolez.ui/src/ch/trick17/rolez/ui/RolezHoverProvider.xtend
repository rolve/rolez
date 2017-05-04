package ch.trick17.rolez.ui

import ch.trick17.rolez.rolez.Program
import javax.inject.Inject
import org.eclipse.emf.ecore.EObject
import org.eclipse.jdt.internal.ui.JavaPlugin
import org.eclipse.jface.internal.text.html.HTMLPrinter
import org.eclipse.jface.viewers.ILabelProvider
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider
import org.eclipse.xtext.ui.label.ILabelProviderImageDescriptorExtension

class RolezHoverProvider extends DefaultEObjectHoverProvider {

    static val FIRST_LINE_BEGIN = "<div style='margin-top: 0.5em; margin-left: 0.2em; white-space: nowrap;'>"
    static val FIRST_LINE_END   = "<span style='visibility:hidden;'>hack</span></div>"

    @Inject ILabelProvider labelProvider
    
    override protected hasHover(EObject o) {
        super.hasHover(o) && !(o instanceof Program)
    }

    override protected getFirstLine(EObject o) {
        val imageTag = imageTag(o)
        val label = HTMLPrinter.convertToHTMLContent(getLabel(o))
        if(imageTag !== null)
            '''«FIRST_LINE_BEGIN»«imageTag» <b>«label»</b>«FIRST_LINE_END»'''
    }

    private def imageTag(EObject o) {
        if (labelProvider instanceof ILabelProviderImageDescriptorExtension) {
            val descriptor = labelProvider.getImageDescriptor(o)
            if(descriptor !== null) {
                val url = JavaPlugin.getDefault().getImagesOnFSRegistry().getImageURL(descriptor)
                if (url !== null)
                    '''<image src='«url.toExternalForm()»' style='vertical-align:middle;' />'''
            }
        }
    }
}