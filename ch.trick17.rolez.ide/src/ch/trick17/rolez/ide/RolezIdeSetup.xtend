package ch.trick17.rolez.ide

import ch.trick17.rolez.RolezRuntimeModule
import ch.trick17.rolez.RolezStandaloneSetup
import com.google.inject.Guice
import org.eclipse.xtext.util.Modules2

/**
 * Initialization support for running Xtext languages as language servers.
 */
class RolezIdeSetup extends RolezStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new RolezRuntimeModule, new RolezIdeModule))
	}
	
}
