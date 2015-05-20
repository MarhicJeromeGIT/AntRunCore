package com.mygdx.game;

import java.util.Stack;

public class GameStateManager 
{
	Stack<GameState> gameStateStack;
	MyGdxGame game;
	
	GameStateManager(MyGdxGame g)
	{
		game = g;
		gameStateStack = new Stack<GameState>();
	}
	
	GameState getCurrentState()
	{
		return gameStateStack.peek();
	}
	
	void pushState( GameState state )
	{
		gameStateStack.push(state);
		state.start();
	}
	
	void popState()
	{
		gameStateStack.pop();
		assert( !gameStateStack.empty() );
		gameStateStack.peek().start();
		
		// Maybe I need to reset the event listener
		game.setEventListener();
	}
}
