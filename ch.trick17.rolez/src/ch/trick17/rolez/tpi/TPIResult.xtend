package ch.trick17.rolez.tpi

import ch.trick17.rolez.rolez.Expr

public class TPIResult {
	
	public val TPINode[] selectedParams
	
	public new() {
		this.selectedParams = #[]
	}
	
	public new(TPINode[] selectedParams) {
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