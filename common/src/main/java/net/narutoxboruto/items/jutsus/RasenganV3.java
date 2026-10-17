package net.narutoxboruto.items.jutsus;

/**
 * Rasengan V3: a test version of the Rasengan with a look of its own, the tangle of thin arms of V2 with two thin spinning rings round it. It works
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
