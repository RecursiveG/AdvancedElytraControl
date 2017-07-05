package me.recursiveg.advelytra.controller;

import net.minecraft.client.entity.EntityPlayerSP;

import java.util.List;

public interface IController {
    void modifyVelocity(EntityPlayerSP player, double preferredSpeed);

    List<String> guiMessages(EntityPlayerSP player);
}
