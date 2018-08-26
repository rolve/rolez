package ch.trick17.rolez.tpi

import ch.trick17.rolez.rolez.ArithmeticUnaryExpr
import ch.trick17.rolez.rolez.Assignment
import ch.trick17.rolez.rolez.OpArithmeticUnary
import ch.trick17.rolez.rolez.ParallelStmt
import ch.trick17.rolez.rolez.Parfor
import ch.trick17.rolez.rolez.Stmt
import com.google.inject.Inject
import com.google.inject.Injector
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.util.OnChangeEvictingCache
import ch.trick17.rolez.rolez.Ref

class TPIProvider {
	
	@Inject extension Injector
    
    val tpis = new OnChangeEvictingCache
    
    def tpi(ParallelStmt it) {
        tpis.get(it, eResource, [selectParameters(it, newNodeBuilder())])
    }
    
    def tpi(Parfor it) {
        tpis.get(it, eResource, [selectParameters(it, newNodeBuilder())])
    }
    
    private def newNodeBuilder() {
        new TPINodeBuilder() => [injectMembers]
    }
	
	private def Map<String, RootTPINode> createTPITrees(TPINodeBuilder it, Stmt stmt, Collection<String> userDefinedParamNames) {
		var EObject prev = stmt
		for (var container = stmt.eContainer.eContainer; container instanceof Stmt; container = container.eContainer) {
			if (container instanceof ParallelStmt) {
				if (container.part1 === prev)
					createTPITrees(stmt, userDefinedParamNames, container, 0)
				else
					createTPITrees(stmt, userDefinedParamNames, container, 1)
			}
			if (container instanceof Parfor)
				createTPITrees(stmt, userDefinedParamNames, container)
		}		
		createTPITrees(stmt, userDefinedParamNames, new TPIResult(), findStepVar(stmt))
	}
	
	private def Map<String, RootTPINode> createTPITrees(TPINodeBuilder it, Stmt stmt, Collection<String> userDefinedParamNames,
		ParallelStmt parent, int taskIndex
	) {
		createTPITrees(stmt, userDefinedParamNames, parent.tpi.get(taskIndex), findStepVar(stmt))
	}
	
	private def Map<String, RootTPINode> createTPITrees(TPINodeBuilder it, Stmt stmt, Collection<String> userDefinedParamNames,
		Parfor parent
	) {
		createTPITrees(stmt, userDefinedParamNames, parent.tpi, findStepVar(stmt))
	}
	
	private static def String findStepVar(Stmt stmt) {
		val container = stmt.eContainer
		if (container instanceof Parfor) {
			val step = container.step
			if (step instanceof Assignment) {
				val l = step.left
				if (l instanceof Ref)
					return l.variable.name
			}
			else if (step instanceof ArithmeticUnaryExpr) {
				if (step.op == OpArithmeticUnary.POST_DECREMENT || step.op == OpArithmeticUnary.POST_INCREMENT ||
					step.op == OpArithmeticUnary.PRE_DECREMENT || step.op == OpArithmeticUnary.PRE_INCREMENT
				) {
					val e = step.expr
					if (e instanceof Ref)
						return e.variable.name
				}
			}
		}
		
		null
	}
			
	private def TPIResult[] selectParameters(ParallelStmt stmt,
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
				throw new TPIException(stmt)
			else if (roleConflict(node1.childRole, node2.childRole)) {
				if (node1.isStandalone || node2.isStandalone)
					throw new TPIException(stmt)
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
	
	private def TPIResult selectParameters(Parfor stmt,
		TPINodeBuilder nodeBuilder
	) {
		val paramNames = stmt.params.map[variable.name]
		
		//could possibly be parallelized
		val nodes = nodeBuilder.createTPITrees(stmt.body, paramNames)
		
		val selectedParams = new ArrayList<TPINode>()
		
		for (RootTPINode node : nodes.values) {
			if (node.nodeType == TPINodeType.STEP_VAR)
				selectedParams.add(node)
			else if (roleConflict(node.role, node.role))
				throw new TPIException(stmt)
			else if (roleConflict(node.childRole, node.childRole)) {
				if (node.isStandalone)
					throw new TPIException(stmt)
				compareChildren(node, selectedParams)
			}
			else
				selectedParams.add(node)
		}
		
		new TPIResult(selectedParams)
	}
	
	private def boolean roleConflict(TPIRole role1, TPIRole role2) {
		switch(role1) {
			case TPIRole.PURE: false
			case TPIRole.READ_ONLY: role2 == TPIRole.READ_WRITE
			case TPIRole.READ_WRITE: role2 != TPIRole.PURE
		}
	}
	
	private def void compareChildren(TPINode node1, TPINode node2,
		ArrayList<TPINode> selectedParams1, ArrayList<TPINode> selectedParams2
	) {
		val handledChildren2 = new HashSet<ChildTPINode>()
		
		for (ChildTPINode child1 : node1.children) {
			val child2 = node2.findEquivChild(child1)
			if (child2 == null)
				selectedParams1.add(child1)
			else if (roleConflict(child1.role, child2.role))
				throw new TPIException()
			else if (roleConflict(child1.childRole, child2.childRole)) {
				if (child1.isStandalone || child2.isStandalone)
					throw new TPIException()
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
	
	private def void compareChildren(TPINode node,
		ArrayList<TPINode> selectedParams
	) {
		for (ChildTPINode child : node.children) {
			if (child.nodeType == TPINodeType.STEP_VAR_ARG_METHOD_CALL)
				selectedParams.add(child)
			else if (roleConflict(child.role, child.role))
				throw new TPIException()
			else if (roleConflict(child.childRole, child.childRole)) {
				if (child.isStandalone)
					throw new TPIException()
				compareChildren(child, selectedParams)
			}
			else
				selectedParams.add(child)
		}
	}
	
	private def ChildTPINode findEquivChild(TPINode parent, ChildTPINode child) {
		parent.findChild(child.nodeType, child.name)
	}
	
}
