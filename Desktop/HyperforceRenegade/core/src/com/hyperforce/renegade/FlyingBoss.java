package com.hyperforce.renegade;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

public class FlyingBoss extends Actor {
    private Sprite spr;

    public FlyingBoss(int index) {
        spr = new Sprite(Enemy.region);
        spr.setRegion(240, index * 128, 128, 128);
        spr.setSize(384, 384);
        setBounds(192, -384, 384, 384);
        spr.setPosition(192, -384);
        addAction(Actions.sequence(Actions.moveTo(192, 768, 1), Actions.removeActor()));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setPosition(getX(), getY());
        spr.draw(batch);
    }
}
