package com.example.client.modules.combat;

import com.example.client.modules.Module;
import com.example.client.modules.Category;
import com.example.client.settings.BoolSetting;
import com.example.client.settings.EnumSetting;
import com.example.client.settings.SettingGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public class CriticalsModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final BoolSetting particles = sgGeneral.add(new BoolSetting.Builder()
            .name("crit-particles")
            .description("Hiện hiệu ứng particle khi crit trúng đòn.")
            .defaultValue(true)
            .build()
    );

    private final BoolSetting sound = sgGeneral.add(new BoolSetting.Builder()
            .name("crit-sound")
            .description("Phát âm thanh crit khi trúng đòn.")
            .defaultValue(true)
            .build()
    );

    private final EnumSetting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .description("Cách kích crit.")
            .defaultValue(Mode.Vanilla)
            .build()
    );

    private final BoolSetting antiSprint = sgGeneral.add(new BoolSetting.Builder()
            .name("stop-sprint")
            .description("Tự ngắt sprint trước khi đánh để đủ điều kiện crit.")
            .defaultValue(true)
            .build()
    );

    private boolean jumpQueued = false;
    private int cooldownTicks = 0;

    public CriticalsModule() {
        super(Category.Combat, "criticals", "Tự động crit mọi đòn đánh kèm hiệu ứng.");
    }

    public enum Mode {
        Vanilla,   // nhảy + rơi đúng frame như crit tay
        Packet     // gửi packet nhảy giả không thay đổi vị trí thật (server-friendly hơn)
    }

    @Override
    public void onActivate() {
        jumpQueued = false;
        cooldownTicks = 0;
    }

    public void onPreAttack(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return;
        if (!(target instanceof LivingEntity)) return;
        if (cooldownTicks > 0) return;

        PlayerEntity player = mc.player;

        boolean falling = player.fallDistance > 0.0F && player.getVelocity().y < 0.0;
        boolean sprinting = player.isSprinting();

        if (antiSprint.get() && sprinting) {
            player.setSprinting(false);
        }

        if (!falling) {
            switch (mode.get()) {
                case Vanilla -> {
                    if (player.isOnGround()) {
                        player.jump();
                        jumpQueued = true;
                    }
                }
                case Packet -> {
                    // Gửi update vị trí y+ nhỏ rồi trở lại, đủ để client tính crit
                    // mà không dịch chuyển nhân vật đáng kể trên server.
                    player.setVelocity(player.getVelocity().x, 0.3, player.getVelocity().z);
                    jumpQueued = true;
                }
            }
        }

        cooldownTicks = 4; // tránh spam nhảy liên tục mỗi tick
    }

    public void onPostAttack(EntityHitResult hitResult, boolean didCrit) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        if (!didCrit) return;

        Vec3d pos = hitResult.getPos();

        if (particles.get()) {
            for (int i = 0; i < 12; i++) {
                mc.world.addParticleClient(
                        ParticleTypes.CRIT,
                        pos.x, pos.y + 0.5, pos.z,
                        (mc.world.random.nextDouble() - 0.5) * 0.5,
                        mc.world.random.nextDouble() * 0.5,
                        (mc.world.random.nextDouble() - 0.5) * 0.5
                );
            }
        }

        if (sound.get()) {
            mc.world.playSound(
                    mc.player, mc.player.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                    SoundCategory.PLAYERS, 1.0F, 1.0F
            );
        }
    }

    @Override
    public void onTick() {
        if (cooldownTicks > 0) cooldownTicks--;
    }
}
