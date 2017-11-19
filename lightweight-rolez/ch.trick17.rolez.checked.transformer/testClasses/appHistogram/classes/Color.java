package classes;

import rolez.annotation.Checked;

@Checked
class Color {
    final int r;
    final int g;
    final int b;
    
    public Color(int rgb) {
    	this.r = (rgb >> 16) & 255;
    	this.g = (rgb >> 8) & 255;
    	this.b = rgb & 255;
    }
}