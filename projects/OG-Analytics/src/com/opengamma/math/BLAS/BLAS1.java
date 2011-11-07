/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.BLAS;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * Provides the BLAS level 1 behaviour for the OG matrix library.
 * Massive amounts of overloading goes on, beware and only use if confident.
*  METHODS: DAXPY
 */
public class BLAS1 {

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DAXPY /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////
  /**
   * DAXPY performs the following vector operation
   *
   *  y := alpha*x + y
   *
   *  where alpha is scalar, x and y are vectors.
   *
   *  For speed, the method is overloaded such that simplified calls can be
   *  made when different parts of the DAXPY operation are not needed.
   *
   */

  /** Stateless versions */

  /**
   * Ensures that the inputs to DAXPY routines are sane when DAXPY is function(Vector,Vector).
   * @param v1 double[] is the vector to be tested (x)
   * @param v2 double[] is the vector to be tested (y)
   */
  public static void daxpyInputSanityChecker(double[] v1, double[] v2) {
    assertNotNull(v1); // check not null
    assertNotNull(v2); // check not null
    final int xlen = v1.length;
    final int ylen = v2.length;
    assertEquals(xlen, ylen); // going to struggle singleton addition unless the vectors are the same length
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a double[] vector
   * @param y a double[] vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(double[] x, double[] y) {
    daxpyInputSanityChecker(x, y);
    final int n = y.length;
    double[] tmp = new double[n];
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      tmp[i] = x[i] + y[i];
      tmp[i + 1] = x[i + 1] + y[i + 1];
      tmp[i + 2] = x[i + 2] + y[i + 2];
      tmp[i + 3] = x[i + 3] + y[i + 3];
      tmp[i + 4] = x[i + 4] + y[i + 4];
      tmp[i + 5] = x[i + 5] + y[i + 5];
      tmp[i + 6] = x[i + 6] + y[i + 6];
      tmp[i + 7] = x[i + 7] + y[i + 7];
      tmp[i + 8] = x[i + 8] + y[i + 8];
      tmp[i + 9] = x[i + 9] + y[i + 9];
      tmp[i + 10] = x[i + 10] + y[i + 10];
      tmp[i + 11] = x[i + 11] + y[i + 11];
      tmp[i + 12] = x[i + 12] + y[i + 12];
      tmp[i + 13] = x[i + 13] + y[i + 13];
      tmp[i + 14] = x[i + 14] + y[i + 14];
      tmp[i + 15] = x[i + 15] + y[i + 15];
    }
    for (int i = extra; i < n; i++) {
      tmp[i] = x[i] + y[i];
    }
    return tmp;
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a double[] vector
   * @param y a DoubleMatrix1D vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(double[] x, DoubleMatrix1D y) {
    return daxpy(x, y.getData());
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(DoubleMatrix1D x, double[] y) {
    return daxpy(x.getData(), y);
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(DoubleMatrix1D x, DoubleMatrix1D y) {
    return daxpy(x.getData(), y.getData());
  }

  /**
   * DAXPY returns:=alpha*x+y
   * @param alpha double
   * @param x a double[] vector
   * @param y a double[] vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(double alpha, double[] x, double[] y) {
    daxpyInputSanityChecker(x, y);
    final int n = y.length;
    double[] tmp = new double[n];
    if (Double.doubleToLongBits(alpha) == 0) { // short cut if alpha = 0, insanely stupid thing to do but might occur if coming from generated results
      System.arraycopy(y, 0, tmp, 0, n);
      return tmp;
    }
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      tmp[i] = alpha * x[i] + y[i];
      tmp[i + 1] = alpha * x[i + 1] + y[i + 1];
      tmp[i + 2] = alpha * x[i + 2] + y[i + 2];
      tmp[i + 3] = alpha * x[i + 3] + y[i + 3];
      tmp[i + 4] = alpha * x[i + 4] + y[i + 4];
      tmp[i + 5] = alpha * x[i + 5] + y[i + 5];
      tmp[i + 6] = alpha * x[i + 6] + y[i + 6];
      tmp[i + 7] = alpha * x[i + 7] + y[i + 7];
      tmp[i + 8] = alpha * x[i + 8] + y[i + 8];
      tmp[i + 9] = alpha * x[i + 9] + y[i + 9];
      tmp[i + 10] = alpha * x[i + 10] + y[i + 10];
      tmp[i + 11] = alpha * x[i + 11] + y[i + 11];
      tmp[i + 12] = alpha * x[i + 12] + y[i + 12];
      tmp[i + 13] = alpha * x[i + 13] + y[i + 13];
      tmp[i + 14] = alpha * x[i + 14] + y[i + 14];
      tmp[i + 15] = alpha * x[i + 15] + y[i + 15];
    }
    for (int i = extra; i < n; i++) {
      tmp[i] = alpha * x[i] + y[i];
    }
    return tmp;
  }

  /**
   * DAXPY returns:=x+y
   * @param alpha double
   * @param x a double[] vector
   * @param y a DoubleMatrix1D vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(double alpha, double[] x, DoubleMatrix1D y) {
    return daxpy(alpha, x, y.getData());
  }

  /**
   * DAXPY returns:=x+y
   * @param alpha double
   * @param x a DoubleMatrix1D vector
   * @param y a double[] vector
   * @return tmp double[] vector
   */
  public static double[] daxpy(double alpha, DoubleMatrix1D x, double[] y) {
    return daxpy(alpha, x.getData(), y);
  }

  /**
   * DAXPY returns:=x+y
   * @param alpha double
   * @param x a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   * @return tmp double[] vector
   */
  public double[] daxpy(double alpha, DoubleMatrix1D x, DoubleMatrix1D y) {
    return daxpy(alpha, x.getData(), y.getData());
  }

  /** Stateful versions - mangle memory! */

  /**
   * DAXPY simplified: y:=x+y
   * @param x a double[] vector
   * @param y a double[] vector
   */
  public static void daxpyInplace(double[] x, double[] y) {
    daxpyInputSanityChecker(x, y);
    final int n = y.length;
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      y[i] += x[i];
      y[i + 1] += x[i + 1];
      y[i + 2] += x[i + 2];
      y[i + 3] += x[i + 3];
      y[i + 4] += x[i + 4];
      y[i + 5] += x[i + 5];
      y[i + 6] += x[i + 6];
      y[i + 7] += x[i + 7];
      y[i + 8] += x[i + 8];
      y[i + 9] += x[i + 9];
      y[i + 10] += x[i + 10];
      y[i + 11] += x[i + 11];
      y[i + 12] += x[i + 12];
      y[i + 13] += x[i + 13];
      y[i + 14] += x[i + 14];
      y[i + 15] += x[i + 15];
    }
    for (int i = extra; i < n; i++) {
      y[i] += x[i];
    }
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a double[] vector
   * @param y a DoubleMatrix1D vector
   */
  public static void daxpyInplace(double[] x, DoubleMatrix1D y) {
    daxpyInplace(x, y.getData());
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a DoubleMatrix1D vector
   * @param y a double[] vector
   */
  public static void daxpyInplace(DoubleMatrix1D x, double[] y) {
    daxpyInplace(x.getData(), y);
  }

  /**
   * DAXPY simplified: returns:=x+y
   * @param x a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   */
  public static void daxpyInplace(DoubleMatrix1D x, DoubleMatrix1D y) {
    daxpyInplace(x.getData(), y.getData());
  }

  /**
   * DAXPY: y:=alpha*x+y
   * @param alpha double
   * @param x a double[] vector
   * @param y a double[] vector
   */
  public static void daxpyInplace(double alpha, double[] x, double[] y) {
    daxpyInputSanityChecker(x, y);
    final int n = y.length;
    if (Double.doubleToLongBits(alpha) == 0) { // short cut if alpha = 0, insanely stupid thing to do but might occur if coming from generated results
      return;
    }
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      y[i] += alpha * x[i];
      y[i + 1] += alpha * x[i + 1];
      y[i + 2] += alpha * x[i + 2];
      y[i + 3] += alpha * x[i + 3];
      y[i + 4] += alpha * x[i + 4];
      y[i + 5] += alpha * x[i + 5];
      y[i + 6] += alpha * x[i + 6];
      y[i + 7] += alpha * x[i + 7];
      y[i + 8] += alpha * x[i + 8];
      y[i + 9] += alpha * x[i + 9];
      y[i + 10] += alpha * x[i + 10];
      y[i + 11] += alpha * x[i + 11];
      y[i + 12] += alpha * x[i + 12];
      y[i + 13] += alpha * x[i + 13];
      y[i + 14] += alpha * x[i + 14];
      y[i + 15] += alpha * x[i + 15];
    }
    for (int i = extra; i < n; i++) {
      y[i] += alpha * x[i];
    }
  }

  /**
   * DAXPY: y:=alpha*x+y
   * @param alpha double
   * @param x a double[] vector
   * @param y a DoubleMatrix1D vector
   */
  public static void daxpyInplace(double alpha, double[] x, DoubleMatrix1D y) {
    daxpyInplace(alpha, x, y.getData());
  }

  /**
   * DAXPY: y:=alpha*x+y
   * @param alpha double
   * @param x a DoubleMatrix1D vector
   * @param y a double[] vector
   */
  public static void daxpyInplace(double alpha, DoubleMatrix1D x, double[] y) {
    daxpyInplace(alpha, x.getData(), y);
  }

  /**
   * DAXPY: y:=alpha*x+y
   * @param alpha double
   * @param x a DoubleMatrix1D vector
   * @param y a DoubleMatrix1D vector
   */
  public void daxpyInplace(double alpha, DoubleMatrix1D x, DoubleMatrix1D y) {
    daxpyInplace(alpha, x.getData(), y.getData());
  }

  ///////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////// DSCAL /////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * DSCAL performs the following vector operation
   *
   *  x := alpha*x
   *
   *  where alpha is scalar, x is a vector.
   *
   *  For speed, the method is overloaded such that simplified calls can be
   *  made when different parts of the DSCAL operation are not needed.
   *
   */

  /** Stateless versions */

  /**
   * DAXPY: returns:=alpha*x
   * @param alpha double
   * @param x a double[] vector
   * @return tmp a double[] vector
   */
  public static double[] dscal(double alpha, double[] x) {
    final int n = x.length;
    double[] tmp = new double[n];
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      tmp[i] = alpha * x[i];
      tmp[i + 1] = alpha * x[i + 1];
      tmp[i + 2] = alpha * x[i + 2];
      tmp[i + 3] = alpha * x[i + 3];
      tmp[i + 4] = alpha * x[i + 4];
      tmp[i + 5] = alpha * x[i + 5];
      tmp[i + 6] = alpha * x[i + 6];
      tmp[i + 7] = alpha * x[i + 7];
      tmp[i + 8] = alpha * x[i + 8];
      tmp[i + 9] = alpha * x[i + 9];
      tmp[i + 10] = alpha * x[i + 10];
      tmp[i + 11] = alpha * x[i + 11];
      tmp[i + 12] = alpha * x[i + 12];
      tmp[i + 13] = alpha * x[i + 13];
      tmp[i + 14] = alpha * x[i + 14];
      tmp[i + 15] = alpha * x[i + 15];
    }
    for (int i = extra; i < n; i++) {
      tmp[i] = alpha * x[i];
    }
    return tmp;
  }

  /**
   * DAXPY: returns:=alpha*x
   * @param alpha double
   * @param x a DoubleMatrix1D vector
   * @return a double[] vector
   */
  public static double[] dscal(double alpha, DoubleMatrix1D x) {
    return dscal(alpha, x.getData());
  }

  /** Stateful versions */

  /**
   * DAXPY: x:=alpha*x
   * @param alpha double
   * @param x a double[] vector
   */
  public static void dscalInplace(double alpha, double[] x) {
    final int n = x.length;
    final int extra = n - n % 16;
    final int ub = ((n / 16) * 16) - 1;
    // the induction (variable + loop unwind) common subexpression is actually spotted by the JIT and so
    // doesn't need to be spelled out which is a nice change
    for (int i = 0; i < ub; i += 16) {
      x[i] *= alpha;
      x[i + 1] *= alpha;
      x[i + 2] *= alpha;
      x[i + 3] *= alpha;
      x[i + 4] *= alpha;
      x[i + 5] *= alpha;
      x[i + 6] *= alpha;
      x[i + 7] *= alpha;
      x[i + 8] *= alpha;
      x[i + 9] *= alpha;
      x[i + 10] *= alpha;
      x[i + 11] *= alpha;
      x[i + 12] *= alpha;
      x[i + 13] *= alpha;
      x[i + 14] *= alpha;
      x[i + 15] *= alpha;
    }
    for (int i = extra; i < n; i++) {
      x[i] *= alpha;
    }
  }

  /**
   * DAXPY: x:=alpha*x
   * @param alpha double
   * @param x a DoubleMatrix1D vector
   */
  public static void dscalInplace(double alpha, DoubleMatrix1D x) {
    dscalInplace(alpha, x.getData());
  }


} // class end
