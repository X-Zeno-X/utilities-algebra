package zeno.util.algebra.linear.alg.solvers;

import zeno.util.algebra.attempt4.linear.mat.Matrices;
import zeno.util.algebra.attempt4.linear.mat.Matrix;
import zeno.util.algebra.attempt4.linear.types.Symmetric;
import zeno.util.algebra.attempt4.linear.types.banded.Diagonal;
import zeno.util.algebra.attempt4.linear.types.banded.UpperTriangular;
import zeno.util.algebra.linear.alg.LinearSolver;
import zeno.util.algebra.linear.alg.factor.FCTCholesky;
import zeno.util.algebra.linear.error.DimensionError;
import zeno.util.algebra.linear.error.PosDefiniteError;
import zeno.util.algebra.linear.error.SymmetricError;
import zeno.util.tools.primitives.Doubles;
import zeno.util.tools.primitives.Floats;

/**
 * The {@code SLVCholesky} class solves exact linear systems using {@code Cholesky's method}.
 * This method is a variant of {@code Gauss elimination} that takes advantage of symmetric matrices
 * to cut computation time roughly in half. It decomposes a matrix {@code M = R*R}, where R
 * is an upper triangular matrix.
 * 
 * @author Zeno
 * @since Jul 6, 2018
 * @version 1.0
 * 
 * @see LinearSolver
 * @see FCTCholesky
 */
public class SLVCholesky implements FCTCholesky, LinearSolver
{
	private static final int ULPS = 3;
	
	
	private Float det;
	private Matrix inv, u;
	private Matrix mat, c;
	private int iError;
		
	/**
	 * Creates a new {@code SLVCholesky}.
	 * This algorithm requires a symmetric matrix.
	 * Otherwise, an exception will be thrown during the process.
	 * 
	 * @param m  a co�fficient matrix
	 * @see Matrix
	 */
	public SLVCholesky(Matrix m)
	{
		this(m, ULPS);
	}
	
	/**
	 * Creates a new {@code SLVCholesky}.
	 * This algorithm requires a symmetric matrix.
	 * Otherwise, an exception will be thrown during the process.
	 * 
	 * @param m  a co�fficient matrix
	 * @param ulps  an error margin
	 * @see Matrix
	 */
	public SLVCholesky(Matrix m, int ulps)
	{
		iError = ulps;
		mat = m;
	}

	@Override
	public <M extends Matrix> M solve(M b)
	{		
		// Matrix dimensions.
		int mRows = mat.Rows();
		int bRows = b.Rows();
				
		// If the right-hand side does not have the right dimensions...
		if(mRows != bRows)
		{
			// The linear system cannot be solved.
			throw new DimensionError("Solving a linear system requires compatible dimensions: ", mat, b);
		}
		
		// If no decomposition has been made yet...
		if(needsUpdate())
		{			
			// Perform Cholesky factorization.
			decompose();
		}
		
		
		// Compute the result through substitution.
		M x = (M) b.copy();
		x = new SLVTriangular(U().transpose(), iError).solve(x);
		x = new SLVTriangular(U(), iError).solve(x);
		return x;
	}

	
	@Override
	public void requestUpdate()
	{
		c = u = inv = null;
	}
	
	@Override
	public boolean needsUpdate()
	{
		return c == null;
	}
		
	@Override
	public float determinant()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform Cholesky's method.
			decompose();
		}
		
		return det;
	}
	
	@Override
	public Matrix inverse()
	{
		// If no inverse has been computed yet...
		if(inv == null)
		{
			// Compute the inverse through substitution.
			inv = solve(Matrices.identity(mat.Rows()));
			inv.setType(Symmetric.Type());
		}
		
		return inv;
	}
	
	
	private void choleskySimplified()
	{
		// Matrix row count.
		int rows = mat.Rows();
		
		double dVal = 1d;
		// For each row in the matrix...
		for(int i = 0; i < rows; i++)
		{
			dVal *= c.get(i, i);
			// Take the square root of the diagonal.
			c.set(Floats.sqrt(c.get(i, i)), i, i);
		}
		
		det = (float) dVal;
	}
	
	private void choleskysMethod()
	{
		// Matrix dimensions.
		int rows = mat.Rows();
		int cols = mat.Columns();
				
		double dVal = 1d;
		// For each row in the matrix...
		for(int k = 0; k < rows; k++)
		{
			// If the diagonal element is negative...
			if(c.get(k, k) < 0)
			{
				// ...the matrix is not positive definite.
				throw new PosDefiniteError(mat);
			}
			
			
			dVal *= c.get(k, k);
			// For each row below the diagonal...
			for(int i = k + 1; i < rows; i++)
			{
				double val = (double) c.get(k, i) / c.get(k, k);
				
				// Eliminate a column of superdiagonal values.
				for(int j = i; j < cols; j++)
				{
					c.set((float) (c.get(i, j) - c.get(k, j) * val), i, j);
				}
			}

			
			double vSqrt = Doubles.sqrt(c.get(k, k));
			// For each row below the diagonal...
			for(int i = k; i < cols; i++)
			{
				// Divide the root of the diagonal element.
				c.set((float) (c.get(k, i) / vSqrt), k, i);
			}
		}
		
		det = (float) dVal;
	}
	
	private void decompose()
	{
		// Copy the target matrix.
		c = mat.copy();
		
		
		// If the matrix is diagonal...
		if(mat.is(Diagonal.Type(), iError))
		{
			// Perform the simplified crout method.
			choleskySimplified();
			return;
		}
		
		// If the matrix is symmetric...
		if(mat.is(Symmetric.Type(), iError))
		{
			// Perform the full crout method.
			choleskysMethod();
			return;
		}
		

		// Otherwise, Cholesky's method is not applicable.
		throw new SymmetricError(mat);
	}

	
	@Override
	public Matrix U()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform Cholesky factorization.
			decompose();
		}
		
		// If matrix R hasn't been computed yet...
		if(u == null)
		{
			// Matrix dimensions.
			int rows = c.Rows();
			int cols = c.Columns();
			
			// Create the upper triangular matrix.
			u = Matrices.create(rows, cols);
			// Assign the type of the matrix U.
			u.setType(UpperTriangular.Type());
			
			// Copy the elements from the decomposed matrix.
			for(int i = 0; i < rows; i++)
			{
				for(int j = i; j < cols; j++)
				{
					u.set(c.get(i, j), i, j);
				}
			}
		}
		
		return u;
	}
}