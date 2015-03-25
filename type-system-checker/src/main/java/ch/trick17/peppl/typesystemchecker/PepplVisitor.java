package ch.trick17.peppl.typesystemchecker;

import static ch.trick17.peppl.typesystemchecker.PepplChecker.ILLEGAL_READ;
import static ch.trick17.peppl.typesystemchecker.PepplChecker.ILLEGAL_WRITE;
import static java.util.Arrays.asList;

import javax.lang.model.element.AnnotationMirror;

import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;

public class PepplVisitor extends BaseTypeVisitor<BaseAnnotatedTypeFactory> {
    
    private final AnnotationMirror INACCESSIBLE, READONLY, READWRITE;
    
    public PepplVisitor(BaseTypeChecker checker) {
        super(checker);
        
        INACCESSIBLE = AnnotationUtils.fromClass(elements, Inaccessible.class);
        READONLY = AnnotationUtils.fromClass(elements, ReadOnly.class);
        READWRITE = AnnotationUtils.fromClass(elements, ReadWrite.class);
    }
    
    /* The default implementation checks whether useType is a subclass of
     * declarationType. So far, we don't need this check and, because the
     * default qualifier (applied to all declarations) is ReadWrite, we don't
     * *want* it. */
    @Override
    public boolean isValidUse(AnnotatedDeclaredType declarationType,
            AnnotatedDeclaredType useType, Tree tree) {
        return true;
    }
    
    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree lhs = node.getVariable();
        if(TreeUtils.isFieldAccess(lhs))
            if(!atypeFactory.getQualifierHierarchy().isSubtype(
                    atypeFactory.getReceiverType(lhs).getAnnotations(),
                    asList(READWRITE))) {
                checker.report(Result.failure(ILLEGAL_WRITE, node), node);
            }
        return super.visitAssignment(node, p);
    }
    
    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void p) {
        checkFieldRead(node);
        return super.visitMemberSelect(node, p);
    }
    
    @Override
    public Void visitIdentifier(IdentifierTree node, Void p) {
        checkFieldRead(node);
        return super.visitIdentifier(node, p);
    }
    
    private void checkFieldRead(ExpressionTree node) {
        if(TreeUtils.isFieldAccess(node))
            if(!atypeFactory.getQualifierHierarchy().isSubtype(
                    atypeFactory.getReceiverType(node).getAnnotations(),
                    asList(READONLY))) {
                checker.report(Result.failure(ILLEGAL_READ, node), node);
            }
    }
}
