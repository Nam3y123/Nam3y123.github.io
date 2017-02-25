package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;
import com.hyperforce.renegade.ProjectileAi.ShieldProjectile;
import com.hyperforce.renegade.Ship;

import static com.hyperforce.renegade.Projectile.group;

public class ShieldShip extends Enemy {
    private boolean shieldUp, timerSet;
    private int duration;

    public ShieldShip(int x, int y) {
        super(x, 768);
        spr.setRegion(164, 0, 18, 18);
        setBounds(x, 768, 54, 54);
        spr.setSize(54, 54);
        addAction(Actions.sequence(Actions.moveTo(x, y, (768 - y) / 1024f), Actions.run(() -> timerSet = true)));
        hp = 1;

        shieldUp = true;
        timerSet = false;
        duration = 180;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(duration <= 30 && duration > 0) {
            if(duration % 10 == 0) {
                spr.setRegion(182, 0, 18, 18);
                spr.setSize(54, 54);
            } else if(duration % 10 == 5) {
                spr.setRegion(164, 0, 18, 18);
                spr.setSize(54, 54);
            }
        }
        super.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(timerSet)
            duration--;
        if(duration == 0) {
            group.addActor(new ShieldProjectile(getX(), getY()));
            spr.setRegion(146, 0, 18, 18);
            spr.setSize(54, 54);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    addAction(Actions.run(() -> shieldUp = false));
                }
            }, 0.25f);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    addAction(Actions.sequence(Actions.moveBy(0, 768, 1f), Actions.removeActor()));
                }
            }, 0.75f);
        }
    }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof BasicLaser && shieldUp) {
            long id = Ship.sounds[12].play();
            Ship.sounds[12].setVolume(id, Ship.volume / 150f);
            ((Projectile) offender).remove();
        } else if(!(offender instanceof ShieldProjectile))
            super.largeStarOnHit(offender);
    }
}
