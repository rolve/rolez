package ch.trick17.rolez.tpi

import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.BinaryExpr
import ch.trick17.rolez.rolez.Block
import ch.trick17.rolez.rolez.Expr
import ch.trick17.rolez.rolez.ExprStmt
import ch.trick17.rolez.rolez.ForLoop
import ch.trick17.rolez.rolez.IfStmt
import ch.trick17.rolez.rolez.Literal
import ch.trick17.rolez.rolez.LocalVarDecl
import ch.trick17.rolez.rolez.MemberAccess
import ch.trick17.rolez.rolez.New
import ch.trick17.rolez.rolez.Null
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.Parenthesized
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.PrimitiveType
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.Super
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.UnaryExpr
import ch.trick17.rolez.rolez.WhileLoop
import ch.trick17.rolez.typesystem.RolezSystem
import java.util.Collection
import java.util.HashMap
import java.util.HashSet
import java.util.Map
import java.util.Set
import javax.inject.Inject

import static extension ch.trick17.rolez.RolezExtensions.enclosingClass
import ch.trick17.rolez.rolez.ArithmeticUnaryExpr
import ch.trick17.rolez.rolez.OpArithmeticUnary
import ch.trick17.rolez.rolez.Ref
import ch.trick17.rolez.rolez.Method
import ch.trick17.rolez.rolez.SingletonClass
import ch.trick17.rolez.rolez.Return
import ch.trick17.rolez.rolez.FinishStmt
import ch.trick17.rolez.rolez.VarKind

class TPINodeBuilder {
	
    @Inject RolezSystem system
	
	private ThisTPINode thisNode
	private Map<String, LocalVarTPINode> varNodes
	private Map<String, SingletonTPINode> singletonNodes
	private Map<Integer, InferredParamTPINode> inferredParamNodes
	private Set<String> paramNames
	private Set<String> insideVarNames
	private TPIResult parentResult
	private String stepVar
	
	public new() {
		this.thisNode = null
		this.varNodes = new HashMap
		this.singletonNodes = new HashMap
		this.inferredParamNodes = new HashMap
		this.paramNames = new HashSet
		this.insideVarNames = new HashSet
		this.parentResult = null
		this.stepVar = null
	}
	
	public def Map<String, RootTPINode> createTPITrees(Stmt stmt, Collection<String> userDefinedParamNames,
		TPIResult parentResult, String stepVar
	) {
		this.paramNames.addAll(userDefinedParamNames)
		this.parentResult = parentResult
		this.stepVar = stepVar
		
		createNodes(stmt)
		
		val result = new HashMap<String, RootTPINode>(this.varNodes)
		result.putAll(this.singletonNodes)
		if (this.thisNode != null)
			result.put("this", this.thisNode)
		
		this.thisNode = null
		this.varNodes.clear()
		this.singletonNodes.clear()
		this.paramNames.clear()
		this.parentResult = null
		this.stepVar = null
		
		result
	}
	
	private dispatch def void createNodes(Block stmt) {
		for (Stmt s : stmt.stmts)
			createNodes(s)
	}
	
	private dispatch def void createNodes(ExprStmt stmt) {
		createNodesE(stmt.expr)
	}
	
	private dispatch def void createNodes(FinishStmt stmt) { }
	
	private dispatch def void createNodes(ForLoop stmt) {
		createNodes(stmt.initializer)
		createNode(stmt.condition, TPIRole.PURE, true)
		createNode(stmt.step, TPIRole.PURE, true)
		createNodes(stmt.body)
	}
	
	private dispatch def void createNodes(IfStmt stmt) {
		createNode(stmt.condition, TPIRole.PURE, true)
		createNodes(stmt.thenPart)
		if (stmt.elsePart !== null)
			createNodes(stmt.elsePart)
	}
	
	private dispatch def void createNodes(LocalVarDecl stmt) {
		this.insideVarNames.add(stmt.variable.name)
		if (stmt.initializer !== null) {
			val type = if (stmt.variable.type != null) stmt.variable.type else system.type(stmt.initializer).value
			createNode(stmt.initializer, getTPIRoleFromType(type), true)
		}
	}
	
	private dispatch def void createNodes(ParallelStmt stmt) {
		if (stmt.params1 != null)
			for (LocalVarDecl decl : stmt.params1)
				createNodes(decl)
		createNodes(stmt.part1)
		
		if (stmt.params2 != null)
			for (LocalVarDecl decl : stmt.params2)
				createNodes(decl)
		createNodes(stmt.part2)
	}
	
	private dispatch def void createNodes(Parfor stmt) {
		if (stmt.params != null)
			for (LocalVarDecl decl : stmt.params)
				createNodes(decl)
		createNodes(stmt.initializer)
		createNode(stmt.condition, TPIRole.PURE, true)
		createNode(stmt.step, TPIRole.PURE, true)
		createNodes(stmt.body)
	}
	
	private dispatch def void createNodes(Return stmt) { }
	
	private dispatch def void createNodes(WhileLoop stmt) {
		createNode(stmt.condition, TPIRole.PURE, true)
		createNodes(stmt.body)
	}
	
	// (no createNodes method for Return and SuperConstrCall)
	
	private dispatch def void createNodesE(BinaryExpr e) {
		createNodesE(e.left)
		createNodesE(e.right)
	}
	
	private dispatch def void createNodesE(Assignment e) {
		val left = e.left;
		if (left instanceof MemberAccess) {
			createNode(left.target, TPIRole.READ_WRITE, true);
			createNode(e.right, getTPIRoleFromType(left.field.type), true)
		}
		else // @= assignments
			createNode(e.right, TPIRole.PURE, true)
	}
	
	private dispatch def void createNodesE(Literal e) { }
	
	private dispatch def void createNodesE(MemberAccess e) {
		createNode(e, TPIRole.PURE, true)
	}
	
	private dispatch def void createNodesE(New e) {
		for (var i = 0; i < e.args.length; i++)
			createNode(e.args.get(i), getTPIRoleFromType(e.constr.params.get(i).type), true)
	}
	
	private dispatch def void createNodesE(Slicing e) {
		createNode(e, TPIRole.PURE, true)
	}
	
	private dispatch def void createNodesE(UnaryExpr e) {
		if (e instanceof ArithmeticUnaryExpr) {
			if (e.op == OpArithmeticUnary.POST_DECREMENT ||
				e.op == OpArithmeticUnary.POST_INCREMENT ||
				e.op == OpArithmeticUnary.PRE_DECREMENT ||
				e.op == OpArithmeticUnary.PRE_INCREMENT
			) {
				createNode(e.expr, TPIRole.READ_WRITE, true)
				return	
			}
		}
		createNodesE(e.expr)
	}
	
	private dispatch def void createNodesE(Ref e) {
		createNode(e, TPIRole.PURE, true)
	}
	
	private dispatch def TPINode createNode(Expr e, TPIRole role, boolean standalone) {
		createNodesE(e)
		null
	}
	
	private dispatch def TPINode createNode(MemberAccess e, TPIRole role, boolean standalone) {
    	val tpiidx = parentResult.paramIndex(e)
    	if (tpiidx >= 0)
    		return createInferredParamNode(e, tpiidx, parentResult.selectedParams.get(tpiidx), role, standalone)
		
		if (e.fieldAccess) {
			var parentRole = TPIRole.READ_ONLY
			if (e.field.kind == VarKind.VAL)
				parentRole = TPIRole.PURE
			
			val parent = createNode(e.target, parentRole, false)
			if (parent != null) {
				val name = e.field.name
				var node = parent.findChild(TPINodeType.FIELD_ACCESS, name)
				if (node == null)
					node = new FieldAccessTPINode(parent, name, system.type(e).value)
				
				node.setRole(role)
				if (standalone)
					node.setStandalone(role)
				
				node
			}
			else
				null
		}
		else if (e.args.empty) {
			val thisParamRole = getTPIRoleFromMethod(e.method)
			val parent = createNode(e.target, thisParamRole, false)
			if (parent != null) {
				if (thisParamRole == TPIRole.READ_WRITE) {
					parent.setStandalone(thisParamRole)
					return null
				}
				
				val name = e.method.name
				var node = parent.findChild(TPINodeType.NO_ARG_METHOD_CALL, name)
				if (node == null)
					node = new NoArgMethodCallTPINode(parent, name, system.type(e).value)
				
				node.setRole(role)
				if (standalone)
					node.setStandalone(role)
				
				node
			}
			else
				null
		}
		else if (stepVar != null && StepVarArgMethodCallTPINode.isStepVarArgMethodCall(e, stepVar)) {
			val thisParamRole = getTPIRoleFromMethod(e.method)
			val parent = createNode(e.target, thisParamRole, false)
			if (parent != null) {
				if (thisParamRole == TPIRole.READ_WRITE) {
					parent.setStandalone(thisParamRole)
					return null
				}
				
				val name = e.method.name
				var node = parent.findChild(TPINodeType.STEP_VAR_ARG_METHOD_CALL, name)
				if (node == null)
					node = new StepVarArgMethodCallTPINode(parent, name, system.type(e).value, stepVar)
				
				node.setRole(role)
				if (standalone)
					node.setStandalone(role)
				
				node
			}
		}
		else {
			val thisParamRole = getTPIRoleFromType(e.method.thisParam.type)
			createNode(e.target, thisParamRole, true)
			
			val params = e.method.params
			for (var i = 0; i < params.length; i++) {
				val paramRole = getTPIRoleFromType(params.get(i).type)
				createNode(e.args.get(i), paramRole, true)
			}
			
			null
		}
	}
	
	private dispatch def TPINode createNode(Parenthesized e, TPIRole role, boolean standalone) {
		createNode(e.expr, role, standalone)
	}
	
	private dispatch def TPINode createNode(Slicing e, TPIRole role, boolean standalone) {
    	val tpiidx = parentResult.paramIndex(e)
    	if (tpiidx >= 0)
    		return createInferredParamNode(e, tpiidx, parentResult.selectedParams.get(tpiidx), role, standalone)
    	
		val parent = createNode(e.target, TPIRole.READ_ONLY, false)
		if (parent != null) {
			val name = e.slice.name
			var node = parent.findChild(TPINodeType.SLICING, name)
			if (node == null)
				node = new SlicingTPINode(parent, name, system.type(e).value)
			
			node.setRole(role)
			if (standalone)
				node.setStandalone(role)
			
			node
		}
		else
			null
	}
	
	private dispatch def TPINode createNode(Ref e, TPIRole role, boolean standalone) {
    	val tpiidx = parentResult.paramIndex(e)
    	if (tpiidx >= 0)
    		return createInferredParamNode(e, tpiidx, parentResult.selectedParams.get(tpiidx), role, standalone)
    	
    	if (e.varRef) {
			val name = e.variable.name
			if (this.paramNames.contains(name) || this.insideVarNames.contains(name))
				return null
			
			var node = this.varNodes.get(name)
			if (node == null) {
				if (name == stepVar)
					node = new StepVarTPINode(name, system.type(e).value)
				else
					node = new LocalVarTPINode(name, system.type(e).value)
				this.varNodes.put(name, node)
			}
			
			node.setRole(role)
			if (standalone)
				node.setStandalone(role)
			
			node
		}
		else if (e.singletonRef) {
			val name = getFullName(e.referee as SingletonClass)
			
			var node = this.singletonNodes.get(name)
			if (node == null) {
				node = new SingletonTPINode(e.referee as SingletonClass, system.type(e).value)
				this.singletonNodes.put(name, node)
			}
			
			node.setRole(role)
			if (standalone)
				node.setStandalone(role)
			
			node
		}
		else
			return null
	}
	
	private static def String getFullName(SingletonClass singleton) {
		singleton.qualifiedName.toString
	}
	
	private dispatch def TPINode createNode(This e, TPIRole role, boolean standalone) {
    	val tpiidx = parentResult.paramIndex(e)
    	if (tpiidx >= 0)
    		return createInferredParamNode(e, tpiidx, parentResult.selectedParams.get(tpiidx), role, standalone)
    		
		createThisNode(e, role, standalone)
	}
	
	private dispatch def TPINode createNode(Super e, TPIRole role, boolean standalone) {
    	val tpiidx = parentResult.paramIndex(e)
    	if (tpiidx >= 0)
    		return createInferredParamNode(e, tpiidx, parentResult.selectedParams.get(tpiidx), role, standalone)
    	
		createThisNode(e, role, standalone) //TODO: super method calls
	}
	
	private def ThisTPINode createThisNode(Expr e, TPIRole role, boolean standalone) {
		if (this.thisNode == null)
			this.thisNode = new ThisTPINode(e.enclosingClass, system.type(e).value)
		
		this.thisNode.setRole(role)
		if (standalone)
			this.thisNode.setStandalone(role)
		
		this.thisNode
	}
	
	private def InferredParamTPINode createInferredParamNode(Expr e, int index, TPINode param,
		TPIRole role, boolean standalone
	) {
		var node = this.inferredParamNodes.get(index)
		if (node == null) {
			node = new InferredParamTPINode(index, param)
			this.inferredParamNodes.put(index, node)
		}
		
		node.setRole(role)
		if (standalone)
			node.setStandalone(role)
		
		node
	}
	
	private static dispatch def TPIRole getTPIRoleFromType(PrimitiveType type) {
		TPIRole.PURE
	}
	
	private static dispatch def TPIRole getTPIRoleFromType(Null type) {
		TPIRole.PURE
	}
	
	private static dispatch def TPIRole getTPIRoleFromType(RoleType type) {
		getTPIRoleFromRole(type.role)
	}
	
	private static dispatch def TPIRole getTPIRoleFromType(TypeParamRef type) {
		getTPIRoleFromRole(type.restrictingRole)
	}
	
	private static dispatch def TPIRole getTPIRoleFromRole(RoleParamRef role) {
		getTPIRoleFromRole(role.param.upperBound)
	}
	
	private static dispatch def TPIRole getTPIRoleFromRole(Pure role) {
		TPIRole.PURE
	}
	
	private static dispatch def TPIRole getTPIRoleFromRole(ReadOnly role) {
		TPIRole.READ_ONLY
	}
	
	private static dispatch def TPIRole getTPIRoleFromRole(ReadWrite role) {
		TPIRole.READ_WRITE
	}
	
	private static def TPIRole getTPIRoleFromMethod(Method method) {
		// TODO: For some reason, we need to use this workaround to get the actual role of this method. Maybe there is a better solution.
		val declString = method.toString();
		val split = declString.split(" ");
		val roleString = split.get(0);
		
		switch (roleString) {
			case "pure":
				return TPIRole.PURE
			case "readonly":
				return TPIRole.READ_ONLY
			case "readwrite":
				return TPIRole.READ_WRITE
			default:
				for (roleParam : method.roleParams) {
					if (roleParam.name.equals(roleString))
						return getTPIRoleFromRole(roleParam.upperBound)
				}
		}
		null
	}
	
	public def StepVarTPINode createStepVarNode(Parfor parforStmt) {
		val step = parforStmt.step
		if (step instanceof Assignment) {
			val l = step.left
			if (l instanceof Ref)
				return new StepVarTPINode(l.variable.name, system.type(l).value)
		}
		else if (step instanceof ArithmeticUnaryExpr) {
			if (step.op == OpArithmeticUnary.POST_DECREMENT || step.op == OpArithmeticUnary.POST_INCREMENT ||
				step.op == OpArithmeticUnary.PRE_DECREMENT || step.op == OpArithmeticUnary.PRE_INCREMENT
			) {
				val e = step.expr
				if (e instanceof Ref)
					return new StepVarTPINode(e.variable.name, system.type(e).value)
			}
		}
		
		null
	}
	
}