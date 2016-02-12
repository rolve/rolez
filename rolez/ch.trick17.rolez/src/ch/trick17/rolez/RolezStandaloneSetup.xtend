package ch.trick17.rolez

import com.google.inject.Injector
import org.eclipse.emf.ecore.EPackage
import ch.trick17.rolez.rolez.RolezPackage

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class RolezStandaloneSetup extends RolezStandaloneSetupGenerated {
    
    private static val ROLEZ_MODEL = "http://trick17.ch/rolez/Rolez"

	def static void doSetup() {
		new RolezStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
	
    override register(Injector injector) {
        if(!EPackage.Registry.INSTANCE.containsKey(ROLEZ_MODEL))
            EPackage.Registry.INSTANCE.put(ROLEZ_MODEL, RolezPackage.eINSTANCE);
        super.register(injector);
    }
}
