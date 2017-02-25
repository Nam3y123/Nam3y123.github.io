package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

/**
 * Created by jordan on 2/24/17.
 */
public class Explosion extends Projectile implements Entity {
    public Explosion(float x, float  y) {
        super(x, y, 0, 0, 0, 0, 4);

        long id = Ship.sounds[4].play();
        Ship.sounds[4].setVolume(id, Ship.volume);
        dmg = 4;

        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        for(Actor a : actors) {
            if(a != null && a instanceof Entity && !a.equals(this)) {
                if(Math.sqrt(Math.pow(getX() - a.getX(), 2) + Math.pow(getY() - a.getY(), 2)) <= 144) {
                    ((Entity)a).onHit(this);
                }
            }
        }

        dmg = 0;
        speed = 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Projectile.drawExplosion(getX(), getY(), (5 - age) * 28, batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(age == 5)
            remove();
    }

    @Override
    public void onHit(Entity offender) {

    }
}
