package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	
	OrthographicCamera camera;
	public AntRun game;
	
	Rectangle rect;
	Texture img;
	
	MyInputProcessor inputProcessor;
	
	GameStateManager gameStateManager;
	
	@Override
	public void resize (int width, int height)
	{
		gameStateManager.getCurrentState().resize( width, height );
	}
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		rect = new Rectangle();
		rect.x = 100;
		rect.y = 100;
		rect.width = 64;
		rect.height = 64;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false,800,480);
		game = new AntRun();
		
		gameStateManager = new GameStateManager(this);
		gameStateManager.pushState( new InGameState(gameStateManager, game));
		//gameStateManager.pushState( new LostGameState(gameStateManager, game));
	
		setEventListener();
	}
	
	public void setEventListener()
	{
		MyInputProcessor inputProcessor = new MyInputProcessor(this);
		Gdx.input.setInputProcessor(inputProcessor);	
	}
	
	public void update(float dt)
	{
		gameStateManager.getCurrentState().update(dt);
	}
	
	@Override
	public void render () 
	{
		float dt = Gdx.graphics.getDeltaTime();
		update(dt);
		
		Gdx.gl.glClearColor(0,0,0.2f,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		gameStateManager.getCurrentState().draw(batch);
		batch.end();
	}
	
}
