package rolez.util;

/**
 * A subclass of {@link java.util.Random} that allows <code>int</code>s to be used as seeds (since
 * there are no <code>long</code>s in Rolez yet). Below is the documentation of the original class:
 * <hr>
 * An instance of this class is used to generate a stream of pseudorandom numbers. The class uses a
 * 48-bit seed, which is modified using a linear congruential formula. (See Donald Knuth, <i>The Art
 * of Computer Programming, Volume 2</i>, Section 3.2.1.)
 * <p>
 * If two instances of {@code Random} are created with the same seed, and the same sequence of
 * method calls is made for each, they will generate and return identical sequences of numbers. In
 * order to guarantee this property, particular algorithms are specified for the class
 * {@code Random}. Java implementations must use all the algorithms shown here for the class
 * {@code Random}, for the sake of absolute portability of Java code. However, subclasses of class
 * {@code Random} are permitted to use other algorithms, so long as they adhere to the general
 * contracts for all the methods.
 * <p>
 * The algorithms implemented by class {@code Random} use a {@code protected} utility method that on
 * each invocation can supply up to 32 pseudorandomly generated bits.
 * <p>
 * Many applications will find the method {@link Math#random} simpler to use.
 * <p>
 * Instances of {@code java.util.Random} are threadsafe. However, the concurrent use of the same
 * {@code java.util.Random} instance across threads may encounter contention and consequent poor
 * performance. Consider instead using {@link java.util.concurrent.ThreadLocalRandom} in
 * multithreaded designs.
 * <p>
 * Instances of {@code java.util.Random} are not cryptographically secure. Consider instead using
 * {@link java.security.SecureRandom} to get a cryptographically secure pseudo-random number
 * generator for use by security-sensitive applications.
 *
 * @author Frank Yellin
 * @since 1.0
 */
public class Random extends java.util.Random {
    
    public Random() {}
    
    public Random(int seed) {
        super(seed);
    }
}
