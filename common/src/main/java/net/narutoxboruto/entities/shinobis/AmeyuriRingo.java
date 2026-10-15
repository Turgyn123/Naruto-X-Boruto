package net.narutoxboruto.entities.shinobis;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.narutoxboruto.items.ModItems;
import org.jetbrains.annotations.Nullable;

/** Ameyuri Ringo of the Seven Swordsmen: she fights with a Kiba blade in each hand. */
public class AmeyuriRingo extends AbstractShinobiMob {
    public AmeyuriRingo(EntityType<? extends AmeyuriRingo> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setItemInHand(InteractionHand.MAIN_HAND, ModItems.KIBA_ITEM.getDefaultInstance());
        this.setItemInHand(InteractionHand.OFF_HAND, ModItems.KIBA_ITEM.getDefaultInstance());
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason,
                                        @Nullable SpawnGroupData pSpawnData) {
        SpawnGroupData data = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
        // The second Kiba, in the off hand. When she throws a shuriken it takes the off hand for a moment and
        // goes back to the Kiba afterwards, see getOffhandWeapon.
        this.setItemInHand(InteractionHand.OFF_HAND, this.getOffhandWeapon());
        return data;
    }

    @Override
    protected ItemStack getWeapon() {
        return ModItems.KIBA_ITEM.getDefaultInstance();
    }

    @Override
    protected ItemStack getOffhandWeapon() {
        return ModItems.KIBA_ITEM.getDefaultInstance();
    }

    public static AttributeSupplier setAttribute() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .build();
    }
}
