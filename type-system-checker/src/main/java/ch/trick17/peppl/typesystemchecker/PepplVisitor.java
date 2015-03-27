package ch.trick17.peppl.typesystemchecker;

import static ch.trick17.peppl.typesystemchecker.PepplChecker.ILLEGAL_WRITE;
import static java.util.Arrays.asList;

import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.source.Result;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedDeclaredType;
import org.checkerframework.javacutil.TreeUtils;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

public class PepplVisitor extends BaseTypeVisitor<PepplAnnotatedTypeFactory> {
    
    public PepplVisitor(BaseTypeChecker checker) {
        super(checker);
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
        if(TreeUtils.isFieldAccess(lhs)) {
            AnnotatedTypeMirror recType = atypeFactory.getReceiverType(lhs);
            if(!atypeFactory.getQualifierHierarchy().isSubtype(
                    recType.getAnnotations(), asList(atypeFactory.readWrite))) {
                checker.report(Result.failure(ILLEGAL_WRITE, recType), node);
            }
        }
        return super.visitAssignment(node, p);
    }
}
