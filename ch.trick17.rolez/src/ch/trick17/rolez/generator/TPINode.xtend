package ch.trick17.rolez.generator

import java.util.List
import java.util.ArrayList
import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Super
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Slicing
import java.util.Map
import java.util.Collections
import java.util.HashMap
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Type

abstract class TPINode {
	
	public val Type expressionType
	
	private val List<ChildTPINode> children
	private val Map<String, FieldAccessTPINode> fieldAccesses
	private val Map<String, NoArgMethodCallTPINode> noArgMethodCalls
	private val Map<String, SlicingTPINode> slicings
	private var TPIRole role = TPIRole.PURE
	private var TPIRole childRole = TPIRole.PURE
	private var boolean standalone = false
	
	new(Type expressionType) {
		this.children = new ArrayList()
		this.fieldAccesses = new HashMap()
		this.noArgMethodCalls = new HashMap()
		this.slicings = new HashMap()
		this.expressionType = expressionType
	}
	
	protected dispatch def void addChild(FieldAccessTPINode child) {
		this.children.add(child)
		this.fieldAccesses.put(child.id(), child)
	}
	
	protected dispatch def void addChild(NoArgMethodCallTPINode child) {
		this.children.add(child)
		this.noArgMethodCalls.put(child.id(), child)
	}
	
	protected dispatch def void addChild(SlicingTPINode child) {
		this.children.add(child)
		this.slicings.put(child.id(), child)
	}
	
	def ChildTPINode findChild(TPINodeType nodeType, String name) {
		switch (nodeType) {
			case TPINodeType.FIELD_ACCESS:
				this.fieldAccesses.get(name)
			case TPINodeType.NO_ARG_METHOD_CALL:
				this.noArgMethodCalls.get(name)
			case TPINodeType.SLICING:
				this.slicings.get(name)
			default:
				null
		}
	}
	
	def List<ChildTPINode> getChildren() {
		Collections.unmodifiableList(this.children)
	}
	
	abstract def TPINode getParent()
	
	abstract def boolean hasParent()
	
	def TPIRole getRole() {
		this.role
	}
	
	def void setRole(TPIRole role) {
		if (isStrongerThan(role, this.role)) {
			this.role = role
			if (hasParent)
				parent.propagateChildRole(role)
		}
	}
	
	def TPIRole getChildRole() {
		this.childRole
	}
		
	private def void propagateChildRole(TPIRole role) {
		if (isStrongerThan(role, this.childRole)) {
			this.childRole = role
			if (hasParent)
				parent.propagateChildRole(role)
		}
	}
	
	abstract def TPINode getRoot()
	
	def boolean isStandalone() {
		this.standalone
	}
	
	def void setStandalone() {
		this.standalone = true
	}
	
	abstract def boolean matches(Expr expr)
	
	abstract def TPINodeType nodeType()
	
	abstract def String id()
	
	static def boolean isStrongerThan(TPIRole role1, TPIRole role2) {
		if (role1 == TPIRole.PURE)
			false
		else if (role1 == TPIRole.READ_ONLY)
			role2 == TPIRole.PURE
		else
			role2 != TPIRole.READ_WRITE
	}
	
}
	
abstract class ChildTPINode extends TPINode {

	val TPINode parent
	public val String name

	new(TPINode parent, String name, Type expressionType) {
		super(expressionType)
		this.parent = parent
		this.name = name
		
		this.parent.addChild(this)
	}
	
	override TPINode getParent() {
		this.parent
	}
	
	override boolean hasParent() {
		true
	}
	
	override TPINode getRoot() {
		this.parent.getRoot()
	}
	
	override id() {
		this.name
	}

}

class FieldAccessTPINode extends ChildTPINode {
	
	new(TPINode parent, String fieldName, Type expressionType) {
		super(parent, fieldName, expressionType)
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		if (expr instanceof MemberAccess)
			expr.fieldAccess && expr.field.name.equals(name) && parent.matches(expr.target)
		false
	}
	
	override nodeType() {
		TPINodeType.FIELD_ACCESS
	}
	
	override toString() {
		parent.toString() + "." + this.name
	}
	
}

class NoArgMethodCallTPINode extends ChildTPINode {
	
	new(TPINode parent, String methodName, Type expressionType) {
		super(parent, methodName, expressionType)
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		if (expr instanceof MemberAccess)
			expr.methodInvoke && expr.method.name.equals(name) && expr.args.empty && parent.matches(expr.target)
		false
	}
	
	override nodeType() {
		TPINodeType.NO_ARG_METHOD_CALL
	}
	
	override toString() {
		parent.toString() + "." + this.name + "()"
	}
	
}

class SlicingTPINode extends ChildTPINode {
	
	new(TPINode parent, String sliceName, Type expressionType) {
		super(parent, sliceName, expressionType)
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		if (expr instanceof Slicing)
			expr.slice.name.equals(name) && parent.matches(expr.target)
		false
	}
	
	override nodeType() {
		TPINodeType.SLICING
	}
	
	override toString() {
		parent.toString() + " slice " + this.name
	}
	
}

abstract class RootTPINode extends TPINode {
	
	new(Type expressionType) {
		super(expressionType)
	}
	
	override TPINode getParent() {
		null
	}
	
	override boolean hasParent() {
		false
	}
	
	override TPINode getRoot() {
		this
	}
	
	override toString() {
		this.id()
	}
	
}

class InferredParamTPINode extends RootTPINode {
	
	public val int index
	public val TPINode param
	
	new(int index, TPINode param) {
		super(param.expressionType)
		this.index = index
		this.param = param
	}
	
	override matches(Expr expr) {
		param.matches(expr)
	}
	
	override nodeType() {
		TPINodeType.INFERRED_PARAM
	}
	
	override id() {
		"$tpi" + this.index
	}
	
}

class LocalVarTPINode extends RootTPINode {
	
	public val String name
	
	new(String name, Type expressionType) {
		super(expressionType)
		this.name = name
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		expr instanceof VarRef && (expr as VarRef).variable.name.equals(this.name)
	}
	
	override nodeType() {
		TPINodeType.LOCAL_VAR
	}
	
	override id() {
		this.name
	}
	
}

class ThisTPINode extends RootTPINode {
	
	public val Class enclosingClass
	
	new(Type expressionType, Class enclosingClass) {
		super(expressionType)
		this.enclosingClass = enclosingClass
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		expr instanceof This || expr instanceof Super
	}
	
	override nodeType() {
		TPINodeType.THIS
	}
	
	override id() {
		"this"
	}
	
}

enum TPINodeType {
	FIELD_ACCESS, NO_ARG_METHOD_CALL, SLICING, LOCAL_VAR, THIS, INFERRED_PARAM
}

enum TPIRole {
	PURE, READ_ONLY, READ_WRITE
}
