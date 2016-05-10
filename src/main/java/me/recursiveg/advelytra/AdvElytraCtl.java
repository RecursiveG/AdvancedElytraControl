package me.recursiveg.advelytra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Method;

@SideOnly(Side.CLIENT)
@Mod(modid = "advelytractl")
public class AdvElytraCtl {
    private static final double BASE_SPEED = 0.35;
    @Mod.Instance
    public static AdvElytraCtl instance;

    EntityPlayerSP localPlayer = null;
    boolean enabled = false;
    double motionPitch = 0; // negative = up; positive = down
    double motionYaw = 0;
    double speed = 0;

    KeyBinding toggle    = new KeyBinding("Toggle"    , Keyboard.KEY_O, "Advanced Elytra Control");
    KeyBinding reset     = new KeyBinding("Reset"     , Keyboard.KEY_P, "Advanced Elytra Control");
    KeyBinding pitchUp   = new KeyBinding("Pitch Up"  , Keyboard.KEY_I, "Advanced Elytra Control");
    KeyBinding pitchDown = new KeyBinding("Pitch Down", Keyboard.KEY_K, "Advanced Elytra Control");
    KeyBinding yawLeft   = new KeyBinding("Yaw Left"  , Keyboard.KEY_J, "Advanced Elytra Control");
    KeyBinding yawRight  = new KeyBinding("Yaw Right" , Keyboard.KEY_L, "Advanced Elytra Control");
    KeyBinding accelerate= new KeyBinding("Accelerate", Keyboard.KEY_U, "Advanced Elytra Control");
    KeyBinding brake     = new KeyBinding("Brake"     , Keyboard.KEY_M, "Advanced Elytra Control");


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        try {
            Class.forName("net.minecraft.entity.EntityLivingBase");
        } catch (Exception ex) {ex.printStackTrace();}
        ClientRegistry.registerKeyBinding(toggle);
        ClientRegistry.registerKeyBinding(reset);
        ClientRegistry.registerKeyBinding(pitchUp);
        ClientRegistry.registerKeyBinding(pitchDown);
        ClientRegistry.registerKeyBinding(yawLeft);
        ClientRegistry.registerKeyBinding(yawRight);
        ClientRegistry.registerKeyBinding(accelerate);
        ClientRegistry.registerKeyBinding(brake);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ev) {

    }

    @SubscribeEvent
    public void onToggle(InputEvent.KeyInputEvent e) {
        if (toggle.isPressed()) {
            if (!FMLClientHandler.instance().getClient().thePlayer.isElytraFlying()) {
                msg("You must wear elytra to fly");
                enabled = false;
                return;
            }
            localPlayer = FMLClientHandler.instance().getClientPlayerEntity();
            speed = Math.sqrt(localPlayer.motionX * localPlayer.motionX + localPlayer.motionZ * localPlayer.motionZ) - BASE_SPEED;
            if (speed < 0) speed = 0;
            enabled = !enabled;
            if (enabled) msg("Direction Locked.");
            else msg("Disabled");
        } else if (pitchUp.isPressed()) {
            if (reset.isKeyDown()) {
                motionPitch = 0;
            } else {
                motionPitch -= 1;
                if (motionPitch < -90) motionPitch = -90;
            }
        } else if (pitchDown.isPressed()) {
            if (reset.isKeyDown()) {
                motionPitch = 0;
            } else {
                motionPitch += 1;
                if (motionPitch > 90) motionPitch = 90;
            }
        } else if (yawRight.isPressed()) {
            if (reset.isKeyDown()) {
                motionYaw = 0;
            } else {
                motionYaw += 1;
                if (motionYaw > 50) motionYaw = 50;
            }
        } else if (yawLeft.isPressed()) {
            if (reset.isKeyDown()) {
                motionYaw = 0;
            } else {
                motionYaw -= 1;
                if (motionYaw < -50) motionYaw = -50;
            }
        } else if (accelerate.isPressed()) {
            if (reset.isKeyDown()) {
                speed = 0;
            } else {
                speed += 0.1;
                if (speed > 2) speed = 2;
            }
        } else if (brake.isPressed()) {
            if (reset.isKeyDown()) {
                speed = 0;
            } else {
                speed -= 0.1;
                if (speed < 0) speed = 0;
            }
        }
    }

    public void onBeforePlayerMotion(EntityLivingBase elb) {
        if (!enabled || elb != localPlayer) return;
        if (!localPlayer.isElytraFlying()) {
            msg("You are not flying");
            enabled = false;
            return;
        }
        if (localPlayer.isCollidedHorizontally || localPlayer.isCollidedVertically) {
            msg("Boom!");
            enabled = false;
            return;
        }
        localPlayer.motionY = -motionPitch/100;
        double scale = 1D/(Math.sqrt(localPlayer.motionX*localPlayer.motionX+localPlayer.motionZ*localPlayer.motionZ))*(speed+BASE_SPEED);
        localPlayer.motionX *= scale;
        localPlayer.motionZ *= scale;
    }

    @SubscribeEvent
    public void drawCrossOverlay(RenderGameOverlayEvent.Post event) {
        if (!enabled) return;
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
            int centerX = event.getResolution().getScaledWidth() / 2;
            int centerY = event.getResolution().getScaledHeight() / 2;

            int pitchLineY = (int)(centerY + (motionPitch / 9));
            int yawLineX   = (int)(centerX + (motionYaw / 5));
            double speedPercent = (speed + BASE_SPEED) / 2.6D;
            int speedTopY = centerY + (int)(-15*speedPercent);
            boolean speedUp = Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown();
            Gui.drawRect(centerX - 7, pitchLineY, centerX + 7, pitchLineY + 1, 0xFFFF0000);
            Gui.drawRect(yawLineX, centerY -7, yawLineX + 1, centerY + 7, 0xFFFF0000);
            Gui.drawRect(centerX + 9, speedTopY, centerX + 10, centerY, speedUp?0xFFFF0000:0xFF00FF00);
        }
    }

    public static void msg(String msg) {
        FMLClientHandler.instance().getClient().thePlayer.addChatMessage(new TextComponentString("[AEC] " + msg));
    }

    public static void beforeMotion(EntityLivingBase elb) {
        instance.onBeforePlayerMotion(elb);
    }
}
