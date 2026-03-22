package net.narutoxboruto.capabilities.stats;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.platform.Services;

public class Taijutsu {
    private int value;
    private static final int MAX_VALUE = 500;

    public static final Codec<Taijutsu> CODEC = Codec.INT.xmap(Taijutsu::new, Taijutsu::getValue);

    public Taijutsu() { this.value = 0; }
    public Taijutsu(int value) { this.value = value; }

    public int getValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public void setValue(int value, ServerPlayer player) {
        this.value = Math.min(value, MAX_VALUE);
        updateAttackDamage(player);
        this.syncValue(player);
    }
    public void addValue(int amount, ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        updateAttackDamage(player);
        this.syncValue(player);
    }
    public void subValue(int amount, ServerPlayer player) {
        this.value = Math.max(this.value - amount, 0);
        updateAttackDamage(player);
        this.syncValue(player);
    }
    public void incrementValue(int amount, ServerPlayer player) {
        this.value = Math.min(this.value + amount, MAX_VALUE);
        updateAttackDamage(player);
        this.syncValue(player);
    }
    public void syncValue(ServerPlayer player) {
        Services.PLATFORM.syncTaijutsu(player, this.value);
    }

    private void updateAttackDamage(ServerPlayer player) {
        AttributeInstance attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            attackDamageAttr.setBaseValue(1.0 + this.value * 0.04);
        }
    }
}
