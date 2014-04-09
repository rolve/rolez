package ch.trick17.peppl.manual.nbodies;

import static java.lang.Math.PI;
import static java.lang.Math.random;
import ch.trick17.peppl.lib.guard.Array;
import ch.trick17.peppl.lib.guard.GuardedObject;
import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;

public class NBodies implements Runnable {
    
    static final TaskSystem SYSTEM = new ThreadPoolTaskSystem();
    
    static final double SOLAR_MASS = 4 * PI * PI;
    static final double DAYS_PER_YEAR = 365.24;
    static final double DT = 0.01;
    
    public static void main(final String[] args) {
        SYSTEM.runDirectly(new NBodies(500, 10000));
    }
    
    private final int bodies;
    private final double iterations;
    
    public NBodies(final int bodies, final double iterations) {
        this.bodies = bodies;
        this.iterations = iterations;
    }
    
    public void run() {
        final Array<Body> system = createSystem();
        
        for(int i = 0; i < iterations; i++) {
            advanceSystem(system);
        }
    }
    
    private Array<Body> createSystem() {
        final Array<Body> system = new Array<>(new Body[bodies]);
        
        double px = 0, py = 0, pz = 0;
        for(int i = 1; i < bodies; i++) {
            final Body body = new Body();
            system.data[i] = body;
            
            px += body.vx * body.mass;
            py += body.vy * body.mass;
            pz += body.vz * body.mass;
        }
        
        // Offset momentum
        system.data[0] = new Body(0, 0, 0, -px / SOLAR_MASS, -py / SOLAR_MASS,
                -pz / SOLAR_MASS, SOLAR_MASS);
        
        return system;
    }
    
    private static void advanceSystem(final Array<Body> system) {
        for(int i = system.begin; i < system.end; i++) {
            for(int j = i + 1; j < system.end; j++) {
                final Body body1 = system.data[i];
                final Body body2 = system.data[j];
                final double dx = body1.x - body2.x;
                final double dy = body1.y - body2.y;
                final double dz = body1.z - body2.z;
                
                final double dSquared = dx * dx + dy * dy + dz * dz;
                final double mag = DT / (dSquared);
                
                body1.vx -= dx * body2.mass * mag;
                body1.vy -= dy * body2.mass * mag;
                body1.vz -= dz * body2.mass * mag;
                
                body2.vx += dx * body1.mass * mag;
                body2.vy += dy * body1.mass * mag;
                body2.vz += dz * body1.mass * mag;
            }
        }
        
        for(final Body body : system.data) {
            body.x += DT * body.vx;
            body.y += DT * body.vy;
            body.z += DT * body.vz;
        }
    }
    
    /* Body class */
    
    static class Body extends GuardedObject {
        double x, y, z, vx, vy, vz, mass;
        
        public Body(final double x, final double y, final double z,
                final double vx, final double vy, final double vz,
                final double mass) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.mass = mass;
        }
        
        public Body() {
            x = randomPos();
            y = randomPos();
            z = randomPos();
            vx = randomV();
            vy = randomV();
            vz = randomV();
            mass = random() * 0.001 * SOLAR_MASS;
        }
        
        private static double randomPos() {
            return (2 * random() - 1) * 20;
        }
        
        private static double randomV() {
            return (2 * random() - 1) * 0.01 * DAYS_PER_YEAR;
        }
    }
}
