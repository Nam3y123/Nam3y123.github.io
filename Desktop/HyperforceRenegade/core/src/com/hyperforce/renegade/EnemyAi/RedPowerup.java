package com.hyperforce.renegade.EnemyAi;

import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

public class RedPowerup extends Enemy {
    public RedPowerup() {
        super((int)player.getX() + 16, (int)player.getY() + 12);
        spr.setRegion(73, 0, 5, 8);
        spr.setSize(15, 24);
        setBounds((int)player.getX() + 16, (int)player.getY() + 12, 15, 24);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        setPosition(player.getX() + 16, player.getY() + 12);
        if(age % 30 == 0) {
            getParent().addActor(new Projectile(getX() + 3, getY() + 24, 9, 18, 90, 24, 244, 0, 1) {
                @Override
                public void onHit(Entity offender) {
                    if(offender instanceof Enemy)
                        ((Enemy) offender).setInvinDur(1);
                    super.onHit(offender);
                }
            });
        }
        if(age == 360)
            remove();
    }

    @Override
    public void onHit(Entity offender) {

    }
}
