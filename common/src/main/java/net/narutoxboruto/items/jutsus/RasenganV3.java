package net.narutoxboruto.items.jutsus;

/**
 * Rasengan V3: a test version of the Rasengan with a look of its own, the seven bands of V2 with a second, bigger layer of thinner bands whirling the other way outside them. It works
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
