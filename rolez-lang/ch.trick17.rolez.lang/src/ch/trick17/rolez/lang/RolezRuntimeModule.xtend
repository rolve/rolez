package ch.trick17.rolez.lang

import ch.trick17.rolez.lang.desugar.DesugaringLazyLinkingResource
import ch.trick17.rolez.lang.desugar.IDesugarer
import ch.trick17.rolez.lang.desugar.RolezDesugarer
import ch.trick17.rolez.lang.rolez.RolezFactory
import ch.trick17.rolez.lang.scoping.RolezImportedNamespaceScopeProvider
import ch.trick17.rolez.lang.typesystem.RolezValidatorFilter
import com.google.inject.Binder
import com.google.inject.name.Names
import it.xsemantics.runtime.StringRepresentation
import it.xsemantics.runtime.validation.XsemanticsValidatorFilter
import org.eclipse.xtext.conversion.IValueConverterService
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.scoping.IScopeProvider
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
class RolezRuntimeModule extends AbstractRolezRuntimeModule {
    
    override configure(Binder binder) {
        super.configure(binder);
        binder.bind(IDesugarer).to(RolezDesugarer);
    }
    
    override configureIScopeProviderDelegate(Binder binder) {
        binder.bind(IScopeProvider).annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE))
                .to(RolezImportedNamespaceScopeProvider);
    }
    
    override Class<? extends IValueConverterService> bindIValueConverterService() {
        RolezValueConverterService;
    }
    
    override Class<? extends XtextResource> bindXtextResource() {
        DesugaringLazyLinkingResource;
    }
    
    def Class<? extends StringRepresentation> bindStringRepresentation() {
        StringRepresentationFix;
    }
    
    def Class<? extends XsemanticsValidatorFilter> bindValidatorFilter() {
        RolezValidatorFilter;
    }
    
    def RolezFactory bindRolezFactoryToInstance() {
        return RolezFactory.eINSTANCE;
    }
}
