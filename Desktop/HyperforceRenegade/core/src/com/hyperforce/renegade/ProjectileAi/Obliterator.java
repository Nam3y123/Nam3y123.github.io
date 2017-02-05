package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

public class Obliterator extends Projectile {
    private Ship player;

    public Obliterator(Ship player) {
        super(player.getX(), player.getY(), 144, 768, 0, 0, 0, 500, 0);
        spr.setRegion(0, 500, 24, 12);
        spr.setSize(72, 36);
        this.player = player;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if(age >= 90) {
            spr.setRegion(48, 488 - (((age / 5) % 2) * 24), 48, 12);
            spr.setSize(144, 732);
            spr.translate(0, 36);
            spr.draw(batch);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        setPosition(player.getX() - (age < 90 ? 12 : 48), player.getY() + 49);
        if(age < 90) {
            spr.setRegion(((age / 10) % 2) * 24, 500, 24, 12);
            spr.setSize(72, 36);
        } else {
            spr.setRegion(48, 500 - (((age / 5) % 2) * 24), 48, 12);
            spr.setSize(144, 36);
            dmg = 4;
        }

        if(age == 270)
            remove();
    }

    @Override
    public void onHit(Entity offender) {

    }
}
