package com.lovetropics.gamemodebuild;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

import com.lovetropics.gamemodebuild.message.SetActiveMessage;
import com.lovetropics.gamemodebuild.state.GBClientState;

@EventBusSubscriber(modid = GamemodeBuild.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class GBKeyBindings {
	
	public static final KeyBinding SWITCH_MODE = new KeyBinding("Enable/Disable Survival+ Mode", GLFW.GLFW_KEY_P, "Survival+");
	
	@SubscribeEvent
	public static void onKeyInput(ClientTickEvent event) {
		if (event.phase == Phase.END && SWITCH_MODE.isPressed()) {
			ClientPlayerEntity player = Minecraft.getInstance().player;
			if (player != null) {
				// don't set local state: await confirmation from the server
				boolean active = !GBClientState.isActive();
				GamemodeBuild.NETWORK.sendToServer(new SetActiveMessage(active));
			}
		}
	}
	
	public static void register() {
	}
}
