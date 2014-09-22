package pl.falcone.car;

import org.jbox2d.common.Vec2;

/**
 * Utility methods for vector calculations.
 */
public class VecUtils {
    public static Vec2 rotate(Vec2 vec, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        float x = (float) ((vec.x * cos) - (vec.y * sin));
        float y = (float) ((vec.x * sin) + (vec.y * cos));

        return new Vec2(x, y);
    }
}
