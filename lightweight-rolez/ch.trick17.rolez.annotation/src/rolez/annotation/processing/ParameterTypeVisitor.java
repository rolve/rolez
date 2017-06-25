package rolez.annotation.processing;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.tools.Diagnostic.Kind;

import rolez.annotation.Guarded;

public class ParameterTypeVisitor implements TypeVisitor<Message,Void> {
	@Override
	public Message visit(TypeMirror t, Void p) {
		return null;
	}

	@Override
	public Message visit(TypeMirror t) {
		return null;
	}

	@Override
	public Message visitPrimitive(PrimitiveType t, Void p) {
		return null;
	}

	@Override
	public Message visitNull(NullType t, Void p) {
		return null;
	}

	@Override
	public Message visitArray(ArrayType t, Void p) {
		return null;
	}

	@Override
	public Message visitDeclared(DeclaredType t, Void p) {
		Element declaredType = t.asElement();
		Guarded annotation = declaredType.getAnnotation(Guarded.class);
		if (annotation == null) {
			return new Message(Kind.ERROR, "Parameter has to be a guarded Type (i.e. annotated with @Guarded).");
		}
		
		return null;
	}

	@Override
	public Message visitError(ErrorType t, Void p) {
		return null;
	}

	@Override
	public Message visitTypeVariable(TypeVariable t, Void p) {
		return null;
	}

	@Override
	public Message visitWildcard(WildcardType t, Void p) {
		return null;
	}

	@Override
	public Message visitExecutable(ExecutableType t, Void p) {
		return null;
	}

	@Override
	public Message visitNoType(NoType t, Void p) {
		return null;
	}

	@Override
	public Message visitUnknown(TypeMirror t, Void p) {
		return null;
	}

	@Override
	public Message visitUnion(UnionType t, Void p) {
		return null;
	}
}
