package net.narutoxboruto.items.jutsus;

/**
 * Rasengan V3: a test version of the Rasengan with a look of its own, a glowing orb held by three thin spinning rings with jagged bolts inside it, like a gyroscope. It works
 * exactly like the Rasengan.
 */
public class RasenganV3 extends Rasengan {

    public RasenganV3(Properties properties) {
        super(properties);
    }

    @Override
    public String getJutsuName() {
        return "Rasengan V3";
    }

    @Override
    protected int sphereVariant() {
        return 2;
    }
}
