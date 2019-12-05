package chumbanotz.mutantbeasts.util;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IndirectDamageSource extends EntityDamageSource {
	private final Entity indirectEntity;
	private final Vec3d damageLocation;

	public IndirectDamageSource(String damageTypeIn, @Nullable Entity source, @Nullable Entity indirectEntityIn) {
		this(damageTypeIn, source, indirectEntityIn, new Vec3d(source.posX, source.posY, source.posZ));
	}

	public IndirectDamageSource(String damageTypeIn, @Nullable Entity source, @Nullable Entity indirectEntityIn, @Nullable Vec3d damageLocation) {
		super(damageTypeIn, source);
		this.indirectEntity = indirectEntityIn;
		this.damageLocation = damageLocation;
	}

	@Override
	@Nullable
	public Entity getImmediateSource() {
		return this.damageSourceEntity;
	}

	@Override
	@Nullable
	public Entity getTrueSource() {
		return this.indirectEntity;
	}

	@Override
	public ITextComponent getDeathMessage(LivingEntity entityLivingBaseIn) {
		ITextComponent itextcomponent = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName() : this.indirectEntity.getDisplayName();
		ItemStack itemstack = this.indirectEntity instanceof LivingEntity ? ((LivingEntity)this.indirectEntity).getHeldItemMainhand() : ItemStack.EMPTY;
		String s = "death.attack." + this.damageType;
		String s1 = s + ".item";
		return !itemstack.isEmpty() && itemstack.hasDisplayName() ? new TranslationTextComponent(s1, entityLivingBaseIn.getDisplayName(), itextcomponent, itemstack.getTextComponent()) : new TranslationTextComponent(s, entityLivingBaseIn.getDisplayName(), itextcomponent);
	}

	@Override
	@Nullable
	public Vec3d getDamageLocation() {
		return this.damageLocation;
	}
}