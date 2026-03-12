package net.narutoxboruto.entities;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.narutoxboruto.entities.throwables.*;
import net.narutoxboruto.main.Main;

public class ForgeEntities {

    public static final DeferredRegister<EntityType<?>> MOD_ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Main.MOD_ID);

    //Throwables
    public static final RegistryObject<EntityType<Kunai>> KUNAI = registerEntity("kunai", Kunai::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final RegistryObject<EntityType<Shuriken>> SHURIKEN = registerEntity("shuriken", Shuriken::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final RegistryObject<EntityType<ExplosiveKunai>> EXPLOSIVE_KUNAI = registerEntity("explosive_kunai", ExplosiveKunai::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final RegistryObject<EntityType<PoisonSenbon>> POISON_SENBON = registerEntity("poison_senbon", PoisonSenbon::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final RegistryObject<EntityType<Senbon>> SENBON = registerEntity("senbon", Senbon::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final RegistryObject<EntityType<ThrownFumaShuriken>> FUMA_SHURIKEN = registerEntity("fuma_shuriken", ThrownFumaShuriken::new, MobCategory.MISC, 0.5F, 0.5F);

    //Jutsus
   // public static final RegistryObject<EntityType<FireBallEntity>> FIRE_BALL = registerEntity("fire_ball", FireBallEntity::new, MobCategory.MISC, 1.0F, 1.0F);
   // public static final RegistryObject<EntityType<SharkBombEntity>> SHARK_BOMB = registerEntity("shark_bomb", SharkBombEntity::new, MobCategory.MISC, 1.0F, 1.0F);
   // public static final RegistryObject<EntityType<WaterDragonEntity>> WATER_DRAGON = registerEntity("water_dragon", WaterDragonEntity::new, MobCategory.MISC, 1.5F, 1.5F);

    //Effects
   // public static final RegistryObject<EntityType<LightningArcEntity>> LIGHTNING_ARC =
   //         MOD_ENTITIES.register("lightning_arc", () -> EntityType.Builder.<LightningArcEntity>of(LightningArcEntity::new, MobCategory.MISC)
   //                 .sized(0.1F, 0.1F)
   //                 .clientTrackingRange(16)
   //                 .updateInterval(1)
   //                 .fireImmune()
   //                 .build("lightning_arc"));

    //Bosses
   // public static final RegistryObject<EntityType<KizameHoshigaki>> KISAME_HOSHIGAKI = registerEntity("kisame_hoshigaki", KizameHoshigaki::new, MobCategory.MONSTER, 0.6F, 1.8F);
   // public static final RegistryObject<EntityType<JinpachiMunashi>> JINPACHI_MUNASHI = registerEntity("jinpachi_munashi", JinpachiMunashi::new, MobCategory.MONSTER, 0.6F, 1.8F);
   // public static final RegistryObject<EntityType<ZabuzaMomochi>> ZABUZA_MOMOCHI = registerEntity("zabuza_momochi", ZabuzaMomochi::new, MobCategory.MONSTER, 0.6F, 1.8F);

    public static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(
            String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
        return MOD_ENTITIES.register(name, () -> EntityType.Builder.of(factory, category).sized(width, height).build(name)
        );
    }


    public static void register(IEventBus eventBus) {
        MOD_ENTITIES.register(eventBus);
    }
}
