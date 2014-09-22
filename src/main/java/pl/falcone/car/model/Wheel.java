package pl.falcone.car.model;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import pl.falcone.car.VecUtils;

public class Wheel {
    public final Body body;
    public final Body carBody;

    private final float relXPos;
    private final float relYPos;

    public Wheel(float relXPos, float relYPos, float wheelWidth, float wheelDiameter,
                 boolean revolving, Body carBody, World m_world) {
        this.relXPos = relXPos;
        this.relYPos = relYPos;
        this.carBody = carBody;

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position = carBody.getWorldPoint(new Vec2(relXPos, relYPos));
        bd.angle = carBody.getAngle();

        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.isSensor = true; // do not include wheels in collision system (for performance)
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(wheelWidth / 2, wheelDiameter / 2);
        fd.shape = shape;

        Body m_wheel = m_world.createBody(bd);
        m_wheel.createFixture(fd);

        if (revolving) {
            RevoluteJointDef jd = new RevoluteJointDef();
            jd.initialize(carBody, m_wheel, m_wheel.getWorldCenter());
            jd.enableMotor = true;
            m_world.createJoint(jd);
        } else {
            PrismaticJointDef jd = new PrismaticJointDef();
            jd.initialize(carBody, m_wheel, m_wheel.getWorldCenter(), new Vec2(1, 0));
            jd.enableLimit = true;
            jd.lowerTranslation = jd.upperTranslation = 0;
            m_world.createJoint(jd);
        }

        body = m_wheel;
    }

    /**
     * Sets wheel's angle relative to the car's body (in degrees).
     */
    public void setAngle(float angle) {
        body.m_sweep.a = carBody.getAngle() + angle;
    }

    /**
     * Sets wheel's velocity vector with sideways velocity subtracted.
     */
    public void eliminateSidewaysVelocity() {
        Vec2 velocity = body.getLinearVelocity();
        Vec2 sidewaysAxis = getDirectionVector();
        float dotProd = Vec2.dot(velocity, sidewaysAxis);

        body.setLinearVelocity(new Vec2(sidewaysAxis.x * dotProd, sidewaysAxis.y * dotProd));
    }

    private Vec2 getDirectionVector() {
        Vec2 velocityFromLocalPoint = carBody.getLinearVelocityFromLocalPoint(new Vec2(relXPos, relYPos));
        Vec2 localVelocity = carBody.getLocalVector(velocityFromLocalPoint);
        Vec2 xAxisNormalVec = (localVelocity.y > 0) ? new Vec2(0, 1) : new Vec2(0, -1);

        return VecUtils.rotate(xAxisNormalVec, body.getAngle());
    }

    public void applyForce(Vec2 forceVec) {
        Vec2 position = body.getWorldCenter();
        body.applyForce(body.getWorldVector(forceVec), position);
    }
}
