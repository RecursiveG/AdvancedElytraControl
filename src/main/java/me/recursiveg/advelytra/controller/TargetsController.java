package me.recursiveg.advelytra.controller;

import net.minecraft.client.entity.EntityPlayerSP;

import java.util.List;

public class TargetsController implements IController {
    @Override
    public void modifyVelocity(EntityPlayerSP player, double preferredSpeed) {

    }

    @Override
    public List<String> guiMessages(EntityPlayerSP player) {
        return null;
    }
    /*
    @Override
    public void modifyVelocity(EntityPlayerSP player) {
        LocationPair p = beacons.peek();
        Vec3d direction = new Vec3d(p.x - localPlayer.posX, 0, p.z - localPlayer.posZ);
        if (direction.xCoord * direction.xCoord + direction.zCoord * direction.zCoord <= BEACON_RADIUS_SQUARED) {
            beacons.poll();
            msg(String.format("Target reached: [x=%d z=%d]", p.x, p.z));
            adjustFaceDirectionToTopTarget();
        }
        direction = direction.normalize();
        localPlayer.motionX = direction.xCoord * speed;
        localPlayer.motionZ = direction.zCoord * speed;
    }


    public void adjustFaceDirectionToTopTarget() {
        if (beacons.isEmpty() || localPlayer == null) return;
        LocationPair p = beacons.peek();
        Vec3d direction = new Vec3d(p.x - localPlayer.posX, 0, p.z - localPlayer.posZ).normalize();
        double degree = Math.asin(direction.xCoord) / (Math.PI / 2) * 90;
        double yaw;
        if (degree == 0) {
            yaw = direction.zCoord >= 0 ? 0 : 180;
        } else if (degree > 0) {
            yaw = direction.zCoord >= 0 ? -degree : degree - 180;
        } else {
            yaw = direction.zCoord >= 0 ? -degree : 180 + degree;
        }
        localPlayer.rotationYaw = (float) yaw;
    }

    public drawGUI() {
        if (!beacons.isEmpty() && localPlayer != null) {
            LocationPair p = beacons.peek();
            double dx = p.x - localPlayer.posX;
            double dz = p.z - localPlayer.posZ;
            int fh = guiGame.getFontRenderer().FONT_HEIGHT;
            guiGame.drawCenteredString(guiGame.getFontRenderer(), String.format("Target[%d:%d]", p.x, p.z), centerX, centerY + 9 + fh, 0xFFFF0000);
            guiGame.drawCenteredString(guiGame.getFontRenderer(), String.format("Dist: %.1f", Math.sqrt(dx * dx + dz * dz)), centerX, centerY + 9 + 2 * fh, 0xFFFF0000);
        }
    }

                case "target":
                    if (args.length < 3) {
                        msg(p, "/ap target <x> <z>");
                        return;
                    }
                    int x, z;
                    try {
                        x = Integer.parseInt(args[1]);
                        z = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ex) {
                        msg(p, "Bad number");
                        return;
                    }
                    AdvElytraCtl.instance.beacons.add(new AdvElytraCtl.LocationPair(x, z));
                    msg(p, "Added");
                    return;
                case "list":
                    if (AdvElytraCtl.instance.beacons.isEmpty()) {
                        msg(p, "No targets");
                        return;
                    }
                    int i = 0;
                    for (AdvElytraCtl.LocationPair loc : AdvElytraCtl.instance.beacons) {
                        msg(p, "Target %d: [x=%d z=%d]", ++i, loc.x, loc.z);
                    }
                    return;
                case "clear":
                    AdvElytraCtl.instance.beacons.clear();
                    msg(p, "Done");
                    return;
                case "adjust":
                    if (!AdvElytraCtl.instance.beacons.isEmpty()) {
                        AdvElytraCtl.instance.adjustFaceDirectionToTopTarget();
                        msg(p, "Adjusted");
                        break;
                    }
                    if (p == FMLClientHandler.instance().getClientPlayerEntity()) {
                        EntityPlayerSP player = (EntityPlayerSP) p;
                        switch (player.getHorizontalFacing()) {
                            case SOUTH:
                                player.rotationYaw = 0;
                                break;
                            case EAST:
                                player.rotationYaw = -90;
                                break;
                            case NORTH:
                                player.rotationYaw = 180;
                                break;
                            case WEST:
                                player.rotationYaw = 90;
                                break;
                        }
                    }
                    msg(p, "Adjusted");
                    break;

     */
}
