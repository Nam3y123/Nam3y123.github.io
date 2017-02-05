package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;

public class BlitzBomb extends Projectile {
    public BlitzBomb(float x, float y) {
        super(x, y, 96, 96, 0, 0, 45, 0, 4);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(age < 3) {
            spr.setRegion(48, 0 , 16, 16);
            spr.setPosition(getX() + 24, getY() + 24);
            spr.setSize(48, 48);
        } else if(age < 6) {
            spr.setRegion(48, 16, 24, 24);
            spr.setPosition(getX() + 12, getY() + 12);
            spr.setSize(72, 72);
        } else {
            spr.setRegion(48, 40, 32, 32);
            spr.setPosition(getX(), getY());
            spr.setSize(96, 96);
        }
        spr.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(age >= 9)
            remove();
    }

    @Override
    public void checkCollision() {
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        for(Actor a : actors) {
            if(a != null && a instanceof Entity && !a.equals(this)) {
                Circle aRect = new Circle(a.getX(), a.getY(), a.getWidth());
                Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                if(Intersector.overlaps(aRect, thisRect))
                    if(a instanceof Enemy)
                        ((Entity)a).onHit(this);
                    else if(a instanceof Projectile)
                        a.remove();
            }
        }
    }
}
