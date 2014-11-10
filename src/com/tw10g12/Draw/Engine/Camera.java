package com.tw10g12.Draw.Engine;

import com.tw10g12.Maths.Matrix4;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;

public abstract interface Camera
{
	public abstract Matrix4 getMatrix(double scale);
	public abstract Matrix4 getSkybox(double scale);
	
	public abstract double getRotX();
	public abstract void setRotX(double rot);
	
	public abstract double getRotY();
	public abstract void setRotY(double rot);
	
	public abstract double getRotZ();
	public abstract void setRotZ(double rot);
	
	public abstract Vector3 getCameraPos();
	public abstract void setCameraPos(Vector3 pos);
	
	public abstract void doMouseAction(Vector2 mouseDelta, MouseMode mode);
	public abstract void doZoomAction(double mouseDelta);
	
	public enum MouseMode
	{
		PAN,
		ROTATE
	}
}
