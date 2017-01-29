package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.hyperforce.renegade.Enemy;

public class ShieldShip extends Enemy {
    public ShieldShip(int x, int y) {
        super(x, 768);
        spr.setRegion(164, 0, 18, 18);
        setBounds(x, 768, 54, 54);
        spr.setSize(54, 54);
        addAction(Actions.moveTo(x, y, (768 - y) / 1024f));
    }
}
