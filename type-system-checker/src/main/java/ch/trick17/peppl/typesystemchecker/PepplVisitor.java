package ch.trick17.peppl.typesystemchecker;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;

import com.sun.source.tree.Tree;

public class PepplVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {
    
    public PepplVisitor(BaseTypeChecker checker) {
        super(checker);
    }
    
    /**
     * The default implementation checks whether <code>useType</code> is a
     * subclass of <code>declarationType</code>. So far, we don't need this
     * check and, because the default qualifier (applied to all declarations) is
     * {@link ReadWrite}, we don't <em>want</em> it.
     */
    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType, Tree tree) {
        return true;
    }
}
