package classes;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Pure;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;

import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.CheckedSlice;
import rolez.checked.lang.SliceRange;
import rolez.checked.lang.ContiguousPartitioner;

@Checked
public class Histogram {

	CheckedArray<CheckedArray<int[]>[]> image;
	
	CheckedArray<int[]> rHist;
	CheckedArray<int[]> gHist;
	CheckedArray<int[]> bHist;
	
	public Histogram(CheckedArray<CheckedArray<int[]>[]> image) {
		this.image = image;
	}
	
	public void compute(int numTasks) {
		CheckedArray<CheckedSlice<CheckedArray<int[]>[]>[]> imageParts = image.partition(ContiguousPartitioner.INSTANCE, numTasks);
		CheckedArray<HistPart[]> results = new CheckedArray<HistPart[]>(new HistPart[numTasks]);
		for (int i = 0; i < numTasks; i++)
			this.computePart(results.slice(i,i+1), (CheckedSlice)imageParts.get(i), true);
		
		HistPart task0Result = (HistPart)results.get(0);
		this.rHist = task0Result.r;
		this.gHist = task0Result.g;
		this.bHist = task0Result.b;
		for (int i = 1; i < numTasks; i++) {
			HistPart taskIResult = (HistPart)results.get(i);
			this.merge(taskIResult);
		}
	}
	
	@Task
	@Readonly // TODO: This actually does not have to be RO, because method is called on final instance!
	public void computePart(@Readwrite CheckedSlice<HistPart[]> result, @Readonly CheckedSlice<CheckedArray<int[]>[]> imageSlice, boolean $asTask) {
		CheckedArray<int[]> r = new CheckedArray<int[]>(new int[256]);
		CheckedArray<int[]> g = new CheckedArray<int[]>(new int[256]);
		CheckedArray<int[]> b = new CheckedArray<int[]>(new int[256]);

		SliceRange sr = imageSlice.getSliceRange();
		for (int y = sr.begin; y < sr.end; y+= sr.step) {
			CheckedArray<int[]> row = (CheckedArray<int[]>)imageSlice.get(y);
			for (int x = 0; x < row.arrayLength(); x++) {
				Color color = new Color(row.getInt(x));
				r.setInt(color.r, r.getInt(color.r) + 1);
				g.setInt(color.g, g.getInt(color.g) + 1);
				b.setInt(color.b, b.getInt(color.b) + 1);
			}
		}
		result.set(result.getSliceRange().begin, new HistPart(r,g,b));
	}
	
	public void merge(HistPart histPart) {
		for (int c = 0; c < 256; c++) {
			this.rHist.setInt(c, this.rHist.getInt(c) + histPart.r.getInt(c));
			this.gHist.setInt(c, this.gHist.getInt(c) + histPart.g.getInt(c));
			this.bHist.setInt(c, this.bHist.getInt(c) + histPart.b.getInt(c));
		}
	}
}
