package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

public class GigaBomb extends Projectile {
    public GigaBomb(float x, float y, float dir) {
        super(x, y, 36, 36, dir, 12, 0, 60, 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (age < 24)
            super.draw(batch, parentAlpha);
        else
            Projectile.drawExplosion(getX() + 24, getY() + 24, (29 - age) * 28, batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(age == 24) {
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
        }  else if (age >= 29)
            remove();
    }

    @Override
    public void onHit(Entity offender) {

    }
}
