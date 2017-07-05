package me.recursiveg.advelytra;

import me.recursiveg.advelytra.controller.IController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
@Mod(modid = "advelytractl")
public class AdvElytraCtl {
    private static final double MAX_SPEED = 3;
    private static final double DEFAULT_SPEED = 0.4;
    private static final double MIN_SPEED = 0;
    private static final double BOOST_SPEED = 7;
    private static final double LAUNCH_SPEED = BOOST_SPEED / 3;

    public static class LocationPair {
        public LocationPair(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int x;
        public int z;
    }

    @Mod.Instance
    public static AdvElytraCtl instance;

    public Float lockedYaw = null;
    private EntityPlayerSP localPlayer = null;
    private boolean enabled = false;
    private double speed = 0;
    public IController autoController = null;

    private KeyBinding toggle = new KeyBinding("Toggle", Keyboard.KEY_G, "Advanced Elytra Control");
    private KeyBinding reset = new KeyBinding("Reset", Keyboard.KEY_H, "Advanced Elytra Control");
    private KeyBinding accelerate = new KeyBinding("Accelerate", Keyboard.KEY_Y, "Advanced Elytra Control");
    private KeyBinding brake = new KeyBinding("Brake", Keyboard.KEY_B, "Advanced Elytra Control");
    private KeyBinding boost = new KeyBinding("Boost", Keyboard.KEY_N, "Advanced Elytra Control");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        ClientRegistry.registerKeyBinding(toggle);
        ClientRegistry.registerKeyBinding(reset);
        ClientRegistry.registerKeyBinding(accelerate);
        ClientRegistry.registerKeyBinding(brake);
        ClientRegistry.registerKeyBinding(boost);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ev) {
        ClientCommandHandler.instance.registerCommand(new Command());
    }

    @SubscribeEvent
    public void onToggle(InputEvent.KeyInputEvent e) {
        if (toggle.isPressed()) {
            boolean isFlying = FMLClientHandler.instance().getClient().player.isElytraFlying();
            autoController = null;
            if (enabled) {
                localPlayer = null;
                speed = DEFAULT_SPEED;
                enabled = false;
                msg("Disabled");
            } else if (!isFlying) {
                msg("You can only do this when flying.");
            } else {
                localPlayer = FMLClientHandler.instance().getClientPlayerEntity();
                speed = Math.sqrt(localPlayer.motionX * localPlayer.motionX + localPlayer.motionZ * localPlayer.motionZ);
                if (speed < MIN_SPEED) speed = MIN_SPEED;
                if (speed > MAX_SPEED) speed = MAX_SPEED;
                enabled = true;
                msg("Height Locked!");
            }
        } else if (accelerate.isPressed()) {
            speed += 0.1;
            if (speed > MAX_SPEED) speed = MAX_SPEED;
        } else if (brake.isPressed()) {
            speed -= 0.1;
            if (speed < MIN_SPEED) speed = MIN_SPEED;
        } else if (reset.isPressed()) {
            speed = DEFAULT_SPEED;
        } else if (boost.isPressed()) {
            EntityPlayerSP p = FMLClientHandler.instance().getClient().player;
            ItemStack chestItem = p.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (chestItem != null && chestItem.getItem() == Items.ELYTRA && ItemElytra.isUsable(chestItem)) {
                if (enabled) {
                    msg("Booster conflicts with AEC");
                } else {
                    if (!p.isElytraFlying()) {
                        groundProject();
                    } else {
                        Vec3d look = p.getLookVec();
                        look = look.normalize().scale(BOOST_SPEED);
                        p.motionX += look.x;
                        p.motionY += look.y;
                        p.motionZ += look.z;
                    }
                }
            } else {
                Vec3d look = p.getLookVec();
                look = look.normalize().scale(LAUNCH_SPEED);
                p.motionX += look.x;
                p.motionY += look.y;
                p.motionZ += look.z;
            }
        }
    }

    // launch player directly from ground
    public void groundProject() {
        EntityPlayerSP p = FMLClientHandler.instance().getClientPlayerEntity();
        p.connection.sendPacket(new CPacketPlayer.Position(p.posX, p.posY, p.posZ, false));
        p.connection.sendPacket(new CPacketEntityAction(p, CPacketEntityAction.Action.START_FALL_FLYING));
        Vec3d look = p.getLookVec();
        look = look.normalize().scale(LAUNCH_SPEED);
        p.motionX += look.x;
        p.motionY += look.y;
        p.motionZ += look.z;
    }

    // modify player's velocity
    public void onBeforePlayerMotion(EntityLivingBase elb) {
        if (!enabled || elb != localPlayer) return;
        if (!localPlayer.isElytraFlying()) {
            msg("You are not flying");
            enabled = false;
            return;
        }

        // base direction
        double speed = this.speed;
        if (Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown()) speed += 0.5;
        if (Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown()) speed -= 0.4;
        double sightYaw = (double) (localPlayer.rotationYaw + 90) / 180D * Math.PI;
        double directionYaw = lockedYaw == null ? sightYaw : (double) (lockedYaw + 90) / 180D * Math.PI;

        localPlayer.motionX = Math.cos(directionYaw) * speed;
        localPlayer.motionZ = Math.sin(directionYaw) * speed;
        localPlayer.motionY = 0;

        // up/down adjustment
        if (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown()) localPlayer.motionY += 0.5;
        if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown()) {
            localPlayer.motionY -= 0.5;
            localPlayer.movementInput.sneak = false;
        }

        // left/right adjustment
        if (!MathHelper.epsilonEquals(localPlayer.moveStrafing, 0F)) {
            Vec3d look = new Vec3d(Math.cos(sightYaw), 0, Math.sin(sightYaw)).crossProduct(new Vec3d(0, -localPlayer.moveStrafing, 0));
            localPlayer.motionX += look.x;
            localPlayer.motionZ += look.z;
        }

        // auto controller adjustment
        if (autoController != null) {
            autoController.modifyVelocity(localPlayer, speed);
        }

        // collide protection
        if (localPlayer.isCollidedHorizontally) {
            localPlayer.motionX *= 0.1;
            localPlayer.motionZ *= 0.1;
            localPlayer.isCollidedHorizontally = false;
        }
        if (localPlayer.isCollidedVertically) {
            localPlayer.motionY = 0;
            localPlayer.isCollidedVertically = false;
        }
    }

    public void onBeforeNotFlyMotion(EntityLivingBase elb) {
        if (!enabled || elb != localPlayer) return;
        msg("You are not flying");
        enabled = false;
    }

    @SubscribeEvent
    public void drawCrossOverlay(RenderGameOverlayEvent.Post event) {
        if (!enabled) return;
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.CROSSHAIRS)) {
            int centerX = event.getResolution().getScaledWidth() / 2;
            int centerY = event.getResolution().getScaledHeight() / 2;
            Gui.drawRect(centerX - 7, centerY, centerX + 7, centerY + 1, 0xFFFF0000);
            Gui.drawRect(centerX, centerY - 7, centerX + 1, centerY + 7, 0xFFFF0000);
            GuiIngame guiGame = FMLClientHandler.instance().getClient().ingameGUI;
            guiGame.drawCenteredString(guiGame.getFontRenderer(), String.format("Speed: %.2f", speed), centerX, centerY + 9, 0xFFFF0000);
            if (autoController != null) {
                int fh = guiGame.getFontRenderer().FONT_HEIGHT;
                int yoffset = 9 + fh;
                for (String msg : autoController.guiMessages(localPlayer)) {
                    guiGame.drawCenteredString(guiGame.getFontRenderer(), String.format("%s", msg), centerX, centerY + yoffset, 0xFFFF0000);
                    yoffset += fh;
                }
            }
        }
    }

    public static void msg(String msg) {
        FMLClientHandler.instance().getClient().player.sendMessage(new TextComponentString("[AEC] " + msg));
    }

    public static void beforeMotion(EntityLivingBase elb) {
        instance.onBeforePlayerMotion(elb);
    }

    public static void beforeNotFlyMotion(EntityLivingBase elb) {
        instance.onBeforeNotFlyMotion(elb);
    }
}
