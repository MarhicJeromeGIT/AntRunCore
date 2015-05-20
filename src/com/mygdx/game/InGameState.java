package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class InGameState extends GameState
{
	AntRun game;
	
	boolean started;
	float timeBeforeStart;
	InGameState( GameStateManager manager, AntRun g )
	{
		super(manager);
		game = g;
		started = false;
		timeBeforeStart = 3.0f;
	}
	
	@Override
	public void draw(SpriteBatch batch) 
	{		
		game.draw(batch);
	}
	
	@Override
	public void start()
	{
		started = false;
		timeBeforeStart = 3.0f;
		game.restartGame();
		
	}
	
	@Override
	public void update(float dt)
	{
		if( !started )
		{
			timeBeforeStart -= dt;
			if( timeBeforeStart <= 0.0f )
			{
				game.startAnt();
				started = true;
			}	
		}
		else // started
		{
			game.update(dt);
		}
		
		if( game.died )
		{
			// restart the game :
			//start();
			stateManager.pushState( new LostGameState(stateManager, game) );
		}
	}
	
}
