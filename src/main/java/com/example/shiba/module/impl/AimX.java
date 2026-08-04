public float[] getAimAngles(MinecraftClient mc) {
    if (target == null || mc.player == null) return null;
    String currentMode = mode.getValue();
    if (currentMode.equals("None")) return null;

    Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
    Vec3d playerPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
    Vec3d diff = targetPos.subtract(playerPos);

    float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90);
    float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, Math.sqrt(diff.x * diff.x + diff.z * diff.z))));
    pitch = MathHelper.clamp(pitch, -90, 90);

    if (currentMode.equals("Legit")) {
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float yawDiff = MathHelper.wrapDegrees(yaw - currentYaw);
        float pitchDiff = pitch - currentPitch;
        float maxSpeed = (float) legitSpeed.getValue();
        if (Math.abs(yawDiff) > maxSpeed) {
            yaw = currentYaw + Math.signum(yawDiff) * maxSpeed;
        }
        if (Math.abs(pitchDiff) > maxSpeed / 2) {
            pitch = currentPitch + Math.signum(pitchDiff) * maxSpeed / 2;
        }
    }

    return new float[]{yaw, pitch};
}
