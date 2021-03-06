/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2016  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.sosy_lab.java_smt.test;

import static com.google.common.truth.Truth.assert_;
import static com.google.common.truth.TruthJUnit.assume;
import static org.sosy_lab.java_smt.test.ProverEnvironmentSubject.proverEnvironment;

import com.google.common.truth.FailureStrategy;
import com.google.common.truth.SubjectFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.junit.After;
import org.junit.Before;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.ConfigurationBuilder;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.ArrayFormulaManager;
import org.sosy_lab.java_smt.api.BasicProverEnvironment;
import org.sosy_lab.java_smt.api.BitvectorFormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FloatingPointFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager;
import org.sosy_lab.java_smt.api.RationalFormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.UFManager;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Abstract base class with helpful utilities for writing tests
 * that use an SMT solver.
 * It instantiates and closes the SMT solver before and after each test,
 * and provides fields with direct access to the most relevant instances.
 *
 * <p>To run the tests using all available solvers, add the following code to your class:
 * <pre>
 * <code>
 *  {@literal @}Parameters(name="{0}")
 *  public static List{@literal <Object[]>} getAllSolvers() {
 *    return allSolversAsParameters();
 *  }
 *
 *  {@literal @}Parameter(0)
 *  public Solvers solver;
 *
 *  {@literal @}Override
 *  protected Solvers solverToUse() {
 *    return solver;
 *  }
 * </code>
 * </pre>
 *
 * {@link #assertThatFormula(BooleanFormula)} can be used to easily write assertions
 * about formulas using Truth.
 *
 * <p>Test that rely on a theory that not all solvers support
 * should call one of the {@code require} methods at the beginning.
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "test code")
public abstract class SolverBasedTest0 {

  protected Configuration config;
  protected final LogManager logger = LogManager.createTestLogManager();

  protected SolverContextFactory factory;
  protected SolverContext context;
  protected FormulaManager mgr;
  protected BooleanFormulaManager bmgr;
  protected UFManager fmgr;
  protected IntegerFormulaManager imgr;
  protected @Nullable RationalFormulaManager rmgr;
  protected @Nullable BitvectorFormulaManager bvmgr;
  protected @Nullable QuantifiedFormulaManager qmgr;
  protected @Nullable ArrayFormulaManager amgr;
  protected @Nullable FloatingPointFormulaManager fpmgr;
  protected ShutdownManager shutdownManager = ShutdownManager.create();

  protected ShutdownNotifier shutdownNotifierToUse() {
    return shutdownManager.getNotifier();
  }

  /**
   * Return the solver to use in this test.
   * The default is SMTInterpol because it's the only solver guaranteed on all platforms.
   * Overwrite to specify a different solver.
   */
  protected Solvers solverToUse() {
    return Solvers.SMTINTERPOL;
  }

  protected ConfigurationBuilder createTestConfigBuilder() {
    return Configuration.builder().setOption("solver.solver", solverToUse().toString());
  }

  @Before
  public final void initSolver() throws Exception {
    config = createTestConfigBuilder().build();

    factory = new SolverContextFactory(config, logger, shutdownNotifierToUse());
    context = factory.generateContext();
    mgr = context.getFormulaManager();

    fmgr = mgr.getUFManager();
    bmgr = mgr.getBooleanFormulaManager();
    imgr = mgr.getIntegerFormulaManager();
    try {
      rmgr = mgr.getRationalFormulaManager();
    } catch (UnsupportedOperationException e) {
      rmgr = null;
    }
    try {
      bvmgr = mgr.getBitvectorFormulaManager();
    } catch (UnsupportedOperationException e) {
      bvmgr = null;
    }
    try {
      qmgr = mgr.getQuantifiedFormulaManager();
    } catch (UnsupportedOperationException e) {
      qmgr = null;
    }
    try {
      amgr = mgr.getArrayFormulaManager();
    } catch (UnsupportedOperationException e) {
      amgr = null;
    }
    try {
      fpmgr = mgr.getFloatingPointFormulaManager();
    } catch (UnsupportedOperationException e) {
      fpmgr = null;
    }
  }

  @After
  public final void closeSolver() throws Exception {
    context.close();
  }

  /**
   * Skip test if the solver does not support rationals.
   */
  protected final void requireRationals() {
    assume()
        .withFailureMessage("Solver " + solverToUse() + " does not support the theory of rationals")
        .that(rmgr)
        .isNotNull();
  }

  /**
   * Skip test if the solver does not support bitvectors.
   */
  protected final void requireBitvectors() {
    assume()
        .withFailureMessage(
            "Solver " + solverToUse() + " does not support the theory of bitvectors")
        .that(bvmgr)
        .isNotNull();
  }
  /**
   * Skip test if the solver does not support quantifiers.
   */
  protected final void requireQuantifiers() {
    assume()
        .withFailureMessage("Solver " + solverToUse() + " does not support quantifiers")
        .that(qmgr)
        .isNotNull();
  }

  /**
   * Skip test if the solver does not support arrays.
   */
  protected final void requireArrays() {
    assume()
        .withFailureMessage("Solver " + solverToUse() + " does not support the theory of arrays")
        .that(amgr)
        .isNotNull();
  }

  protected final void requireFloats() {
    assume()
        .withFailureMessage("Solver " + solverToUse() + " does not support the theory of floats")
        .that(fpmgr)
        .isNotNull();
  }

  /**
   * Skip test if the solver does not support optimization.
   */
  protected final void requireOptimization() {

    // TODO: re-enable opti-mathsat, currently it has too many bugs.
    assume().that(solverToUse()).isNotEqualTo(Solvers.MATHSAT5);

    try {
      context.newOptimizationProverEnvironment().close();
    } catch (UnsupportedOperationException e) {
      assume()
          .withFailureMessage("Solver " + solverToUse() + " does not support optimization")
          .that(e)
          .isNull();
    }
  }

  protected final void requireInterpolation() {
    try {
      context.newProverEnvironmentWithInterpolation().close();
    } catch (UnsupportedOperationException e) {
      assume()
          .withFailureMessage("Solver " + solverToUse() + " does not support interpolation")
          .that(e)
          .isNull();
    }
  }

  @Deprecated
  protected final void requireFalse(String failureMessage) {
    assume().withFailureMessage(failureMessage).fail();
  }

  /**
   * Use this for checking assertions about BooleanFormulas with Truth:
   * <code>assertThatFormula(formula).is...()</code>.
   */
  protected final BooleanFormulaSubject assertThatFormula(BooleanFormula formula) {
    return assert_().about(BooleanFormulaSubject.forSolver(context)).that(formula);
  }

  /**
   * Use this for checking assertions about ProverEnvironments with Truth:
   * <code>assertThatEnvironment(stack).is...()</code>.
   */
  protected final ProverEnvironmentSubject assertThatEnvironment(BasicProverEnvironment<?> prover) {
    return assert_().about(proverEnvironment()).that(prover);
  }

  @Deprecated
  protected final JavaOptionalSubject assertThatOptional(Optional<?> pOptional) {
    return assert_()
        .about(
            new SubjectFactory<JavaOptionalSubject, Optional<?>>() {
              @Override
              public JavaOptionalSubject getSubject(
                  FailureStrategy pFailureStrategy, Optional<?> pOptional) {
                return new JavaOptionalSubject(pFailureStrategy, pOptional);
              }
            })
        .that(pOptional);
  }
}
