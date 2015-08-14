package ch.trick17.peppl.lang.scoping

import ch.trick17.peppl.lang.peppl.Method
import ch.trick17.peppl.lang.typesystem.PepplSystem
import java.util.ArrayList
import javax.inject.Inject
import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider
import org.eclipse.xtext.naming.QualifiedName

class PepplNameProvider extends DefaultDeclarativeQualifiedNameProvider {
    
    @Inject
    private extension PepplSystem
    
    def QualifiedName qualifiedName(Method m) {
        val segments = new ArrayList(m.eContainer.fullyQualifiedName.segments)
        segments.add(m.name + ":" + m.thisRole
                + m.params.join("(", ",", ")", [type.stringRep]))
        QualifiedName.create(segments)
    }
}