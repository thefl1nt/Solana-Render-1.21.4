package ru.solana.utils.math;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.util.math.MathHelper.lerp;

@UtilityClass
public class MathUtil {
    MinecraftClient mc = MinecraftClient.getInstance();

    public float interpolate(double oldValue, double newValue, double interpolationValue){
        return (float) (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public Vector3d interpolate(Vector3d prevPos, Vector3d pos) {
        return new Vector3d(interpolate(prevPos.x, pos.x), interpolate(prevPos.y, pos.y), interpolate(prevPos.z, pos.z));
    }

    public Vec3d interpolate(Vec3d prevPos, Vec3d pos) {
        return new Vec3d(interpolate(prevPos.x, pos.x), interpolate(prevPos.y, pos.y), interpolate(prevPos.z, pos.z));
    }

    public Vec3d interpolate(Entity entity) {
        if (entity == null) return Vec3d.ZERO;
        return new Vec3d(interpolate(entity.prevX, entity.getX()), interpolate(entity.prevY, entity.getY()), interpolate(entity.prevZ, entity.getZ()));
    }

    public float interpolate(float prev, float orig) {
        return lerp(mc.getRenderTickCounter().getTickDelta(false), prev, orig);
    }

    public double interpolate(double prev, double orig) {
        return lerp(mc.getRenderTickCounter().getTickDelta(false), prev, orig);
    }

    public int interpolateSmooth(double smooth, int prev, int orig) {
        return (int) lerp(mc.getRenderTickCounter().getLastDuration() / smooth, prev, orig);
    }

    public float interpolateSmooth(double smooth, float prev, float orig) {
        return (float) lerp(mc.getRenderTickCounter().getLastDuration() / smooth, prev, orig);
    }

    public double interpolateSmooth(double smooth, double prev, double orig) {
        return lerp(mc.getRenderTickCounter().getLastDuration() / smooth, prev, orig);
    }
}