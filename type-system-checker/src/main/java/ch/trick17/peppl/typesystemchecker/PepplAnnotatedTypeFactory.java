package ch.trick17.peppl.typesystemchecker;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import ch.trick17.peppl.typesystemchecker.qual.ReadOnly;
import ch.trick17.peppl.typesystemchecker.qual.ReadWrite;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;

public class PepplAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    
    public final AnnotationMirror readWrite = AnnotationUtils.fromClass(
            elements, ReadWrite.class);
    public final AnnotationMirror readOnly = AnnotationUtils.fromClass(
            elements, ReadOnly.class);
    /**
     * A annotation in the PEPPL hierarchy. To be used with
     * {@link AnnotatedTypeMirror#getAnnotationInHierarchy(AnnotationMirror)} or
     * {@link AnnotatedTypeMirror#getEffectiveAnnotationInHierarchy(AnnotationMirror)}
     */
    public final AnnotationMirror peppl = readWrite;
    
    public PepplAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        shouldCache = false;
        postInit();
    }
    
    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type,
            boolean iUseFlow) {
        super.annotateImplicit(tree, type, iUseFlow);
        
        /* After everything else, apply custom rules */
        new PepplAnnotator().visit(tree, type);
    }
    
    private class PepplAnnotator extends TreeAnnotator {
        
        public PepplAnnotator() {
            super(PepplAnnotatedTypeFactory.this);
        }
        
        @Override
        public Void visitMemberSelect(MemberSelectTree node,
                AnnotatedTypeMirror type) {
            handleFieldAccess(node, type);
            return null;
        }
        
        @Override
        public Void visitIdentifier(IdentifierTree node,
                AnnotatedTypeMirror type) {
            handleFieldAccess(node, type);
            return null;
        }
        
        private void handleFieldAccess(ExpressionTree node,
                AnnotatedTypeMirror type) {
            if(TreeUtils.isFieldAccess(node) && !type.getKind().isPrimitive()) {
                AnnotationMirror recAnnot = getReceiverType(node)
                        .getEffectiveAnnotationInHierarchy(peppl);
                AnnotationMirror fieldAnnot = type
                        .getEffectiveAnnotationInHierarchy(peppl);
                
                type.replaceAnnotation(getQualifierHierarchy().leastUpperBound(
                        recAnnot, fieldAnnot));
            }
        }
    }
}
