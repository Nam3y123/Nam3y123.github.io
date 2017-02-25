package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;
import com.hyperforce.renegade.*;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;

import static com.hyperforce.renegade.EnemyAi.EyeBlasterBoss.screenFill;

/**
 * Created by jordan on 2/10/17.
 */
public class LaserBoss extends Enemy {
    private int shieldDist, shieldY;
    private boolean shieldUp, vulnerable, dying, bigExplosion;
    private float explosionX, explosionY;
    private int explosionDur;
    private int anim, age;

    public LaserBoss(int x, int y) {
        super(x, 768);
        setBounds(x, 768, 384, 480);
        addAction(Actions.sequence(Actions.moveTo(x, y, 1f), Actions.run(() -> vulnerable = true)));

        Ship.bossHealth = 30;
        Ship.bossHealthMax = 30;
        bigExplosion = false;

        anim = 0;
        age = 0;
        shieldDist = 0;
        shieldUp = true;

        if(screenFill.getTexture() == null) {
            Pixmap screen = new Pixmap(768, 768, Pixmap.Format.RGBA8888);
            screen.setColor(Color.ORANGE);
            screen.fill();
            screenFill.setTexture(new Texture(screen));
            screenFill.setSize(768, 768);
        }

        MainClass.soundtrack[MainClass.songPlaying].stop();
        MainClass.soundtrack[2].play();
        MainClass.songPlaying = 2;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(112, 18, 128, 128);
        spr.setSize(384, 384);
        spr.setPosition(getX(), getY() + (shieldUp ? 96 : 0));
        spr.draw(batch);

        spr.setRegion(112 + (anim * 10), 146, 10, 11);
        spr.setSize(30, 33);
        spr.setPosition(getX() + 177, getY() + (shieldUp ? 102 : 6));
        spr.draw(batch);

        spr.setRegion(112, 157, 64, 32);
        spr.setSize(192, 96);
        spr.setPosition(getX() - (shieldDist / 2f), getY() - (shieldUp ? 0 : 96));
        spr.draw(batch);

        spr.setRegion(176, 157, 64, 32);
        spr.setSize(192, 96);
        spr.setPosition(getX() + 192 + (shieldDist / 2f), getY() - (shieldUp ? 0 : 96));
        spr.draw(batch);

        if(Ship.bossHealth <= 0) {
            explosionDur--;
            if(!bigExplosion) {
                Projectile.drawExplosion(getX() + explosionX + 168, getY() + explosionY + 192, 12 * (12 - explosionDur),
                        batch, Color.WHITE);
                if(explosionDur == 0) {
                    long id = Ship.sounds[0].play();
                    Ship.sounds[0].setVolume(id, Ship.volume / 100f);
                    explosionDur = 12;
                    double exRadian = GENERATOR.nextDouble() * Math.PI * 2;
                    explosionX = (GENERATOR.nextInt(384) - 192) * (float)Math.cos(exRadian);
                    explosionY = (GENERATOR.nextInt(384) - 192) * (float)Math.sin(exRadian);
                }
            } else {
                if(explosionDur <= -56) {
                    if(explosionDur >= -64)
                        screenFill.setAlpha(((-1 * explosionDur) - 56) / 8f);
                    if(explosionDur == -64)
                        Ship.bossDead = true;
                    screenFill.draw(batch);
                }
                if(explosionDur > -64)
                    Projectile.drawExplosion(getX() + 168, getY() + 192, -24 * explosionDur, batch, Color.ORANGE);
                //Note: explosionDur is less than zero here; if -24 becomes 24, you get a giant black rectangle
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        age++;
        if(anim < 2 && age % 60 == 0)
            anim = anim == 0 ? 1 : 0;
        if(age % 480 >= 420 && age % 480 < 440)
            anim = ((age % 480) - 420) / 10 + 2;
        if(age % 480 == 450)
            anim = 1;
        shieldUp = (age % 480 < 420);
        if(age % 480 == 420)
            setBounds(getX(), getY() + 96, 384, 384);
        if(age % 480 == 0 && getHeight() != 480)
            setBounds(getX(), getY() - 96, 384, 480);

        if(age % 480 >= 420 && age % 480 <= 450)
            shieldDist = ((age % 480) - 420) * 64;
        else if(age % 480 > 450)
            shieldDist = 1920 - ((age % 480) - 450) * 64;
        else
            shieldDist = 0;
    }

    private void die() {
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
        Ship.score += 20000;
        Ship.stars += 20;
        dying = true;
        bigExplosion = false;
        vulnerable = false;
        MainClass.soundtrack[MainClass.songPlaying].stop();
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                bigExplosion = true;
                explosionDur = 0;
            }
        }, 2f);
    }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof Ship && vulnerable)
            ((Ship)offender).setHp(((Ship)offender).getHp() - 1);
        else if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0 && invinDur <= 0 && vulnerable &&
                !shieldUp) {
            Projectile proj = (Projectile)offender;
            Ship.bossHealth -= proj.getDmg();
            invinDur = 15;
            long id = Ship.sounds[0].play();
            Ship.sounds[0].setVolume(id, Ship.volume / 150f);
            if(Ship.bossHealth <= 0) {
                die();
            }
        }
    }
}
