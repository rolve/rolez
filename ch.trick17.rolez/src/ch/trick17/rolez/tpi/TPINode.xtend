package ch.trick17.rolez.tpi

import ch.trick17.rolez.rolez.Class
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.Super
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Type
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.rolez.SingletonClass

abstract class TPINode {
	
	public val Type expressionType
	
	private val List<ChildTPINode> children
	private val Map<String, FieldAccessTPINode> fieldAccesses
	private val Map<String, NoArgMethodCallTPINode> noArgMethodCalls
	private val Map<String, SlicingTPINode> slicings
	private val Map<String, StepVarArgMethodCallTPINode> stepVarArgMethodCalls
	private var TPIRole role = TPIRole.PURE
	private var TPIRole childRole = TPIRole.PURE
	private var boolean standalone = false
	private var TPIRole standaloneRole = TPIRole.PURE
	
	new(Type expressionType) {
		this.children = new ArrayList()
		this.fieldAccesses = new HashMap()
		this.noArgMethodCalls = new HashMap()
		this.slicings = new HashMap()
		this.stepVarArgMethodCalls = new HashMap()
		this.expressionType = expressionType
	}
	
	protected new(Type expressionType, TPIRole role) {
		this(expressionType)
		this.role = role
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
	
	protected dispatch def void addChild(StepVarArgMethodCallTPINode child) {
		this.children.add(child)
		this.stepVarArgMethodCalls.put(child.id(), child)
	}
	
	def ChildTPINode findChild(TPINodeType nodeType, String name) {
		switch (nodeType) {
			case TPINodeType.FIELD_ACCESS:
				this.fieldAccesses.get(name)
			case TPINodeType.NO_ARG_METHOD_CALL:
				this.noArgMethodCalls.get(name)
			case TPINodeType.SLICING:
				this.slicings.get(name)
			case TPINodeType.STEP_VAR_ARG_METHOD_CALL:
				this.stepVarArgMethodCalls.get(name)
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
		if (isStrongerThan(role, this.childRole))
			this.childRole = role
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
	
	def void setStandalone(TPIRole role) {
		this.standalone = true
		if (isStrongerThan(role, this.standaloneRole))
			this.standaloneRole = role
	}
	
	def TPIRole getStandaloneRole() {
		this.standaloneRole
	}
	
	abstract def boolean matches(Expr expr)
	
	abstract def TPINodeType nodeType()
	
	abstract def String id()
	
	abstract def TPINode standaloneNode()
	
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
	
	protected new(TPINode parent, String name, Type expressionType, TPIRole role) {
		super(expressionType, role)
		this.parent = parent
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
	
	override id() {
		this.name
	}

}

class FieldAccessTPINode extends ChildTPINode {
	
	new(TPINode parent, String fieldName, Type expressionType) {
		super(parent, fieldName, expressionType)
	}
	
	private new(TPINode parent, String name, Type expressionType, TPIRole role) {
		super(parent, name, expressionType, role)
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else if (expr instanceof MemberAccess)
			expr.fieldAccess && expr.field.name.equals(name) && parent.matches(expr.target)
		else
			false
	}
	
	override nodeType() {
		TPINodeType.FIELD_ACCESS
	}
	
	override toString() {
		parent.toString() + "." + this.name
	}
	
	override standaloneNode() {
		new FieldAccessTPINode(this.parent, this.name, this.expressionType, this.standaloneRole)
	}
	
}

class NoArgMethodCallTPINode extends ChildTPINode {
	
	new(TPINode parent, String methodName, Type expressionType) {
		super(parent, methodName, expressionType)
	}
	
	private new(TPINode parent, String name, Type expressionType, TPIRole role) {
		super(parent, name, expressionType, role)
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else if (expr instanceof MemberAccess)
			expr.methodInvoke && expr.method.name.equals(name) && expr.args.empty && parent.matches(expr.target)
		else
			false
	}
	
	override nodeType() {
		TPINodeType.NO_ARG_METHOD_CALL
	}
	
	override toString() {
		parent.toString() + "." + this.name + "()"
	}
	
	override standaloneNode() {
		new NoArgMethodCallTPINode(this.parent, this.name, this.expressionType, this.standaloneRole)
	}
	
}

class SlicingTPINode extends ChildTPINode {
	
	new(TPINode parent, String sliceName, Type expressionType) {
		super(parent, sliceName, expressionType)
	}
	
	private new(TPINode parent, String name, Type expressionType, TPIRole role) {
		super(parent, name, expressionType, role)
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else if (expr instanceof Slicing)
			expr.slice.name.equals(name) && parent.matches(expr.target)
		else
			false
	}
	
	override nodeType() {
		TPINodeType.SLICING
	}
	
	override toString() {
		parent.toString() + " slice " + this.name
	}
	
	override standaloneNode() {
		new SlicingTPINode(this.parent, this.name, this.expressionType, this.standaloneRole)
	}
	
}

class StepVarArgMethodCallTPINode extends ChildTPINode {
	
	public static def boolean isStepVarArgMethodCall(MemberAccess expr, String stepVar) {
		if (expr.args.length == 1) {
			val arg = expr.args.get(0)
			if (arg instanceof Ref)
				return arg.varRef && arg.variable.name == stepVar
		}
		false
	}
	
	public val String stepVar
	
	new(TPINode parent, String methodName, Type expressionType, String stepVar) {
		super(parent, methodName, expressionType)
		this.stepVar = stepVar
	}
	
	private new(TPINode parent, String name, Type expressionType, String stepVar, TPIRole role) {
		super(parent, name, expressionType, role)
		this.stepVar = stepVar
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else if (expr instanceof MemberAccess)
			expr.methodInvoke && expr.method.name.equals(name) && parent.matches(expr.target) && isStepVarArgMethodCall(expr, stepVar)
		else
			false
	}
	
	override nodeType() {
		TPINodeType.STEP_VAR_ARG_METHOD_CALL
	}
	
	override toString() {
		parent.toString() + "." + this.name + "(" + stepVar + ")"
	}
	
	override standaloneNode() {
		new StepVarArgMethodCallTPINode(this.parent, this.name, this.expressionType, this.stepVar, this.standaloneRole)
	}
	
}

abstract class RootTPINode extends TPINode {
	
	new(Type expressionType) {
		super(expressionType)
	}
	
	protected new(Type expressionType, TPIRole role) {
		super(expressionType, role)
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
	
	new(int index, TPINode param, TPIRole role) {
		super(param.expressionType, role)
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
	
	override standaloneNode() {
		new InferredParamTPINode(this.index, this.param, this.standaloneRole)
	}
	
}

class LocalVarTPINode extends RootTPINode {
	
	public val String name
	
	new(String name, Type expressionType) {
		super(expressionType)
		this.name = name
	}
	
	new(String name, Type expressionType, TPIRole role) {
		super(expressionType)
		this.name = name
		this.role = role
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else
			expr instanceof Ref && (expr as Ref).varRef && (expr as Ref).variable.name.equals(this.name)
	}
	
	override nodeType() {
		TPINodeType.LOCAL_VAR
	}
	
	override id() {
		this.name
	}
	
	override standaloneNode() {
		new LocalVarTPINode(this.name, this.expressionType, this.standaloneRole)
	}
	
}

class SingletonTPINode extends RootTPINode {
	
	public val SingletonClass singleton
	
	new(SingletonClass singleton, Type expressionType) {
		super(expressionType)
		this.singleton = singleton
	}
	
	new(SingletonClass singleton, Type expressionType, TPIRole role) {
		super(expressionType)
		this.singleton = singleton
		this.role = role
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else
			expr instanceof Ref && (expr as Ref).singletonRef && (expr as Ref).referee === this.singleton
	}
	
	override nodeType() {
		TPINodeType.SINGLETON
	}
	
	override id() {
		this.singleton.qualifiedName.toString
	}
	
	override standaloneNode() {
		new SingletonTPINode(this.singleton, this.expressionType, this.standaloneRole)
	}
	
}

class StepVarTPINode extends LocalVarTPINode {
	
	new(String name, Type expressionType) {
		super(name, expressionType)
	}
	
	override nodeType() {
		TPINodeType.STEP_VAR
	}
	
}

class ThisTPINode extends RootTPINode {
	
	public val Class enclosingClass
	
	new(Class enclosingClass, Type expressionType) {
		super(expressionType)
		this.enclosingClass = enclosingClass
	}
	
	new(Class enclosingClass, Type expressionType, TPIRole role) {
		super(expressionType)
		this.enclosingClass = enclosingClass
		this.role = role
	}
	
	override matches(Expr expr) {
		if (expr instanceof Parenthesized)
			matches(expr.expr)
		else
			expr instanceof This || expr instanceof Super
	}
	
	override nodeType() {
		TPINodeType.THIS
	}
	
	override id() {
		"this"
	}
	
	override standaloneNode() {
		new ThisTPINode(this.enclosingClass, this.expressionType, this.standaloneRole)
	}
	
}

enum TPINodeType {
	FIELD_ACCESS, NO_ARG_METHOD_CALL, SLICING, LOCAL_VAR, SINGLETON, STEP_VAR, STEP_VAR_ARG_METHOD_CALL, THIS, INFERRED_PARAM
}

enum TPIRole {
	PURE, READ_ONLY, READ_WRITE
}
