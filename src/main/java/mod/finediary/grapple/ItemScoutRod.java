package mod.finediary.grapple;

import mod.finediary.grapple.EntityScoutHook.GrappleState;
import mod.finediary.grapple.packet.GrappleMSG;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemScoutRod extends ItemFishingRod{

	static int FALLDAMAGE_MODE = 1;
	static boolean USE_NEW_ROD_SOUND = true;
	static boolean GO_FORWARD = true;

	public ItemScoutRod(Item.Properties properties){
		super(properties);

		this.setRegistryName(GrappleMod.MODID, "grapple");

		this.addPropertyOverride(new ResourceLocation("cast"), (stack, worldIn, entityIn) -> {
			if (entityIn == null) {
				return 0.0F;
			} else {
				boolean flag = entityIn.getHeldItemMainhand() == stack;
				boolean flag1 = entityIn.getHeldItemOffhand() == stack;
				if (entityIn.getHeldItemMainhand().getItem() instanceof ItemScoutRod) {
					flag1 = false;
				}
				return (flag || flag1) && entityIn instanceof EntityPlayer && ((EntityPlayer)entityIn).fishEntity != null ? 1.0F : 0.0F;
			}
		});
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn){

		ItemStack itemstack = playerIn.getHeldItem(handIn);
		if (worldIn.isRemote){
			playerIn.swingArm(handIn);
			if (handIn.equals(EnumHand.MAIN_HAND)){
				GrappleMSG.EnumGrappleMessage.SWING_MAINHAND.sendToServer();
			} else {
				GrappleMSG.EnumGrappleMessage.SWING_OFFHAND.sendToServer();
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
		}

		if (playerIn.fishEntity != null){
			if (playerIn.fishEntity instanceof EntityScoutHook){
				boolean isVelocityChanged = true;
				float dx = (float) (playerIn.fishEntity.posX - playerIn.posX);
				float dy = (float) (playerIn.fishEntity.posY - playerIn.posY);
				float dz = (float) (playerIn.fishEntity.posZ - playerIn.posZ);
				GrappleState gstate = ((EntityScoutHook)playerIn.fishEntity).getGrappleReadyState();
				if (gstate == GrappleState.NOT_READY){
					playerIn.sendMessage(new TextComponentTranslation("unable.to.grapple"));
					isVelocityChanged = false;
				} else if (!GrappleHandler.checkCooltime(playerIn)){
					playerIn.sendMessage(new TextComponentTranslation("grapple.not.ready"));
					isVelocityChanged = false;
				}

				if (isVelocityChanged){
					float c_xz = 0.15F + 0.000015F * MathHelper.clamp(dy * dy, 0, 64);

					double xz_incr =c_xz - (c_xz/Math.cosh(dx*dx+dz*dz));
					double y_incr = 0.1 + Math.signum(dy) * Math.log(Math.cosh(0.225D*dy));

					if (dy > 0 && dy <= 8.1F) {
						y_incr *= 2.0F - 0.12333F * dy;
					} else if (dy <= -2.0F) {
						y_incr *= 0.8F;
						xz_incr *= 1.2F;
					}

					if (GO_FORWARD && dy < -5.0F){
						y_incr *= 1 / Math.abs(dy);
						xz_incr *= 1.6F;
					}

					for (PotionEffect effect :playerIn.getActivePotionEffects()){
						if (effect.getPotion().equals(MobEffects.SPEED)) {
							int a = MathHelper.clamp(effect.getAmplifier(), 1, 5);
							xz_incr *= 0.16F * (a + 6);
						}
					}

					double newMotionX = xz_incr * dx;
					double newMotionY = y_incr;
					double newMotionZ = xz_incr * dz;

					setMotion(playerIn, newMotionX, newMotionY, newMotionZ);
					GrappleHandler.addPullPlayerTask(new PullPlayerTask(playerIn, 1, newMotionX, newMotionY, newMotionZ));
					GrappleHandler.addCooltime(playerIn);

					switch (FALLDAMAGE_MODE){
					case 0:
						playerIn.fallDistance -= Math.abs(dy);
						break;
					case 1:
						if (dy < -3.0D || playerIn.fallDistance > 8.0F) {
							playerIn.fallDistance = 0.7F * playerIn.fallDistance - 0.5F * Math.abs(dy);
						}
						break;
					case 2:
						playerIn.fallDistance -= 3F + 0.5F * Math.abs(dy);
						break;
					case 3:
						playerIn.fallDistance -= 111;
						break;
					}

					worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ZOMBIE_INFECT, SoundCategory.NEUTRAL, 0.6F, 1.6F);
					itemstack.damageItem(1, playerIn);
					//GrappleMod.LOGGER.info(EnchantmentDurability.negateDamage(itemstack, EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, itemstack), random));
				} else if (USE_NEW_ROD_SOUND) {
					worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
				}

				((EntityScoutHook)playerIn.fishEntity).handleHookRetraction(itemstack);
			} else {
				playerIn.fishEntity.handleHookRetraction(itemstack);
			}
		} else {
			if (USE_NEW_ROD_SOUND) {
				worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ,
						SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL,
						0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
			} else {
				worldIn.playSound((EntityPlayer)null, playerIn.posX, playerIn.posY, playerIn.posZ,
						SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL,
						0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
			}

			if (!worldIn.isRemote)
			{
				EntityScoutHook entityfishhook = new EntityScoutHook(worldIn, playerIn);
				int j = EnchantmentHelper.getFishingLuckBonus(itemstack);

				// ?
				if (j > 0)
				{
					entityfishhook.setLuck(j);
				}

				int k = EnchantmentHelper.getFishingSpeedBonus(itemstack);

				// ?
				if (k > 0)
				{
					entityfishhook.setLureSpeed(k);
				}

				worldIn.spawnEntity(entityfishhook);
			}
			playerIn.addStat(StatList.ITEM_USED.get(this));
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	public static void setMotion(EntityPlayer player, double x, double y, double z){
		Entity e = player.getRidingEntity();
		if (e == null){
			player.motionX = x;
			player.motionY = y;
			player.motionZ = z;
			player.velocityChanged = true;
		} else {
			e.motionX = x;
			e.motionY = y;
			e.motionZ = z;
			e.velocityChanged = true;
		}
	}
}
