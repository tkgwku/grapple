package mod.finediary.grapple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.entity.player.EntityPlayer;

public class GrappleEventHandler {
    static List<PullPlayerTask> tasks = new ArrayList<PullPlayerTask>();
    static Map<UUID, Integer> cooltime = Maps.newHashMap();
    static int defaultct = 10;

    public GrappleEventHandler(){}

    static void addPullPlayerTask(PullPlayerTask e){
        tasks.add(e);
    }

    static void addCooltime(EntityPlayer player){
    	if (defaultct > 0) cooltime.put(player.getUniqueID(), defaultct);
    }

    static boolean canHook(EntityPlayer player){
    	return !cooltime.containsKey(player.getUniqueID());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if (event.phase == Phase.END){
            if (!tasks.isEmpty()) {
                List<PullPlayerTask> finished = new ArrayList<PullPlayerTask>();
                for (PullPlayerTask t: tasks){
                    if (t.pertick()) {
                        finished.add(t);
                    }
                }
                if (!finished.isEmpty()) tasks.removeAll(finished);
            }
            if (!cooltime.isEmpty()){
            	Map<UUID, Integer> newmap = Maps.newHashMap();
            	for (UUID key : cooltime.keySet()){
            		int value = cooltime.get(key);
            		if (value > 1){
            			newmap.put(key, value-1);
            		}
            	}
            	cooltime = newmap;
            }
        }
    }
}
