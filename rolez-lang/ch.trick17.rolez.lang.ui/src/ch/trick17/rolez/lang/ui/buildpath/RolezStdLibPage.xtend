package ch.trick17.rolez.lang.ui.buildpath

import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage
import org.eclipse.jdt.ui.wizards.NewElementWizardPage
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.Label

class RolezStdLibPage extends NewElementWizardPage implements IClasspathContainerPage {
    
    val IClasspathEntry containerEntry
    
    new() {
        super("RolezStandardLibrary")
        title = "Rolez Standard Library"
        imageDescriptor = org.eclipse.jdt.internal.ui.JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY
        description = "This library contains the classes required for every Rolez program."
        containerEntry = JavaCore.newContainerEntry(RolezStdLibInitializer.ROLEZ_STD_LIB_PATH)
    }
    
    override createControl(Composite parent) {
        val composite = new Composite(parent, SWT.NONE) => [
            layout = new FillLayout
        ]
        new Label(composite, SWT.NONE) => [
            text = "This library contains the classes required for every Rolez program, and then some."
        ]
        control = composite
    }
    
    override finish() { true }
    override getSelection() { containerEntry }
    override setSelection(IClasspathEntry containerEntry) {}
}
