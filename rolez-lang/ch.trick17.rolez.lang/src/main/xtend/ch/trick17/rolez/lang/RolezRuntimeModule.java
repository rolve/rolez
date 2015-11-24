package ch.trick17.rolez.lang;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import ch.trick17.rolez.lang.desugar.DesugaringLazyLinkingResource;
import ch.trick17.rolez.lang.desugar.IDesugarer;
import ch.trick17.rolez.lang.desugar.RolezDesugarer;
import ch.trick17.rolez.lang.scoping.RolezImportedNamespaceScopeProvider;
import ch.trick17.rolez.lang.typesystem.RolezValidatorFilter;
import it.xsemantics.runtime.StringRepresentation;
import it.xsemantics.runtime.validation.XsemanticsValidatorFilter;

/**
 * Use this class to register components to be used at runtime / without the
 * Equinox extension registry.
 */
public class RolezRuntimeModule extends ch.trick17.rolez.lang.AbstractRolezRuntimeModule {
    
    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.bind(IDesugarer.class).to(RolezDesugarer.class);
    }
    
    @Override
    public void configureIScopeProviderDelegate(Binder binder) {
        binder.bind(IScopeProvider.class).annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE))
                .to(RolezImportedNamespaceScopeProvider.class);
    }
    
    @Override
    public Class<? extends IValueConverterService> bindIValueConverterService() {
        return RolezValueConverterService.class;
    }
    
    @Override
    public Class<? extends XtextResource> bindXtextResource() {
        return DesugaringLazyLinkingResource.class;
    }
    
    public Class<? extends StringRepresentation> bindStringRepresentation() {
        return StringRepresentationFix.class;
    }
    
    public Class<? extends XsemanticsValidatorFilter> bindValidatorFilter() {
        return RolezValidatorFilter.class;
    }
}
