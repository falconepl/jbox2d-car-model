package pl.falcone.car.model;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

/**
 * Four-wheeled vehicle's representation.
 */
public class Car {
    public final Body body;

    public final Wheel frontLWheel;
    public final Wheel frontRWheel;
    public final Wheel backLWheel;
    public final Wheel backRWheel;

    public final float width;
    public final float length;

    public float wheelsSteerAngle;

    /**
     * Maximum wheels steer angle (in degrees).
     */
    private final float wheelMaxSteerAngle = 25;

    /**
     * Time it takes to maximally turn the wheel (in milliseconds).
     */
    private final int wheelMaxTurnTimeInMs = 200;

    /**
     * Current speed (in km/h).
     */
    private float speed;

    /**
     * Maximum car's speed (in km/h).
     */
    private float maxSpeed = 60;

    /**
     * Maximum car's reverse speed (in km/h).
     */
    private float maxReverseSpeed = 20;

    /**
     * Minimal car's speed (in km/h). Any lower speed causes
     * car to stop (to prevent infinite sliding).
     */
    private float minSpeed = 3;

    /**
     * Engine's power factor.
     */
    private float power = 60;

    public Car(float width, float length, float angle, float wheelWidth, float wheelDiameter, final World m_world) {
        this.width = width;
        this.length = length;

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(0.0f, 0.0f);
        bd.angle = (float) Math.toRadians(angle);
        bd.linearDamping = 0.15f;   // simulates friction
        bd.bullet = true;   // prevents tunneling
        bd.angularDamping = 0.3f;

        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.3f;
        fd.restitution = 0.4f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, length / 2);
        fd.shape = shape;

        Body m_car = m_world.createBody(bd);
        m_car.createFixture(fd);
        this.body = m_car;

        // Create wheels
        this.frontLWheel = new Wheel(-1, 1.2f, wheelWidth, wheelDiameter, true, body, m_world);
        this.frontRWheel = new Wheel(1, 1.2f, wheelWidth, wheelDiameter, true, body, m_world);
        this.backLWheel = new Wheel(-1, -1.2f, wheelWidth, wheelDiameter, false, body, m_world);
        this.backRWheel = new Wheel(1, -1.2f, wheelWidth, wheelDiameter, false, body, m_world);
    }

    public void update(Acceleration accel, SteerDirection steerDir, float msDuration) {
        // Update measured speed
        updateSpeed();

        // Kill sideways velocity
        frontLWheel.eliminateSidewaysVelocity();
        frontRWheel.eliminateSidewaysVelocity();
        backLWheel.eliminateSidewaysVelocity();
        backRWheel.eliminateSidewaysVelocity();

        // Update revolving wheels
        updateWheelsSteerAngle(steerDir, msDuration);
        frontLWheel.setAngle(wheelsSteerAngle);
        frontRWheel.setAngle(wheelsSteerAngle);

        // Apply force to powered wheels
        Vec2 forceVec = getForceVector(accel);
        frontLWheel.applyForce(forceVec);
        frontRWheel.applyForce(forceVec);

        // Prevent infinite sliding for low speed
        preventInfiniteSliding(accel);
    }

    private void updateWheelsSteerAngle(SteerDirection steerDir, float msDuration) {
        float wheelMaxSteerAngle = (float) Math.toRadians(this.wheelMaxSteerAngle);
        float angleDiff = (wheelMaxSteerAngle / wheelMaxTurnTimeInMs) * msDuration;

        if (steerDir == SteerDirection.STEER_LEFT) {
            wheelsSteerAngle = Math.min(Math.max(wheelsSteerAngle, 0) + angleDiff, wheelMaxSteerAngle);
        } else if (steerDir == SteerDirection.STEER_RIGHT) {
            wheelsSteerAngle = Math.max(Math.min(wheelsSteerAngle, 0) - angleDiff, -wheelMaxSteerAngle);
        } else {
            wheelsSteerAngle = 0;
        }
    }

    private Vec2 getForceVector(Acceleration accel) {
        Vec2 baseVec = new Vec2();

        if (accel == Acceleration.ACCELERATE && speed < maxSpeed) {
            baseVec.set(0, 1);
        } else if (accel == Acceleration.BREAK && -speed < maxReverseSpeed) {
            baseVec.set((getLocalVelocity().y < 0) ? new Vec2(0, -1.3f) : new Vec2(0, -0.7f));
        } else {
            baseVec.set(0, 0);
        }

        return new Vec2(power * baseVec.x, power * baseVec.y);
    }

    private Vec2 getLocalVelocity() {
        Vec2 velocityFromLocalPoint = body.getLinearVelocityFromLocalPoint(new Vec2(0, 0));
        return body.getLocalVector(velocityFromLocalPoint);
    }

    private void updateSpeed() {
        speed = getLocalVelocity().y / 1000 * 3600;
    }

    private void preventInfiniteSliding(Acceleration accel) {
        if (accel == Acceleration.IDLE && Math.abs(speed) < minSpeed) {
            body.setLinearVelocity(new Vec2(0, 0));
        }
    }
}
