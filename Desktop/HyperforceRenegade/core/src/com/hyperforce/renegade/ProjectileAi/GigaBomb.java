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
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(age == 24) {
            group.addActor(new Explosion(getX() + 24, getY() + 24));
        }  else if (age >= 29)
            remove();
    }

    @Override
    public void onHit(Entity offender) {

    }
}
