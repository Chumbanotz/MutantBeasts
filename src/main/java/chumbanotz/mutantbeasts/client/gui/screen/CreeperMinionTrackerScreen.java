package chumbanotz.mutantbeasts.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import chumbanotz.mutantbeasts.MutantBeasts;
import chumbanotz.mutantbeasts.entity.CreeperMinionEntity;
import chumbanotz.mutantbeasts.item.MBItems;
import chumbanotz.mutantbeasts.packet.CreeperMinionTrackerPacket;
import chumbanotz.mutantbeasts.packet.PacketHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperMinionTrackerScreen extends Screen {
	private static final ResourceLocation TEXTURE = MutantBeasts.prefix("textures/gui/creeper_minion_tracker.png");
	private final int xSize = 176;
	private final int ySize = 166;
	private int guiX;
	private int guiY;
	private final CreeperMinionEntity creeperMinion;
	private boolean canRideOnShoulder;
	private boolean canDestroyBlocks;
	private boolean alwaysShowName;

	public CreeperMinionTrackerScreen(CreeperMinionEntity creeperMinion) {
		super(creeperMinion.getDisplayName());
		this.creeperMinion = creeperMinion;
	}

	@Override
	protected void init() {
		this.canDestroyBlocks = this.creeperMinion.canDestroyBlocks();
		this.alwaysShowName = this.creeperMinion.isCustomNameVisible();
		this.canRideOnShoulder = this.creeperMinion.canRideOnShoulder();
		this.guiX = (this.width - this.xSize) / 2;
		this.guiY = (this.height - this.ySize) / 2; //22 pixels up
		int buttonWidth = this.xSize / 2 - 10;
		this.addButton(new Button(this.guiX + 8, this.guiY + this.ySize - 78, buttonWidth * 2 + 4, 20, this.canDestroyBlocks(), button -> {
			this.canDestroyBlocks = !this.canDestroyBlocks;
			PacketHandler.sendToServer(new CreeperMinionTrackerPacket(this.creeperMinion, 0, this.canDestroyBlocks));
			button.setMessage(this.canDestroyBlocks());
		}));
		this.addButton(new Button(this.guiX + 8, this.guiY + this.ySize - 54, buttonWidth * 2 + 4, 20, this.alwaysShowName(), button -> {
			this.alwaysShowName = !this.alwaysShowName;
			PacketHandler.sendToServer(new CreeperMinionTrackerPacket(this.creeperMinion, 1, this.alwaysShowName));
			button.setMessage(this.alwaysShowName());
		}));
		this.addButton(new Button(this.guiX + 8, this.guiY + this.ySize - 30, buttonWidth * 2 + 4, 20, this.canRideOnShoulder(), button -> {
			this.canRideOnShoulder = !this.canRideOnShoulder;
			PacketHandler.sendToServer(new CreeperMinionTrackerPacket(this.creeperMinion, 2, this.canRideOnShoulder));
			button.setMessage(this.canRideOnShoulder());
		}));

		if (!this.creeperMinion.isOwner(this.minecraft.player)) {
			for (Widget widget : this.buttons) {
				widget.active = false;
			}
		}
	}

	@Override
	public void tick() {
		if (!this.creeperMinion.isAlive()) {
			this.minecraft.player.closeScreen();
		}
	}

	private String alwaysShowName() {
		return format("show_name") + ": " + I18n.format(this.alwaysShowName ? "options.on" : "options.off");
	}

	private String canDestroyBlocks() {
		return format("destroys_blocks") + ": " + I18n.format(this.canDestroyBlocks ? "options.on" : "options.off");
	}

	private String canRideOnShoulder() {
		return format("ride_on_shoulder") + ": " + I18n.format(this.canRideOnShoulder ? "options.on" : "options.off");
	}

	@Override
	public void render(int screenX, int screenY, float elapsedPartialTicks) {
		this.renderBackground();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.textureManager.bindTexture(TEXTURE);
		this.blit(this.guiX, this.guiY, 0, 0, this.xSize, this.ySize);
		int health = (int)(this.creeperMinion.getHealth() * 150.0F / this.creeperMinion.getMaxHealth());
		this.blit(this.guiX + 13, this.guiY + 16, 0, 166, health, 6);
		this.font.drawString(this.title.getFormattedText(), this.guiX + 13, this.guiY + 5, 4210752);
		this.font.drawString(format("health"), this.guiX + 13, this.guiY + 28, 4210752);
		this.font.drawString(format("explosion"), this.guiX + 13, this.guiY + 48, 4210752);
		this.font.drawString(format("blast_radius"), this.guiX + 13, this.guiY + 68, 4210752);
		StringBuilder sb = new StringBuilder();
		sb.append(this.creeperMinion.getHealth() / 2.0F).append(" / ").append(this.creeperMinion.getMaxHealth() / 2.0F);
		this.drawCenteredString(this.font, sb.toString(), this.guiX + this.xSize / 2 + 38, this.guiY + 30, 16777215);
		this.drawCenteredString(this.font, this.creeperMinion.canExplodeContinuously() ? format("explosion.continuous") : format("explosion.one_time"), this.guiX + this.xSize / 2 + 38, this.guiY + 50, 16777215);
		int temp = (int)((this.creeperMinion.getExplosionRadius()) * 10.0F);
		sb = (new StringBuilder()).append((float)temp / 10.0F);
		this.drawCenteredString(this.font, sb.toString(), this.guiX + this.xSize / 2 + 38, this.guiY + 70, 16777215);
		super.render(screenX, screenY, elapsedPartialTicks);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static String format(String key, Object... parameters) {
		return I18n.format("gui." + MBItems.CREEPER_MINION_TRACKER.getTranslationKey().replace("item.", "") + "." + key, parameters);
	}
}