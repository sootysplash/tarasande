package su.mandora.tarasande.mixin.accessor;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public interface IEntity {
    Vec3d invokeGetRotationVector(float pitch, float yaw);

    void setRandom(Random random);

}
