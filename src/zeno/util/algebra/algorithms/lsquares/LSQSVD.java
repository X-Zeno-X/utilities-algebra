package zeno.util.algebra.algorithms.lsquares;

import zeno.util.algebra.algorithms.Determinant;
import zeno.util.algebra.algorithms.LeastSquares;
import zeno.util.algebra.algorithms.RankReveal;
import zeno.util.algebra.algorithms.factor.FCTSingular;
import zeno.util.algebra.algorithms.factor.hh.FCTRBidiagonalHH;
import zeno.util.algebra.linear.matrix.Matrices;
import zeno.util.algebra.linear.matrix.Matrix;
import zeno.util.algebra.linear.matrix.types.Square;
import zeno.util.algebra.linear.matrix.types.banded.Diagonal;
import zeno.util.algebra.linear.matrix.types.dimensions.Tall;
import zeno.util.algebra.linear.matrix.types.orthogonal.Orthogonal;
import zeno.util.algebra.linear.tensor.Tensors;
import zeno.util.algebra.linear.vector.Vector;
import zeno.util.tools.Floats;
import zeno.util.tools.Integers;

/**
 * The {@code LSQSVD} class solves least squares linear systems using {@code SVD factorization}.
 * This method factorizes a matrix {@code M = UEV*} where U is a matrix with orthonormal columns,
 * E is a diagonal matrix of singular values, and V is an orthogonal matrix.
 * 
 * This algorithm is an implementation of the Demmel & Kahan zero-shift downward sweep.
 * It is a simplified version from the proposal in the paper, using only one algorithm and convergence type.
 * During every iteration Givens iterations are applied to the subdiagonal from top to bottom.
 *
 * @author Zeno
 * @since Jul 10, 2018
 * @version 1.0 
 * 
 * 
 * @see <a href="https://epubs.siam.org/doi/abs/10.1137/0911052">James Demmel & William Kahan, "Accurate singular values of bidiagonal matrices."</a>
 * @see LeastSquares
 * @see Determinant
 * @see FCTSingular
 * @see RankReveal
 */
public class LSQSVD implements Determinant, FCTSingular, LeastSquares, RankReveal
{
	private static final int MAX_SWEEPS = 1000;
	private static final int ULPS = 3;
	
	
	private Float det;
	private Integer rank;
	private Matrix mat, inv;
	private Matrix c, e, u, v;	
	private int iError;
	
	/**
	 * Creates a new {@code LSQSVD}.
	 * 
	 * @param m  a co�fficient matrix
	 * 
	 * 
	 * @see Matrix
	 */
	public LSQSVD(Matrix m)
	{
		this(m, ULPS);
	}
	
	/**
	 * Creates a new {@code LSQSVD}.
	 * 
	 * @param m  a co�fficient matrix
	 * @param ulps  an error margin
	 * 
	 * 
	 * @see Matrix
	 */
	public LSQSVD(Matrix m, int ulps)
	{
		iError = ulps;
		mat = m;
	}
	
	
	@Override
	public <M extends Matrix> M approx(M b)
	{
		// Matrix dimensions.
		int mRows = mat.Rows();
		int bRows = b.Rows();
				
		// If the right-hand side does not have the right dimensions...
		if(mRows != bRows)
		{
			// The least squares system cannot be solved.
			throw new Tensors.DimensionError("Solving a least squares system requires compatible dimensions: ", mat, b);
		}
		
		
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform SVD factorization.
			decompose();
		}
		

		// Compute the result through substitution.
		Matrix x = U().transpose();
		x = E().pseudoinverse().times(x.times(b));
		return (M) V().times(x);
	}
	
	
	@Override
	public Matrix pseudoinverse()
	{
		// If no inverse has been computed yet...
		if(inv == null)
		{		
			// Compute the inverse through substitution.
			inv = approx(Matrices.identity(mat.Rows()));
		}
		
		return inv;
	}
	
	@Override
	public boolean needsUpdate()
	{
		return c == null;
	}
	
	@Override
	public void requestUpdate()
	{
		c = e = u = v = inv = null;
	}
	
	private void decompose()
	{
		// If the matrix is not tall...
		if(!mat.is(Tall.Type()))
		{
			// Perform bidiagonal factorization on the transpose.
			FCTRBidiagonalHH bih = new FCTRBidiagonalHH(mat.transpose(), iError);
			u = bih.U(); c = bih.B(); v = bih.V();		
		}
		else
		{
			// Perform bidiagonal factorization on the matrix.
			FCTRBidiagonalHH bih = new FCTRBidiagonalHH(mat, iError);
			u = bih.U(); c = bih.B(); v = bih.V();
			
			// If the matrix is square...
			if(mat.is(Square.Type()))
			{
				// Compute the determinant.
				det = bih.determinant();
			}
		}
				
				
		int iCount = 0;
		// As long as the target matrix is not diagonalized...
		while(!c.is(Diagonal.Type()))
		{
			float lErr = 0f, rErr = 0f;
			// For every row/column in the target matrix...
			for(int i = 0; i < c.Columns() - 1; i++)
			{
				float rVal = c.get(i, i + 1);
				// Calculate the error margin for the right offdiagonal.
				if(i != 0)
					rErr = Floats.abs(c.get(i, i)) * rErr / (rErr + rVal);
				else
					rErr = Floats.abs(c.get(i, i));
				
				
				// If it is close enough to zero...
				if(Floats.isZero(rVal / rErr, iError))
					// Set it to zero entirely.
					c.set(0f, i, i + 1);
				else
				{
					// Create the right Givens matrix.
					Matrix rg = Matrices.rightGivens(c, i, i + 1);
					// Right rotate the target matrix.
					c = c.times(rg); v = v.times(rg);
				}
				
				
				float lVal = c.get(i + 1, i);
				// Calculate the error margin for the left offdiagonal.
				if(i != 0)
					lErr = Floats.abs(c.get(i, i)) * lErr / (lErr + lVal);
				else
					lErr = Floats.abs(c.get(i, i));
				
				
				// If it is close enough to zero...
				if(Floats.isZero(c.get(i + 1, i), iError))
					// Set it to zero entirely.
					c.set(0f, i + 1, i);
				else
				{
					// Create the left Givens matrix.
					Matrix lg = Matrices.leftGivens(c, i + 1, i);
					// Left rotate the target matrix.
					c = lg.times(c); u = u.times(lg.transpose());
				}
			}
						
			iCount++;
			// Prevent an infinite loop.
			if(iCount > MAX_SWEEPS)
			{
				break;
			}
		}
		
		
		// Matrix dimensions.
		int size = c.Columns();
		
		// The singular values have to be positive.
		Matrix mSign = Matrices.identity(size);
		mSign.setOperator(Diagonal.Type());
		for(int i = 0; i < size; i++)
		{
			if(c.get(i, i) < 0)
			{
				mSign.set(-1f, i, i);
			}
		}
		
		c = c.times(mSign);
		v = v.times(mSign);
		
		
		// Assign the type of matrix U.
		if(u.is(Square.Type()))
		{
			u.setOperator(Orthogonal.Type());
		}
		
		// Assign the type of matrix V.
		if(v.is(Square.Type()))
		{
			v.setOperator(Orthogonal.Type());
		}
	}

	
	@Override
	public float determinant()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform SVD factorization.
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
			// Perform SVD factorization.
			decompose();
		}
		
		// If the matrix is not square...
		if(!mat.is(Square.Type()))
		{
			// Invertibility cannot be determined.
			throw new Tensors.DimensionError("Invertibility requires a square matrix: ", mat);
		}
		
		
		// Matrix dimensions.
		int cols = mat.Columns();
		// The matrix must be full rank.	
		return cols == rank();
	}
		
	
	@Override
	public Matrix E()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform SVD factorization.
			decompose();
		}

		
		// If matrix E hasn't been computed yet...
		if(e == null)
		{
			// Matrix dimensions.
			int cols = c.Columns();
			
			// Create the diagonal matrix.
			e = Matrices.create(cols, cols);
			// Assign the type of matrix E.
			e.setOperator(Diagonal.Type());
			
			
			// Copy the elements from the decomposed matrix.
			for(int i = 0; i < cols; i++)
			{
				float val = Floats.abs(c.get(i, i));
				e.set(val, i, i);
			}	
		}
		
		return e;
	}
	
	@Override
	public Matrix U()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform SVD factorization.
			decompose();
		}
		
		
		// If the matrix is not tall...
		if(!mat.is(Tall.Type()))
		{
			// Return the matrix V.
			return v;
		}
		
		// Return the matrix U.
		return u;
	}
	
	@Override
	public Matrix V()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform SVD factorization.
			decompose();
		}
		
		
		// If the matrix is not tall...
		if(!mat.is(Tall.Type()))
		{
			// Return the matrix U.
			return u;
		}
		
		// Return the matrix V.
		return v;
	}
	
	@Override
	public int rank()
	{
		// If no decomposition has been made yet...
		if(needsUpdate())
		{
			// Perform SVD factorization.
			decompose();
		}
		
		// If rank hasn't been computed yet...
		if(rank == null)
		{
			// Matrix dimensions.
			int cols = mat.Columns();
			int rows = mat.Rows();
			
			// Calculate the singular value tolerance.
			float eTol = Integers.max(rows, cols) * Floats.nextEps(condition());
						
			rank = 0;
			// Loop over singular values to find rank.
			Vector sv = SingularValues();
			for(int i = 0; i < sv.Size(); i++)
			{
				if(Floats.isZero(sv.get(i), (int) eTol))
				{
					break;
				}
				
				rank++;
			}
		}
		
		return rank;
	}
}