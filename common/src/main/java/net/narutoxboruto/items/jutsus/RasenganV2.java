package net.narutoxboruto.items.jutsus;

/**
 * Rasengan V2: a test version of the Rasengan with a look of its own, seven bands of wind whirling round the ball like the layers of a spinning top. It works
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
