package de.strongholdhere;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

public final class StrongholdHereMod implements ModInitializer {
    public static final int EYE_SEARCH_RADIUS = 4096;
    private static final int GENERATION_Y = -25;

    @Override public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("stronghold")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("generate").executes(StrongholdHereMod::generate))
                .then(Commands.literal("info").executes(StrongholdHereMod::info))
                .then(Commands.literal("clear").executes(StrongholdHereMod::clear))));
    }

    private static ServerLevel overworld(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.level();
        if (!level.dimension().equals(Level.OVERWORLD)) {
            source.sendFailure(Component.literal("Stronghold Here: Nur in der Oberwelt."));
            return null;
        }
        return level;
    }

    private static int generate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = overworld(source);
        if (level == null) return 0;
        BlockPos playerPos = source.getPlayerOrException().blockPosition();
        BlockPos target = new BlockPos(playerPos.getX(), GENERATION_Y, playerPos.getZ());
        String place = "place structure minecraft:stronghold " + target.getX() + " " + target.getY() + " " + target.getZ();
        final int placed;
        try {
            placed = source.getServer().getCommands().getDispatcher().execute(place, source);
        } catch (CommandSyntaxException failure) {
            source.sendFailure(Component.literal("Stronghold konnte nicht erzeugt werden: " + failure.getMessage()));
            return 0;
        }
        if (placed < 1) {
            source.sendFailure(Component.literal("Keine Struktur erzeugt; kein Enderaugen-Ziel gespeichert."));
            return 0;
        }
        StrongholdData.get(level).add(target);
        source.sendSuccess(() -> Component.literal("Stronghold erzeugt, Enderaugen-Ziel: X=" + target.getX()
            + " Z=" + target.getZ() + " (ca. Y=" + GENERATION_Y + ")"), true);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = overworld(source);
        if (level == null) return 0;
        StrongholdData data = StrongholdData.get(level);
        BlockPos nearest = data.nearest(source.getPlayerOrException().blockPosition(), Integer.MAX_VALUE);
        if (nearest == null) {
            source.sendSuccess(() -> Component.literal("Noch keine Strongholds gespeichert."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Gespeicherte Ziele: " + data.count() + "; naechstes Ziel: "
            + nearest.getX() + " " + nearest.getY() + " " + nearest.getZ()), false);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = overworld(source);
        if (level == null) return 0;
        StrongholdData data = StrongholdData.get(level);
        int count = data.count();
        data.clear();
        source.sendSuccess(() -> Component.literal(count + " Enderaugen-Ziele entfernt (Bloecke bleiben)."), true);
        return 1;
    }
}
