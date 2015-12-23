package ch.trick17.rolez


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
class RolezStandaloneSetup extends RolezStandaloneSetupGenerated {

	def static void doSetup() {
		new RolezStandaloneSetup().createInjectorAndDoEMFRegistration()
	}
}
