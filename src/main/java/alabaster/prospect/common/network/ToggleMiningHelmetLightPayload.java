package alabaster.prospect.common.network;

import alabaster.prospect.Prospect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleMiningHelmetLightPayload() implements CustomPacketPayload {

    public static final Type<ToggleMiningHelmetLightPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "toggle_mining_helmet_light"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMiningHelmetLightPayload> STREAM_CODEC =
            StreamCodec.unit(new ToggleMiningHelmetLightPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}