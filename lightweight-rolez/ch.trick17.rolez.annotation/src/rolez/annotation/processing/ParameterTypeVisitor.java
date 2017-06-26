package rolez.annotation.processing;


import javax.lang.model.element.TypeElement;
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
import javax.lang.model.util.Types;

public class ParameterTypeVisitor implements TypeVisitor<Boolean,Types> {
	
	@Override
	public Boolean visit(TypeMirror t, Types p) {
		return null;
	}

	@Override
	public Boolean visit(TypeMirror t) {
		return null;
	}

	@Override
	public Boolean visitPrimitive(PrimitiveType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitNull(NullType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitArray(ArrayType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitDeclared(DeclaredType t, Types p) {
		TypeElement declaredType = (TypeElement) t.asElement();
		rolez.annotation.Guarded annotation = declaredType.getAnnotation(rolez.annotation.Guarded.class);
		return annotation != null;
	}

	@Override
	public Boolean visitError(ErrorType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitTypeVariable(TypeVariable t, Types p) {
		return null;
	}

	@Override
	public Boolean visitWildcard(WildcardType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitExecutable(ExecutableType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitNoType(NoType t, Types p) {
		return null;
	}

	@Override
	public Boolean visitUnknown(TypeMirror t, Types p) {
		return null;
	}

	@Override
	public Boolean visitUnion(UnionType t, Types p) {
		return null;
	}
}
