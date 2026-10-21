package net.narutoxboruto.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.narutoxboruto.main.Main;

/**
 * The sounds of the mod. They are played by their name (see {@code sounds.json}), so no loader has to register them: the
 * game sends the whole sound event along with the sound.
 */
public class ModSounds {

    /** The Rasengan takes shape in the hand. */
    public static final SoundEvent RASENGAN_FORM = create("rasengan_form");
    /** The whirr of the Rasengan, 0.5 seconds that can be played over and over. */
    public static final SoundEvent RASENGAN_SPIN = create("rasengan_spin");
    /** The Rasengan explodes. */
    public static final SoundEvent RASENGAN_HIT = create("rasengan_hit");

    private static SoundEvent create(String name) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, name));
    }
}
