package me.recursiveg.advelytra.controller;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class CirclingController implements IController {
    public static final double WIDTH_THRESHOLD = 1;
    public final Vec3d center;
    public final double radius;

    public CirclingController(double x, double z, double r) {
        center = new Vec3d(x, 0, z);
        radius = r;
    }

    @Override
    public void modifyVelocity(EntityPlayerSP player, double preferredSpeed) {
        Vec3d currentRadiusVector = new Vec3d(player.posX, 0, player.posZ).subtract(center);
        double currentRadius = currentRadiusVector.lengthVector();
        Vec3d velocity;
        if (currentRadius > radius + WIDTH_THRESHOLD) { // too far
            velocity = currentRadiusVector.normalize().scale(-1D).scale(preferredSpeed);
        } else if (currentRadius < radius - WIDTH_THRESHOLD) { // too close
            velocity = currentRadiusVector.normalize().scale(preferredSpeed);
        } else { // move along circle
            velocity = currentRadiusVector.normalize().crossProduct(new Vec3d(0, 1, 0)).normalize().scale(preferredSpeed);
        }
        player.motionX = velocity.x;
        player.motionZ = velocity.z;
    }

    @Override
    public List<String> guiMessages(EntityPlayerSP player) {
        Vec3d currentRadiusVector = new Vec3d(player.posX, 0, player.posZ).subtract(center);
        double currentRadius = currentRadiusVector.lengthVector();
        return Arrays.asList(
                String.format("[x=%.0f z=%.0f r=%.0f]", center.x, center.z, radius),
                String.format("[realR = %.1f]", currentRadius)
        );
    }
}
