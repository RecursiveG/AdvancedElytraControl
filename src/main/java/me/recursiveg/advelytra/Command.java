package me.recursiveg.advelytra;

import me.recursiveg.advelytra.controller.CirclingController;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Collections;
import java.util.List;

public class Command extends CommandBase {
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
            msg(p, "/ap lock");
            msg(p, "/ap unlock");
            msg(p, "/ap cancel");
            msg(p, "/ap circling <x> <z> <r>");
            return;
        }
        try {
            switch (args[0]) {
                case "lock":
                    if (p == FMLClientHandler.instance().getClientPlayerEntity()) {
                        AdvElytraCtl.instance.lockedYaw = ((EntityPlayerSP) p).rotationYaw;
                        msg(p, "Yaw locked");
                    }
                    break;
                case "unlock":
                    AdvElytraCtl.instance.lockedYaw = null;
                    msg(p, "Yaw unlocked");
                    break;
                case "cancel":
                    AdvElytraCtl.instance.autoController = null;
                    msg(p, "Controller cancelled");
                    break;
                case "circling":
                    if (args.length < 4) {
                        msg(p, "/ap circling <x> <z> <r>");
                    } else {
                        AdvElytraCtl.instance.autoController = new CirclingController(
                                Double.parseDouble(args[1]),
                                Double.parseDouble(args[2]),
                                Double.parseDouble(args[3]));
                        msg(p, "Controller established!");
                    }
                    break;
                default:
                    msg(p, "Unknown command. Type `/ap` to view help.");
            }
        } catch (NumberFormatException ex) {
            msg(p, "Invalid number");
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
