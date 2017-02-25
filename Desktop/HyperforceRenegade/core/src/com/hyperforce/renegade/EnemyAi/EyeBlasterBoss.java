package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.Timer;
import com.hyperforce.renegade.*;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;

import static com.hyperforce.renegade.Projectile.group;

public class EyeBlasterBoss extends Enemy {
    private int phase;
    private int fireballTimer; // Time until next shot
    private int fireballCharge; // Time until shot charged
    private boolean vulnerable, dying, bigExplosion;
    private float explosionX, explosionY;
    private int explosionDur;
    private int atk;
    private int atkAge;
    private int simultaneousSweep, fireAtksUsed;

    /*
    ----------------------
    |atk values|attack    |
    |0         |Side Sweep|
    |1         |Shoot-Off |
    |2         |Mad Dash  |
    -----------------------
     */


    public static final Sprite screenFill = new Sprite();

    public EyeBlasterBoss(int x, int y) {
        super(x, 768);
        setBounds(x, 768, 336, 384);
        addAction(Actions.sequence(Actions.moveTo(x, y, 1f), Actions.run(() -> vulnerable = true)));

        Ship.bossHealth = 100;
        Ship.bossHealthMax = 100;
        vulnerable = false;
        dying = false;
        bigExplosion = false;

        phase = 1;
        fireballTimer = 150;
        fireballCharge = -1;

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
        spr.setRegion(0, 17, 112, 128); // Main body
        spr.setSize(336, 384);
        spr.setPosition(getX(), getY());
        super.draw(batch, parentAlpha);

        spr.setRegion(0, 145, 8, 60); // Left side
        spr.setSize(24, 180);
        spr.setPosition(getX() - 24, getY() + 102);
        spr.draw(batch);

        spr.setRegion(0, 205, 8, 60); // Right side
        spr.setSize(24, 180);
        spr.setPosition(getX() + 336, getY() + 102);
        spr.draw(batch);

        float dir = (float)Math.atan((getX() - player.getX() + 144) / (-getY() + player.getY() - 168)) +
                (float)(Math.PI / 2f);
        if(-getY() + player.getY() - 168 < 0)
            dir += Math.PI;
        if(-getY() + player.getY() - 168 == 0) {
            if(getX() - player.getX() + 144 < 0)
                dir = 0;
            else
                dir = (float)Math.PI;
        }

        spr.setRegion(39, 145, 44, 44); // Outer eye
        spr.setSize(132, 132);
        spr.setPosition(getX() + 102 + (12 * (float)Math.cos(dir)), getY() + 126 + (12 * (float)Math.sin(dir)));
        spr.draw(batch);

        spr.setRegion(39, 189, 14, 14); // Inner eye
        spr.setSize(42, 42);
        spr.setPosition(getX() + 147 + (34 * (float)Math.cos(dir)), getY() + 171 + (34 * (float)Math.sin(dir)));
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

        if(!dying) {
            if(phase == 1)
                phase1Atk();
            else
                switch (atk) {
                    case 0:
                        sideSweep();
                        break;
                    case 1:
                        phase2FireAtk();
                        break;
                    case 2:
                        break;
                }

            if(fireballTimer > 0)
                fireballTimer--;
            if(fireballCharge > 0)
                fireballCharge--;
            atkAge++;
        }
    }

    private void atkChange() {
        atk = GENERATOR.nextInt(2);
        if(atk == 0)
            simultaneousSweep++;
        if(simultaneousSweep == 3)
            atk = 1;
        switch (atk) {
            case 0:
                vulnerable = true;
                int sideSweepY = 512 - GENERATOR.nextInt(144);
                if(getX() > 192) {
                    setPosition(792, sideSweepY);
                    addAction(Actions.moveTo(-385, sideSweepY, 1.5f));
                } else {
                    setPosition(-384, sideSweepY);
                    addAction(Actions.moveTo(793, sideSweepY, 1.5f));
                }
                atk = 0;
                break;
            case 1:
                int newX = GENERATOR.nextInt(768) - 168;
                addAction(Actions.sequence(Actions.moveTo(newX, 768), Actions.moveTo(newX, 480, 1f)));
                fireballTimer = 150;
                fireballCharge = -1;
                fireAtksUsed = 0;
                break;
        }
        atkAge = 0;
    }

    private void phase1Atk() {
        if(fireballTimer == 0) {
            fireballCharge = 45;
            fireballTimer = -1;

            long id = Ship.sounds[3].play();
            Ship.sounds[3].setVolume(id, Ship.volume / 100f);
            Ship.sounds[3].setPitch(id, 0.75f);

            int[] xOfs = {-24, 156, 336, 156};
            int[] yOfs = {180, 0, 180, 360};
            for(int i = 0; i < 4; i++)
                group.addActor(new Projectile(getX() + xOfs[i], getY() + yOfs[i], 24, 24, i * 90, 3.733333f, 0, 96, 0) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        if(this.age == 45)
                            remove();
                    }

                    @Override
                    public void onHit(Entity offender) {


                    }
                });
        }
        if(fireballCharge == 0) {
            fireballCharge = -1;
            fireballTimer = 90;

            fireAtk();
        }
    }

    private void sideSweep() {
        if(atkAge == 30 || atkAge == 60) {
            fireAtk();
        }
        if(getX() <= -384 || getX() >= 792)
            atkChange();
    }

    private void phase2FireAtk() {
        if(fireballTimer == 0) {
            fireballCharge = 45;
            fireballTimer = -1;

            long id = Ship.sounds[3].play();
            Ship.sounds[3].setVolume(id, Ship.volume / 100f);
            Ship.sounds[3].setPitch(id, 0.75f);

            int[] xOfs = {-24, 156, 336, 156};
            int[] yOfs = {180, 0, 180, 360};
            for(int i = 0; i < 4; i++)
                group.addActor(new Projectile(getX() + xOfs[i], getY() + yOfs[i], 24, 24, i * 90, 3.733333f, 0, 96, 0) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        if(this.age == 45)
                            remove();
                    }

                    @Override
                    public void onHit(Entity offender) {}
                });
        }
        if(fireballCharge == 0) {
            fireballCharge = -1;
            fireballTimer = 90;
            fireAtksUsed++;

            fireAtk();
        }

        if(fireAtksUsed == 3) {
            addAction(Actions.sequence(Actions.moveBy(0, 288, 1f), Actions.delay(0.5f), Actions.run(() -> {
                        long id = Ship.sounds[10].loop();
                        Ship.sounds[10].setVolume(id, Ship.volume / 50f);
                    }), Actions.moveBy(0, -1152, 0.5f), Actions.run(() -> Ship.sounds[10].stop()), Actions.delay(0.5f),
                    Actions.run(this::atkChange)));
            atk = 2;
        }
    }

    private void fireAtk() {
        long id = Ship.sounds[5].play();
        Ship.sounds[5].setVolume(id, Ship.volume / 100f);

        float dir = (float)Math.atan((getX() - player.getX() + 144) / (-getY() + player.getY() - 168)) +
                (float)(Math.PI / 2f);
        if(-getY() + player.getY() - 168 < 0)
            dir += Math.PI;
        if(-getY() + player.getY() - 168 == 0) {
            if(getX() - player.getX() + 144 < 0)
                dir = 0;
            else
                dir = (float)Math.PI;
        }
        dir *= 180 / (float)Math.PI;

        group.addActor(new Projectile(getX() + 42, getY() + 66, 252, 252, dir, 18, 0, 0, 1) {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                spr.setRegion(80, 0, 80, 84);
                spr.setSize(252, 252);
                super.draw(batch, parentAlpha);
            }

            @Override
            public void checkCollision() {
                SnapshotArray<Actor> actors = new SnapshotArray<>(group.getChildren());
                if(age > 2)
                    for(Actor a : actors) {
                        if(a != null && a != EyeBlasterBoss.this && a instanceof Entity && !a.equals(this)) {
                            Rectangle aRect = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                            Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                            if(Intersector.overlaps(aRect, thisRect)) {
                                ((Entity)a).onHit(this);
                                onHit((Entity)a);
                            }
                        }
                    }
            }
        });
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
        else if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0 && invinDur <= 0 && vulnerable) {
            Projectile proj = (Projectile)offender;
            Ship.bossHealth -= proj.getDmg();
            invinDur = 15;
            long id = Ship.sounds[0].play();
            Ship.sounds[0].setVolume(id, Ship.volume / 150f);
            if(Ship.bossHealth <= 0) {
                if(phase == 1) {
                    explosionDur = 12;
                    double exRadian = GENERATOR.nextDouble() * Math.PI * 2;
                    explosionX = (GENERATOR.nextInt(384) - 192) * (float)Math.cos(exRadian);
                    explosionY = (GENERATOR.nextInt(384) - 192) * (float)Math.sin(exRadian);
                    phase = -1;

                    Ship.bossHealthMax = 300;
                    vulnerable = false;
                    addAction(Actions.sequence(Actions.moveTo(getX(), 792, 2f), Actions.parallel(Actions.repeat(100,
                            Actions.run(() -> Ship.bossHealth += 2)), Actions.moveTo(getX(), -384, 1.5f)),
                            Actions.run(() -> {
                        phase = 2;
                        vulnerable = true;
                        int sideSweepY = 512 - GENERATOR.nextInt(144);
                        setPosition(768, sideSweepY);
                        addAction(Actions.moveTo(-385, sideSweepY, 1.5f));
                        atk = 0;
                        atkAge = 0;
                    })));
                } else {
                    die();
                }
            }
        }
    }
}
