package com.hyperforce.renegade.EnemyAi;

import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;
import com.hyperforce.renegade.ProjectileAi.ShieldProjectile;
import com.hyperforce.renegade.Ship;

import static com.hyperforce.renegade.Projectile.group;

/**
 * Created by jordan on 2/10/17.
 */
public class ShieldCannonShip extends CannonShip {
    private boolean shieldUp;

    public ShieldCannonShip(int x, int y) {
        super(x, y);
        spr.setRegion(218, 0, 18, 18);
        spr.setSize(54, 54);
        spr.setPosition(getX(), getY());
        setBounds(getX(), getY(), 54, 54);

        shieldUp = true;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(age == 79 && shieldUp) {
            group.addActor(new ShieldProjectile(getX(), getY()));
            shieldUp = false;
            spr.setRegion(0, 0, 16, 16);
            spr.setSize(48, 48);
            setBounds(getX(), getY() - 3, 48, 48);
        }
    }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof BasicLaser && shieldUp) {
            long id = Ship.sounds[12].play();
            Ship.sounds[12].setVolume(id, Ship.volume / 150f);
            ((Projectile) offender).remove();
        } else if(!(offender instanceof ShieldProjectile))
            super.onHit(offender);
    }
}
