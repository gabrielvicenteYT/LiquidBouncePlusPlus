/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.TextValue
import kotlin.math.max

public class NCPStable extends SpeedMode {

    public NCPStable() {
        super("NCPStable");
    }
    
    private val timerValue = FloatValue("${valuePrefix}Timer", 1.088f, 1f, 2f)
    private val jumpMovementFactorValue = FloatValue("${valuePrefix}Speed", 0.029f, 0f, 0.1f)
    private val notice = TextValue("Please set value with command", "")
    private val noticeLine2 = TextValue("for precise values", "")

    @Override
    public void onEnable() {
        mc.timer.timerSpeed = timerValue.get()
        super.onEnable()
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer.jumpMovementFactor = 0.2f
        mc.thePlayer.setVelocity(
            // reduce the motion a bit to avoid flags but don't stop completely
            mc.thePlayer.motionX / 3,
            mc.thePlayer.motionY,
            mc.thePlayer.motionZ / 3
        )
        super.onDisable()
    }

    @Override
    public void onUpdate() {
        if (MovementUtils.isMoving()) {
            mc.thePlayer.jumpMovementFactor = jumpMovementFactorValue.get()
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
            // speed adapts based on speed potion
            MovementUtils.strafe(max(MovementUtils.getSpeed(), MovementUtils.getSpeedWithPotionEffects(0.27).toFloat()))
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}
