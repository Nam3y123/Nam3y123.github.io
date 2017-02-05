package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

public class BasicLaser extends Projectile {
    public static int stacks = 0;
    public static int stackDur = 0;

    public BasicLaser(float x, float y, float w, float h, float dir, float speed, int texX, int texY, int dmg) {
        super(x, y, w, h, dir, speed, texX, texY, dmg);
        if(stackDur > 0) {
            if(w == 48)
                spr.setRegion(texX, 201 + (24 * stacks), (int)w, (int)h);
            else
                spr.setRegion(texX, 273 + (24 * stacks), (int)w, (int)h);
        }
    }

    @Override
    public void checkCollision() {
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        if(age > 2)
            for(Actor a : actors) {
                if(a != null && a instanceof Entity && !(a instanceof Ship) && !a.equals(this)) {
                    Rectangle aRect = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                    Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                    if(Intersector.overlaps(aRect, thisRect)) {
                        ((Entity)a).onHit(this);
                        if(!Ship.upgrades[3][1])
                            onHit((Entity)a);
                    }
                }
            }
    }
}
