package valoeghese.loadimmunity.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import valoeghese.loadimmunity.LoadImmunity;

import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Shadow protected UUID uuid;

	@Redirect(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;getEntityCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"
	), method = "collide")
	private Stream<VoxelShape> updatedGetEntityCollisions(World instance, Entity entity, AxisAlignedBB axisAlignedBB, Predicate predicate) {
		if ((Object)this instanceof ServerPlayerEntity) {
			if (LoadImmunity.isImmune(this.uuid)) {
				return Stream.of();
			}
		}

		return instance.getEntityCollisions(entity, axisAlignedBB, k -> predicate.test(k) && !LoadImmunity.isImmune(k.getUUID()));
	}
}
