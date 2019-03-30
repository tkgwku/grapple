package mod.finediary.grapple;

import net.minecraft.entity.player.EntityPlayer;

public class PullPlayerTask {
	public PullPlayerTask(EntityPlayer player, int tick, double motx, double moty, double motz){
		this.player = player;
		this.tick = tick;
		this.motx = motx;
		this.moty = moty;
		this.motz = motz;
	}

	EntityPlayer player; int tick; double motx; double moty; double motz;

	/*
	 * check if player's motion one tick after, and if it seems strange, give him the motion again.
	 * */
	public void execute(){
		if (Math.abs(this.player.motionY) / Math.abs(this.moty) > 0.9D) return;
		ItemScoutRod.setMotion(player, this.motx, this.moty, this.motz);
	}

	/*
	 * executed on tick to wait for time to begin
	 */
	public boolean pertick(){
		if (tick > 0){ tick--; return false; } else { execute(); return true; }
	}
}
