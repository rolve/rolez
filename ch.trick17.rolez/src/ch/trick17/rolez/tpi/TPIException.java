package ch.trick17.rolez.tpi;

import ch.trick17.rolez.rolez.ParallelStmt;
import ch.trick17.rolez.rolez.Parfor;

public class TPIException extends Exception {
	
	private static final long serialVersionUID = -8135706644641753826L;

	public TPIException() {
		super("Could not find a TPI solution for parallel or parfor statement");
	}
	
	public TPIException(ParallelStmt stmt) {
		super("Could not find a TPI solution for parallel statement " + stmt);
	}
	
	public TPIException(Parfor stmt) {
		super("Could not find a TPI solution for parfor statement " + stmt);
	}

}
