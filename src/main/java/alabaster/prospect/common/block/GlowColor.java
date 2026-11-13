package alabaster.prospect.common.block;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.joml.Vector3f;

public enum GlowColor implements StringRepresentable {
    BLUE("blue", new Vector3f(0.35F, 0.75F, 1.0F)),
    GREEN("green", new Vector3f(0.4F, 1.0F, 0.5F)),
    PURPLE("purple", new Vector3f(0.78F, 0.45F, 1.0F));

    public static final Codec<GlowColor> CODEC = StringRepresentable.fromEnum(GlowColor::values);

    private final String name;
    private final Vector3f particleColor;

    GlowColor(String name, Vector3f particleColor) {
        this.name = name;
        this.particleColor = particleColor;
    }

    public Vector3f particleColor() {
        return particleColor;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}