package zeno.util.algebra.linear.matrix.functions.identity;

import zeno.util.algebra.linear.matrix.Matrix;
import zeno.util.tools.Integers;
import zeno.util.tools.patterns.properties.operator.Operation;

/**
 * The {@code IdentityDotProduct} class defines the identity dot product.
 * The dot product in this case is simplified to the matrix trace.
 *
 * @author Zeno
 * @since Jul 13, 2018
 * @version 1.0
 * 
 * 
 * @see Operation
 * @see Float
 */
public class IdentityDotProduct implements Operation<Float>
{
	private Matrix m;
	
	/**
	 * Creates a new {@code IdentityDotProduct}.
	 * 
	 * @param m  the  matrix to multiply
	 * 
	 * 
	 * @see Matrix
	 */
	public IdentityDotProduct(Matrix m)
	{
		this.m = m;
	}
	

	@Override
	public Float result()
	{
		return m.Trace();
	}
	
	@Override
	public int cost()
	{
		int rows = m.Rows();
		int cols = m.Columns();
	
		if(rows != cols)
		{
			return Integers.MAX_VALUE;
		}
		
		
		return rows - 1;
	}
}