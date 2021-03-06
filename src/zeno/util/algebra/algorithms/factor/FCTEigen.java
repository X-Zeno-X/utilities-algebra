package zeno.util.algebra.algorithms.factor;

import zeno.util.algebra.linear.matrix.Matrix;
import zeno.util.tools.patterns.manipulators.Adaptable;

/**
 * The {@code FCTEigen} interface defines an algorithm that performs eigenvalue factorization.
 * Every diagonalizable matrix can be decomposed as {@code M = QEQ*} where Q is an orthogonal
 * matrix, and E is a diagonal matrix of eigenvalues.
 *
 * @author Zeno
 * @since Jul 10, 2018
 * @version 1.0
 * 
 * 
 * @see Adaptable
 */
public interface FCTEigen extends Adaptable
{
	/**
	 * Returns the diagonal matrix E from the {@code FCTEigen}.
	 * 
	 * @return  the diagonal matrix E
	 * 
	 * 
	 * @see Matrix
	 */
	public abstract Matrix E();
	
	/**
	 * Returns the orthogonal matrix Q from the {@code FCTEigen}.
	 * 
	 * @return  the orthogonal matrix Q
	 * 
	 * 
	 * @see Matrix
	 */
	public abstract Matrix Q();
}