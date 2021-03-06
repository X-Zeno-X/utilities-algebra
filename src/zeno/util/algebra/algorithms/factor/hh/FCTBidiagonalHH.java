package zeno.util.algebra.algorithms.factor.hh;

import zeno.util.algebra.algorithms.Determinant;
import zeno.util.algebra.algorithms.factor.FCTBidiagonal;
import zeno.util.algebra.linear.matrix.Matrices;
import zeno.util.algebra.linear.matrix.Matrix;
import zeno.util.algebra.linear.matrix.types.Square;
import zeno.util.algebra.linear.matrix.types.banded.upper.UpperBidiagonal;
import zeno.util.algebra.linear.matrix.types.orthogonal.Identity;
import zeno.util.algebra.linear.matrix.types.orthogonal.Orthogonal;
import zeno.util.algebra.linear.tensor.Tensors;
import zeno.util.algebra.linear.vector.Vector;
import zeno.util.tools.Floats;
import zeno.util.tools.Integers;

/**
 * The {@code FCTBidiagonalHH} class performs a {@code Bidiagonal} factorization.
 * This algorithm applies {@code Householder} transformations to induce zeroes in a matrix.
 * This is known as the Golub-Kahan Bidiagonalization.
 * 
 * @author Zeno
 * @since Jul 10, 2018
 * @version 1.0 
 * 
 * 
 * @see FCTBidiagonal
 * @see Determinant
 */
public class FCTBidiagonalHH implements Determinant, FCTBidiagonal
{
	private static final int ULPS = 3;
	
	
	private Float det;
	private Matrix mat, c;
	private Matrix b, u, v;
	private int iError;
	
	/**
	 * Creates a new {@code FCTBidiagonalHH}.
	 * 
	 * @param m  a co�fficient matrix
	 * 
	 * 
	 * @see Matrix
	 */
	public FCTBidiagonalHH(Matrix m)
	{
		this(m, ULPS);
	}
	
	/**
	 * Creates a new {@code FCTBidiagonalHH}.
	 * 
	 * @param m  a co�fficient matrix
	 * @param ulps  an error margin
	 * 
	 * 
	 * @see Matrix
	 */
	public FCTBidiagonalHH(Matrix m, int ulps)
	{
		iError = ulps;
		mat = m;
	}
	
	
	@Override
	public float determinant()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform bidiagonal factorization.
			decompose();
		}
		
		// If the matrix is not square...
		if(!mat.is(Square.Type()))
		{
			// A determinant cannot be calculated.
			throw new Tensors.DimensionError("Computing the determinant requires a square matrix: ", mat);
		}
		
		// If the matrix is not invertible...
		if(!isInvertible())
		{
			// It has zero determinant.
			return 0f;
		}
		
		return det;
	}
	
	@Override
	public boolean isInvertible()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform bidiagonal factorization.
			decompose();
		}
		
		// If the matrix is not square...
		if(!mat.is(Square.Type()))
		{
			// Invertibility cannot be determined.
			throw new Tensors.DimensionError("Invertibility requires a square matrix: ", mat);
		}
		
		return !Floats.isZero(det, iError);
	}	
	
	@Override
	public boolean needsUpdate()
	{
		return c == null;
	}

	@Override
	public void requestUpdate()
	{
		b = c = u = v = null;
	}
	
	private void houseHolder()
	{
		// Copy the target matrix.
		c = mat.copy();
				
				
		// Matrix dimensions.
		int rows = mat.Rows();
		int cols = mat.Columns();
				
		// Create the orthogonal matrices U, V.
		u = Matrices.identity(rows);
		v = Matrices.identity(cols);
		// Assign the type of matrices U, V.
		u.setOperator(Identity.Type());
		v.setOperator(Identity.Type());


		det = 1f;
		// For every row/column in the target matrix...
		for(int k = 0; k < Integers.min(rows, cols); k++)
		{
			// If the subdiagonal is not finished...
			if(k < rows - 1)
			{
				// Create the column reflection normal.
				Vector uk = c.Column(k);
				for(int i = 0; i < k; i++)
				{
					uk.set(0f, i);
				}
				
				// If the reflection is feasible...
				if(!Floats.isZero(uk.normSqr(), iError))
				{
					// Create the column reflection matrix.
					Matrix uhh = Matrices.houseHolder(uk, k);
					// Column reflect the target matrix.
					c = uhh.times(c);
					u = u.times(uhh);
					det = -det;
				}
			}
			
			// If the superdiagonal is not finished...
			if(k < cols - 2)
			{
				// Create the row reflection normal.
				Vector vk = c.Row(k);
				for(int i = 0; i <= k; i++)
				{
					vk.set(0f, i);
				}
				
				// If the reflection is feasible...
				if(!Floats.isZero(vk.normSqr(), iError))
				{
					// Create the row reflection matrix.
					Matrix vhh = Matrices.houseHolder(vk, k + 1);
					// Row reflect the target matrix.
					c = c.times(vhh);
					v = v.times(vhh);
					det = -det;
				}
			}
		}
	}
	
	private void decompose()
	{	
		// Matrix dimensions.
		int rows = mat.Rows();
		int cols = mat.Columns();

		// If the matrix is not upper bidiagonal...
		if(!mat.is(UpperBidiagonal.Type(), iError))
			// Perform Householder's method.
			houseHolder();
		else
		{
			// Otherwise, skip the Householder method.
			
			c = mat.copy();
			c.setOperator(UpperBidiagonal.Type());
			
			u = Matrices.identity(rows);
			v = Matrices.identity(cols);
			u.setOperator(Identity.Type());
			v.setOperator(Identity.Type());
			
			det = 1f;
		}

		// If the matrix is square...
		if(mat.is(Square.Type()))
		{
			// Compute the determinant.
			
			Vector d = c.Diagonal();			
			for(float val : d.Values())
			{
				det *= val;
			}
		}
	}
	
	
	@Override
	public Matrix B()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform bidiagonal factorization.
			decompose();
		}
		
		// If B has not been computed yet...
		if(b == null)
		{
			// Matrix dimensions.
			int rows = mat.Rows();
			int cols = mat.Columns();
			
			// Create the bidiagonal matrix B.
			b = Matrices.create(rows, cols);
			
			// Copy the elements from the decomposed matrix.
			for(int i = 0; i < rows; i++)
			{
				int jMin = Integers.max(i, 0);
				int jMax = Integers.min(i + 2, cols);
				
				for(int j = jMin; j < jMax; j++)
				{
					b.set(c.get(i, j), i, j);
				}
			}

		}
		
		return b;
	}

	@Override
	public Matrix U()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform bidiagonal factorization.
			decompose();
		}


		// Assign the type of matrix Q.
		if(u.is(Square.Type()))
		{
			u.setOperator(Orthogonal.Type());
		}

		return u;
	}

	@Override
	public Matrix V()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform bidiagonal factorization.
			decompose();
		}


		// Assign the type of matrix V.
		if(v.is(Square.Type()))
		{
			v.setOperator(Orthogonal.Type());
		}
		
		return v;
	}


}