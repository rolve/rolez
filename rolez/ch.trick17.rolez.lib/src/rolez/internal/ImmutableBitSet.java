package rolez.internal;

import static java.lang.Long.bitCount;
import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.copyOf;
import static java.util.Objects.requireNonNull;

import java.util.BitSet;

/**
 * Basically a copy of {@link BitSet} from JDK 7, with all mutating (and other) methods removed or
 * modified to return a new instance instead. Other methods have been adapted and optimized a bit.
 */
public class ImmutableBitSet implements Cloneable {
    /* BitSets are packed into arrays of "words." Currently a word is a long, which consists of 64
     * bits, requiring 6 address bits. The choice of word size is determined purely by performance
     * concerns. */
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    
    /* Used to shift left or right for a partial word mask */
    private static final long WORD_MASK = 0xffffffffffffffffL;
    
    public static final ImmutableBitSet EMPTY = new ImmutableBitSet(new long[0]);
    
    private final long[] words;
    
    /**
     * Given a bit index, return word index containing it.
     */
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }
    
    /**
     * Creates a bit set using <code>words</code> as the internal representation. <strong>This array
     * must not be used for anything else after calling this constructor!</strong> The last word (if
     * there is one) must be non-zero.
     */
    private ImmutableBitSet(long[] words) {
        this.words = requireNonNull(words);
    }
    
    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex
     *            a bit index
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     */
    public ImmutableBitSet set(int bitIndex) {
        if(bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        
        long[] newWords = copyOf(words, max(wordIndex(bitIndex) + 1, words.length));
        newWords[wordIndex(bitIndex)] |= (1L << bitIndex);
        return new ImmutableBitSet(newWords);
    }
    
    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex
     *            a bit index
     * @param value
     *            a boolean value to set
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     */
    public ImmutableBitSet set(int bitIndex, boolean value) {
        if(value)
            return set(bitIndex);
        else
            return clear(bitIndex);
    }
    
    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param bitIndex
     *            the index of the bit to be cleared
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     */
    public ImmutableBitSet clear(int bitIndex) {
        if(bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        
        int wordIndex = wordIndex(bitIndex);
        if(wordIndex >= words.length)
            return this;
        
        long newWord = words[wordIndex] & ~(1L << bitIndex);
        int newWordLength;
        if(wordIndex < words.length - 1 || newWord != 0)
            newWordLength = words.length; // "Most significant" word still has at least one bit set
        else {
            int i;
            for(i = words.length - 2; i >= 0; i--) // Find next "most significant" word with at least one bit set
                if(words[i] != 0)
                    break;
            newWordLength = i + 1;
        }
        
        long[] newWords = copyOf(words, newWordLength);
        if(wordIndex < newWordLength)
            newWords[wordIndex] = newWord;
        return new ImmutableBitSet(newWords);
    }
    
    /**
     * Returns the value of the bit with the specified index. The value is {@code true} if the bit
     * with the index {@code bitIndex} is currently set in this {@code BitSet}; otherwise, the
     * result is {@code false}.
     *
     * @param bitIndex
     *            the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     */
    public boolean get(int bitIndex) {
        if(bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        
        int wordIndex = wordIndex(bitIndex);
        return (wordIndex < words.length) && ((words[wordIndex] & (1L << bitIndex)) != 0);
    }
    
    /**
     * Returns the index of the first bit that is set to {@code true} that occurs on or after the
     * specified starting index. If no such bit exists then {@code -1} is returned.
     * <p>
     * To iterate over the {@code true} bits in a {@code BitSet}, use the following loop:
     *
     * <pre>
     * for(int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
     *     // operate on index i here
     * }
     * </pre>
     *
     * @param fromIndex
     *            the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there is no such bit
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     */
    public int nextSetBit(int fromIndex) {
        if(fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        
        int u = wordIndex(fromIndex);
        if(u >= words.length)
            return -1;
        
        long word = words[u] & (WORD_MASK << fromIndex);
        
        while(true) {
            if(word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if(++u == words.length)
                return -1;
            word = words[u];
        }
    }
    
    /**
     * Returns the index of the first bit that is set to {@code false} that occurs on or after the
     * specified starting index.
     *
     * @param fromIndex
     *            the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     */
    public int nextClearBit(int fromIndex) {
        // Neither spec nor implementation handle bitsets of maximal length.
        // See 4816253.
        if(fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        
        int u = wordIndex(fromIndex);
        if(u >= words.length)
            return fromIndex;
        
        long word = ~words[u] & (WORD_MASK << fromIndex);
        
        while(true) {
            if(word != 0)
                return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
            if(++u == words.length)
                return words.length * BITS_PER_WORD;
            word = ~words[u];
        }
    }
    
    /**
     * Returns the index of the nearest bit that is set to {@code true} that occurs on or before the
     * specified starting index. If no such bit exists, or if {@code -1} is given as the starting
     * index, then {@code -1} is returned.
     * <p>
     * To iterate over the {@code true} bits in a {@code BitSet}, use the following loop:
     *
     * <pre>
     * for(int i = bs.length(); (i = bs.previousSetBit(i - 1)) >= 0;) {
     *     // operate on index i here
     * }
     * </pre>
     *
     * @param fromIndex
     *            the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there is no such bit
     * @throws IndexOutOfBoundsException
     *             if the specified index is less than {@code -1}
     */
    public int previousSetBit(int fromIndex) {
        if(fromIndex < 0) {
            if(fromIndex == -1)
                return -1;
            throw new IndexOutOfBoundsException(
                    "fromIndex < -1: " + fromIndex);
        }
        
        int u = wordIndex(fromIndex);
        if(u >= words.length)
            return length() - 1;
        
        long word = words[u] & (WORD_MASK >>> -(fromIndex + 1));
        
        while(true) {
            if(word != 0)
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if(u-- == 0)
                return -1;
            word = words[u];
        }
    }
    
    /**
     * Returns the index of the nearest bit that is set to {@code false} that occurs on or before
     * the specified starting index. If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex
     *            the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there is no such bit
     * @throws IndexOutOfBoundsException
     *             if the specified index is less than {@code -1}
     */
    public int previousClearBit(int fromIndex) {
        if(fromIndex < 0) {
            if(fromIndex == -1)
                return -1;
            throw new IndexOutOfBoundsException(
                    "fromIndex < -1: " + fromIndex);
        }
        
        int u = wordIndex(fromIndex);
        if(u >= words.length)
            return fromIndex;
        
        long word = ~words[u] & (WORD_MASK >>> -(fromIndex + 1));
        
        while(true) {
            if(word != 0)
                return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
            if(u-- == 0)
                return -1;
            word = ~words[u];
        }
    }
    
    /**
     * Returns the "logical size" of this {@code BitSet}: the index of the highest set bit in the
     * {@code BitSet} plus one. Returns zero if the {@code BitSet} contains no set bits.
     *
     * @return the logical size of this {@code BitSet}
     */
    public int length() {
        if(words.length == 0)
            return 0;
        
        return BITS_PER_WORD * (words.length - 1) +
                (BITS_PER_WORD - numberOfLeadingZeros(words[words.length - 1]));
    }
    
    /**
     * Returns true if this {@code BitSet} contains no bits that are set to {@code true}.
     *
     * @return boolean indicating whether this {@code BitSet} is empty
     */
    public boolean isEmpty() {
        return words.length == 0;
    }
    
    /**
     * Returns true if the specified {@code BitSet} has any bits set to {@code true} that are also
     * set to {@code true} in this {@code BitSet}.
     *
     * @param set
     *            {@code BitSet} to intersect with
     * @return boolean indicating whether this {@code BitSet} intersects the specified
     *         {@code BitSet}
     */
    public boolean intersects(ImmutableBitSet set) {
        for(int i = min(words.length, set.words.length) - 1; i >= 0; i--)
            if((words[i] & set.words[i]) != 0)
                return true;
        return false;
    }
    
    /**
     * Returns the number of bits set to {@code true} in this {@code BitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code BitSet}
     */
    public int cardinality() {
        int sum = 0;
        for(long word : words)
            sum += bitCount(word);
        return sum;
    }
    
    /**
     * Returns the hash code value for this bit set. The hash code depends only on which bits are
     * set within this {@code BitSet}.
     * <p>
     * The hash code is defined to be the result of the following calculation:
     * 
     * <pre>
     * public int hashCode() {
     *     long h = 1234;
     *     long[] words = toLongArray();
     *     for(int i = words.length; --i >= 0;)
     *         h ^= words[i] * (i + 1);
     *     return (int) ((h >> 32) ^ h);
     * }
     * </pre>
     * 
     * Note that the hash code changes if the set of bits is altered.
     *
     * @return the hash code value for this bit set
     */
    @Override
    public int hashCode() {
        long h = 1234;
        for(int i = words.length; --i >= 0;)
            h ^= words[i] * (i + 1);
        
        return (int) ((h >> 32) ^ h);
    }
    
    /**
     * Returns the number of bits of space actually in use by this {@code BitSet} to represent bit
     * values. The maximum element in the set is the size - 1st element.
     *
     * @return the number of bits currently in this bit set
     */
    public int size() {
        return words.length * BITS_PER_WORD;
    }
    
    /**
     * Compares this object against the specified object. The result is {@code true} if and only if
     * the argument is not {@code null} and is a {@code Bitset} object that has exactly the same set
     * of bits set to {@code true} as this bit set. That is, for every nonnegative {@code int} index
     * {@code k},
     * 
     * <pre>
     * ((BitSet) obj).get(k) == this.get(k)
     * </pre>
     * 
     * must be true. The current sizes of the two bit sets are not compared.
     *
     * @param obj
     *            the object to compare with
     * @return {@code true} if the objects are the same; {@code false} otherwise
     * @see #size()
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ImmutableBitSet))
            return false;
        if(this == obj)
            return true;
        
        ImmutableBitSet set = (ImmutableBitSet) obj;
        
        if(words.length != set.words.length)
            return false;
        
        // Check words in use by both BitSets
        for(int i = 0; i < words.length; i++)
            if(words[i] != set.words[i])
                return false;
            
        return true;
    }
    
    /**
     * Returns a string representation of this bit set. For every index for which this
     * {@code BitSet} contains a bit in the set state, the decimal representation of that index is
     * included in the result. Such indices are listed in order from lowest to highest, separated by
     * ",&nbsp;" (a comma and a space) and surrounded by braces, resulting in the usual mathematical
     * notation for a set of integers.
     * <p>
     * Example:
     * 
     * <pre>
     * BitSet drPepper = new BitSet();
     * </pre>
     * 
     * Now {@code drPepper.toString()} returns "<code>{}</code>".
     * <p>
     * 
     * <pre>
     * drPepper.set(2);
     * </pre>
     * 
     * Now {@code drPepper.toString()} returns "<code>{2}</code>".
     * <p>
     * 
     * <pre>
     * drPepper.set(4);
     * drPepper.set(10);
     * </pre>
     * 
     * Now {@code drPepper.toString()} returns "<code>{2, 4, 10}</code>".
     *
     * @return a string representation of this bit set
     */
    @Override
    public String toString() {
        int numBits = (words.length > 128) ? cardinality() : words.length * BITS_PER_WORD;
        StringBuilder b = new StringBuilder(6 * numBits + 2);
        b.append('{');
        
        int i = nextSetBit(0);
        if(i != -1) {
            b.append(i);
            for(i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
                int endOfRun = nextClearBit(i);
                do {
                    b.append(", ").append(i);
                } while(++i < endOfRun);
            }
        }
        
        b.append('}');
        return b.toString();
    }
}
