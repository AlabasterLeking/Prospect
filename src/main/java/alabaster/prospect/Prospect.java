package alabaster.prospect;

import alabaster.prospect.common.prospecting.ProspectingGems;
import alabaster.prospect.common.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Prospect.MODID)
public class Prospect {
    public static final String MODID = "prospect";
    public static final Logger LOGGER = LogManager.getLogger();

    public Prospect(IEventBus modEventBus, ModContainer modContainer) {

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        ProspectModBlocks.BLOCKS.register(modEventBus);
        ProspectModItems.ITEMS.register(modEventBus);
        ProspectModEntities.ENTITIES.register(modEventBus);
        ProspectModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ProspectParticleTypes.PARTICLE_TYPES.register(modEventBus);
        ProspectRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ProspectDataComponents.DATA_COMPONENTS.register(modEventBus);
        ProspectingGems.bootstrap();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Prospect is starting");
    }
}