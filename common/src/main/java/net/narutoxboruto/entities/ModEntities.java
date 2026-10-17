package net.narutoxboruto.entities;

import net.minecraft.world.entity.EntityType;
import net.narutoxboruto.entities.effects.LightningArcEntity;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.entities.jutsus.FireBallEntity;
import net.narutoxboruto.entities.jutsus.SharkBombEntity;
import net.narutoxboruto.entities.jutsus.WaterDragonEntity;
import net.narutoxboruto.entities.shinobis.JinpachiMunashi;
import net.narutoxboruto.entities.shinobis.JininAkebino;
import net.narutoxboruto.entities.shinobis.KushimaruKuriarare;
import net.narutoxboruto.entities.shinobis.AmeyuriRingo;
import net.narutoxboruto.entities.shinobis.Chojuro;
import net.narutoxboruto.entities.shinobis.KizameHoshigaki;
import net.narutoxboruto.entities.shinobis.ZabuzaMomochi;
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
    public static EntityType<WeaponEffectEntity> WEAPON_EFFECT;
    public static EntityType<ChakraSphereEntity> CHAKRA_SPHERE;

    //Bosses
    public static EntityType<ZabuzaMomochi> ZABUZA_MOMOCHI;
    public static EntityType<JinpachiMunashi> JINPACHI_MUNASHI;
    public static EntityType<JininAkebino> JININ_AKEBINO;
    public static EntityType<KushimaruKuriarare> KUSHIMARU_KURIARARE;
    public static EntityType<AmeyuriRingo> AMEYURI_RINGO;
    public static EntityType<Chojuro> CHOJURO;
    public static EntityType<KizameHoshigaki> KISAME_HOSHIGAKI;
}