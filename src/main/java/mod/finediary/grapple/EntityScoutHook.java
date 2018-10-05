package mod.finediary.grapple;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityScoutHook extends EntityFishHook{
    private int field_146037_g;
    private int field_146048_h;
    private int field_146050_i;
    private Block field_146046_j;
    private boolean field_146051_au;
    private int field_146049_av;
    private int field_146047_aw;
    private int field_146045_ax;
    private int field_146040_ay;
    private int field_146038_az;
    private float field_146054_aA;
    private int field_146055_aB;
    private double field_146056_aC;
    private double field_146057_aD;
    private double field_146058_aE;
    private double field_146059_aF;
    private double field_146060_aG;
    @SideOnly(Side.CLIENT)
    private double field_146061_aH;
    @SideOnly(Side.CLIENT)
    private double field_146052_aI;
    @SideOnly(Side.CLIENT)
    private double field_146053_aJ;
    private static final String __OBFID = "CL_00001663";

    // config
    static double maxLength = 32D;

	public EntityScoutHook(World p_i1764_1_) {
		super(p_i1764_1_);
        this.field_146037_g = -1;
        this.field_146048_h = -1;
        this.field_146050_i = -1;
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
	}


    @SideOnly(Side.CLIENT)
    public EntityScoutHook(World p_i1765_1_, double p_i1765_2_, double p_i1765_4_, double p_i1765_6_, EntityPlayer p_i1765_8_) {
	    this(p_i1765_1_);
	    this.setPosition(p_i1765_2_, p_i1765_4_, p_i1765_6_);
	    this.ignoreFrustumCheck = true;
	    this.field_146042_b = p_i1765_8_;
	    p_i1765_8_.fishEntity = this;
    }

    public EntityScoutHook(World p_i1766_1_, EntityPlayer p_i1766_2_) {
    	super(p_i1766_1_, p_i1766_2_);
        this.field_146037_g = -1;
        this.field_146048_h = -1;
        this.field_146050_i = -1;
        this.ignoreFrustumCheck = true;
        this.field_146042_b = p_i1766_2_;
        this.field_146042_b.fishEntity = this;
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(p_i1766_2_.posX, p_i1766_2_.posY + 1.62D - (double)p_i1766_2_.yOffset, p_i1766_2_.posZ, p_i1766_2_.rotationYaw, p_i1766_2_.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.func_146035_c(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
    }

	public void onUpdate(){
        if (this.field_146055_aB > 0){
            double d7 = this.posX + (this.field_146056_aC - this.posX) / (double)this.field_146055_aB;
            double d8 = this.posY + (this.field_146057_aD - this.posY) / (double)this.field_146055_aB;
            double d9 = this.posZ + (this.field_146058_aE - this.posZ) / (double)this.field_146055_aB;
            double d1 = MathHelper.wrapAngleTo180_double(this.field_146059_aF - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d1 / (double)this.field_146055_aB);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.field_146060_aG - (double)this.rotationPitch) / (double)this.field_146055_aB);
            --this.field_146055_aB;
            this.setPosition(d7, d8, d9);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        } else {
            if (!this.worldObj.isRemote) {
                ItemStack itemstack = this.field_146042_b.getCurrentEquippedItem();

                // (+) not to set dead automatically
                // (+) let max distance configurable
                if (this.field_146042_b.isDead || !this.field_146042_b.isEntityAlive() || itemstack == null || !(itemstack.getItem() instanceof ItemFishingRod) || this.getDistanceSqToEntity(this.field_146042_b) > maxLength * maxLength) {
                    this.setDead();
                    this.field_146042_b.fishEntity = null;
                    return;
                }

                if (this.field_146043_c != null)
                {
                    if (!this.field_146043_c.isDead)
                    {
                        this.posX = this.field_146043_c.posX;
                        this.posY = this.field_146043_c.boundingBox.minY + (double)this.field_146043_c.height * 0.8D;
                        this.posZ = this.field_146043_c.posZ;
                        return;
                    }

                    this.field_146043_c = null;
                }
            }

            if (this.field_146044_a > 0)
            {
                --this.field_146044_a;
            }

            if (this.field_146051_au)
            {
                if (this.worldObj.getBlock(this.field_146037_g, this.field_146048_h, this.field_146050_i) == this.field_146046_j)
                {
                    ++this.field_146049_av;

                    if (this.field_146049_av == 1200)
                    {
                        this.setDead();
                    }

                    return;
                }

                this.field_146051_au = false;
                this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
                this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
                this.field_146049_av = 0;
                this.field_146047_aw = 0;
            }
            else
            {
                ++this.field_146047_aw;
            }

            Vec3 vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3);
            vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            double d2;

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity1 = (Entity)list.get(i);

                if (entity1.canBeCollidedWith() && (entity1 != this.field_146042_b || this.field_146047_aw >= 5))
                {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.boundingBox.expand((double)f, (double)f, (double)f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec31, vec3);

                    if (movingobjectposition1 != null)
                    {
                        d2 = vec31.distanceTo(movingobjectposition1.hitVec);

                        if (d2 < d0 || d0 == 0.0D)
                        {
                            entity = entity1;
                            d0 = d2;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.field_146042_b), 0.0F))
                    {
                        this.field_146043_c = movingobjectposition.entityHit;
                    }
                }
                else
                {
                    this.field_146051_au = true;
                }
            }

            if (!this.field_146051_au)
            {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                float f5 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

                for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f5) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
                {
                    ;
                }

                while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
                {
                    this.prevRotationPitch += 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw < -180.0F)
                {
                    this.prevRotationYaw -= 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
                {
                    this.prevRotationYaw += 360.0F;
                }

                this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
                this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
                float f6 = 0.92F;

                if (this.onGround || this.isCollidedHorizontally)
                {
                    f6 = 0.5F;
                }

                byte b0 = 5;
                double d10 = 0.0D;

                for (int j = 0; j < b0; ++j)
                {
                    double d3 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (double)(j + 0) / (double)b0 - 0.125D + 0.125D;
                    double d4 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (double)(j + 1) / (double)b0 - 0.125D + 0.125D;
                    AxisAlignedBB axisalignedbb1 = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d3, this.boundingBox.minZ, this.boundingBox.maxX, d4, this.boundingBox.maxZ);

                    if (this.worldObj.isAABBInMaterial(axisalignedbb1, Material.water))
                    {
                        d10 += 1.0D / (double)b0;
                    }
                }

                if (!this.worldObj.isRemote && d10 > 0.0D)
                {
                    WorldServer worldserver = (WorldServer)this.worldObj;
                    int k = 1;

                    if (this.rand.nextFloat() < 0.25F && this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ)))
                    {
                        k = 2;
                    }

                    if (this.rand.nextFloat() < 0.5F && !this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ)))
                    {
                        --k;
                    }

                    if (this.field_146045_ax > 0)
                    {
                        --this.field_146045_ax;

                        if (this.field_146045_ax <= 0)
                        {
                            this.field_146040_ay = 0;
                            this.field_146038_az = 0;
                        }
                    }
                    else
                    {
                        float f1;
                        float f2;
                        double d5;
                        double d6;
                        float f7;
                        double d11;

                        if (this.field_146038_az > 0)
                        {
                            this.field_146038_az -= k;

                            if (this.field_146038_az <= 0)
                            {
                                this.motionY -= 0.20000000298023224D;
                                this.playSound("random.splash", 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                                f1 = (float)MathHelper.floor_double(this.boundingBox.minY);
                                worldserver.func_147487_a("bubble", this.posX, (double)(f1 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                                worldserver.func_147487_a("wake", this.posX, (double)(f1 + 1.0F), this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D);
                                this.field_146045_ax = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                            }
                            else
                            {
                                this.field_146054_aA = (float)((double)this.field_146054_aA + this.rand.nextGaussian() * 4.0D);
                                f1 = this.field_146054_aA * 0.017453292F;
                                f7 = MathHelper.sin(f1);
                                f2 = MathHelper.cos(f1);
                                d11 = this.posX + (double)(f7 * (float)this.field_146038_az * 0.1F);
                                d5 = (double)((float)MathHelper.floor_double(this.boundingBox.minY) + 1.0F);
                                d6 = this.posZ + (double)(f2 * (float)this.field_146038_az * 0.1F);

                                if (this.rand.nextFloat() < 0.15F)
                                {
                                    worldserver.func_147487_a("bubble", d11, d5 - 0.10000000149011612D, d6, 1, (double)f7, 0.1D, (double)f2, 0.0D);
                                }

                                float f3 = f7 * 0.04F;
                                float f4 = f2 * 0.04F;
                                worldserver.func_147487_a("wake", d11, d5, d6, 0, (double)f4, 0.01D, (double)(-f3), 1.0D);
                                worldserver.func_147487_a("wake", d11, d5, d6, 0, (double)(-f4), 0.01D, (double)f3, 1.0D);
                            }
                        }
                        else if (this.field_146040_ay > 0)
                        {
                            this.field_146040_ay -= k;
                            f1 = 0.15F;

                            if (this.field_146040_ay < 20)
                            {
                                f1 = (float)((double)f1 + (double)(20 - this.field_146040_ay) * 0.05D);
                            }
                            else if (this.field_146040_ay < 40)
                            {
                                f1 = (float)((double)f1 + (double)(40 - this.field_146040_ay) * 0.02D);
                            }
                            else if (this.field_146040_ay < 60)
                            {
                                f1 = (float)((double)f1 + (double)(60 - this.field_146040_ay) * 0.01D);
                            }

                            if (this.rand.nextFloat() < f1)
                            {
                                f7 = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292F;
                                f2 = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
                                d11 = this.posX + (double)(MathHelper.sin(f7) * f2 * 0.1F);
                                d5 = (double)((float)MathHelper.floor_double(this.boundingBox.minY) + 1.0F);
                                d6 = this.posZ + (double)(MathHelper.cos(f7) * f2 * 0.1F);
                                worldserver.func_147487_a("splash", d11, d5, d6, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
                            }

                            if (this.field_146040_ay <= 0)
                            {
                                this.field_146054_aA = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                                this.field_146038_az = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                            }
                        }
                        else
                        {
                            this.field_146040_ay = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                            this.field_146040_ay -= EnchantmentHelper.func_151387_h(this.field_146042_b) * 20 * 5;
                        }
                    }

                    if (this.field_146045_ax > 0)
                    {
                        this.motionY -= (double)(this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat()) * 0.2D;
                    }
                }

                d2 = d10 * 2.0D - 1.0D;
                this.motionY += 0.03999999910593033D * d2;

                if (d10 > 0.0D)
                {
                    f6 = (float)((double)f6 * 0.9D);
                    this.motionY *= 0.8D;
                }

                this.motionX *= (double)f6;
                this.motionY *= (double)f6;
                this.motionZ *= (double)f6;
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }
	}

	GrappleState getGrappleReadyState(){
		int iposX = (int) posX;
		int iposY = (int) posY;
		int iposZ = (int) posZ;

		double _d = posY - Math.floor(posY);
		Block state = this.worldObj.getBlock(iposX, iposY, iposZ);
		Block upstate = this.worldObj.getBlock(iposX, iposY+1, iposZ);
		Block downstate = this.worldObj.getBlock(iposX, iposY-1, iposZ);
		Block down2state = this.worldObj.getBlock(iposX, iposY-2, iposZ);

		if (state.equals(Blocks.ladder)
				|| state.equals(Blocks.vine)
				|| state.equals(Blocks.iron_bars)) {
			return GrappleState.LADDER;
		}

		if (state.getMaterial().isLiquid() && state.equals(Blocks.lava)){
			if (isGrapplable(downstate) && _d < 0.8D){
				return GrappleState.GROUND;
			}
		} else {
			if (isGrapplable(downstate) || (_d < 0.5D && downstate.equals(Blocks.air) && isGrapplable(down2state))) {
				return GrappleState.GROUND;
			}
		}

		if (isGrapplable(upstate) || isGrapplable(state)){
			return GrappleState.ROOF;
		}

		return GrappleState.NOT_READY;
	}

	private boolean isGrapplable(Block state){
		if (state.getMaterial().isLiquid() || state.equals(Blocks.air) || state instanceof BlockWeb) return false;
		return true;
	}

	static enum GrappleState
	{
		NOT_READY,
		ROOF,
		GROUND,
		LADDER
	}
}
