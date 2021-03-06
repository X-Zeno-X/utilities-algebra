package zeno.util.algebra.linear.vector.fixed;

import zeno.util.algebra.linear.vector.Vector;

/**
 * The {@code Vector4} class defines a four-dimensional vector.
 *
 * @author Zeno
 * @since Mar 21, 2016
 * @version 1.0
 * 
 * 
 * @see Vector
 */
public class Vector4 extends Vector
{
	/**
	 * Defines the four-dimensional x-axis unit vector.
	 */
    public static final Vector4 X_AXIS = new Vector4(1, 0, 0, 0);
    /**
     * Defines the four-dimensional y-axis unit vector.
     */
    public static final Vector4 Y_AXIS = new Vector4(0, 1, 0, 0);
    /**
     * Defines the four-dimensional z-axis unit vector.
     */
    public static final Vector4 Z_AXIS = new Vector4(0, 0, 1, 0);
    /**
     * Defines the four-dimensional w-axis unit vector.
     */
    public static final Vector4 W_AXIS = new Vector4(0, 0, 0, 1);
	
    
    
    /**
	 * Creates a new {@code Vector4}.
	 * 
	 * @param x  the vector's x-co�rdinate
	 * @param y  the vector's y-co�rdinate
	 * @param z  the vector's z-co�rdinate
	 * @param w  the vector's w-co�rdinate
	 */
	public Vector4(float x, float y, float z, float w)
	{
		super(4);
		setX(x);
		setY(y);
		setZ(z);
		setW(w);
	}
		
	/**
	 * Creates a new {@code Vector4}.
	 * 
	 * @param val  a co�rdinate value
	 */
	public Vector4(float val)
	{
		this(val, val, val, val);
	}
	
	/**
	 * Creates a new {@code Vector4}.
	 */
	public Vector4()
	{
		super(4);
	}

	
	/**
	 * Changes the x-co�rdinate of the {@code Vector4}.
	 * 
	 * @param x  a new x-co�rdinate
	 */
	public void setX(float x)
	{
		set(x, 0);
	}
	
	/**
	 * Changes the y-co�rdinate of the {@code Vector4}.
	 * 
	 * @param y  a new y-co�rdinate
	 */
	public void setY(float y)
	{
		set(y, 1);
	}
	
	/**
	 * Changes the z-co�rdinate of the {@code Vector4}.
	 * 
	 * @param z  a new z-co�rdinate
	 */
	public void setZ(float z)
	{
		set(z, 2);
	}
	
	/**
	 * Changes the w-co�rdinate of the {@code Vector4}.
	 * 
	 * @param w  a new w-co�rdinate
	 */
	public void setW(float w)
	{
		set(w, 3);
	}
		
	/**
	 * Returns the x-co�rdinate of the {@code Vector4}.
	 * 
	 * @return  the vector's x-co�rdinate
	 */
	public float X()
	{
		return get(0);
	}
	
	/**
	 * Returns the y-co�rdinate of the {@code Vector4}.
	 * 
	 * @return  the vector's y-co�rdinate
	 */
	public float Y()
	{
		return get(1);
	}
	
	/**
	 * Returns the z-co�rdinate of the {@code Vector4}.
	 * 
	 * @return  the vector's z-co�rdinate
	 */
	public float Z()
	{
		return get(2);
	}
	
	/**
	 * Returns the w-co�rdinate of the {@code Vector4}.
	 * 
	 * @return  the vector's w-co�rdinate
	 */
	public float W()
	{
		return get(3);
	}

	
	/**
	 * Returns the {@code Vector}'s subtraction.
	 * 
	 * @param v  a vector to subtract
	 * @return  the vector difference
	 */
	public Vector4 minus(Vector4 v)
	{
		return (Vector4) super.minus(v);
	}
	
	/**
	 * Returns the {@code Vector}'s sum.
	 * 
	 * @param v  a vector to add
	 * @return  the vector sum
	 */
	public Vector4 plus(Vector4 v)
	{
		return (Vector4) super.plus(v);
	}
	
	
	@Override
	public Vector4 normalize()
	{
		return (Vector4) super.normalize();
	}
		
	@Override
	public Vector4 times(float v)
	{
		return (Vector4) super.times(v);
	}
	
	@Override
	public Vector4 instance()
	{
		return new Vector4();
	}
	
	@Override
	public Vector4 copy()
	{
		return (Vector4) super.copy();
	}
}