package bluemoonjune.atomic.mixin;

import bluemoonjune.atomic.Atomic;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.save.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.*;
import java.nio.file.Files;

@Mixin(value = World.class)
public abstract class WorldMixin implements WorldSource {

	@Shadow
	public LevelStorage levelStorage;

	@Inject(
		method = "saveWorldData",
		at = @At("HEAD"),
		remap = false
	)
	public void saveFeatures(CallbackInfo ci) throws IOException {
		File file = levelStorage.getDataFile("atomic");
		if (file != null && (file.exists() || file.createNewFile())) {
			CompoundTag tag = new CompoundTag();
			for (String feature : Atomic.FEATURES.keySet()) {
				tag.putBoolean(feature, Atomic.FEATURES.get(feature));
			}
			tag.write(new DataOutputStream(Files.newOutputStream(file.toPath())));
		}
	}

	@Inject(
		method = "<init>(Lnet/minecraft/core/world/save/LevelStorage;Lnet/minecraft/core/world/settings/WorldConfiguration;Lnet/minecraft/core/world/Dimension;)V",
		at = @At("TAIL"),
		remap = false
	)
	public void loadFeatures(CallbackInfo ci) throws IOException {
		File file = levelStorage.getDataFile("atomic");
		if (file != null && file.exists()) {
			CompoundTag tag = new CompoundTag();
			tag.read(new DataInputStream(Files.newInputStream(file.toPath())));
			for (String feature : tag.getValue().keySet()) {
				Atomic.FEATURES.put(feature, tag.getBoolean(feature));
			}
		} else {
			for (String feature : Atomic.FEATURES.keySet()) {
				Atomic.FEATURES.put(feature, false);
			}
		}
	}
}
