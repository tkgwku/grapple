package mod.finediary.grapple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Mod.EventBusSubscriber(modid = GrappleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GrappleHandler {
	static List<PullPlayerTask> tasks = new ArrayList<PullPlayerTask>();
	static Map<String, Integer> cooltime = Maps.newHashMap();
	static int GRAPPLE_COOLTIME = 10;

	static void addPullPlayerTask(PullPlayerTask e){
		tasks.add(e);
	}

	static void addCooltime(EntityPlayer player){
		if (GRAPPLE_COOLTIME > 0) cooltime.put(player.getName().getString(), GRAPPLE_COOLTIME);
	}

	static boolean checkCooltime(EntityPlayer player){
		return !cooltime.containsKey(player.getName().getString());
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event){
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
}