package net.narutoxboruto.entities;

import net.minecraft.world.entity.EntityType;
import net.narutoxboruto.entities.effects.LightningArcEntity;
import net.narutoxboruto.entities.jutsus.FireBallEntity;
import net.narutoxboruto.entities.jutsus.SharkBombEntity;
import net.narutoxboruto.entities.jutsus.WaterDragonEntity;
import net.narutoxboruto.entities.throwables.*;

public class ModEntities {

    //Throwables
    public static EntityType<Kunai> KUNAI;
    public static EntityType<ExplosiveKunai> EXPLOSIVE_KUNAI;
    public static EntityType<Shuriken> SHURIKEN;
    public static EntityType<Senbon> SENBON;
    public static EntityType<PoisonSenbon> POISON_SENBON;
    public static EntityType<ThrownFumaShuriken> FUMA_SHURIKEN;

    //Jutsus
    public static EntityType<SharkBombEntity> SHARK_BOMB;
    public static EntityType<WaterDragonEntity> WATER_DRAGON;
    public static EntityType<FireBallEntity> FIRE_BALL;

    //Effects
    public static EntityType<LightningArcEntity> LIGHTNING_ARC;
}