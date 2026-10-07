package net.narutoxboruto.capabilities.stats;

import net.minecraft.server.level.ServerPlayer;

/**
 * Shared behaviour of every trainable stat: a clamped integer that syncs itself to the client.
 *
 * All mutators return the amount the stat actually changed (new value - old value, so negative for
 * a decrease). Callers that award something for progress, like Shinobi Points, should check that
 * result instead of assuming the change went through, since a stat at its cap gains nothing.
 */
public abstract class AbstractStat {

    public static final int DEFAULT_MAX_VALUE = 500;

    private final int maxValue;
    private int value;

    protected AbstractStat(int value, int maxValue) {
        this.maxValue = maxValue;
        this.value = clamp(value);
    }

    public int getValue() {
        return value;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public boolean isMaxed() {
        return value >= maxValue;
    }

    /** Raw setter used when loading or copying data. It clamps but neither syncs nor triggers side effects. */
    public void setValue(int value) {
        this.value = clamp(value);
    }

    public int setValue(int value, ServerPlayer player) {
        return change(value, player);
    }

    public int addValue(int amount, ServerPlayer player) {
        return change((long) this.value + amount, player);
    }

    public int subValue(int amount, ServerPlayer player) {
        return change((long) this.value - amount, player);
    }

    public int incrementValue(int amount, ServerPlayer player) {
        return addValue(amount, player);
    }

    public abstract void syncValue(ServerPlayer player);

    /** Called after the value changed, before it is synced. {@code delta} is never 0. */
    protected void onChanged(ServerPlayer player, int delta) {
    }

    private int change(long requested, ServerPlayer player) {
        int old = this.value;
        this.value = clamp(requested);
        int delta = this.value - old;
        if (delta != 0) {
            onChanged(player, delta);
        }
        syncValue(player);
        return delta;
    }

    private int clamp(long requested) {
        return (int) Math.max(0L, Math.min(requested, (long) maxValue));
    }
}
