package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.utils.Array;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;
import com.hyperforce.renegade.Ship;
import com.sun.istack.internal.NotNull;

import static com.hyperforce.renegade.Projectile.group;

public class LargeShip extends Enemy {
    private SmallTurret[] turrets;
    private Core core;
    private Map map;
    private int lastRedirection;
    private boolean bossFight;
    private float oldX, oldY;
    private boolean defeated;
    private int amtExposed; // For the core opening animation
    private int stage;
    private boolean finalBoss; // Easier than saying "stage == 2" all the time

    private final int cutsceneTime = 120;

    public LargeShip(int x, int y, int stage) {
        super(x, y);
        spr.setRegion(0, 293, 128, 128);
        spr.setSize(384, 384);
        spr.setPosition(getX(), getY());
        vulnerable = false;

        finalBoss = stage == 2;

        turrets = new SmallTurret[4];
        turrets[0] = new SmallTurret(x + 78, y + 78, finalBoss);
        turrets[1] = new SmallTurret(x + 78, y + 306, finalBoss);
        turrets[2] = new SmallTurret(x + 306, y + 306, finalBoss);
        turrets[3] = new SmallTurret(x + 306, y + 78, finalBoss);
        core = new Core((int)getX() + 192, (int)getY() + 192, this);
        map = new Map(this);

        lastRedirection = 0;
        oldX = x;
        oldY = y;
        bossFight = false;
        defeated = false;
        amtExposed = 0;
        this.stage = stage;
        if(finalBoss) {
            addAction(Actions.moveTo(192, 192, 1f));
            age = -1 * cutsceneTime;
            //setVisible(false);
        }

        group.addActorAt(1, this);
        for(SmallTurret t : turrets)
            group.addActorAfter(this, t);
        group.addActorAfter(this, core);
        group.getParent().addActor(map); // Map isn't in the group so that it is always on top
    }

    @Override
    public void act(float delta) {
        if(stage == 1)
            super.act(delta * (float)(0.5f * Math.sin(age / 30f) + 1f));
        else
            super.act(delta);
        if(age >= 0) {
            if(age % 120 == 100) {
                boolean turretIntact = false;
                for(SmallTurret t : turrets) {
                    t.prepareFireAnim();
                    if(t.alive)
                        turretIntact = true;
                }
                if(turretIntact) {
                    long id = Ship.sounds[3].play();
                    Ship.sounds[3].setVolume(id, Ship.volume / 100f);
                    Ship.sounds[3].setPitch(id, 0.75f);
                }

            }
            if(age % 120 == 0 && age > 0) {
                boolean turretIntact = false;
                for(SmallTurret t : turrets) {
                    t.fire();
                    if(t.alive)
                        turretIntact = true;
                }
                if(turretIntact) {
                    long id = Ship.sounds[5].play();
                    Ship.sounds[5].setVolume(id, Ship.volume / 100f);
                }
            }
            boolean moving = !(oldX == getX() && oldY == getY());
            if(!defeated && (!bossFight || stage > 0) && (lastRedirection <= 0 || !moving) && (age >= 0)) {
                int x = GENERATOR.nextInt(1152) - 384;
                int y = GENERATOR.nextInt(384) + 384;
                if(bossFight) {
                    x = GENERATOR.nextInt(768) - 192;
                    y = GENERATOR.nextInt(768) - 192;
                }
                Array<Action> actionses = new Array<>(getActions()); // This name is dumb and I love it
                for(Action a : actionses)
                    if(a instanceof MoveToAction)
                        removeAction(a);
                addAction(Actions.moveTo(x, y, (float)Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2)) / 256f));
                lastRedirection = 150 + GENERATOR.nextInt(150);
            }
            if(lastRedirection > 0)
                lastRedirection--;
        }
        if(!defeated) {
            for(SmallTurret t : turrets)
                t.moveBy(getX() - oldX, getY() - oldY);
            core.moveBy(getX() - oldX, getY() - oldY);
        }
        oldX = getX();
        oldY = getY();
    }

    @Override
    public void onHit(Entity offender) {

    }

    public void mainFight() {
        bossFight = true;
        Array<Action> actionses = new Array<>(getActions()); // This name is dumb and I love it
        for(Action a : actionses)
            if(a instanceof MoveToAction)
                removeAction(a);
        addAction(Actions.sequence(Actions.moveTo(192, 192, (float)Math.sqrt(Math.pow(getX() - 192, 2) + Math.pow(getY()
                - 192, 2)) / 256f), Actions.repeat(18, Actions.run(() -> amtExposed += 2))));
    }

    public boolean inBossFight() {
        return bossFight;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public int getAmtExposed() { return amtExposed; }

    public int getFightStage() { return stage; }

    public void die() {
        group.removeActor(this);
        for(SmallTurret t : turrets)
            group.removeActor(t);
        group.removeActor(core);
        group.getParent().removeActor(map);
    }


    // The other things
    private class SmallTurret extends Enemy {
        private boolean alive;
        private boolean finalBoss;

        public SmallTurret(int centerX, int centerY, boolean finalBoss) {
            super(centerX - 39, centerY - 39);
            spr.setRegion(128, 293, 26, 26);
            spr.setSize(78, 78);
            spr.setPosition(getX(), getY());
            setBounds(getX(), getY(), 78, 78);
            vulnerable = false;
            alive = true;
            hp = 5;
            this.finalBoss = finalBoss;
            if(finalBoss)
                age = -1 * cutsceneTime;
        }

        public void prepareFireAnim() {
            if(alive) {
                spr.setRegion(154, 293, 26, 26);
                spr.setSize(78, 78);
                spr.setPosition(getX(), getY());
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if(finalBoss && (age % 360 < 160 || ((age % 360 < 180 || (age > -20 && age < 0)) && age % 4 < 2))) {
                spr.setRegion(206, 293, 36, 36);
                spr.setSize(72, 72);
                spr.setPosition(getX() - 15, getY() - 15);
                spr.draw(batch);

                spr.setRegion(128, 293, 26, 26);
                spr.setSize(78, 78);
                spr.setPosition(getX(), getY());
            }
        }

        public void fire() {
            if(alive) {
                spr.setRegion(128, 293, 26, 26);
                spr.setSize(78, 78);
                spr.setPosition(getX(), getY());

                int finalDir = findFinalDir();
                float cannonX = getX() + 12 + (float)(Math.cos(Math.toRadians(finalDir)) * 24);
                float cannonY = getY() + 12 + (float)(Math.sin(Math.toRadians(finalDir)) * 24);
                getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir, 16, 0, 96, 1));
                // The below is really hard to deal with when there is more than the LargeShip on screen
                /*getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir - 5, 18, 0, 96, 1));
                getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir + 5, 18, 0, 96, 1));*/
            }
        }

        public int findFinalDir() {
            float dir = (float)Math.atan((getY() - player.getY()) / (getX() - player.getX()));
            if(getX() >= player.getX())
                dir += Math.PI;
            int finalDir = (int)Math.toDegrees(dir);
            if(finalDir < 0)
                finalDir += 360;
            return finalDir;
        }

        @Override
        public void onHit(Entity offender) {
            if(hp > 0 && offender instanceof BasicLaser) {
                if(finalBoss && age % 360 < 180) {
                    long id = Ship.sounds[12].play();
                    Ship.sounds[12].setVolume(id, Ship.volume / 150f);
                    ((Projectile) offender).remove();
                } else {
                    super.onHit(offender);
                    if(hp <= 0) {
                        group.addActor(this);
                        spr.setRegion(180, 293, 26, 26);
                        spr.setSize(78, 78);
                        spr.setPosition(getX(), getY());
                        alive = false;
                    }
                    ((BasicLaser) offender).addAction(Actions.removeActor());
                }
            }
        }
    }

    private class Core extends Enemy {
        private LargeShip parent;

        public Core(int centerX, int centerY, @NotNull LargeShip parent) {
            super(centerX - 48, centerY - 48);
            spr.setRegion(160, 319, 32, 32);
            spr.setSize(96, 96);
            spr.setPosition(getX(), getY());
            setBounds(getX(), getY(), 96, 96);
            vulnerable = false;
            hp = 45;
            this.parent = parent;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            int amtExposed = parent.getAmtExposed();

            spr.setRegion(160, 319, 32, 32);
            spr.setSize(96, 96);
            spr.setPosition(getX(), getY());
            spr.draw(batch);

            spr.setRegion(128, 319, 16, 16);
            spr.setSize(48, 48);
            spr.setPosition(getX() - amtExposed, getY() + 48 + amtExposed);
            spr.draw(batch);

            spr.setRegion(144, 319, 16, 16);
            spr.setSize(48, 48);
            spr.setPosition(getX() + 48 + amtExposed, getY() + 48 + amtExposed);
            spr.draw(batch);

            spr.setRegion(128, 335, 16, 19);
            spr.setSize(48, 57);
            spr.setPosition(getX() - amtExposed, getY() - amtExposed);
            spr.draw(batch);

            spr.setRegion(144, 335, 16, 19);
            spr.setSize(48, 57);
            spr.setPosition(getX() + 48 + amtExposed, getY() - amtExposed);
            spr.draw(batch);
        }

        @Override
        public void onHit(Entity offender) {
            if(parent.inBossFight() && hp > 0 && offender instanceof BasicLaser) {
                super.onHit(offender);
                if(hp <= 0) {
                    group.addActor(this);
                    spr.setRegion(180, 293, 26, 26);
                    spr.setSize(78, 78);
                    spr.setPosition(getX(), getY());
                    parent.setDefeated(true);
                }
                ((BasicLaser) offender).addAction(Actions.removeActor());
            }
        }
    }

    private class Map extends Actor {
        private LargeShip parent;
        private Sprite spr;

        private Map(LargeShip parent) {
            this.parent = parent;
            spr = new Sprite(Enemy.region);
            spr.setRegion(192, 329, 20, 10);
            spr.setSize(60, 60);
            spr.setPosition(player.getX() + 48, player.getY());
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            spr.setRegion(192, 329, 20, 20);
            spr.setSize(60, 60);
            spr.setPosition(player.getX() + 48, player.getY());
            spr.draw(batch);

            int xOfs = (int)((parent.getX() + 384) * 15 / 384f);
            int yOfs = (int)((parent.getY() + 384) * 15 / 384f);
            spr.setRegion(192, 324, 5, 5);
            spr.setSize(15, 15);
            spr.setPosition(player.getX() + 48 + xOfs, player.getY() + yOfs);
            spr.draw(batch);
        }
    }
}
