package com.tw10g12.Draw.Engine;

import com.tw10g12.Maths.Matrix4;
import com.tw10g12.Maths.Vector2;
import com.tw10g12.Maths.Vector3;
import com.tw10g12.Maths.Vector4;

public class OrbitCamera implements Camera
{
	double rotX;
	double rotY;
	double rotZ;
	double distance;
	
	Vector3 lookAt;
	public Vector3 forward;
	public Vector3 up;
	public Vector3 right;
	
	public OrbitCamera(double distance)
	{
		this(new Vector3(0,0,0), distance, 0, 0, 0);
	}
	
	public OrbitCamera(Vector3 lookAt, double distance)
	{
		this(lookAt, distance, 0,0,0);
	}
	
	public OrbitCamera(Vector3 lookAt, double distance, double rotX, double rotY, double rotZ)
	{
		this.lookAt = lookAt;
		this.distance = distance;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
        updateCamera();
	}
	
	@Override
	public Matrix4 getMatrix(double scale)
	{
		Matrix4 mdl = Matrix4.getTranslationMatrix(lookAt.multiply(-1));
		mdl = Matrix4.getRotationY(getRotY()/180.0*Math.PI).multiply(mdl);
		mdl = Matrix4.getRotationX(getRotX()/180.0 * Math.PI).multiply(mdl);
		mdl = Matrix4.getRotationZ(getRotZ()/180.0 * Math.PI).multiply(mdl);
		mdl = Matrix4.getTranslationMatrix(new Vector3(0,0,-distance)).multiply(mdl);
		mdl = Matrix4.getScaleMatrix(scale).multiply(mdl);
		
		//Scale 
		
		return mdl;
	}
	
	@Override
	public Matrix4 getSkybox(double scale)
	{
		Matrix4 mdl = Matrix4.getRotationY(getRotY()/180.0*Math.PI);
		mdl = Matrix4.getRotationX(getRotX()/180.0 * Math.PI).multiply(mdl);
		mdl = Matrix4.getRotationZ(getRotZ()/180.0 * Math.PI).multiply(mdl);
		mdl = Matrix4.getScaleMatrix(scale).multiply(mdl);
		return mdl;
	}
	
	@Override
	public double getRotX()
	{
		return rotX;
	}
	@Override
	public void setRotX(double rot)
	{
		this.rotX = rot;
		updateCamera();
	}
	
	@Override
	public double getRotY()
	{
		return rotY;
	}
	@Override
	public void setRotY(double rot)
	{
		this.rotY = rot;
		updateCamera();
	}
	
	@Override
	public double getRotZ()
	{
		return rotZ;
	}
	@Override
	public void setRotZ(double rot)
	{
		this.rotZ = rot;
		updateCamera();
	}
	
	@Override
	public void setCameraPos(Vector3 pos)
	{
		lookAt = pos;
		updateCamera();
	}
	@Override
	public Vector3 getCameraPos()
	{
		return lookAt;
	}

    public Vector3 getActualCameraPos() { return lookAt.subtract(forward.multiply(distance)); }

    public double getDistance() { return distance; }
	public void setDistance(double dist)
	{
		this.distance = dist;
	}
	
	void updateCamera()
	{
		forward = new Vector3(0,0,-1);
		Matrix4 rot = Matrix4.getRotationX(-getRotX()/180.0 * Math.PI);
		rot = Matrix4.getRotationY(-getRotY()/180.0*Math.PI).multiply(rot);
		rot = Matrix4.getRotationZ(-getRotZ()/180.0 * Math.PI).multiply(rot);
		forward = rot.multiply(new Vector4(forward, 0)).getXYZ().normalise();
		double roll = -getRotZ()/180.0 * Math.PI;
		up = new Vector3(Math.sin(roll), -Math.cos(roll), 0).normalise();
		right = up.cross(forward).normalise();
		up = right.cross(forward).normalise();
	}

	@Override
	public void doMouseAction(Vector2 mouseDelta, MouseMode mode)
	{
		
		if(mode == MouseMode.PAN)
		{
			double scrollAmount = 0.001 * distance;
			mouseDelta = mouseDelta.multiply(scrollAmount);
			Vector3 newCamera = getCameraPos().add(right.multiply(-mouseDelta.getX())).add(up.multiply(mouseDelta.getY()));
			//newCamera.setZ(Math.min(-10, newCamera.getZ()));
			//newCamera.setY(Math.max(0, newCamera.getY()));
			setCameraPos(newCamera);
		}
		else if(mode == MouseMode.ROTATE)
		{
			double newRotX = getRotX()+mouseDelta.getY()/10.0;
			double newRotY = getRotY()+mouseDelta.getX()/10.0;
			newRotX = Math.max(-90, newRotX);
			newRotX = Math.min(90, newRotX);
			if(newRotY < 0) newRotY += 360;
			if(newRotY >= 360) newRotY -= 360;
			setRotX(newRotX);
			setRotY(newRotY);
		}
	}
	
	@Override
	public void doZoomAction(double mouseDelta)
	{
		double scrollAmount = 0.05*distance;
		double zoomDelta = mouseDelta * scrollAmount;
		distance += zoomDelta;
		/*Vector3 newCamera = getCameraPos().add(new Vector3(0, 0, mouseDelta));
		newCamera.setZ(Math.min(-10, newCamera.getZ()));
		setCameraPos(newCamera);*/
	}
}
