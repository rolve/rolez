package ch.trick17.rolez.generator

import java.util.List
import java.util.ArrayList
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

abstract class TPINode {
	
	private val List<ChildTPINode> children
	private val Map<String, FieldAccessTPINode> fieldAccesses
	private val Map<String, NoArgMethodCallTPINode> noArgMethodCalls
	private val Map<String, SlicingTPINode> slicings
	private var TPIRole role = TPIRole.PURE
	private var TPIRole childRole = TPIRole.PURE
	private var boolean standalone = false
	
	new() {
		this.children = new ArrayList()
		this.fieldAccesses = new HashMap()
		this.noArgMethodCalls = new HashMap()
		this.slicings = new HashMap()
	}
	
	protected dispatch def addChild(FieldAccessTPINode child) {
		this.children.add(child)
		this.fieldAccesses.put(child.name, child)
	}
	
	protected dispatch def addChild(NoArgMethodCallTPINode child) {
		this.children.add(child)
		this.noArgMethodCalls.put(child.name, child)
	}
	
	protected dispatch def addChild(SlicingTPINode child) {
		this.children.add(child)
		this.slicings.put(child.name, child)
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

	new(TPINode parent, String name) {
		this.parent = parent
		this.parent.addChild(this)
		this.name = name
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

}

class FieldAccessTPINode extends ChildTPINode {
	
	new(TPINode parent, String fieldName) {
		super(parent, fieldName)
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
	
}

class NoArgMethodCallTPINode extends ChildTPINode {
	
	new(TPINode parent, String methodName) {
		super(parent, methodName)
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
	
}

class SlicingTPINode extends ChildTPINode {
	
	new(TPINode parent, String sliceName) {
		super(parent, sliceName)
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
	
}

abstract class RootTPINode extends TPINode {
	
	override TPINode getParent() {
		null
	}
	
	override boolean hasParent() {
		false
	}
	
	override TPINode getRoot() {
		this
	}
	
}

class ThisTPINode extends RootTPINode {
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		expr instanceof This || expr instanceof Super
	}
	
	override nodeType() {
		TPINodeType.THIS
	}
	
}

class LocalVarTPINode extends RootTPINode {
	
	public val String name
	
	new(String name) {
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
	
}

enum TPINodeType {
	FIELD_ACCESS, NO_ARG_METHOD_CALL, SLICING, THIS, LOCAL_VAR
}

enum TPIRole {
	PURE, READ_ONLY, READ_WRITE
}
