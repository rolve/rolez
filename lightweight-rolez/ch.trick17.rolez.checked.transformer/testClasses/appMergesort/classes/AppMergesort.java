package classes;

import rolez.annotation.Checked;
import rolez.annotation.Readwrite;
import rolez.annotation.Task;
import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.CheckedSlice;
import rolez.checked.util.StopWatch;

@Checked
public class AppMergesort {

	public final int maxLevel;
    
    public AppMergesort() {
        this.maxLevel = 3;
    }
    
    public AppMergesort(final int maxLevel) {
        this.maxLevel = maxLevel;
    }
	
	public static void main(String[] args) {
		for (int j=1; j<=5; j++) {
			AppMergesort instance = new AppMergesort(j);
			CheckedArray<int[]> s1 = instance.shuffledInts(6000000, new java.util.Random());
			CheckedArray<int[]> s2 = instance.shuffledInts(6000000, new java.util.Random());
			CheckedArray<int[]> s3 = instance.shuffledInts(6000000, new java.util.Random());
			
			StopWatch sw = new StopWatch();
			sw.go();
			instance.sort(s1);
			instance.sort(s2);
			instance.sort(s3);
			// Uncomment to see performance, but test will fail
			//System.out.println(sw.get());
			
			for (int i = 0; i < s1.arrayLength(); i++) {
				if (s1.getInt(i) != i || s2.getInt(i) != i || s3.getInt(i) != i) {
					System.out.println("ERROR!");
					break;
				}
			}
		}
		System.out.println("NO ERROR FOUND!");
	}
	
	public CheckedArray<int[]> shuffledInts(final int n, final java.util.Random random) {
        final CheckedArray<int[]> array = new CheckedArray<int[]>(new int[n]);
        for(int i = 0; i < n; i++)
            array.setInt(i, i);
        for(int i = n - 1; i > 0; i -= 1) {
            final int index = random.nextInt(i + 1);
            final int t = array.getInt(index);
            array.setInt(index,array.getInt(i));
            array.setInt(i, t);
        }
        return array;
    }
	
	public void sort(CheckedArray<int[]> a) {
		CheckedArray<int[]> b = a.clone();
		doSort(b, a, 0, a.arrayLength(), 0, false);
	}
	
	@Task
	public void doSort(@Readwrite CheckedSlice<int[]> b, 
			@Readwrite CheckedSlice<int[]> a, int begin, int end, int level, boolean $asTask) {
		if (end - begin < 2)
			return;
		
		if (end - begin == 2) {
			if (b.getInt(begin) > b.getInt(begin+1)) {
				a.setInt(begin, b.getInt(begin+1));
				a.setInt(begin+1, b.getInt(begin));
			}
			return;
		}
		
		int middle = (begin + end) / 2;
		
		if (level < this.maxLevel) {
			doSort(a.slice(begin, middle), b.slice(begin, middle), begin, middle, level + 1, true);
			doSort(a.slice(middle, end), b.slice(middle, end), middle, end, level + 1, false);
		} else {
			doSort(a, b, begin, middle, level + 1, false);
			doSort(a, b, middle, end, level + 1, false);
		}
		
		merge(b, a, begin, middle, end);
	}
	
	public void merge(CheckedSlice<int[]> b, CheckedSlice<int[]> a, int begin, int middle, int end) {
		int i = begin;
		int j = middle;
		
		for (int k = begin; k < end; k++) {
			if (i < middle && (j >= end || b.getInt(i) <= b.getInt(j))) {
				a.setInt(k, b.getInt(i));
				i++;
			} else {
				a.setInt(k, b.getInt(j));
				j++;
			}
		}
	}
}
