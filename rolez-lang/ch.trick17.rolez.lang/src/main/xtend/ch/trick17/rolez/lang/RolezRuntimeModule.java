package ch.trick17.rolez.lang;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import ch.trick17.rolez.lang.scoping.RolezImportedNamespaceScopeProvider;

/**
 * Use this class to register components to be used at runtime / without the
 * Equinox extension registry.
 */
public class RolezRuntimeModule extends ch.trick17.rolez.lang.AbstractRolezRuntimeModule {
    
    @Override
    public void configureIScopeProviderDelegate(Binder binder) {
        binder.bind(IScopeProvider.class).annotatedWith(Names.named(AbstractDeclarativeScopeProvider.NAMED_DELEGATE))
                .to(RolezImportedNamespaceScopeProvider.class);
    }
    
    @Override
    public Class<? extends IValueConverterService> bindIValueConverterService() {
        return RolezValueConverterService.class;
    }
}
