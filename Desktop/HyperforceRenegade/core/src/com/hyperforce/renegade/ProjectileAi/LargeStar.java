package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Ship;

/**
 * Created by jordan on 2/24/17.
 */
public class LargeStar extends Star {
    public LargeStar(float x, float y) {
        super(x, y);
        spr.setRegion(0, 131, 22, 22);
        spr.setSize(66, 66);
        setBounds(x, y, 66, 66);
    }

    @Override
    public void checkCollision() {
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        for(Actor a : actors) {
            if(a != null && a instanceof Entity && !a.equals(this)) {
                Circle aRect = new Circle(a.getX(), a.getY(), a.getWidth());
                Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                if(Intersector.overlaps(aRect, thisRect))
                    if(a instanceof Ship) {
                        Ship.stars += 5;
                        long id = Ship.sounds[7].play();
                        Ship.sounds[7].setVolume(id, Ship.volume / 100f);
                        onHit((Entity)a);
                    }
            }
        }
    }
}
