package alabaster.prospect.integration.jade;

import alabaster.prospect.common.block.AbstractPlacedDynamiteBlock;
import alabaster.prospect.common.block.ExtinguishedLanternBlock;
import alabaster.prospect.common.block.ExtinguishedTorchBlock;
import alabaster.prospect.common.block.ExtinguishedWallTorchBlock;
import alabaster.prospect.common.block.PayloadRailBlock;
import alabaster.prospect.common.entity.dynamite.DynamiteEntity;
import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ProspectJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(SparkleNodeProvider.INSTANCE, SparkleNodeEntity.class);
        registration.registerEntityComponent(DynamiteFuseProvider.INSTANCE, DynamiteEntity.class);
        registration.registerBlockComponent(PlacedDynamiteProvider.INSTANCE, AbstractPlacedDynamiteBlock.class);
        registration.registerBlockComponent(PayloadRailProvider.INSTANCE, PayloadRailBlock.class);
        registration.registerBlockComponent(ExtinguishedLightProvider.INSTANCE, ExtinguishedTorchBlock.class);
        registration.registerBlockComponent(ExtinguishedLightProvider.INSTANCE, ExtinguishedWallTorchBlock.class);
        registration.registerBlockComponent(ExtinguishedLightProvider.INSTANCE, ExtinguishedLanternBlock.class);
    }
}
