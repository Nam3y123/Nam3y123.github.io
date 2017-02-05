package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;
import com.hyperforce.renegade.ProjectileAi.PowerupSphere;
import com.hyperforce.renegade.ProjectileAi.Star;
import com.hyperforce.renegade.Ship;

import java.util.Random;

public class MysteryShip extends Enemy {
    private static final Random generator = new Random();
    private static final Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.VIOLET};

    public MysteryShip(int y) {
        super(768, y);
        spr.setRegion(41, 0, 16, 16);
        spr.setSize(48, 48);
        hp = 1;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(age % 12 == 0) {
            spr.setRegion(41, 0, 16, 16);
            spr.setSize(48, 48);
            spr.setColor(colors[generator.nextInt(6)]);
        } else if(age % 12 == 6) {
            spr.setRegion(57, 0, 16, 16);
            spr.setSize(48, 48);
        }
        super.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moveBy(-4 - (float)(4 * (1 + Math.cos(age / 7.5f))), 0);
    }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof Ship)
            ((Ship)offender).setHp(((Ship)offender).getHp() - 1);
        else if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0) {
            Projectile proj = (Projectile)offender;
            hp -= proj.getDmg();
            long id = Ship.sounds[0].play();
            Ship.sounds[0].setVolume(id, Ship.volume / 150f);
            if(hp <= 0) {
                if(Ship.upgrades[3][4]) {
                    if(BasicLaser.stacks < 3)
                        BasicLaser.stacks++;
                    BasicLaser.stackDur = 180;
                }
                if(Ship.upgrades[1][2]) {
                    Ship.vampirism++;
                    if(Ship.vampirism == 5) {
                        Ship.vampirism = 0;
                        if(player.getHp() < 3)
                            player.setHp(player.getHp() + 1);
                    }
                }
                Ship.score += 500;
                getParent().addActor(new PowerupSphere(getX(), getY(), generator.nextInt(5)));
                //getParent().addActor(new PowerupSphere(getX(), getY(), 0));
                MysteryShip.this.remove();
            }
        }
    }
}
