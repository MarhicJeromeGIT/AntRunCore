package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Gauge 
{
	Texture gauge_empty;
	Texture gauge_grad;
	
	int gaugeX;
	int gaugeY;
	int gaugeW;
	int gaugeH;
	
	int gradX;
	int gradY;
	int gradW;
	int gradH;
	
	int nbSteps;
	int maxSteps = 20;
	Gauge()
	{
		gauge_empty = new Texture("gauge_empty.png");
		gauge_grad  = new Texture("gauge_grad.png");
		
		gaugeX = 650; //(>78*8)
		gaugeY = 20;
		gaugeW = 105;
		gaugeH = 250;
		
		gradX = 660;
		gradY = 25;
		gradW = 47;
		gradH = 200;
	
		nbSteps = 0;
	}
	
	public boolean isFull()
	{
		return nbSteps >= maxSteps;
	}
	public void reset()
	{
		nbSteps = 0;
	}
	public void addStep(int steps)
	{
		nbSteps += steps;
		nbSteps = Math.min( nbSteps, maxSteps );
	}
	public int getSteps()
	{
		return nbSteps;
	}
	
	void draw( SpriteBatch batch )
	{
		batch.draw( gauge_empty, gaugeX, gaugeY, gaugeW, gaugeH );
		batch.draw (gauge_grad,  gradX, gradY, 0, 0, gradW, gradH*nbSteps/maxSteps, 1,
		1, 0.0f, 0, 0, gradW, gradH*nbSteps/maxSteps, false,true);

	}
}
