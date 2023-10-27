package io.github.lucaargolo.seasons.mixin;

import io.github.lucaargolo.seasons.FabricSeasons;
import io.github.lucaargolo.seasons.mixed.BiomeMixed;
import io.github.lucaargolo.seasons.resources.FoliageSeasonColors;
import io.github.lucaargolo.seasons.resources.GrassSeasonColors;
import io.github.lucaargolo.seasons.utils.ColorHelper;
import io.github.lucaargolo.seasons.utils.ColorsCache;
import io.github.lucaargolo.seasons.utils.Season;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static io.github.lucaargolo.seasons.FabricSeasons.getPercentageToNextSeason;

@Mixin(Biome.class)
public abstract class BiomeMixin implements BiomeMixed {

    @Shadow @Final public Biome.Weather weather;
    @Shadow @Final private BiomeEffects effects;

    @Shadow protected abstract int getDefaultGrassColor();

    @Shadow protected abstract int getDefaultFoliageColor();

    private Biome.Weather originalWeather;

    @SuppressWarnings({"ConstantConditions", "removal", "OptionalAssignedToNull"})
    @Environment(EnvType.CLIENT)
    @Inject(at = @At("TAIL"), method = "getGrassColorAt", cancellable = true)
    public void seasons$getSeasonGrassColor(double x, double z, CallbackInfoReturnable<Integer> cir) {
        Biome biome = (Biome) ((Object) this);
        Optional<Integer> overridedColor;
        Season currentSeason = FabricSeasons.getCurrentSeason();
        if(ColorsCache.hasGrassCache(biome)) {
            overridedColor = ColorsCache.getGrassCache(biome);
        }
        else {
            overridedColor = effects.getGrassColor();
            World world = MinecraftClient.getInstance().world;
            if(world != null) {
                Identifier biomeIdentifier = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
                Optional<Integer> seasonGrassColor = GrassSeasonColors.getSeasonGrassColor(biome, biomeIdentifier, FabricSeasons.getCurrentSeason());
                Optional<Integer> nextSeasonGrassColor = GrassSeasonColors.getSeasonGrassColor(biome, biomeIdentifier, FabricSeasons.getCurrentSeason().getNext());

                if(seasonGrassColor.isPresent() && nextSeasonGrassColor.isPresent()) {
                    overridedColor = Optional.of(ColorHelper.mixHexColors(nextSeasonGrassColor.get(),
                            seasonGrassColor.get(),
                            getPercentageToNextSeason(world)));
                }
            }
            ColorsCache.createGrassCache(biome, overridedColor);
        }
        if(effects.getGrassColorModifier() == BiomeEffects.GrassColorModifier.SWAMP) {
            int swampColor1 = GrassSeasonColors.getSwampColor1(currentSeason);
            int swampColor2 = GrassSeasonColors.getSwampColor2(currentSeason);

            double d = Biome.FOLIAGE_NOISE.sample(x * 0.0225D, z * 0.0225D, false);
            cir.setReturnValue(d < -0.1D ? swampColor1 : swampColor2);
        }else if(overridedColor != null){
            Integer integer = overridedColor.orElseGet(this::getDefaultGrassColor);
            cir.setReturnValue(effects.getGrassColorModifier().getModifiedGrassColor(x, z, integer));
        }
    }

    @SuppressWarnings({"ConstantConditions", "OptionalAssignedToNull"})
    @Environment(EnvType.CLIENT)
    @Inject(at = @At("TAIL"), method = "getFoliageColor", cancellable = true)
    public void seasons$getSeasonFoliageColor(CallbackInfoReturnable<Integer> cir) {
        Biome biome = (Biome) ((Object) this);
        Optional<Integer> overridedColor;
        if(ColorsCache.hasFoliageCache(biome)) {
            overridedColor = ColorsCache.getFoliageCache(biome);
        }else{
            overridedColor = effects.getFoliageColor();
            World world = MinecraftClient.getInstance().world;
            if(world != null) {
                Identifier biomeIdentifier = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
                Optional<Integer> seasonFoliageColor = FoliageSeasonColors.getSeasonFoliageColor(biome, biomeIdentifier, FabricSeasons.getCurrentSeason());
                if(seasonFoliageColor.isPresent()) {
                    overridedColor = seasonFoliageColor;
                }
            }
            ColorsCache.createFoliageCache(biome, overridedColor);
        }
        if(overridedColor != null) {
            Integer integer = overridedColor.orElseGet(this::getDefaultFoliageColor);
            cir.setReturnValue(integer);
        }
    }

    @Environment(EnvType.CLIENT)
    @Inject(at = @At("HEAD"), method = "getDefaultFoliageColor", cancellable = true)
    public void seasons$getSeasonDefaultFolliageColor(CallbackInfoReturnable<Integer> cir) {
        Season currentSeason = FabricSeasons.getCurrentSeason();
        double temperature;
        double downfall;
        if (this.originalWeather != null) {
            temperature = MathHelper.clamp(this.originalWeather.temperature(), 0.0F, 1.0F);
            downfall = MathHelper.clamp(this.originalWeather.downfall(), 0.0F, 1.0F);
        } else {
            temperature = MathHelper.clamp(this.weather.temperature(), 0.0F, 1.0F);
            downfall = MathHelper.clamp(this.weather.downfall(), 0.0F, 1.0F);
        }
        cir.setReturnValue(
                ColorHelper.mixHexColors(
                        FoliageSeasonColors.getColor(currentSeason.getNext(), temperature, downfall),
                        FoliageSeasonColors.getColor(currentSeason, temperature, downfall),
                        getPercentageToNextSeason(MinecraftClient.getInstance().world)
                ));
    }

    @Environment(EnvType.CLIENT)
    @Inject(at = @At("HEAD"), method = "getDefaultGrassColor", cancellable = true)
    public void seasons$getSeasonDefaultGrassColor(CallbackInfoReturnable<Integer> cir) {
        Season currentSeason = FabricSeasons.getCurrentSeason();
        double temperature;
        double downfall;
        if (this.originalWeather != null) {
            temperature = MathHelper.clamp(this.originalWeather.temperature(), 0.0F, 1.0F);
            downfall = MathHelper.clamp(this.originalWeather.downfall(), 0.0F, 1.0F);
        } else {
            temperature = MathHelper.clamp(this.weather.temperature(), 0.0F, 1.0F);
            downfall = MathHelper.clamp(this.weather.downfall(), 0.0F, 1.0F);
        }
        cir.setReturnValue(
                ColorHelper.mixHexColors(
                        GrassSeasonColors.getColor(currentSeason.getNext(), temperature, downfall),
                        GrassSeasonColors.getColor(currentSeason, temperature, downfall),
                        getPercentageToNextSeason(MinecraftClient.getInstance().world)
                ));
    }

    @Override
    public Biome.Weather seasons$getOriginalWeather() {
        return this.originalWeather;
    }

    @Override
    public void seasons$setOriginalWeather(Biome.Weather originalWeather) {
        this.originalWeather = originalWeather;
    }
}
