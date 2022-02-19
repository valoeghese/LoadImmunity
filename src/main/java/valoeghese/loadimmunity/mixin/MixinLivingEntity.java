package valoeghese.loadimmunity.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import valoeghese.loadimmunity.LoadImmunity;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	public MixinLivingEntity(EntityType<?> p_i48580_1_, World p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	@Inject(at = @At("HEAD"), method = "isPushable", cancellable = true)
	private void onIsPushable(CallbackInfoReturnable<Boolean> info) {
		if ((Object)this instanceof ServerPlayerEntity) {
			if (LoadImmunity.isImmune(this.uuid)) {
				info.setReturnValue(false);
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "push", cancellable = true)
	private void onPush(CallbackInfo info) {
		if ((Object)this instanceof ServerPlayerEntity) {
			if (LoadImmunity.isImmune(this.uuid)) {
				info.cancel();
			}
		}
	}
}
