package com.hyperforce.renegade.ProjectileAi;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.EnemyAi.RedPowerup;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Ship;

public class PowerupSphere extends Star {
    int col;

    public static int bluePowerDur = 0;
    public static int greenPowerDur = 0;
    public static int yellowPowerDur = 0;

    public PowerupSphere(float x, float y, int col) {
        super(x, y);
        spr.setRegion(160 + 42 * (col % 2), 42 * (col / 2), 42, 42);
        setBounds(x, y, 42, 42);
        this.col = col;
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
                        switch(col) {
                            case 0:
                                getParent().addActor(new RedPowerup());
                                break;
                            case 1:
                                if(Ship.lives < 4)
                                    Ship.lives++;
                                break;
                            case 2:
                                player.setInvin(360);
                                yellowPowerDur = 360;
                                break;
                            case 3:
                                greenPowerDur = 360;
                                break;
                            case 4:
                                bluePowerDur = 360;
                                break;
                            case 5:
                                break;
                        }
                        long id = Ship.sounds[8].play();
                        Ship.sounds[8].setVolume(id, Ship.volume / 100f);
                        onHit((Entity)a);
                    }
            }
        }
    }
}
