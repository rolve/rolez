package ch.trick17.rolez.generator

import ch.trick17.rolez.rolez.ParallelStmt
import java.util.ArrayList
import java.util.HashSet
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.Expr

public class TPIResult {
			
	static def TPIResult[] selectParameters(ParallelStmt stmt,
		TPINodeBuilder nodeBuilder
	) {
		val paramNames1 = stmt.params1.map[variable.name]
		val paramNames2 = stmt.params2.map[variable.name]
		
		//could possibly be parallelized
		val nodes1 = nodeBuilder.createTPITrees(stmt.part1, paramNames1)
		val nodes2 = nodeBuilder.createTPITrees(stmt.part2, paramNames2)
		
		val selectedParams1 = new ArrayList<TPINode>()
		val selectedParams2 = new ArrayList<TPINode>()
		
		val handledIds2 = new HashSet<String>()
		
		for (RootTPINode node1 : nodes1.values) {
			val node2 = nodes2.get(node1.id())
			if (node2 == null)
				selectedParams1.add(node1)
			else if (roleConflict(node1.role, node2.role))
				throw new Exception("could not find a solution") //TODO: better exception
			else if (roleConflict(node1.childRole, node2.childRole)) {
				if (node1.isStandalone || node2.isStandalone)
					throw new Exception("could not find a solution") //TODO: better exception
				compareChildren(node1, node2, selectedParams1, selectedParams2)
				handledIds2.add(node2.id())
			}
			else {
				selectedParams1.add(node1)
				selectedParams2.add(node2)
				handledIds2.add(node2.id())
			}
		}
		
		for (RootTPINode node2 : nodes2.values) {
			if (!handledIds2.contains(node2.id()))
				selectedParams2.add(node2)
		}
		
		val result1 = new TPIResult(selectedParams1)
		val result2 = new TPIResult(selectedParams2)
		
		#[result1, result2]
	}
	
	static def TPIResult selectParameters(Parfor stmt,
		TPINodeBuilder nodeBuilder
	) {
		val paramNames = stmt.params.map[variable.name]
		
		//could possibly be parallelized
		val nodes = nodeBuilder.createTPITrees(stmt.body, paramNames)
		
		val selectedParams = new ArrayList<TPINode>()
		
		for (RootTPINode node : nodes.values) {
			if (roleConflict(node.role, node.role))
				throw new Exception("could not find a solution") //TODO: better exception
			else if (roleConflict(node.childRole, node.childRole)) {
				if (node.isStandalone)
					throw new Exception("could not find a solution") //TODO: better exception
				compareChildren(node, selectedParams)
			}
			else
				selectedParams.add(node)
		}
		
		new TPIResult(selectedParams)
	}
	
	private static def boolean roleConflict(TPIRole role1, TPIRole role2) {
		switch(role1) {
			case TPIRole.PURE: false
			case TPIRole.READ_ONLY: role2 == TPIRole.READ_WRITE
			case TPIRole.READ_WRITE: role2 != TPIRole.PURE
		}
	}
	
	private static def void compareChildren(TPINode node1, TPINode node2,
		ArrayList<TPINode> selectedParams1, ArrayList<TPINode> selectedParams2
	) {
		val handledChildren2 = new HashSet<ChildTPINode>()
		
		for (ChildTPINode child1 : node1.children) {
			val child2 = node2.findEquivChild(child1)
			if (child2 == null)
				selectedParams1.add(child1)
			else if (roleConflict(child1.role, child2.role))
				throw new Exception("could not find a solution") //TODO: better exception
			else if (roleConflict(child1.childRole, child2.childRole)) {
				if (child1.isStandalone || child2.isStandalone)
					throw new Exception("could not find a solution") //TODO: better exception
				compareChildren(child1, child2, selectedParams1, selectedParams2)
				handledChildren2.add(child2)
			}
			else {
				selectedParams1.add(child1)
				selectedParams2.add(child2)
				handledChildren2.add(child2)
			}
		}
		
		for (ChildTPINode child2 : node2.children) {
			if (!handledChildren2.contains(child2))
				selectedParams2.add(child2)
		}
	}
	
	private static def void compareChildren(TPINode node,
		ArrayList<TPINode> selectedParams
	) {
		for (ChildTPINode child : node.children) {
			if (roleConflict(child.role, child.role))
				throw new Exception("could not find a solution") //TODO: better exception
			else if (roleConflict(child.childRole, child.childRole)) {
				if (child.isStandalone)
					throw new Exception("could not find a solution") //TODO: better exception
				compareChildren(child, selectedParams)
			}
			else
				selectedParams.add(child)
		}
	}
	
	private static def ChildTPINode findEquivChild(TPINode parent, ChildTPINode child) {
		parent.findChild(child.nodeType, child.name)
	}
	
	public val TPINode[] selectedParams
	
	public new() {
		this.selectedParams = #[]
	}
	
	private new(TPINode[] selectedParams) {
		this.selectedParams = selectedParams
	}
	
	def int paramIndex(Expr expr) {
		for (var i = 0; i < this.selectedParams.length; i++) {
			if (this.selectedParams.get(i).matches(expr))
				return i
		}
		return -1
	}
	
}