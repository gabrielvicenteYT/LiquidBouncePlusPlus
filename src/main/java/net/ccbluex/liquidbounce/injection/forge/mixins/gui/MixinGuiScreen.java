/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.misc.HoverDetails;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.ccbluex.liquidbounce.ui.client.GuiBackground;
import net.ccbluex.liquidbounce.utils.render.ParticleUtils;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.BackgroundShader;
import net.ccbluex.liquidbounce.utils.render.shader.shaders.BackgroundDarkShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {
    @Shadow
    public Minecraft mc;

    @Shadow
    protected List<GuiButton> buttonList;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected FontRenderer fontRendererObj;

    @Shadow
    public void updateScreen() {
    }

    @Shadow
    public abstract void handleComponentHover(IChatComponent component, int x, int y);

    @Shadow
    protected abstract void drawHoveringText(List<String> textLines, int x, int y);

    @Redirect(method = "handleKeyboardInput", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z", remap = false))
    private boolean checkCharacter() {
        return Keyboard.getEventKey() == 0 && Keyboard.getEventCharacter() >= ' ' || Keyboard.getEventKeyState();
    }

    @Inject(method = "drawWorldBackground", at = @At("HEAD"), cancellable = true)
    private void drawWorldBackground(final CallbackInfo callbackInfo) {
        if (!shouldRenderBackground()) {
            callbackInfo.cancel();
            return;
        }

        final HUD hud = LiquidBounce.moduleManager.getModule(HUD.class);

        if(hud.getInventoryParticle().get() && mc.thePlayer != null) {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int width = scaledResolution.getScaledWidth();
            final int height = scaledResolution.getScaledHeight();
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
        }
    }

    /**
     * @author CCBlueX & TheMosKau
     */
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawClientBackground(final CallbackInfo callbackInfo) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();

        if(GuiBackground.Companion.getEnabled()) {
            if (LiquidBounce.INSTANCE.getBackground() == null) {
                if(!LiquidBounce.INSTANCE.getdarkMode()) {
                   BackgroundShader.BACKGROUND_SHADER.startShader();

                   final Tessellator instance = Tessellator.getInstance();
                   final WorldRenderer worldRenderer = instance.getWorldRenderer();
                   worldRenderer.begin(7, DefaultVertexFormats.POSITION);
                   worldRenderer.pos(0, height, 0.0D).endVertex();
                   worldRenderer.pos(width, height, 0.0D).endVertex();
                   worldRenderer.pos(width, 0, 0.0D).endVertex();
                   worldRenderer.pos(0, 0, 0.0D).endVertex();
                   instance.draw();

                   BackgroundShader.BACKGROUND_SHADER.stopShader();
                } else {
                   BackgroundDarkShader.BACKGROUNDDARK_SHADER.startShader();

                   final Tessellator instance = Tessellator.getInstance();
                   final WorldRenderer worldRenderer = instance.getWorldRenderer();
                   worldRenderer.begin(7, DefaultVertexFormats.POSITION);
                   worldRenderer.pos(0, height, 0.0D).endVertex();
                   worldRenderer.pos(width, height, 0.0D).endVertex();
                   worldRenderer.pos(width, 0, 0.0D).endVertex();
                   worldRenderer.pos(0, 0, 0.0D).endVertex();
                   instance.draw();

                   BackgroundDarkShader.BACKGROUNDDARK_SHADER.stopShader();
                  }
            }else{
                final ScaledResolution scaledResolution = new ScaledResolution(mc);
                final int width = scaledResolution.getScaledWidth();
                final int height = scaledResolution.getScaledHeight();

                mc.getTextureManager().bindTexture(LiquidBounce.INSTANCE.getBackground());
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Gui.drawScaledCustomSizeModalRect(0, 0, 0.0F, 0.0F, width, height, width, height, width, height);
            }

            if (GuiBackground.Companion.getParticles())
                ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "drawBackground", at = @At("RETURN"))
    private void drawParticles(final CallbackInfo callbackInfo) {
        if(GuiBackground.Companion.getParticles())
            ParticleUtils.drawParticles(Mouse.getX() * width / mc.displayWidth, height - Mouse.getY() * height / mc.displayHeight - 1);
    }

    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void messageSend(String msg, boolean addToChat, final CallbackInfo callbackInfo) {
        if (msg.startsWith(String.valueOf(LiquidBounce.commandManager.getPrefix())) && addToChat) {
            this.mc.ingameGUI.getChatGUI().addToSentMessages(msg);

            LiquidBounce.commandManager.executeCommands(msg);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "handleComponentHover", at = @At("HEAD"))
    private void handleHoverOverComponent(IChatComponent component, int x, int y, final CallbackInfo callbackInfo) {
        if (component == null || component.getChatStyle().getChatClickEvent() == null || !LiquidBounce.moduleManager.getModule(HoverDetails.class).getState())
            return;

        final ChatStyle chatStyle = component.getChatStyle();

        final ClickEvent clickEvent = chatStyle.getChatClickEvent();
        final HoverEvent hoverEvent = chatStyle.getChatHoverEvent();

        drawHoveringText(Collections.singletonList("§c§l" + clickEvent.getAction().getCanonicalName().toUpperCase() + ": §a" + clickEvent.getValue()), x, y - (hoverEvent != null ? 17 : 0));
    }

    /**
     * @author CCBlueX (superblaubeere27)
     * @reason Making it possible for other mixins to receive actions
     */
    @Inject(method = "actionPerformed", at = @At("RETURN"))
    protected void injectActionPerformed(GuiButton button, CallbackInfo callbackInfo) {
        this.injectedActionPerformed(button);
    }

    protected boolean shouldRenderBackground() {
        return true;
    }

    protected void injectedActionPerformed(GuiButton button) {

    }
}
