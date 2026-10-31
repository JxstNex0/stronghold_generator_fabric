package de.strongholdhere;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.ArrayList;
import java.util.List;

public final class StrongholdData extends SavedData {
    private static final Codec<StrongholdData> CODEC = BlockPos.CODEC.listOf()
        .xmap(StrongholdData::new, data -> data.positions);
    private static final SavedDataType<StrongholdData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("strongholdhere", "strongholds"),
        StrongholdData::new, CODEC, null);
    private final List<BlockPos> positions;
    public StrongholdData() { this.positions = new ArrayList<>(); }
    private StrongholdData(List<BlockPos> positions) { this.positions = new ArrayList<>(positions); }
    public static StrongholdData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    public int count() { return positions.size(); }
    public void add(BlockPos pos) {
        if (!positions.contains(pos)) { positions.add(pos.immutable()); setDirty(); }
    }
    public void clear() {
        if (!positions.isEmpty()) { positions.clear(); setDirty(); }
    }
    public BlockPos nearest(BlockPos origin, int radiusBlocks) {
        long bestDistanceSquared = (long)radiusBlocks * radiusBlocks;
        BlockPos best = null;
        for (BlockPos target : positions) {
            long dx = (long)target.getX() - origin.getX();
            long dz = (long)target.getZ() - origin.getZ();
            long dist = dx * dx + dz * dz;
            if (dist <= bestDistanceSquared) { bestDistanceSquared = dist; best = target; }
        }
        return best;
    }
}
