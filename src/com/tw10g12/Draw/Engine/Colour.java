package com.tw10g12.Draw.Engine;

public class Colour 
{
	public static Colour Red = new Colour(1f,0,0,1);
	public static Colour Green = new Colour(0f,1,0,1);
	public static Colour Blue = new Colour(0f,0,1,1);
    public static Colour PleasantBlue = new Colour(12,113,183,255);
	public static Colour Black = new Colour(0f,0,0,1);
	public static Colour White = new Colour(1f,1,1,1);
	public static Colour Transparent = new Colour(0f,0,0,0);
	public static Colour Orange = new Colour(229, 145, 75, 255);
	protected float r,g,b,a;

    public Colour(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Colour(float r, float g, float b)
    {
        this(r,g,b,1);
    }

    public Colour(int r, int g, int b, int a)
    {
        r%=256;
        g%=256;
        b%=256;
        a%=256;

        this.r = r/255f;
        this.g = g/255f;
        this.b = b/255f;
        this.a = a/255f;
    }

    public Colour(int r, int g, int b)
    {
        this(r,g,b,255);
    }

    public Colour(Colour col, float a)
    {
        this.r = col.getR();
        this.g = col.getG();
        this.b = col.getB();
        this.a = a;
    }

    public Colour(Colour col, int a)
    {
        this.r = col.getR();
        this.g = col.getG();
        this.b = col.getB();
        this.a = a/255f;
    }

    public float getR()
    {
        return r;
    }

    public float getG()
    {
        return g;
    }

    public float getB()
    {
        return b;
    }

    public float getA()
    {
        return a;
    }
}

