package com.lovetropics.gamemodebuild;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lovetropics.gamemodebuild.command.GamemodeBuildCommand;
import com.lovetropics.gamemodebuild.command.ItemFilterArgument;
import com.lovetropics.gamemodebuild.message.ListUpdateMessage;
import com.lovetropics.gamemodebuild.message.OpenBuildInventoryMessage;
import com.lovetropics.gamemodebuild.message.SetActiveMessage;
import com.lovetropics.gamemodebuild.message.SetScrollMessage;
import com.lovetropics.gamemodebuild.message.UpdateFilterMessage;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(GamemodeBuild.MODID)
public class GamemodeBuild {
	public static final String MODID = "gamemodebuild";
	public static final String NAME = "Build Mode";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String NET_PROTOCOL = "2";
	
	public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(MODID, "net"))
			.networkProtocolVersion(() -> NET_PROTOCOL)
			.clientAcceptedVersions(NET_PROTOCOL::equals)
			.serverAcceptedVersions(NET_PROTOCOL::equals)
			.simpleChannel();
	
	public GamemodeBuild() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		
		ModLoadingContext.get().registerConfig(Type.SERVER, GBConfigs.serverSpec);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		NETWORK.messageBuilder(OpenBuildInventoryMessage.class, 0, NetworkDirection.PLAY_TO_SERVER)
				.encoder(OpenBuildInventoryMessage::serialize).decoder(OpenBuildInventoryMessage::deserialize)
				.consumer(OpenBuildInventoryMessage::handle)
				.add();
		
		NETWORK.messageBuilder(SetActiveMessage.class, 1)
				.encoder(SetActiveMessage::serialize).decoder(SetActiveMessage::deserialize)
				.consumer(SetActiveMessage::handle)
				.add();
		
		NETWORK.messageBuilder(SetScrollMessage.class, 2, NetworkDirection.PLAY_TO_SERVER)
				.encoder(SetScrollMessage::serialize).decoder(SetScrollMessage::deserialize)
				.consumer(SetScrollMessage::handle)
				.add();
		
		NETWORK.messageBuilder(ListUpdateMessage.class, 3, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ListUpdateMessage::serialize).decoder(ListUpdateMessage::new)
				.consumer(ListUpdateMessage::handle)
				.add();
		
		NETWORK.messageBuilder(UpdateFilterMessage.class, 4, NetworkDirection.PLAY_TO_SERVER)
				.encoder(UpdateFilterMessage::serialize).decoder(UpdateFilterMessage::new)
				.consumer(UpdateFilterMessage::handle)
				.add();
		
		ArgumentTypes.register(GamemodeBuild.MODID + ":item_filter", ItemFilterArgument.class, new ArgumentSerializer<>(ItemFilterArgument::itemFilter));
	}
	
	private void doClientStuff(final FMLClientSetupEvent event) {
		GBKeyBindings.register();
	}
	
	@SubscribeEvent
	public void serverStarting(FMLServerStartingEvent event) {
		GamemodeBuildCommand.register(event.getCommandDispatcher());
	}
}
