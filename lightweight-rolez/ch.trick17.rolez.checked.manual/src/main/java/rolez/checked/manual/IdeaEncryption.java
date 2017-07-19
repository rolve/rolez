package rolez.checked.manual;

import rolez.checked.lang.Checked;

public class IdeaEncryption extends Checked {
    
    public rolez.checked.lang.CheckedArray<byte[]> plain;
    
    public rolez.checked.lang.CheckedArray<byte[]> encrypted;
    
    public rolez.checked.lang.CheckedArray<byte[]> decrypted;
    
    public short[] userKey;
    
    public int[] encryptKey;
    
    public int[] decryptKey;
    
    public final int size;
    
    public final int tasks;
    
    public IdeaEncryption(final long $task) {
        super();
        this.size = 50000000;
        this.tasks = 2;
    }
    
    public IdeaEncryption(final int size, final int tasks, final long $task) {
        super();
        this.size = size;
        this.tasks = tasks;
    }
    
    public void buildTestData(final java.util.Random random, final long $task) {
        final rolez.checked.lang.GuardedVectorBuilder<short[]> userKey = new rolez.checked.lang.GuardedVectorBuilder<short[]>(new short[8]);
        for(int i = 0; i < 8; i++)
            userKey.setShort(i, (short) random.nextInt());
        checkLegalWrite(this, $task).userKey = userKey.build();
        this.encryptKey = this.calcEncryptKey(this.userKey, $task);
        this.decryptKey = this.calcDecryptKey(this.encryptKey, $task);
        this.plain = new rolez.checked.lang.CheckedArray<byte[]>(new byte[this.size]);
        this.encrypted = new rolez.checked.lang.CheckedArray<byte[]>(new byte[this.size]);
        this.decrypted = new rolez.checked.lang.CheckedArray<byte[]>(new byte[this.size]);
        for(int i = 0; i < this.size; i++)
            checkLegalWrite(this.plain, $task).data[i] = (byte) i;
    }
    
    public int[] calcEncryptKey(final short[] userKey, final long $task) {
        final int xFFFF = 65535;
        rolez.checked.lang.GuardedVectorBuilder<int[]> key = new rolez.checked.lang.GuardedVectorBuilder<int[]>(new int[52]);
        for(int i = 0; i < 52; i++)
            key.setInt(i, 0);
        for(int i = 0; i < 8; i++)
            key.setInt(i, userKey[i] & xFFFF);
        for(int i = 8; i < 52; i++) {
            int j = i % 8;
            if(j < 6)
                key.setInt(i, ((key.data[i - 7] >>> 9) | (key.data[i - 6] << 7)) & xFFFF);
            else
                if(j == 6)
                key.setInt(i, ((key.data[i - 7] >>> 9) | (key.data[i - 14] << 7)) & xFFFF);
            else
                key.setInt(i, ((key.data[i - 15] >>> 9) | (key.data[i - 14] << 7)) & xFFFF);
        }
        return key.build();
    }
    
    public int[] calcDecryptKey(final int[] encryptKey, final long $task) {
        final int xFFFF = 65535;
        rolez.checked.lang.GuardedVectorBuilder<int[]> key = new rolez.checked.lang.GuardedVectorBuilder<int[]>(new int[52]);
        key.setInt(51, this.inv(encryptKey[3], $task));
        key.setInt(50, (-encryptKey[2]) & xFFFF);
        key.setInt(49, (-encryptKey[1]) & xFFFF);
        key.setInt(48, this.inv(encryptKey[0], $task));
        int j = 47;
        int k = 4;
        for(int i = 0; i < 7; i++) {
            int t0 = encryptKey[k++];
            key.setInt(j--, encryptKey[k++]);
            key.setInt(j--, t0);
            int t1 = this.inv(encryptKey[k++], $task);
            int t2 = (-encryptKey[k++]) & xFFFF;
            int t3 = (-encryptKey[k++]) & xFFFF;
            key.setInt(j--, this.inv(encryptKey[k++], $task));
            key.setInt(j--, t2);
            key.setInt(j--, t3);
            key.setInt(j--, t1);
        }
        int t0 = encryptKey[k++];
        key.setInt(j--, encryptKey[k++]);
        key.setInt(j--, t0);
        int t1 = this.inv(encryptKey[k++], $task);
        int t2 = (-encryptKey[k++]) & xFFFF;
        int t3 = (-encryptKey[k++]) & xFFFF;
        key.setInt(j--, this.inv(encryptKey[k++], $task));
        key.setInt(j--, t3);
        key.setInt(j--, t2);
        key.setInt(j--, t1);
        return key.build();
    }
    
    public int inv(final int theX, final long $task) {
        if(theX <= 1)
            return theX;
        
        final int xFFFF = 65535;
        final int x10001 = 65537;
        int x = theX;
        int t0 = x10001 / x;
        int y = x10001 % x;
        if(y == 1)
            return (1 - t0) & xFFFF;
        
        int t1 = 1;
        while(y != 1) {
            final int q0 = x / y;
            x = x % y;
            t1 += q0 * t0;
            if(x == 1)
                return t1;
            
            final int q1 = y / x;
            y = y % x;
            t0 += q1 * t1;
        }
        return (1 - t0) & xFFFF;
    }
    
    public void run(final long $task) {
        final rolez.checked.internal.Tasks $tasks = new rolez.checked.internal.Tasks();
        try {
            final rolez.checked.lang.BlockPartitioner partitioner = new rolez.checked.lang.BlockPartitioner(8, $task);
            final rolez.checked.lang.CheckedArray<rolez.checked.lang.CheckedSlice<byte[]>[]> plainSlices = checkLegalRead(this, $task).plain.partition(partitioner, this.tasks);
            final rolez.checked.lang.CheckedArray<rolez.checked.lang.CheckedSlice<byte[]>[]> encryptedSlices = this.encrypted.partition(partitioner, this.tasks);
            final rolez.checked.lang.CheckedArray<rolez.checked.lang.CheckedSlice<byte[]>[]> decryptedSlices = this.decrypted.partition(partitioner, this.tasks);
            for(int i = 0; i < this.tasks; i++)
                $tasks.addInline(rolez.checked.lang.TaskSystem.getDefault().start(this.$encryptDecryptTask(checkLegalRead(plainSlices, $task).data[i], checkLegalRead(encryptedSlices, $task).data[i], checkLegalRead(this, $task).encryptKey)));
            for(int i = 0; i < this.tasks; i++)
                $tasks.addInline(rolez.checked.lang.TaskSystem.getDefault().start(this.$encryptDecryptTask(checkLegalRead(encryptedSlices, $task).data[i], checkLegalRead(decryptedSlices, $task).data[i], checkLegalRead(this, $task).decryptKey)));
        }
        finally {
            $tasks.joinAll();
        }
    }
    
    public void encryptDecrypt(final rolez.checked.lang.CheckedSlice<byte[]> src, final rolez.checked.lang.CheckedSlice<byte[]> dst, final int[] key, final long $task) {
        final int xFF = 255;
        final int xFFFF = 65535;
        final long x10001L = 65537L;
        int iSrc = src.range.begin;
        int iDst = src.range.begin;
        for(int i = src.range.begin; i < src.range.end; i += 8) {
            int x1 = checkLegalRead(src, $task).getByte(iSrc++) & xFF;
            x1 = x1 | ((src.getByte(iSrc++) & xFF) << 8);
            int x2 = src.getByte(iSrc++) & xFF;
            x2 = x2 | ((src.getByte(iSrc++) & xFF) << 8);
            int x3 = src.getByte(iSrc++) & xFF;
            x3 = x3 | ((src.getByte(iSrc++) & xFF) << 8);
            int x4 = src.getByte(iSrc++) & xFF;
            x4 = x4 | ((src.getByte(iSrc++) & xFF) << 8);
            int iKey = 0;
            for(int round = 0; round < 8; round++) {
                x1 = (int) (((((long) x1) * key[iKey++]) % x10001L) & xFFFF);
                x2 = (x2 + key[iKey++]) & xFFFF;
                x3 = (x3 + key[iKey++]) & xFFFF;
                x4 = (int) (((((long) x4) * key[iKey++]) % x10001L) & xFFFF);
                int t0 = x1 ^ x3;
                t0 = (int) (((((long) t0) * key[iKey++]) % x10001L) & xFFFF);
                int t1 = (t0 + (x2 ^ x4)) & xFFFF;
                t1 = (int) (((((long) t1) * key[iKey++]) % x10001L) & xFFFF);
                t0 = (t1 + t0) & xFFFF;
                x1 = x1 ^ t1;
                x4 = x4 ^ t0;
                t0 = t0 ^ x2;
                x2 = x3 ^ t1;
                x3 = t0;
            }
            x1 = (int) (((((long) x1) * key[iKey++]) % x10001L) & xFFFF);
            x3 = (x3 + key[iKey++]) & xFFFF;
            x2 = (x2 + key[iKey++]) & xFFFF;
            x4 = (int) (((((long) x4) * key[iKey++]) % x10001L) & xFFFF);
            checkLegalWrite(dst, $task).setByte(iDst++, (byte) x1);
            dst.setByte(iDst++, (byte) (x1 >>> 8));
            dst.setByte(iDst++, (byte) x3);
            dst.setByte(iDst++, (byte) (x3 >>> 8));
            dst.setByte(iDst++, (byte) x2);
            dst.setByte(iDst++, (byte) (x2 >>> 8));
            dst.setByte(iDst++, (byte) x4);
            dst.setByte(iDst++, (byte) (x4 >>> 8));
        }
    }
    
    public rolez.checked.lang.Task<java.lang.Void> $encryptDecryptTask(final rolez.checked.lang.CheckedSlice<byte[]> src, final rolez.checked.lang.CheckedSlice<byte[]> dst, final int[] key) {
        return new rolez.checked.lang.Task<java.lang.Void>(new Object[]{dst}, new Object[]{src}, new Object[]{}) {
            @java.lang.Override
            protected java.lang.Void runRolez() {
                final long $task = idBits();
                final int xFF = 255;
                final int xFFFF = 65535;
                final long x10001L = 65537L;
                int iSrc = checkLegalRead(src, $task).range.begin;
                int iDst = checkLegalRead(src, $task).range.begin;
                for(int i = checkLegalRead(src, $task).range.begin; i < checkLegalRead(src, $task).range.end; i += 8) {
                    int x1 = checkLegalRead(src, $task).getByte(iSrc++) & xFF;
                    x1 = x1 | ((checkLegalRead(src, $task).getByte(iSrc++) & xFF) << 8);
                    int x2 = checkLegalRead(src, $task).getByte(iSrc++) & xFF;
                    x2 = x2 | ((checkLegalRead(src, $task).getByte(iSrc++) & xFF) << 8);
                    int x3 = checkLegalRead(src, $task).getByte(iSrc++) & xFF;
                    x3 = x3 | ((checkLegalRead(src, $task).getByte(iSrc++) & xFF) << 8);
                    int x4 = checkLegalRead(src, $task).getByte(iSrc++) & xFF;
                    x4 = x4 | ((checkLegalRead(src, $task).getByte(iSrc++) & xFF) << 8);
                    int iKey = 0;
                    for(int round = 0; round < 8; round++) {
                        x1 = (int) (((((long) x1) * key[iKey++]) % x10001L) & xFFFF);
                        x2 = (x2 + key[iKey++]) & xFFFF;
                        x3 = (x3 + key[iKey++]) & xFFFF;
                        x4 = (int) (((((long) x4) * key[iKey++]) % x10001L) & xFFFF);
                        int t0 = x1 ^ x3;
                        t0 = (int) (((((long) t0) * key[iKey++]) % x10001L) & xFFFF);
                        int t1 = (t0 + (x2 ^ x4)) & xFFFF;
                        t1 = (int) (((((long) t1) * key[iKey++]) % x10001L) & xFFFF);
                        t0 = (t1 + t0) & xFFFF;
                        x1 = x1 ^ t1;
                        x4 = x4 ^ t0;
                        t0 = t0 ^ x2;
                        x2 = x3 ^ t1;
                        x3 = t0;
                    }
                    x1 = (int) (((((long) x1) * key[iKey++]) % x10001L) & xFFFF);
                    x3 = (x3 + key[iKey++]) & xFFFF;
                    x2 = (x2 + key[iKey++]) & xFFFF;
                    x4 = (int) (((((long) x4) * key[iKey++]) % x10001L) & xFFFF);
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) x1);
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) (x1 >>> 8));
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) x3);
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) (x3 >>> 8));
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) x2);
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) (x2 >>> 8));
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) x4);
                    checkLegalWrite(dst, $task).setByte(iDst++, (byte) (x4 >>> 8));
                }
                return null;
            }
        };
    }
    
    public void validate(final long $task) {
        new rolez.checked.lang.Assertion(java.util.Arrays.equals(rolez.checked.lang.CheckedArray.unwrap(checkLegalRead(checkLegalRead(this, $task).plain, $task), byte[].class), rolez.checked.lang.CheckedArray.unwrap(checkLegalRead(this.decrypted, $task), byte[].class)));
    }
    
    public void main(final long $task) {
        this.buildTestData(new java.util.Random(42L), $task);
        java.lang.System.out.println("Press Enter to start");
        new java.util.Scanner(java.lang.System.in).nextLine();
        final rolez.checked.util.StopWatch watch = new rolez.checked.util.StopWatch();
        for(int i = 0; i < 20; i++) {
            watch.go();
            this.run($task);
            java.lang.System.out.println(watch.get());
        }
    }
    
    public rolez.checked.lang.Task<java.lang.Void> $mainTask() {
        return new rolez.checked.lang.Task<java.lang.Void>(new Object[]{this}, new Object[]{}, new Object[]{}) {
            @java.lang.Override
            protected java.lang.Void runRolez() {
                final long $task = idBits();
                IdeaEncryption.this.buildTestData(new java.util.Random(42L), $task);
                java.lang.System.out.println("Press Enter to start");
                new java.util.Scanner(java.lang.System.in).nextLine();
                final rolez.checked.util.StopWatch watch = new rolez.checked.util.StopWatch();
                for(int i = 0; i < 20; i++) {
                    watch.go();
                    IdeaEncryption.this.run($task);
                    java.lang.System.out.println(watch.get());
                }
                return null;
            }
        };
    }
    
    public static void main(final java.lang.String[] args) {
        rolez.checked.lang.TaskSystem.getDefault().run(new IdeaEncryption(0L).$mainTask());
    }
    
    @java.lang.Override
    protected java.util.List<?> guardedRefs() {
        return java.util.Arrays.asList(plain, encrypted, decrypted);
    }
}
