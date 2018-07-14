package zeno.util.algebra.linear.alg.factor.hh;

import zeno.util.algebra.attempt4.linear.mat.Matrices;
import zeno.util.algebra.attempt4.linear.mat.Matrix;
import zeno.util.algebra.attempt4.linear.types.Orthogonal;
import zeno.util.algebra.attempt4.linear.types.banded.UpperBidiagonal;
import zeno.util.algebra.attempt4.linear.types.orthogonal.Identity;
import zeno.util.algebra.attempt4.linear.types.size.Square;
import zeno.util.algebra.attempt4.linear.types.size.Tall;
import zeno.util.algebra.attempt4.linear.vec.Vector;
import zeno.util.algebra.linear.alg.factor.FCTBidiagonal;
import zeno.util.algebra.linear.error.DimensionError;
import zeno.util.tools.primitives.Integers;

/**
 * The {@code FCTBidiagonalHH} class performs a {@code Bidiagonal} factorization.
 * This algorithm applies {@code Householder} transformations to induce zeroes in a matrix.
 * 
 * @author Zeno
 * @since Jul 10, 2018
 * @version 1.0 
 * 
 * @see FCTBidiagonal
 */
public class FCTBidiagonalHH implements FCTBidiagonal
{
	private static final int ULPS = 3;
	
	
	private Matrix mat, c;
	private Matrix b, u, v;
	private int iError;
	
	/**
	 * Creates a new {@code FCTBidiagonalHH}.
	 * This algorithm requires a tall matrix.
	 * Otherwise, an exception will be thrown during the process.
	 * 
	 * @param m  a co�fficient matrix
	 * @see Matrix
	 */
	public FCTBidiagonalHH(Matrix m)
	{
		this(m, ULPS);
	}
	
	/**
	 * Creates a new {@code FCTBidiagonalHH}.
	 * This algorithm requires a tall matrix.
	 * Otherwise, an exception will be thrown during the process.
	 * 
	 * @param m  a co�fficient matrix
	 * @param ulps  an error margin
	 * @see Matrix
	 */
	public FCTBidiagonalHH(Matrix m, int ulps)
	{
		iError = ulps;
		mat = m;
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
		u = Matrices.identity(rows, rows);
		v = Matrices.identity(cols, cols);
		// Assign the type of matrices U, V.
		u.setType(Identity.Type());
		v.setType(Identity.Type());
		
		
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
				
				// Create the column reflection matrix.
				Matrix uhh = Matrices.houseHolder(uk, k);
				// Column reflect the target matrix.
				c = uhh.times(c);
				u = u.times(uhh);
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
				
				// Create the row reflection matrix.
				Matrix vhh = Matrices.houseHolder(vk, k + 1);
				// Row reflect the target matrix.
				c = c.times(vhh);
				v = v.times(vhh);
			}
		}
	}
	
	private void decompose()
	{	
		// If the matrix is not tall...
		if(!mat.is(Tall.Type()))
		{
			// A bidiagonal factorization cannot be computed.
			throw new DimensionError("Bidiagonal factorization requires a tall matrix: ", mat);
		}

		
		// Matrix dimensions.
		int rows = mat.Rows();
		int cols = mat.Columns();

		// If the matrix is upper bidiagonal...
		if(mat.is(UpperBidiagonal.Type(), iError))
		{
			// Skip the Householder method.
			c = mat.copy();
			c.setType(UpperBidiagonal.Type());
			u = Matrices.identity(rows, cols);
			v = Matrices.identity(cols, cols);
			u.setType(Identity.Type());
			v.setType(Identity.Type());
			return;
		}
		
		// Otherwise, perform Householder's method.
		houseHolder();
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
			// Reduce the matrix to square size.
			int size = Integers.min(rows, cols);
			
			
			// Create the bidiagonal matrix B.
			b = Matrices.create(size, size);
			// Assign the type of matrix B.
			b.setType(UpperBidiagonal.Type());

			
			// Copy the elements from the decomposed matrix.
			for(int i = 0; i < size; i++)
			{
				int jMin = Integers.max(i, 0);
				int jMax = Integers.min(i + 2, size);
				
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

		
		// Matrix dimensions.
		int uRows = u.Rows();
		int uCols = u.Columns();
		int mCols = mat.Columns();
		
		// If U is not in reduced form...
		if(uCols != mCols)
		{
			// Reduce the orthogonal U matrix.
			u = Matrices.resize(u, uRows, mCols);
		}


		// Assign the type of matrix Q.
		if(u.is(Square.Type()))
		{
			u.setType(Orthogonal.Type());
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
			v.setType(Orthogonal.Type());
		}
		
		return v;
	}
}