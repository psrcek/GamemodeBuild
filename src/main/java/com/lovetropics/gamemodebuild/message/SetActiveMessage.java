package com.lovetropics.gamemodebuild.message;

import java.util.function.Supplier;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.state.GBClientState;
import com.lovetropics.gamemodebuild.state.GBServerState;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public final class SetActiveMessage {
	private final boolean enabled;
	
	public SetActiveMessage(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void serialize(PacketBuffer buffer) {
		buffer.writeBoolean(this.enabled);
	}
	
	public static SetActiveMessage deserialize(PacketBuffer buffer) {
		return new SetActiveMessage(buffer.readBoolean());
	}
	
	public static boolean handle(SetActiveMessage message, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		ctx.enqueueWork(() -> {
			if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
				ServerPlayerEntity player = ctx.getSender();
				if (player != null) {
					if (!GBServerState.isEnabledFor(player)) {
						player.sendMessage(new StringTextComponent(GamemodeBuild.NAME + " is disabled!"), ChatType.GAME_INFO);
					} else {
						GBServerState.setActiveFor(player, message.enabled);
						GBServerState.switchInventories(player, message.enabled);
						if (message.enabled) {
							player.sendMessage(new StringTextComponent(GamemodeBuild.NAME + " activated"), ChatType.GAME_INFO);
						} else {
//							// Clear marked stacks from inventory
//							for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//								if (SPStackMarker.isMarked(player.inventory.getStackInSlot(i))) {
//									player.inventory.removeStackFromSlot(i);
//								}
//							}
							player.sendMessage(new StringTextComponent(GamemodeBuild.NAME + " deactivated"), ChatType.GAME_INFO);
						}
					}
				}
			} else if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> setClientState(message.enabled));
			}
		});
		
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	private static void setClientState(boolean state) {
		GBClientState.setActive(state);
	}
}
