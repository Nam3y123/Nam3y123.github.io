package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

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
    public void act(float delta) {
        super.act(delta);
        if(timerSet)
            duration--;
        if(duration == 0)
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    addAction(Actions.sequence(Actions.moveBy(0, 768, 1f), Actions.removeActor()));
                }
            }, 0.3f);
    }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0 && shieldUp) {
            long id = Ship.sounds[12].play();
            Ship.sounds[12].setVolume(id, Ship.volume / 150f);
            ((Projectile) offender).remove();
        } else
            super.onHit(offender);
    }
}
