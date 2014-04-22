package ch.trick17.peppl.manual.collision;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import ch.trick17.peppl.lib.task.TaskSystem;
import ch.trick17.peppl.lib.task.ThreadPoolTaskSystem;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

public class Collision implements Runnable {
    
    static final TaskSystem SYSTEM = new ThreadPoolTaskSystem();
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();
    
    private static final float MASS = 1;
    private static final float RADIUS = 0.4f;
    private static final float BOX_W = 2;
    
    private static final int SIZE = 20;
    private static final int BALLS = 4000;
    
    public static void main(final String[] args) {
        SYSTEM.runDirectly(new Collision());
    }
    
    public Collision() {}
    
    public void run() {
        final DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        final CollisionDispatcher dispatcher = new CollisionDispatcher(
                collisionConfiguration);
        final BroadphaseInterface broadphase = new DbvtBroadphase();
        final SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        
        final DiscreteDynamicsWorld dynamicsWorld = new DiscreteDynamicsWorld(
                dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, 0, 0));
        
        /* "Fence" */
        final float dist = SIZE / 2 + BOX_W / 2;
        dynamicsWorld.addRigidBody(box(new Vector3f(dist, BOX_W / 2, dist),
                new Vector3f(0, dist, 0)));
        dynamicsWorld.addRigidBody(box(new Vector3f(dist, BOX_W / 2, dist),
                new Vector3f(0, -dist, 0)));
        dynamicsWorld.addRigidBody(box(new Vector3f(BOX_W / 2, dist, dist),
                new Vector3f(dist, 0, 0)));
        dynamicsWorld.addRigidBody(box(new Vector3f(BOX_W / 2, dist, dist),
                new Vector3f(-dist, 0, 0)));
        dynamicsWorld.addRigidBody(box(new Vector3f(dist, dist, BOX_W / 2),
                new Vector3f(0, 0, dist)));
        dynamicsWorld.addRigidBody(box(new Vector3f(dist, dist, BOX_W / 2),
                new Vector3f(0, 0, -dist)));
        
        if(BALLS > SIZE * SIZE * SIZE)
            throw new RuntimeException("Too many balls!");
        
        /* Balls */
        final ArrayList<RigidBody> spheres = new ArrayList<>();
        final Set<Point3i> positions = new HashSet<>();
        for(int i = 0; i < BALLS; i++) {
            Point3i pos;
            do {
                pos = new Point3i(random.nextInt(SIZE), random.nextInt(SIZE),
                        random.nextInt(SIZE));
            } while(positions.contains(pos));
            positions.add(pos);
            final float x = -SIZE / 2 + pos.x + 0.5f;
            final float y = -SIZE / 2 + pos.y + 0.5f;
            final float z = -SIZE / 2 + pos.z + 0.5f;
            
            final RigidBody sphere = sphere(RADIUS, new Vector3f(x, y, z),
                    new Vector3f(randV(), randV(), randV()), MASS);
            dynamicsWorld.addRigidBody(sphere);
            spheres.add(sphere);
        }
        
        /* Run */
        final RigidBody sphere1 = spheres.get(random.nextInt(spheres.size()));
        final RigidBody sphere2 = spheres.get(random.nextInt(spheres.size()));
        debug(sphere1, sphere2);
        for(int i = 0; i < 2000; i++) {
            dynamicsWorld.stepSimulation(1 / 60.f, 10);
            debug(sphere1, sphere2);
        }
    }
    
    private static float randV() {
        return (float) random.nextDouble(-5, 5);
    }
    
    private static void debug(final RigidBody sphere1, final RigidBody sphere2) {
        final Transform trans = new Transform();
        final Vector3f v = new Vector3f();
        
        sphere1.getWorldTransform(trans);
        sphere1.getLinearVelocity(v);
        System.out.print(trans.origin.x + "\t" + trans.origin.y + "\t"
                + v.length() + "\t");
        sphere2.getWorldTransform(trans);
        sphere2.getLinearVelocity(v);
        System.out.println(trans.origin.x + "\t" + trans.origin.y + "\t"
                + v.length());
    }
    
    private static RigidBody box(final Vector3f halfExtents,
            final Vector3f position) {
        final BoxShape shape = new BoxShape(halfExtents);
        final DefaultMotionState state = new DefaultMotionState();
        final RigidBody body = new RigidBody(0, state, shape);
        body.translate(position);
        body.setFriction(0.1f);
        body.setRestitution(1);
        body.setCollisionFlags(CollisionFlags.STATIC_OBJECT);
        
        return body;
    }
    
    private static RigidBody sphere(final float radius,
            final Vector3f position, final Vector3f v, final float mass) {
        final SphereShape shape = new SphereShape(radius);
        final DefaultMotionState state = new DefaultMotionState();
        
        final RigidBody body = new RigidBody(mass, state, shape);
        body.translate(position);
        body.setFriction(0.1f);
        body.setRestitution(1);
        body.setLinearVelocity(v);
        
        return body;
    }
}
