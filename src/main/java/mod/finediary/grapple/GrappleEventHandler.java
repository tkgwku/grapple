package mod.finediary.grapple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class GrappleEventHandler {
    static List<PullPlayerTask> tasks = new ArrayList<PullPlayerTask>();
    static Map<String, Integer> cooltime = Maps.newHashMap();
    static int defaultct = 10;

    public GrappleEventHandler(){}

    static void addPullPlayerTask(PullPlayerTask e){
        tasks.add(e);
    }

    static void addCooltime(EntityPlayer player){
    	if (defaultct > 0) cooltime.put(player.getName(), defaultct);
    }

    static boolean canHook(EntityPlayer player){
    	return !cooltime.containsKey(player.getName());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if (event.phase == Phase.END){
            if (!tasks.isEmpty()) {
                List<PullPlayerTask> finished = new ArrayList<PullPlayerTask>();
                // because tasks.remove(t); causes ConcurrentModificationException
                for (PullPlayerTask t: tasks){
                    if (t.pertick()) {
                        finished.add(t);
                    }
                }
                if (!finished.isEmpty()) tasks.removeAll(finished);
            }
            if (!cooltime.isEmpty()){
            	Map<String, Integer> newmap = Maps.newHashMap();
            	for (String key : cooltime.keySet()){
            		int value = cooltime.get(key);
            		if (value > 1){
            			newmap.put(key, value-1);
            		}
            	}
            	cooltime = newmap;
            }
        }
    }
    /*
     * config reloading for introduction video
     *
    @SubscribeEvent
    public void onChat(ServerChatEvent e){
    	if (e.getMessage().equals(":r")){
    		if (ModContainer.loadConfig()){
    			e.getPlayer().sendMessage(new TextComponentString(TextFormatting.GRAY+"[grapple] Reloaded configuration."));
    		}
        	e.setCanceled(true);
    	}
    }
    */
}
