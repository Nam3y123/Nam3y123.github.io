package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.utils.Array;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.ProjectileAi.BasicLaser;

public class ParryShip extends Enemy {
    private int curAnimFrame;
    private int lastRedirection;
    private float oldX, oldY;
    private int beamDelay, stoHp;

    public ParryShip(int x, int y) {
        super(x, y);
        spr.setRegion(246, 0, 16, 16);
        spr.setSize(48, 48);
        spr.setPosition(getX(), getY());
        setBounds(getX() - 24, getY() - 24, 96, 96);

        lastRedirection = 0;
        oldX = x;
        oldY = y;
        beamDelay = -1;
        hp = 6;
        curAnimFrame = 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(247, 16, 96, 96);
        spr.setSize(288, 288);
        spr.setPosition(getX() - 96, getY() - 96);
        spr.draw(batch);

        spr.setRegion(246 + curAnimFrame * 16, 0, 16, 16);
        spr.setSize(48, 48);
        spr.setPosition(getX() + 24, getY() + 24);
        super.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean moving = !(oldX == getX() && oldY == getY());
        if(lastRedirection <= 0 || !moving) {
            int x = GENERATOR.nextInt(1536) - 384;
            int y = GENERATOR.nextInt(1536) - 384;
            Array<Action> actionses = new Array<>(getActions()); // This name is dumb and I love it
            for(Action a : actionses)
                if(a instanceof MoveToAction)
                    removeAction(a);
            addAction(Actions.moveTo(x, y, (float)Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2)) / 128f));
            lastRedirection = 150 + GENERATOR.nextInt(150);
        }
        if(lastRedirection > 0)
            lastRedirection--;
        oldX = getX();
        oldY = getY();

        for(Actor a : Projectile.group.getChildren()) {
            if(beamDelay < 0 && a instanceof BasicLaser) {
                Rectangle aArea = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                Circle scanArea = new Circle(getX() + 24, getY() + 24, 108);
                if(Intersector.overlaps(scanArea, aArea)) {
                    beamDelay = 20;
                    curAnimFrame = 2;
                }
            }
        }
        beamDelay--;
        if(beamDelay == 2)
            curAnimFrame = 3;
        if(beamDelay == 0) {
            player.setHp(player.getHp() - 1);
            curAnimFrame = 0;
        }
        if(beamDelay == -300)
            curAnimFrame = 0;
        stoHp = hp;
    }

    @Override
    public void onHit(Entity offender) {
        super.onHit(offender);
        if(hp < stoHp) {
            beamDelay = 0;
            curAnimFrame = 4;
            invinDur = 30;
        }
    }
}

/*
Chapter #: 9807
Password: ABE27IJY
 */
