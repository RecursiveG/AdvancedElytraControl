package me.recursiveg.advelytra;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Collections;
import java.util.List;

public class Command extends CommandBase{
    @Override
    public String getName() {
        return "autopilot";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "autopilot [cmd] [args...]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender p, String[] args) throws CommandException {
        if (args.length <= 0) {
            msg(p, "=== Usage ===");
            msg(p, "/ap target <x> <z>");
            msg(p, "/ap list");
            msg(p, "/ap clear");
            msg(p, "/ap adjust");
            msg(p, "/ap lock");
            msg(p, "/ap unlock");
            return;
        }
        switch(args[0]) {
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
                AdvElytraCtl.instance.beacons.add(new AdvElytraCtl.LocationPair(x,z));
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
                    switch(player.getHorizontalFacing()) {
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
            case "lock":
                if (p == FMLClientHandler.instance().getClientPlayerEntity()) {
                    AdvElytraCtl.instance.lockedYaw = ((EntityPlayerSP) p).rotationYaw;
                    msg(p, "Locked");
                }
                break;
            case "unlock":
                AdvElytraCtl.instance.lockedYaw = null;
                msg(p, "Unlocked");
                break;
            default:
                msg(p, "Unknown command");
        }
    }

    private void msg(ICommandSender s, String temp, Object... objs) {
        s.sendMessage(new TextComponentString(String.format(temp, objs)));
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ap");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
