package net.narutoxboruto.entities;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.narutoxboruto.entities.effects.LightningArcEntity;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.entities.jutsus.*;
import net.narutoxboruto.entities.shinobis.*;
import net.narutoxboruto.entities.throwables.*;
import net.narutoxboruto.main.Main;

public class FabricEntities {

    //Throwables
    public static final EntityType<Kunai> KUNAI = registerEntity("kunai", Kunai::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final EntityType<Shuriken> SHURIKEN = registerEntity("shuriken", Shuriken::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final EntityType<ExplosiveKunai> EXPLOSIVE_KUNAI = registerEntity("explosive_kunai", ExplosiveKunai::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final EntityType<PoisonSenbon> POISON_SENBON = registerEntity("poison_senbon", PoisonSenbon::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final EntityType<Senbon> SENBON = registerEntity("senbon", Senbon::new, MobCategory.MISC, 0.5F, 0.5F);
    public static final EntityType<ThrownFumaShuriken> FUMA_SHURIKEN = registerEntity("fuma_shuriken", ThrownFumaShuriken::new, MobCategory.MISC, 0.5F, 0.5F);

    //Jutsus
    public static final EntityType<FireBallEntity> FIRE_BALL = registerEntity("fire_ball", FireBallEntity::new, MobCategory.MISC, 1.5F, 1.5F);
    public static final EntityType<SharkBombEntity> SHARK_BOMB = registerEntity("shark_bomb", SharkBombEntity::new, MobCategory.MISC, 1.0F, 1.0F);
    public static final EntityType<WaterDragonEntity> WATER_DRAGON = registerEntity("water_dragon", WaterDragonEntity::new, MobCategory.MISC, 1.5F, 1.5F);
    public static final EntityType<WaterCloneEntity> WATER_CLONE = registerEntity("water_clone", WaterCloneEntity::new, MobCategory.MISC, 0.6F, 1.8F);
    public static final EntityType<GolemEntity> GOLEM = registerEntity("golem", GolemEntity::new, MobCategory.MISC, 1.8F, 3.5F);

    //Effects
    public static final EntityType<LightningArcEntity> LIGHTNING_ARC = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "lightning_arc"),
            EntityType.Builder.<LightningArcEntity>of(LightningArcEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .fireImmune()
                    .build("lightning_arc")
    );

    public static final EntityType<WeaponEffectEntity> WEAPON_EFFECT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "weapon_effect"),
            EntityType.Builder.<WeaponEffectEntity>of(WeaponEffectEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .fireImmune()
                    .build("weapon_effect")
    );

    public static final EntityType<ChakraSphereEntity> CHAKRA_SPHERE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "chakra_sphere"),
            EntityType.Builder.<ChakraSphereEntity>of(ChakraSphereEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .fireImmune()
                    .build("chakra_sphere")
    );

    //Bosses
    public static final EntityType<KizameHoshigaki> KISAME_HOSHIGAKI = registerEntity("kisame_hoshigaki", KizameHoshigaki::new, MobCategory.MONSTER, 0.6F, 1.8F);
    public static final EntityType<JinpachiMunashi> JINPACHI_MUNASHI = registerEntity("jinpachi_munashi", JinpachiMunashi::new, MobCategory.MONSTER, 0.6F, 1.8F);
    public static final EntityType<JininAkebino> JININ_AKEBINO = registerEntity("jinin_akebino", JininAkebino::new, MobCategory.MONSTER, 0.6F, 1.8F);
    public static final EntityType<KushimaruKuriarare> KUSHIMARU_KURIARARE = registerEntity("kushimaru_kuriarare", KushimaruKuriarare::new, MobCategory.MONSTER, 0.6F, 1.8F);
    public static final EntityType<AmeyuriRingo> AMEYURI_RINGO = registerEntity("ameyuri_ringo", AmeyuriRingo::new, MobCategory.MONSTER, 0.6F, 1.8F);
    public static final EntityType<Chojuro> CHOJURO = registerEntity("chojuro", Chojuro::new, MobCategory.MONSTER, 0.6F, 1.8F);
    public static final EntityType<ZabuzaMomochi> ZABUZA_MOMOCHI = registerEntity("zabuza_momochi", ZabuzaMomochi::new, MobCategory.MONSTER, 0.6F, 1.8F);

    private static <T extends Entity> EntityType<T> registerEntity(
            String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, name),
                EntityType.Builder.of(factory, category).sized(width, height).build(name)
        );
    }

    public static void register() {
        // Class load triggers static field registration
    }
}
