package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record MinecartJumpingPayload(int entityId, boolean airborne, double x, double y, double z, double velX, double velY, double velZ) implements CustomPacketPayload {

    public static final Type<MinecartJumpingPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "minecart_jumping"));

    public static final StreamCodec<FriendlyByteBuf, MinecartJumpingPayload> STREAM_CODEC =
            StreamCodec.ofMember(MinecartJumpingPayload::write, MinecartJumpingPayload::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(airborne);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(velX);
        buf.writeDouble(velY);
        buf.writeDouble(velZ);
    }

    private static MinecartJumpingPayload read(FriendlyByteBuf buf) {
        return new MinecartJumpingPayload(buf.readVarInt(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public Vec3 pos() {
        return new Vec3(x, y, z);
    }

    public Vec3 vel() {
        return new Vec3(velX, velY, velZ);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}