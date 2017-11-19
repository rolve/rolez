package classes;

import rolez.annotation.Checked;

import rolez.checked.lang.CheckedArray;

@Checked
class HistPart {
    CheckedArray<int[]> r;
    CheckedArray<int[]> g;
    CheckedArray<int[]> b;
    
    public HistPart(CheckedArray<int[]> r, CheckedArray<int[]> g, CheckedArray<int[]> b) {
    	this.r = r;
    	this.g = g;
    	this.b = b;
    }
}