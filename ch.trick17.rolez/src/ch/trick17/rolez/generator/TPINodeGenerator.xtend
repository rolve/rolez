package ch.trick17.rolez.generator

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
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.Pure
import ch.trick17.rolez.rolez.ReadOnly
import ch.trick17.rolez.rolez.ReadWrite
import ch.trick17.rolez.rolez.RoleParamRef
import ch.trick17.rolez.rolez.RoleType
import ch.trick17.rolez.rolez.Slicing
import ch.trick17.rolez.rolez.Stmt
import ch.trick17.rolez.rolez.The
import ch.trick17.rolez.rolez.TypeParamRef
import ch.trick17.rolez.rolez.UnaryExpr
import ch.trick17.rolez.rolez.VarRef
import ch.trick17.rolez.rolez.WhileLoop
import java.util.Map
import java.util.Set
import javax.lang.model.type.PrimitiveType
import ch.trick17.rolez.rolez.This
import ch.trick17.rolez.rolez.Super
import java.util.HashMap
import java.util.HashSet
import java.util.Collection
import ch.trick17.rolez.rolez.Parenthesized

class TPINodeGenerator {
	
	private ThisTPINode thisNode
	private Map<String, LocalVarTPINode> varNodes
	private Set<String> paramNames
	
	public new() {
		this.thisNode = null
		this.varNodes = new HashMap
		this.paramNames = new HashSet
	}
	
	public def Set<RootTPINode> createTPITrees(Stmt stmt, Collection<String> userDefinedParamNames) {
		this.paramNames.addAll(userDefinedParamNames)
		
		createNodes(stmt)
		
		val result = new HashSet<RootTPINode>()
		if (this.thisNode != null)
			result.add(this.thisNode)
		for (LocalVarTPINode varNode : this.varNodes.values)
			result.add(varNode)
		
		this.thisNode = null
		this.varNodes.clear()
		this.paramNames.clear()
		
		result
	}
	
	private dispatch def void createNodes(Block stmt) {
		for (Stmt s : stmt.stmts)
			createNodes(s)
	}
	
	private dispatch def void createNodes(ExprStmt stmt) {
		createNodesE(stmt.expr)
	}
	
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
		if (stmt.initializer !== null)
			createNode(stmt.initializer, getTPIRoleFromType(stmt.variable.type), true)
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
		// e.left cannot be a variable, so we assume that e.left is a field access expression
		val left = e.left as MemberAccess;
		createNode(left.target, TPIRole.READ_WRITE, true);
		createNode(e.right, getTPIRoleFromType(left.field.type), true)
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
	
	private dispatch def void createNodesE(The e) { }
	
	private dispatch def void createNodesE(UnaryExpr e) {
		createNodesE(e.expr)
	}
	
	private dispatch def void createNodesE(VarRef e) {
		createNode(e, TPIRole.PURE, true)
	}
	
	private dispatch def TPINode createNode(Expr e, TPIRole role, boolean standalone) {
		createNodesE(e)
		null
	}
	
	private dispatch def TPINode createNode(MemberAccess e, TPIRole role, boolean standalone) {
		if (e.fieldAccess) {
			val parent = createNode(e.target, TPIRole.READ_ONLY, false)
			if (parent != null) {
				val name = e.field.name
				var node = parent.findChild(TPINodeType.FIELD_ACCESS, name)
				if (node == null)
					node = new FieldAccessTPINode(parent, name)
				
				node.setRole(role)
				if (standalone)
					node.setStandalone()
				
				node
			}
			else
				null
		}
		else if (e.args.empty) {
			val methodRole = getTPIRoleFromType(e.method.thisParam.type)
			val parent = createNode(e.target, methodRole, false)
			if (parent != null) {
				if (methodRole == TPIRole.READ_WRITE) {
					parent.setStandalone()
					return null
				}
				
				val name = e.method.name
				var node = parent.findChild(TPINodeType.NO_ARG_METHOD_CALL, name)
				if (node == null)
					node = new NoArgMethodCallTPINode(parent, name)
				
				node.setRole(role)
				if (standalone)
					node.setStandalone()
				
				node
			}
			else
				null
		}
		else
			null
	}
	
	private dispatch def TPINode createNode(Parenthesized e, TPIRole role, boolean standalone) {
		createNode(e.expr, role, standalone)
	}
	
	private dispatch def TPINode createNode(Slicing e, TPIRole role, boolean standalone) {
		val parent = createNode(e.target, TPIRole.READ_ONLY, false)
		if (parent != null) {
			val name = e.slice.name
			var node = parent.findChild(TPINodeType.SLICING, name)
			if (node == null)
				node = new SlicingTPINode(parent, name)
			
			node.setRole(role)
			if (standalone)
				node.setStandalone()
			
			node
		}
		else
			null
	}
	
	private dispatch def TPINode createNode(VarRef e, TPIRole role, boolean standalone) {
		val name = e.variable.name
		if (this.paramNames.contains(name))
			return null
		
		var node = this.varNodes.get(name)
		if (node == null) {
			node = new LocalVarTPINode(name)
			this.varNodes.put(name, node)
		}
		
		node.setRole(role)
		if (standalone)
			node.setStandalone()
		
		node
	}
	
	private dispatch def TPINode createNode(This e, TPIRole role, boolean standalone) {
		createThisNode(role, standalone)
	}
	
	private dispatch def TPINode createNode(Super e, TPIRole role, boolean standalone) {
		createThisNode(role, standalone)
	}
	
	private def TPINode createThisNode(TPIRole role, boolean standalone) {
		if (this.thisNode == null)
			this.thisNode = new ThisTPINode()
		
		this.thisNode.setRole(role)
		if (standalone)
			this.thisNode.setStandalone()
		
		this.thisNode
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
	
}