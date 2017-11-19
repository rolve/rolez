package classes;

import rolez.checked.lang.CheckedArray;
import rolez.annotation.Checked;

import java.io.IOException;
import java.io.PrintStream;

@Checked
public class AppHistogram {
	
	public static void main(String[] args) {
		try {
			CheckedArray<CheckedArray<int[]>[]> image = ImageReader.read("Data/100000000.jpg");
			AppHistogram instance = new AppHistogram();
			for (int tasks = 1; tasks <= 8; tasks = tasks * 2) {
				Histogram histogram = new Histogram(image);
				histogram.compute(tasks);
				instance.print(histogram, 80, 8, System.out);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void print(Histogram hist, int width, int binSize, PrintStream out) {
        int max = 0;
        for(int c = 0; c < 256; c++) {
           max = Math.max(max, hist.rHist.getInt(c));
           max = Math.max(max, hist.gHist.getInt(c));
           max = Math.max(max, hist.bHist.getInt(c));
        }
        
        double scaleWidth = (double)width / max;
        
        out.println("Red:");
        this.printChannel(hist.rHist, scaleWidth, binSize, out);
        out.println("Green:");
        this.printChannel(hist.gHist, scaleWidth, binSize, out);
        out.println("Blue:");
        this.printChannel(hist.bHist, scaleWidth, binSize, out);
    }
	
	public void printChannel(CheckedArray<int[]> hist, double scaleWidth, int binSize, PrintStream out) {
        int c = 0;
        while(c < 256) {
            double total = 0.0;
            for(int j = 0; j < binSize; j++) {
                total += hist.getInt(c);
                c++;
            }
            int barSize = (int)(total / binSize * scaleWidth);
            this.printBar(barSize, out);
            out.println();
        }
    }
	
	public void printBar(int size, PrintStream out) {
        for(int i = 0; i < size; i++)
            out.print("#");
    }
}
