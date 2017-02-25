package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

public class Star extends Projectile {
    Ship player;
    private boolean gravitate;

    public Star(float x, float y) {
        super(x, y, 11, 11, 270, 10, 0, 120, 0);
        spr.setSize(33, 33);
        setBounds(x, y, 33, 33);
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        for(Actor a : actors)
            if(a instanceof Ship)
                player = (Ship)a;
        gravitate = true;
    }

    public Star(float x, float y, float angle) {
        super(x, y, 11, 11, angle, 10, 0, 120, 0);
        spr.setSize(33, 33);
        setBounds(x, y, 33, 33);
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        for(Actor a : actors)
            if(a instanceof Ship)
                player = (Ship)a;
        gravitate = false;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float xDif = 0;
        if(player != null && gravitate)
            xDif = player.getX() - getX();
        if(xDif > 100) // If the star needs to move more than 10 pixels
            xDif = 100;
        else if(xDif < -100) /// For the leftward stars
            xDif = -100;
        moveBy(xDif / 10f, 0);
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
                        Ship.stars++;
                        long id = Ship.sounds[7].play();
                        Ship.sounds[7].setVolume(id, Ship.volume / 100f);
                        onHit((Entity)a);
                    }
            }
        }
    }
}
