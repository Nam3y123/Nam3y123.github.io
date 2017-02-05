package com.hyperforce.renegade;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class Secret extends ApplicationAdapter {
    private Sound mus;
    private int duration;
    private long id;

    @Override
    public void create () {
        mus = Gdx.audio.newSound(Gdx.files.internal("IDKSR/Song.mp3"));
        id = mus.play();
        duration = 5;
    }

    public void render() {
        if((float)Math.pow(3, duration / 1000f) - 0.95f < 100f)
            mus.setPitch(id, ((float)Math.pow(3, duration / 1000f) - 0.95f));
        duration++;
    }

    @Override
    public void dispose() {
        mus.dispose();
        super.dispose();
    }
}
