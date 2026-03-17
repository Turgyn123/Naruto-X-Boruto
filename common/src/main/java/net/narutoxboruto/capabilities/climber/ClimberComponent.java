package net.narutoxboruto.capabilities.climber;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.util.Orientation;

/**
 * Component tracking a player's wall-climbing state.
 */
public class ClimberComponent {
    private boolean climbing;
    private Direction currentWallDirection;
    private Orientation movementOrientation;

    public ClimberComponent() {
        this.climbing = false;
        this.currentWallDirection = null;
        this.movementOrientation = Orientation.fromAttachmentNormal(new Vec3(0, 1, 0));
    }

    public ClimberComponent(boolean climbing) {
        this();
        this.climbing = climbing;
    }

    public boolean isClimbing() { return climbing; }
    public void setClimbing(boolean climbing) { this.climbing = climbing; }

    public Direction getCurrentWallDirection() { return currentWallDirection; }
    public void setCurrentWallDirection(Direction dir) { this.currentWallDirection = dir; }

    public Orientation getMovementOrientation() { return movementOrientation; }
    public void setMovementOrientation(Orientation orientation) { this.movementOrientation = orientation; }

    /** Advance interpolation (stub - wall running disabled). */
    public void tick(float dt) { }

    /** Transition to ground orientation. */
    public void transitionToGround() {
        this.climbing = false;
        this.currentWallDirection = null;
        this.movementOrientation = Orientation.fromAttachmentNormal(new Vec3(0, 1, 0));
    }

    /** Transition to ceiling orientation. */
    public void transitionToCeiling(Vec3 velocity) {
        this.climbing = true;
        this.currentWallDirection = null;
        this.movementOrientation = Orientation.fromAttachmentNormal(new Vec3(0, -1, 0));
    }

    /** Transition to wall orientation. */
    public void transitionToWall(Direction dir, Vec3 velocity) {
        this.climbing = true;
        this.currentWallDirection = dir;
        Vec3 normal = Vec3.atLowerCornerOf(dir.getNormal());
        this.movementOrientation = Orientation.fromAttachmentNormal(normal);
    }
}
