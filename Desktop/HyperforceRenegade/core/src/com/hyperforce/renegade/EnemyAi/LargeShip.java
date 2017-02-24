package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.utils.Array;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;
import com.sun.istack.internal.NotNull;

import static com.hyperforce.renegade.Projectile.group;

public class LargeShip extends Enemy {
    private SmallTurret[] turrets;
    private Core core;
    private int lastRedirection;
    private boolean bossFight;
    private float oldX, oldY;
    private boolean defeated;

    public LargeShip(int x, int y) {
        super(x, y);
        spr.setRegion(0, 293, 128, 128);
        spr.setSize(384, 384);
        spr.setPosition(getX(), getY());
        vulnerable = false;

        turrets = new SmallTurret[4];
        turrets[0] = new SmallTurret(x + 78, y + 78);
        turrets[1] = new SmallTurret(x + 78, y + 306);
        turrets[2] = new SmallTurret(x + 306, y + 306);
        turrets[3] = new SmallTurret(x + 306, y + 78);
        core = new Core((int)getX() + 192, (int)getY() + 192, this);

        lastRedirection = 0;
        oldX = x;
        oldY = y;
        bossFight = false;
        defeated = false;

        group.addActorAt(1, this);
        for(SmallTurret t : turrets)
            group.addActor(t);
        group.addActor(core);
    }

    @Override
    public void act(float delta) {
        super.act(delta * (float)(Math.sin(age / 30f) + 1.5f));
        if(age % 120 == 100)
            for(SmallTurret t : turrets)
                t.prepareFireAnim();
        if(age % 120 == 0 && age > 0)
            for(SmallTurret t : turrets)
                t.fire();
        boolean moving = !(oldX == getX() && oldY == getY());
        if(!bossFight && (lastRedirection <= 0 || !moving)) {
            int x = GENERATOR.nextInt(1536) - 384;
            int y = GENERATOR.nextInt(1536) - 384;
            Array<Action> actionses = new Array<>(getActions()); // This name is dumb and I love it
            for(Action a : actionses)
                if(a instanceof MoveToAction)
                    removeAction(a);
            addAction(Actions.moveTo(x, y, (float)Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2)) / 256f));
            lastRedirection = 150 + GENERATOR.nextInt(150);
        }
        if(lastRedirection > 0)
            lastRedirection--;
        for(SmallTurret t : turrets)
            t.moveBy(getX() - oldX, getY() - oldY);
        core.moveBy(getX() - oldX, getY() - oldY);
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
        addAction(Actions.moveTo(192, 192, (float)Math.sqrt(Math.pow(getX() - 192, 2) + Math.pow(getY() - 192, 2)) / 256f));
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

    public void die() {
        group.removeActor(this);
        group.removeActor(core);
        for(SmallTurret t : turrets)
            group.removeActor(t);
    }


    // The other things
    private class SmallTurret extends Enemy {
        private boolean alive;

        public SmallTurret(int centerX, int centerY) {
            super(centerX - 39, centerY - 39);
            spr.setRegion(128, 293, 26, 26);
            spr.setSize(78, 78);
            spr.setPosition(getX(), getY());
            setBounds(getX(), getY(), 78, 78);
            vulnerable = false;
            alive = true;
            hp = 5;
        }

        public void prepareFireAnim() {
            if(alive) {
                spr.setRegion(154, 293, 26, 26);
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
                getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir, 18, 0, 96, 1));
                // The below is really hard to deal with when there is more thna the LargeShip on screen
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
            spr.setRegion(160, 319, 32, 32);
            spr.setSize(96, 96);
            spr.setPosition(getX(), getY());
            spr.draw(batch);

            spr.setRegion(128, 319, 16, 16);
            spr.setSize(48, 48);
            spr.setPosition(getX(), getY() + 48);
            spr.draw(batch);

            spr.setRegion(144, 319, 16, 16);
            spr.setSize(48, 48);
            spr.setPosition(getX() + 48, getY() + 48);
            spr.draw(batch);

            spr.setRegion(128, 335, 16, 19);
            spr.setSize(48, 57);
            spr.setPosition(getX(), getY());
            spr.draw(batch);

            spr.setRegion(144, 335, 16, 19);
            spr.setSize(48, 57);
            spr.setPosition(getX() + 48, getY());
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
}
