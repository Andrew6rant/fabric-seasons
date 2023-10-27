package io.github.lucaargolo.seasons.mixin;

import io.github.lucaargolo.seasons.FabricSeasons;
import io.github.lucaargolo.seasons.resources.FoliageSeasonColors;
import io.github.lucaargolo.seasons.resources.GrassSeasonColors;
import io.github.lucaargolo.seasons.utils.ColorHelper;
import io.github.lucaargolo.seasons.utils.Season;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.lucaargolo.seasons.FabricSeasons.getPercentageToNextSeason;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    @Inject(method = "method_1693", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/GrassColors;getDefaultColor()I"), cancellable = true)
    private static void seasons$injectGrassColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        Season currentSeason = FabricSeasons.getCurrentSeason();
        info.setReturnValue(
                ColorHelper.mixHexColors(
                        GrassSeasonColors.getColor(currentSeason.getNext(), 0.5D, 1.0D),
                        GrassSeasonColors.getColor(currentSeason, 0.5D, 1.0D),
                        getPercentageToNextSeason(MinecraftClient.getInstance().world)
                ));
    }

    @Inject(method = "method_1695", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/FoliageColors;getSpruceColor()I"), cancellable = true)
    private static void seasons$injectSpruceColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        Season currentSeason = FabricSeasons.getCurrentSeason();
        info.setReturnValue(
                ColorHelper.mixHexColors(
                        FoliageSeasonColors.getSpruceColor(currentSeason.getNext()),
                        FoliageSeasonColors.getSpruceColor(currentSeason),
                        getPercentageToNextSeason(MinecraftClient.getInstance().world)
                ));
    }

    @Inject(method = "method_1687", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/FoliageColors;getBirchColor()I"), cancellable = true)
    private static void seasons$injectBirchColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        Season currentSeason = FabricSeasons.getCurrentSeason();
        info.setReturnValue(
                ColorHelper.mixHexColors(
                        FoliageSeasonColors.getBirchColor(currentSeason.getNext()),
                        FoliageSeasonColors.getBirchColor(currentSeason),
                        getPercentageToNextSeason(MinecraftClient.getInstance().world)
                ));
    }

    @Inject(method = "method_1692", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/color/world/FoliageColors;getDefaultColor()I"), cancellable = true)
    private static void seasons$injectFoliageColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        Season currentSeason = FabricSeasons.getCurrentSeason();
        info.setReturnValue(
                ColorHelper.mixHexColors(
                        FoliageSeasonColors.getDefaultColor(currentSeason.getNext()),
                        FoliageSeasonColors.getDefaultColor(currentSeason),
                        getPercentageToNextSeason(MinecraftClient.getInstance().world)
                ));
    }

}
