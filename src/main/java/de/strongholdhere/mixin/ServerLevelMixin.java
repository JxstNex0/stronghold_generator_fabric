package de.strongholdhere.mixin;

import de.strongholdhere.StrongholdData;
import de.strongholdhere.StrongholdHereMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(
        method = "findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;",
        at = @At("HEAD"), cancellable = true)
    private void strongholdhere$findCustomStronghold(
        TagKey<Structure> structureTag, BlockPos origin, int searchRadius,
        boolean skipKnownStructures, CallbackInfoReturnable<BlockPos> cir
    ) {
        if (!structureTag.equals(StructureTags.EYE_OF_ENDER_LOCATED)) return;
        ServerLevel level = (ServerLevel)(Object)this;
        if (!level.dimension().equals(Level.OVERWORLD)) return;
        BlockPos target = StrongholdData.get(level).nearest(origin, StrongholdHereMod.EYE_SEARCH_RADIUS);
        if (target != null) cir.setReturnValue(target);
    }
}
