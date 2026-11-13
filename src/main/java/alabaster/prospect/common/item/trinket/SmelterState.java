package alabaster.prospect.common.item.trinket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SmelterState(int litTime, int litDuration, int cookingProgress, int cookingTotalTime) {

    public static final SmelterState EMPTY = new SmelterState(0, 0, 0, 0);

    public static final Codec<SmelterState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("lit_time").forGetter(SmelterState::litTime),
            Codec.INT.fieldOf("lit_duration").forGetter(SmelterState::litDuration),
            Codec.INT.fieldOf("cooking_progress").forGetter(SmelterState::cookingProgress),
            Codec.INT.fieldOf("cooking_total_time").forGetter(SmelterState::cookingTotalTime)
    ).apply(instance, SmelterState::new));

    public static final StreamCodec<ByteBuf, SmelterState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SmelterState::litTime,
            ByteBufCodecs.VAR_INT, SmelterState::litDuration,
            ByteBufCodecs.VAR_INT, SmelterState::cookingProgress,
            ByteBufCodecs.VAR_INT, SmelterState::cookingTotalTime,
            SmelterState::new
    );

    public boolean isLit() {
        return litTime > 0;
    }
}