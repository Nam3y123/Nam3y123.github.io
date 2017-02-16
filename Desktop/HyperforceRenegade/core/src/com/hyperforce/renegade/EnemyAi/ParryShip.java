package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.utils.Array;
import com.hyperforce.renegade.Enemy;

public class ParryShip extends Enemy {
    private int lastRedirection;
    private float oldX, oldY;

    public ParryShip(int x, int y) {
        super(x, y);
        spr.setRegion(246, 0, 16, 16);
        spr.setSize(48, 48);
        spr.setPosition(getX(), getY());

        lastRedirection = 0;
        oldX = x;
        oldY = y;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        boolean moving = !(oldX == getX() && oldY == getY());
        if(lastRedirection <= 0 || !moving) {
            int x = GENERATOR.nextInt(1536) - 384;
            int y = GENERATOR.nextInt(1536) - 384;
            Array<Action> actionses = new Array<>(getActions()); // The name is dumb and I love it
            for(Action a : actionses)
                if(a instanceof MoveToAction)
                    removeAction(a);
            addAction(Actions.moveTo(x, y, (float)Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2)) / 128f));
            lastRedirection = 150 + GENERATOR.nextInt(150);
        }
        if(lastRedirection > 0)
            lastRedirection--;
    }
}
