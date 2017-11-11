package classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import rolez.annotation.Checked;
import rolez.annotation.Task;
import rolez.annotation.Readonly;
import rolez.annotation.Pure;
import rolez.annotation.Readwrite;
import rolez.checked.lang.BlockPartitioner;
import rolez.checked.lang.CheckedArray;
import rolez.checked.lang.CheckedSlice;
import rolez.checked.lang.GuardedVectorBuilder;
import rolez.checked.lang.VectorBuilder;
import rolez.checked.util.StopWatch;
import rolez.checked.lang.Vector;
import rolez.checked.lang.SliceRange;

@Checked
public class IdeaChecked {

	public final int size;
    public final int tasks;
	
    public CheckedArray<byte[]> plain;
    public CheckedArray<byte[]> encrypted;
    public CheckedArray<byte[]> decrypted;
    
    public Vector<short[]> userKey;
    public Vector<int[]> encryptKey;
    public Vector<int[]> decryptKey;
	
	public IdeaChecked() {
        this.size = 50000000;
        this.tasks = 2;
    }
    
    public IdeaChecked(final int size, final int tasks) {
        this.size = size;
        this.tasks = tasks;
    }
    
    public void buildTestData(Random random) {

    	GuardedVectorBuilder<short[]> vectorBuilder = new GuardedVectorBuilder<short[]>(new short[8]);
        for(int i = 0; i < 8; i++)
        	vectorBuilder.setShort(i, (short)random.nextInt());
        userKey = vectorBuilder.build();
        
        encryptKey = calcEncryptKey(userKey);
        decryptKey = calcDecryptKey(encryptKey);

    	plain     = new CheckedArray<byte[]>(new byte[size]);
        encrypted = new CheckedArray<byte[]>(new byte[size]);
        decrypted = new CheckedArray<byte[]>(new byte[size]);
        for(int i = 0; i < size; i++)
            plain.setByte(i, (byte) i);
    }
    
    public static void main(String[] args) {
    	IdeaChecked instance = new IdeaChecked();
    	instance.buildTestData(new Random(42L));
        System.out.println("Finished building test data!");
        final StopWatch watch = new StopWatch();
        for(int i = 0; i < 20; i++) {
            watch.go();
            instance.run();
			// Uncomment to see performance, but test will fail
            //java.lang.System.out.println(watch.get());
            instance.validate();
        }
        System.out.println("NO ERROR FOUND!");
    }
    
    public Vector<int[]> calcEncryptKey(Vector<short[]> userKey) {
    	GuardedVectorBuilder<int[]> key = new GuardedVectorBuilder<int[]>(new int[52]);
        for(int i = 0; i < 52; i++)
        	key.setInt(i, 0);
        
        for(int i = 0; i < 8; i++)
        	key.setInt(i, userKey.getShort(i) & 0xffff);

        for(int i = 8; i < 52; i++) {
            int j = i % 8;
            if(j < 6) {
            	key.setInt(i, ((key.data[i - 7] >>> 9) | (key.data[i - 6] << 7)) & 0xFFFF);
                continue;
            }
            
            if(j == 6) {
            	key.setInt(i, ((key.data[i - 7] >>> 9) | (key.data[i - 14] << 7)) & 0xFFFF);
                continue;
            }
            
            key.setInt(i, ((key.data[i - 15] >>> 9) | (key.data[i - 14] << 7)) & 0xFFFF);
        }
        return key.build();
    }
    
    public Vector<int[]> calcDecryptKey(Vector<int[]> encryptKey) {
    	GuardedVectorBuilder<int[]> vectorBuilder = new GuardedVectorBuilder<int[]>(new int[52]);
        
    	vectorBuilder.setInt(51, inv(encryptKey.getInt(3)));
    	vectorBuilder.setInt(50, -encryptKey.getInt(2) & 0xffff);
    	vectorBuilder.setInt(49, -encryptKey.getInt(1) & 0xffff);
    	vectorBuilder.setInt(48, inv(encryptKey.getInt(0)));
        
        int j = 47; // Indices into temp and encrypt arrays.
        int k = 4;
        for(int i = 0; i < 7; i++) {
            int t0 = encryptKey.getInt(k++);
            vectorBuilder.setInt(j--, encryptKey.getInt(k++));
            vectorBuilder.setInt(j--, t0);
            int t1 = inv(encryptKey.getInt(k++));
            int t2 = -encryptKey.getInt(k++) & 0xffff;
            int t3 = -encryptKey.getInt(k++) & 0xffff;
            vectorBuilder.setInt(j--, inv(encryptKey.getInt(k++)));
            vectorBuilder.setInt(j--, t2);
            vectorBuilder.setInt(j--, t3);
            vectorBuilder.setInt(j--, t1);
        }
        
        int t0 = encryptKey.getInt(k++);
        vectorBuilder.setInt(j--, encryptKey.getInt(k++));
        vectorBuilder.setInt(j--, t0);
        int t1 = inv(encryptKey.getInt(k++));
        int t2 = -encryptKey.getInt(k++) & 0xffff;
        int t3 = -encryptKey.getInt(k++) & 0xffff;
        vectorBuilder.setInt(j--, inv(encryptKey.getInt(k++)));
        vectorBuilder.setInt(j--, t3);
        vectorBuilder.setInt(j--, t2);
        vectorBuilder.setInt(j--, t1);
        
        return vectorBuilder.build();
    }
    
    public int inv(int x) {
    	if(x <= 1)
            return x;
            
        int t0 = 0x10001 / x;
        int y = 0x10001 % x;
        if(y == 1)
            return 1 - t0 & 0xFFFF;
        
        int t1 = 1;
        do {
            int q0 = x / y;
            x = x % y;
            t1 += q0 * t0;
            if(x == 1)
                return t1;
            
            int q1 = y / x;
            y = y % x;
            t0 += q1 * t1;
        } while(y != 1);
        
        return 1 - t0 & 0xFFFF;
    }
    
    public void validate() {
    	if(!Arrays.equals(plain.getUncheckedArrayRead(), decrypted.getUncheckedArrayRead()))
            throw new AssertionError("Validation failed");
    }
    
    public void run() {
    	BlockPartitioner partitioner = new BlockPartitioner(8, 0L);
    	CheckedArray<CheckedSlice<byte[]>[]> plainSlices = plain.partition(partitioner, tasks);
    	CheckedArray<CheckedSlice<byte[]>[]> encryptedSlices = encrypted.partition(partitioner, tasks);
    	CheckedArray<CheckedSlice<byte[]>[]> decryptedSlices = decrypted.partition(partitioner, tasks);
    	
    	// encrypt
    	for (int i = 0; i < tasks; i++) {
    		encryptDecrypt((CheckedSlice<byte[]>)plainSlices.get(i), (CheckedSlice<byte[]>)encryptedSlices.get(i), encryptKey, true);
    	}
    	
    	// decrypt
    	for (int i = 0; i < tasks; i++) {
    		encryptDecrypt((CheckedSlice<byte[]>)encryptedSlices.get(i), (CheckedSlice<byte[]>)decryptedSlices.get(i), decryptKey, true);
    	}
    }
    
    @Task
    public void encryptDecrypt(@Readonly CheckedSlice<byte[]> src, @Readwrite CheckedSlice<byte[]> dst, @Pure Vector<int[]> key, boolean $asTask) {
    	SliceRange range = src.getSliceRange();
    	
    	int iSrc = range.begin;
    	int iDst = range.begin;
    	for(int i = range.begin; i < range.end; i += 8) {
    		int x1 = src.getByte(iSrc++) & 0xff;
            x1 |= (src.getByte(iSrc++) & 0xff) << 8;
            int x2 = src.getByte(iSrc++) & 0xff;
            x2 |= (src.getByte(iSrc++) & 0xff) << 8;
            int x3 = src.getByte(iSrc++) & 0xff;
            x3 |= (src.getByte(iSrc++) & 0xff) << 8;
            int x4 = src.getByte(iSrc++) & 0xff;
            x4 |= (src.getByte(iSrc++) & 0xff) << 8;
            
            int round = 8;
            int iKey = 0;
            do {
                x1 = (int) ((long) x1 * key.getInt(iKey++) % 0x10001L & 0xffff);
                x2 = x2 + key.getInt(iKey++) & 0xffff;
                x3 = x3 + key.getInt(iKey++) & 0xffff;
                x4 = (int) ((long) x4 * key.getInt(iKey++) % 0x10001L & 0xffff);
                int t0 = x1 ^ x3;
                t0 = (int) ((long) t0 * key.getInt(iKey++) % 0x10001L & 0xffff);
                int t1 = t0 + (x2 ^ x4) & 0xffff;
                t1 = (int) ((long) t1 * key.getInt(iKey++) % 0x10001L & 0xffff);
                t0 = t1 + t0 & 0xffff;
                x1 ^= t1;
                x4 ^= t0;
                t0 ^= x2;
                x2 = x3 ^ t1;
                x3 = t0;
            } while(--round != 0);
            
            x1 = (int) ((long) x1 * key.getInt(iKey++) % 0x10001L & 0xffff);
            x3 = x3 + key.getInt(iKey++) & 0xffff;
            x2 = x2 + key.getInt(iKey++) & 0xffff;
            x4 = (int) ((long) x4 * key.getInt(iKey++) % 0x10001L & 0xffff);
            
            dst.setByte(iDst++, (byte) x1);
            dst.setByte(iDst++, (byte) (x1 >>> 8));
            dst.setByte(iDst++, (byte) x3);
            dst.setByte(iDst++, (byte) (x3 >>> 8));
            dst.setByte(iDst++, (byte) x2);
            dst.setByte(iDst++, (byte) (x2 >>> 8));
            dst.setByte(iDst++, (byte) x4);
            dst.setByte(iDst++, (byte) (x4 >>> 8));
    	}
    }    
}