package net.narutoxboruto.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.items.jutsus.AbstractJutsuItem;
import net.narutoxboruto.items.jutsus.LightningChakraMode;

import java.util.Locale;

/**
 * The search of the jutsu storage. Everything typed has to be found in the jutsu's name, its nature
 * release (fire, water, earth, lightning, wind), its dojutsu, or an alias of those, so typing "fire" shows
 * every fire jutsu.
 */
public final class JutsuSearch {

    private JutsuSearch() {}

    private static final String[][] NATURES = {
            {"fire", "katon", "flame"},
            {"water", "suiton"},
            {"earth", "doton"},
            {"lightning", "lighting", "raiton", "thunder"},
            {"wind", "fuuton", "futon"},
    };
    private static final String[] DOJUTSU = {"sharingan", "byakugan", "ketsuryugan", "rinnegan", "doujutsu", "dojutsu"};

    /** An empty search matches everything. */
    public static boolean matches(ItemStack stack, String query) {
        String words = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (words.isEmpty()) return true;
        if (stack.isEmpty()) return false;

        String text = searchText(stack);
        for (String word : words.split("\\s+")) {
            if (!text.contains(word)) return false;
        }
        return true;
    }

    private static String searchText(ItemStack stack) {
        StringBuilder text = new StringBuilder();
        text.append(stack.getHoverName().getString().toLowerCase(Locale.ROOT)).append(' ');
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        text.append(path.replace('_', ' ')).append(' ');

        String release = null;
        if (stack.getItem() instanceof AbstractJutsuItem jutsu) {
            release = jutsu.getRequiredRelease();
        } else if (stack.getItem() instanceof LightningChakraMode) {
            release = "lightning";
        }
        if (release != null) text.append(release.toLowerCase(Locale.ROOT)).append(' ');

        // A nature also brings its aliases, and a dojutsu brings the word dojutsu
        String all = text.toString();
        for (String[] nature : NATURES) {
            if (all.contains(nature[0])) {
                for (String alias : nature) text.append(alias).append(' ');
            }
        }
        for (String dojutsu : DOJUTSU) {
            if (all.contains(dojutsu)) text.append("dojutsu doujutsu eye ");
        }
        return text.toString();
    }
}
