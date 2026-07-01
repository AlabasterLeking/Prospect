package alabaster.prospect.common.mixin;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecart.class)
public interface AbstractMinecartLerpAccessor {
    @Accessor("onRails")
    void setOnRails(boolean onRails);

    @Accessor("lerpSteps")
    void setLerpSteps(int steps);

    @Accessor("lerpSteps")
    int getLerpSteps();

    @Accessor("lerpX")
    double getLerpX();

    @Accessor("lerpX")
    void setLerpX(double x);

    @Accessor("lerpY")
    void setLerpY(double y);

    @Accessor("lerpZ")
    double getLerpZ();

    @Accessor("lerpZ")
    void setLerpZ(double z);
}