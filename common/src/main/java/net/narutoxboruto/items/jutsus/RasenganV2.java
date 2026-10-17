package net.narutoxboruto.items.jutsus;

/**
 * Rasengan V2: a test version of the Rasengan with a look of its own, a tangle of nine thin glowing arms spiralling both ways over a crisp ball. It works
 * exactly like the Rasengan.
 */
public class RasenganV2 extends Rasengan {

    public RasenganV2(Properties properties) {
        super(properties);
    }

    @Override
    public String getJutsuName() {
        return "Rasengan V2";
    }

    @Override
    protected int sphereVariant() {
        return 1;
    }
}
