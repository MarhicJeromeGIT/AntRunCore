package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameState 
{
	
	public GameStateManager stateManager;
	
	public GameState(GameStateManager manager)
	{
		stateManager = manager;
	}
	
	public abstract void draw(SpriteBatch batch);
	
	public abstract void update(float dt);
	
	public abstract void start();
	
	public void resize (int width, int height)
	{
		
	}
	
	public abstract void touchEvent( float x, float y, boolean pressed );

}
