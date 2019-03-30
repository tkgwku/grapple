package mod.finediary.grapple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mod.finediary.grapple.packet.ClientProxy;
import mod.finediary.grapple.packet.CommonProxy;
import mod.finediary.grapple.packet.PacketHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ObjectHolder;

@Mod(value=GrappleMod.MODID)
@Mod.EventBusSubscriber(modid = GrappleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GrappleMod {
	public static final Logger LOGGER = LogManager.getLogger();

	public static final String MODID = "grapple";

	static int DURABILITY = 256;

	GrappleMod instance;

	public static CommonProxy proxy = (FMLEnvironment.dist == Dist.CLIENT) ? new ClientProxy() : new CommonProxy();

	public GrappleMod() {
		instance = this;
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(instance);
		//TODO: configuration will be constructed here in the future version
		DURABILITY = 256;
		ItemScoutRod.FALLDAMAGE_MODE = 1;
		EntityScoutHook.ROD_STRING_LENGTH = 2048;
		ItemScoutRod.GO_FORWARD = true;
		ItemScoutRod.USE_NEW_ROD_SOUND = false;
		GrappleHandler.GRAPPLE_COOLTIME = 10;
	}

	@ObjectHolder(GrappleMod.MODID + ":grapple")
	public static class ModItems {
		public static ItemScoutRod grapple;
	}

	private void setup(final FMLCommonSetupEvent event){
		PacketHandler.register();
	}

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		event.getRegistry().register(new ItemScoutRod((new Item.Properties()).group(ItemGroup.TOOLS).defaultMaxDamage(DURABILITY)));
	}
}
