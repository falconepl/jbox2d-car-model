package pl.falcone.car;

import org.jbox2d.common.Vec2;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;
import pl.falcone.car.model.Acceleration;
import pl.falcone.car.model.Car;
import pl.falcone.car.model.SteerDirection;

public class TopDownCar extends TestbedTest {
    private Acceleration accel = Acceleration.IDLE;
    private SteerDirection steerDir = SteerDirection.NONE;

    private Car car;

    @Override
    public void initTest(boolean deserialized) {
        if (deserialized) {
            return;
        }

        // World
        m_world.setGravity(new Vec2(0, 0));

        // Car
        {
            float width = 2;
            float length = 4;
            float angle = 0;
            float wheelWidth = 0.4f;
            float wheelDiameter = 0.8f;

            car = new Car(width, length, angle, wheelWidth, wheelDiameter, m_world);
        }
    }

    @Override
    public String getTestName() {
        return "TopDownCar";
    }

    @Override
    public void keyPressed(char keyChar, int keyCode) {
        switch (keyChar) {
            case 'w':
                accel = Acceleration.ACCELERATE;
                break;
            case 'a':
                steerDir = SteerDirection.STEER_LEFT;
                break;
            case 's':
                accel = Acceleration.BREAK;
                break;
            case 'd':
                steerDir = SteerDirection.STEER_RIGHT;
                break;
        }
    }

    @Override
    public void keyReleased(char keyChar, int keyCode) {
        switch (keyChar) {
            case 'w':
            case 's':
                accel = Acceleration.IDLE;
                break;
        }

        switch (keyChar) {
            case 'a':
            case 'd':
                steerDir = SteerDirection.NONE;
                break;
        }
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
        super.step(settings);
        float hz = settings.getSetting(TestbedSettings.Hz).value;
        float msDuration = (hz > 0) ? (1 / hz * 1000) : 0;
        car.update(accel, steerDir, msDuration);
    }
}
