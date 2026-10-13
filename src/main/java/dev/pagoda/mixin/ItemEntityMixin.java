package dev.pagoda.mixin;

import dev.pagoda.PagodaLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void pagoda$intoShulker(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (self.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer) || self.hasPickUpDelay()) {
			return;
		}
		ItemStack stack = self.getItem();
		int moved = PagodaLogic.absorb(serverPlayer, stack);
		if (moved <= 0) {
			return;
		}
		serverPlayer.take(self, moved);
		if (stack.isEmpty()) {
			self.discard();
			ci.cancel();
		} else {
			self.setItem(stack);
		}
	}
}
