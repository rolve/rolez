package classes;

import rolez.annotation.Checked;
import rolez.annotation.Readwrite;
import rolez.annotation.Readonly;
import rolez.annotation.Task;
import rolez.checked.lang.CheckedSlice;
import rolez.checked.lang.CheckedArray;
import rolez.checked.util.StopWatch;

@Checked
public class QuicksortChecked {
	
	final int maxLevel;
	
	public static void main(String[] args) {
		
		for (int j=1; j<=5; j++) {
			QuicksortChecked instance = new QuicksortChecked(j);
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
	
	public QuicksortChecked() {
		this.maxLevel = 3;
	}
	
    public QuicksortChecked(final int maxLevel) {
        this.maxLevel = maxLevel;
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
	
	public void sort(CheckedSlice<int[]> s) {
		doSort(s, 0, s.arrayLength(), 0, false);
	}
    
    @Task
    public void doSort(@Readwrite CheckedSlice<int[]> s, int begin, int end, int level, boolean $asTask) {
        int pivot = this.pivot(s, begin, end);
        int left = begin;
        int right = end - 1;
        while(left <= right) {
            while(s.getInt(left) < pivot)
                left += 1;
            while(s.getInt(right) > pivot)
                right -= 1;
            if(left <= right) {
                int temp = s.getInt(left);
                s.setInt(left, s.getInt(right));
                s.setInt(right, temp);
                left += 1;
                right -= 1;
            }
        }
        final boolean sortLeft = begin < right;
        final boolean sortRight = left < (end - 1);
        if(level < this.maxLevel) {
            if(sortLeft)
                doSort(s.slice(begin, right + 1, 1), begin, right + 1, level + 1, true);
            
            if(sortRight)
                this.doSort(s.slice(left, end, 1), left, end, level + 1, false);
        }
        else {
            if(sortLeft)
                this.doSort(s, begin, right + 1, level + 1, false);
            
            if(sortRight)
                this.doSort(s, left, end, level + 1, false);
        }
    }
    
    public int pivot(final CheckedSlice<int[]> s, int begin, int end) {
        int l = s.getInt(begin);
        int m = s.getInt(begin + ((end - begin) / 2));
        int r = s.getInt(end - 1);
        if(l < m) {
            if(m < r)
                return m;
            else if(l < r)
                return l;
            else
                return r;
        }
        else {
            if(l < r)
                return l;
            else if(m < r)
                return r;
            else
                return m;
        }
    }
}
