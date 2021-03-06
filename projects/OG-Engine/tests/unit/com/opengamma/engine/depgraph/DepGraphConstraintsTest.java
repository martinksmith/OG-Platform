/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.resolver.FunctionPriority;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Tests the dependency graph building with requirement constraints
 */
@Test
public class DepGraphConstraintsTest extends AbstractDependencyGraphBuilderTest {

  public void functionByName1() {
    final DepGraphTestHelper helper = helper();
    final MockFunction fn = helper.addFunctionProducing2();
    final MockFunction fnBeta = helper.addFunctionProducing2Beta();
    DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
      @Override
      public int getPriority(CompiledFunctionDefinition function) {
        if (function.getFunctionDefinition().getUniqueId().equals(fnBeta.getUniqueId())) {
          return -1;
        }
        return 0;
      }
    });
    builder.addTarget(helper.getRequirement2());
    builder.addTarget(helper.getRequirement2Beta());
    final DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn, fnBeta);
  }

  public void functionByName2() {
    final DepGraphTestHelper helper = helper();
    helper.addFunctionProducing2();
    final MockFunction fnBeta = helper.addFunctionProducing2Beta();
    DependencyGraphBuilder builder = helper.getBuilder(new FunctionPriority() {
      @Override
      public int getPriority(CompiledFunctionDefinition function) {
        if (function.getFunctionDefinition().getUniqueId().equals(fnBeta.getUniqueId())) {
          return 1;
        }
        return 0;
      }
    });
    builder.addTarget(helper.getRequirement2Beta());
    builder.addTarget(helper.getRequirement2Beta());
    final DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fnBeta);
  }

  public void functionByNameMissing() {
    final DepGraphTestHelper helper = helper();
    helper.addFunctionProducing2();
    final DependencyGraphBuilder builder = helper.getBuilder(null);
    expectCompletion(builder, builder.getContext().resolveRequirement(helper.getRequirement2(), null));
    expectFailure(builder, builder.getContext().resolveRequirement(helper.getRequirement2Beta(), null));
  }

  public void functionWithProperty() {
    final DepGraphTestHelper helper = helper();
    final MockFunction fn1 = helper.addFunctionProducing(helper.getValue1Foo());
    helper.addFunctionRequiringProducing(helper.getRequirement1Bar(), helper.getValue2Bar());
    final MockFunction fn2b = helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
    final DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement2Foo());
    final DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    graph.removeUnnecessaryValues();
    assertGraphContains(graph, fn1, fn2b);
  }

  public void functionWithPropertyMissing() {
    final DepGraphTestHelper helper = helper();
    helper.addFunctionProducing(helper.getValue1Foo());
    helper.addFunctionRequiringProducing(helper.getRequirement1Bar(), helper.getValue2Bar());
    helper.addFunctionRequiringProducing(helper.getRequirement1Foo(), helper.getValue2Foo());
    final DependencyGraphBuilder builder = helper.getBuilder(null);
    expectFailure(builder, builder.getContext().resolveRequirement(helper.getRequirement2Bar(), null));
  }

  public void outputBasedRequirements() {
    final DepGraphTestHelper helper = helper();
    helper.addFunctionProducing(helper.getValue2Foo());
    final MockFunction fn2Bar = helper.addFunctionProducing(helper.getValue2Bar());
    final MockFunction fnConv = new MockFunction("conv", helper.getTarget()) {

      @Override
      public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(helper.getRequirement1Any(), getUniqueId()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
        return Collections.singleton(new ValueRequirement(helper.getRequirement2Any().getValueName(), desiredValue.getTargetSpecification(), ValueProperties.with("TEST",
            desiredValue.getConstraints().getValues("TEST")).get()));
      }

    };
    helper.getFunctionRepository().addFunction(fnConv);
    DependencyGraphBuilder builder = helper.getBuilder(null);
    builder.addTarget(helper.getRequirement1Bar());
    DependencyGraph graph = builder.getDependencyGraph();
    assertNotNull(graph);
    assertGraphContains(graph, fn2Bar, fnConv);
  }

}
