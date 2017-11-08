package mod.finediary.grapple;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mod.finediary.grapple.packet.PacketHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = ModContainer.MODID, name = ModContainer.MODNAME, version = ModContainer.VERSION, acceptedMinecraftVersions = ModContainer.MCVER)
public class ModContainer {

    public static final String MODID = "grapple";
    public static final String MODNAME = "Grapple Mod";
    public static final String VERSION = "1.12-1.0.6";
    public static final String MCVER = "1.12";

    static Logger logger = LogManager.getLogger(ModContainer.MODID);

    public static Configuration config;
    public static Property[] conf = new Property[6];

    @ObjectHolder(ModContainer.MODID)
    public static class ModItems {
    	public static ItemScoutRod grapple = new ItemScoutRod();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        config = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();
        if (event.getSide() == Side.CLIENT){
            ModelLoader.setCustomModelResourceLocation(ModItems.grapple, 0, new ModelResourceLocation(ModItems.grapple.getRegistryName(), ""));
        }
        PacketHandler.init();
        MinecraftForge.EVENT_BUS.register(new GrappleEventHandler());
    }

    public static boolean loadConfig(){
        String cat = "grapple_" + VERSION.replaceAll("\\.", "_");
        try {
        	config.load();
            conf[0] = config.get(cat, "Durability", 4096, "durability of grapple rod ('0' means infinite)");
            conf[1] = config.get(cat, "GrappleCooltime", 10, "grapple cooltime (ticks)");
            conf[2] = config.get(cat, "FallDamageBufferMode", 1, "'0': damage almost all softened. '1': damage softened. '2': damage halfed. '3' damage not softened at all.");
            conf[3] = config.get(cat, "MaxDistance2", 32, "max distance scout can hook (must be in 8~256). That of ordinary fishing rod is 32. \nIn my experience, too large number set, hook goes out of drawing range.");
            conf[4] = config.get(cat, "GrappleMovement1", false, "when you grapple down some cliff, \nif true, you will be always thrown foreward, and if false, you will \nhit the ground.");
            conf[5] = config.get(cat, "OldSound", true, "whether use 1.12 bobber_throw sound (a little higher pitch) or still use that of 1.11.2 below");//for1.12
            //conf[5] = config.get(cat, "EnableRecipe", true, "enable crafting grapple");
            config.save();

        } catch (Exception e){
            e.printStackTrace();
        	logger.info("failed to read grapple.cfg");
        } finally {
            ItemScoutRod.durability = conf[0] == null ? 3000 : conf[0].getInt();
            ItemScoutRod.damageMode = conf[2] == null ? 1 : conf[2].getInt();
            EntityScoutHook.maxLength = conf[3] == null ? 32 : MathHelper.clamp(conf[3].getDouble(), 8, 256);
            ItemScoutRod.goForward = conf[4] == null ? false : conf[4].getBoolean();
            ItemScoutRod.throwSound = conf[5] == null || conf[5].getBoolean() ? SoundEvents.ENTITY_ARROW_SHOOT : SoundEvents.ENTITY_BOBBER_THROW;
            GrappleEventHandler.defaultct = conf[1] == null ? 10 : conf[1].getInt();
            //enableRecipes = conf[5] == null ? true : conf[5].getBoolean();

        	logger.info("loaded configuration");
		}
    	return true;
    }

/*
 *
 * In 1.12, we can register custom recipes with external json,
 * and when we register with GameRegistry, those recipes won't trigger tooltips and won't be written in recipe books.
 *
    @EventHandler
    public void init(FMLInitializationEvent event){
    	if (conf[1].getBoolean()) {
    		GameRegistry.addShapelessRecipe(
    				new ResourceLocation(MODID, "recipe_name"),
    				new ResourceLocation(MODID, "recipe_group"),
    				new ItemStack(ModItems.grapple),
    				Ingredient.fromItem(Items.FISHING_ROD),
    				Ingredient.fromItem(Items.BLAZE_ROD)
    				);
    	}
    }
*/
    @Mod.EventBusSubscriber(modid = ModContainer.MODID)
    public static class RegisterationHandler {
		public static final Set<Item> ITEMS = new HashSet<>();
		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
	    	event.getRegistry().register(ModItems.grapple);
		}
    }
}
