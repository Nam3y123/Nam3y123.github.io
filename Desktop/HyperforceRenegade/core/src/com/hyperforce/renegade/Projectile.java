package com.hyperforce.renegade;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.Timer;

public class Projectile extends Actor implements Entity {
    private float dir;
    protected float speed;
    protected Sprite spr;
    protected int age;
    protected int dmg;

    public static final Texture tex = new Texture(Gdx.files.internal("Projectiles.png"));
    public static Group group;
    public static Pixmap pixmap = new Pixmap(0, 0, Pixmap.Format.RGBA8888);

    public Projectile(float x, float y, float dir, float speed, int texX, int texY, int dmg) {
        super();
        setBounds(x, y, 48, 48);
        this.dir = dir;
        this.speed = speed;
        spr = new Sprite(tex);
        spr.setRegion(texX, texY, 48, 48);
        spr.setSize(48, 48);
        spr.setPosition(x, y);
        age = 0;
        this.dmg = dmg;
    }

    public Projectile(float x, float y, float w, float h, float dir, float speed, int texX, int texY, int dmg) {
        super();
        setBounds(x, y, w, h);
        this.dir = dir;
        this.speed = speed;
        spr = new Sprite(tex);
        spr.setRegion(texX, texY, (int)w, (int)h);
        spr.setSize(w, h);
        spr.setPosition(x, y);
        age = 0;
        this.dmg = dmg;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(!Ship.shopping)
        checkCollision();
        moveBy(speed * (float)Math.cos(Math.toRadians(dir)), speed * (float)Math.sin(Math.toRadians(dir)));
        spr.setPosition(getX(), getY());
        if(getX() < -getWidth() || getY() < -getHeight() || getX() > 768 ||
                getY() > 768)
            this.remove();
        age++;
    }

    public void checkCollision() {
        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
        if(age > 2)
            for(Actor a : actors) {
                if(a != null && a instanceof Entity && !a.equals(this)) {
                    Rectangle aRect = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                    Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                    if(Intersector.overlaps(aRect, thisRect)) {
                        ((Entity)a).onHit(this);
                        onHit((Entity)a);
                    }
                }
            }
    }

    public int getDmg() {
        return dmg;
    }

    public float getDir() { return dir; }

    public void setDir(float dir) { this.dir = dir; }

    @Override
    public void onHit(Entity offender) {
        if(!(offender instanceof Projectile))
            remove();
    }

    public static void drawExplosion(float x, float y, int size, Batch batch) {
        pixmap.dispose();
        pixmap = new Pixmap(size / 3 + 1, size / 3 + 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 0.0f);
        pixmap.fill();
        pixmap.setColor(Color.RED);
        if(size < 1536) // If whole screen is filled; Reduces lag & potential crasherinos
            pixmap.fillCircle(size / 6, size / 6, size / 6);
        else
            pixmap.fill();

        final Sprite spr = new Sprite(new Texture(pixmap));
        spr.setSize(size, size);
        spr.setPosition(x - size / 2, y - size / 2);
        spr.draw(batch);
    }

    public static void drawExplosion(float x, float y, int size, Batch batch, Color col) {
        pixmap.dispose();
        pixmap = new Pixmap(size / 3 + 1, size / 3 + 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 0.0f);
        pixmap.fill();
        pixmap.setColor(col);
        if(size < 1536) // If whole screen is filled; Reduces lag & potential crasherinos
            pixmap.fillCircle(size / 6, size / 6, size / 6); // TODO: Make "crasherinos" a word
        else
            pixmap.fill();

        final Sprite spr = new Sprite(new Texture(pixmap));
        spr.setSize(size, size);
        spr.setPosition(x - size / 2, y - size / 2);
        spr.draw(batch);
    }
}
