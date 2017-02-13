package com.hyperforce.renegade.ProjectileAi;

import com.hyperforce.renegade.EnemyAi.ShieldShip;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;

/**
 * Created by jordan on 2/10/17.
 */
public class ShieldProjectile extends Projectile {
    public ShieldProjectile(float x, float y) {
        super(x, y, 54, 54, 270, 18, 0, 369, 1);
    }

    @Override
    public void onHit(Entity offender) {
        if(!(offender instanceof ShieldShip) && !(offender instanceof Projectile))
            remove();
        if(offender instanceof BasicLaser)
            ((BasicLaser) offender).remove();
    }
}
