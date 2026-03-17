package net.narutoxboruto.entities.ai;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;

public class ShinobiSwimUpGoal extends FloatGoal {
    private final AbstractShinobiMob mob1;
    private boolean stuck;

    public ShinobiSwimUpGoal(AbstractShinobiMob mob) {
        super(mob);
        mob1 = mob;
    }

    public void start() {
        this.mob1.setSearchingForLand(true);
        this.stuck = false;
    }

    public void stop() {
        this.mob1.setSearchingForLand(false);
    }

    @Override
    public boolean canUse() {
        return !this.mob1.wantsToSwim() && super.canUse() && !this.stuck;
    }
}
