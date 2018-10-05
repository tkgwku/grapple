package mod.finediary.grapple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import mod.finediary.grapple.packet.PacketHandler;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@Mod(modid = ModContainer.MODID, name = ModContainer.MODNAME, version = ModContainer.VERSION)
public class ModContainer {

    public static final String MODID = "grapple";
    public static final String MODNAME = "Grapple Mod";
    public static final String VERSION = "1.7.10-1.0.0";

    static Configuration config;
    static Property[] conf = new Property[6];
    static Logger logger = LogManager.getLogger(ModContainer.MODID);
    static boolean enableRecipes;

    @EventHandler
    private void preInit(FMLPreInitializationEvent event){
        config = new Configuration(event.getSuggestedConfigurationFile());
        loadConfig();
        ItemScoutRod rod = new ItemScoutRod();
        GameRegistry.registerItem(rod, "grapple:grapple");
        PacketHandler.init();
        FMLCommonHandler.instance().bus().register(new GrappleEventHandler());
    }

    static boolean loadConfig(){
        String cat = "grapple_" + VERSION.replaceAll("\\.", "_");
        try {
        	config.load();
            conf[0] = config.get(cat, "Durability", 4096, "durability of grapple rod ('0' means infinite)");
            conf[1] = config.get(cat, "GrappleCooltime", 10, "grapple cooltime (ticks)");
            conf[2] = config.get(cat, "FallDamageBufferMode", 1, "'0': damage almost all softened. '1': damage softened. '2': damage halfed. '3' damage not softened at all.");
            conf[3] = config.get(cat, "MaxDistance2", 32, "max distance scout can hook (must be in 8~256). That of ordinary fishing rod is 32. \nIn my experience, too large number set, hook goes out of drawing range.");
            conf[4] = config.get(cat, "GrappleMovement1", false, "when you grapple down some cliff, \nif true, you will be always thrown foreward, and if false, you will \nhit the ground.");
            conf[5] = config.get(cat, "EnableRecipe", true, "enable crafting grapple");
            config.save();

        } catch (Exception e){
            e.printStackTrace();
        	logger.info("failed to read grapple.cfg");
        } finally {
            ItemScoutRod.durability = conf[0] == null ? 3000 : conf[0].getInt();
            ItemScoutRod.damageMode = conf[2] == null ? 1 : conf[2].getInt();
            EntityScoutHook.maxLength = conf[3] == null ? 32 : MathHelper.clamp_double(conf[3].getDouble(), 8, 256);
            ItemScoutRod.goForward = conf[4] == null ? false : conf[4].getBoolean();
            //ItemScoutRod.throwSound = conf[5] == null || conf[5].getBoolean() ? SoundEvents.ENTITY_ARROW_SHOOT : SoundEvents.ENTITY_BOBBER_THROW;
            GrappleEventHandler.defaultct = conf[1] == null ? 10 : conf[1].getInt();
            enableRecipes = conf[5] == null ? true : conf[5].getBoolean();

        	logger.info("loaded configuration");
		}
    	return true;
    }



    @EventHandler
    private void init(FMLInitializationEvent event){
        if (enableRecipes) {
            GameRegistry.addShapelessRecipe(
                    new ItemStack((Item)Item.itemRegistry.getObject("grapple:grapple")),
                    Items.fishing_rod,
                    Items.blaze_rod
                    );
        }
    }
}
