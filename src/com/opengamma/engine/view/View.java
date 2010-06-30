/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ThreadUtil;
import com.opengamma.util.monitor.OperationTimer;

/**
 * The base implementation of the {@link View} interface.
 */
public class View implements Lifecycle, LiveDataSnapshotListener {
  private static final Logger s_logger = LoggerFactory.getLogger(View.class);
  // Injected dependencies:
  private final ViewDefinition _definition;
  private final ViewProcessingContext _processingContext;
  // Internal State:
  private PortfolioEvaluationModel _portfolioEvaluationModel;
  private Thread _recalculationThread;
  private ViewCalculationState _calculationState = ViewCalculationState.NOT_INITIALIZED;
  private ViewRecalculationJob _recalcJob;
  private ViewComputationResultModelImpl _mostRecentResult;
  private final Set<ComputationResultListener> _resultListeners = new HashSet<ComputationResultListener>();
  private final Set<DeltaComputationResultListener> _deltaListeners = new HashSet<DeltaComputationResultListener>();

  public View(ViewDefinition definition, ViewProcessingContext processingContext) {
    if (definition == null) {
      throw new NullPointerException("Must provide a definition.");
    }
    if (processingContext == null) {
      throw new NullPointerException("Must provide a processing context.");
    }
    _definition = definition;
    _processingContext = processingContext;
  }
  
  /**
   * @return the definition
   */
  public ViewDefinition getDefinition() {
    return _definition;
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }

  /**
   * @return the recalculationThread
   */
  public Thread getRecalculationThread() {
    return _recalculationThread;
  }

  /**
   * @param recalculationThread the recalculationThread to set
   */
  protected void setRecalculationThread(Thread recalculationThread) {
    _recalculationThread = recalculationThread;
  }

  /**
   * @return the calculationState
   */
  public ViewCalculationState getCalculationState() {
    return _calculationState;
  }

  /**
   * @param calculationState the calculationState to set
   */
  protected void setCalculationState(ViewCalculationState calculationState) {
    _calculationState = calculationState;
  }

  /**
   * @return the recalcJob
   */
  public ViewRecalculationJob getRecalcJob() {
    return _recalcJob;
  }

  /**
   * @param recalcJob the recalcJob to set
   */
  protected void setRecalcJob(ViewRecalculationJob recalcJob) {
    _recalcJob = recalcJob;
  }

  /**
   * @return the portfolioEvaluationModel
   */
  public PortfolioEvaluationModel getPortfolioEvaluationModel() {
    return _portfolioEvaluationModel;
  }

  /**
   * @param portfolioEvaluationModel the portfolioEvaluationModel to set
   */
  public void setPortfolioEvaluationModel(
      PortfolioEvaluationModel portfolioEvaluationModel) {
    _portfolioEvaluationModel = portfolioEvaluationModel;
  }
  
  public void addResultListener(ComputationResultListener resultListener) {
    ArgumentChecker.notNull(resultListener, "Result listener");
    
    checkIsEntitledToResults(resultListener.getUser());
    _resultListeners.add(resultListener);
  }
  
  public void removeResultListener(ComputationResultListener resultListener) {
    ArgumentChecker.notNull(resultListener, "Result listener");
    _resultListeners.remove(resultListener);
  }
  
  public void addDeltaResultListener(DeltaComputationResultListener deltaListener) {
    ArgumentChecker.notNull(deltaListener, "Delta listener");
    
    checkIsEntitledToResults(deltaListener.getUser());
    _deltaListeners.add(deltaListener);
  }
  
  public void removeDeltaResultLister(DeltaComputationResultListener deltaListener) {
    ArgumentChecker.notNull(deltaListener, "Delta listener");
    _deltaListeners.remove(deltaListener);
  }
  
  public String getName() {
    return getDefinition().getName();
  }
  
  public Set<ComputationTargetSpecification> getAllComputationTargets() {
    return getPortfolioEvaluationModel().getAllComputationTargets();
  }
  
  public synchronized void init() {
    OperationTimer timer = new OperationTimer(s_logger, "Initializing view {}", getDefinition().getName());
    setCalculationState(ViewCalculationState.INITIALIZING);

    reloadPortfolio();
    
    setCalculationState(ViewCalculationState.NOT_STARTED);
    timer.finished();
  }

  /**
   * Reloads the portfolio, typically from a database.
   */
  public void reloadPortfolio() {
    OperationTimer timer = new OperationTimer(s_logger, "Reloading portfolio {}", getDefinition().getPortfolioId());
    Portfolio portfolio = getProcessingContext().getPositionMaster().getPortfolio(getDefinition().getPortfolioId());
    if (portfolio == null) {
      throw new OpenGammaRuntimeException("Unable to resolve portfolio " + getDefinition().getPortfolioId() +
          " in position master " + getProcessingContext().getPositionMaster());
    }
    PortfolioEvaluationModel portfolioEvaluationModel = new PortfolioEvaluationModel(portfolio);
    portfolioEvaluationModel.init(
        getProcessingContext().asCompilationServices(),
        getDefinition());
    setPortfolioEvaluationModel(portfolioEvaluationModel);
    addLiveDataSubscriptions();
    timer.finished();
  }

  /**
   * Adds live data subscriptions to the view.
   */
  private void addLiveDataSubscriptions() {
    Set<ValueRequirement> liveDataRequirements = getPortfolioEvaluationModel().getAllLiveDataRequirements();
    OperationTimer timer = new OperationTimer(s_logger, "Adding {} live data subscriptions for portfolio {}", liveDataRequirements.size(), getDefinition().getPortfolioId());
    LiveDataSnapshotProvider snapshotProvider = getProcessingContext().getLiveDataSnapshotProvider();
    snapshotProvider.addListener(this);
    snapshotProvider.addSubscription(getDefinition().getLiveDataUser(), liveDataRequirements);
    timer.finished();
  }
  
  @Override
  public void subscriptionFailed(ValueRequirement requirement, String msg) {
  }

  @Override
  public void subscriptionStopped(ValueRequirement requirement) {
  }

  @Override
  public void subscriptionSucceeded(ValueRequirement requirement) {
  }

  @Override
  public void valueChanged(ValueRequirement requirement) {
    Set<ValueRequirement> liveDataRequirements = getPortfolioEvaluationModel().getAllLiveDataRequirements();
    ViewRecalculationJob recalcJob = getRecalcJob();
    if (recalcJob != null && liveDataRequirements.contains(requirement)) {
      recalcJob.liveDataChanged();      
    }
  }

  public synchronized ViewComputationResultModel getMostRecentResult() {
    return _mostRecentResult;
  }

  public Portfolio getPortfolio() {
    if (getPortfolioEvaluationModel() == null) {
      return null;
    }
    return getPortfolioEvaluationModel().getPortfolio();
  }

  public PortfolioNode getPositionRoot() {
    if (getPortfolioEvaluationModel() == null) {
      return null;
    }
    return getPortfolioEvaluationModel().getPortfolio().getRootNode();
  }

  public synchronized void recalculationPerformed(ViewComputationResultModelImpl result) {
    // REVIEW kirk 2009-09-24 -- We need to consider this method for background execution
    // of some kind. It's synchronized and blocks the recalc thread, so a slow
    // callback implementation (or just the cost of computing the delta model) will
    // be an unnecessary burden. Have to factor in some type of win there.
    s_logger.debug("Recalculation Performed called.");
    // We swap these first so that in the callback the view is consistent.
    ViewComputationResultModelImpl previousResult = _mostRecentResult;
    _mostRecentResult = result;
    for (ComputationResultListener resultListener : _resultListeners) {
      resultListener.computationResultAvailable(result);
    }
    if (!_deltaListeners.isEmpty() && (previousResult != null)) {
      ViewDeltaResultModel deltaModel = computeDeltaModel(previousResult, result);
      for (DeltaComputationResultListener deltaListener : _deltaListeners) {
        deltaListener.deltaResultAvailable(deltaModel);
      }
    }
  }
  
  /**
   * @param previousResult
   * @param result
   * @return
   */
  private ViewDeltaResultModel computeDeltaModel(
      ViewComputationResultModelImpl previousResult,
      ViewComputationResultModelImpl result) {
    ViewDeltaResultModelImpl deltaModel = new ViewDeltaResultModelImpl();
    deltaModel.setInputDataTimestamp(result.getInputDataTimestamp());
    deltaModel.setResultTimestamp(result.getResultTimestamp());
    deltaModel.setPreviousResultTimestamp(previousResult.getResultTimestamp());
    for (ComputationTargetSpecification targetSpec : result.getAllTargets()) {
      computeDeltaModel(deltaModel, targetSpec, previousResult, result);
    }
    
    return deltaModel;
  }
  
  private void computeDeltaModel(
      ViewDeltaResultModelImpl deltaModel,
      ComputationTargetSpecification targetSpec,
      ViewComputationResultModelImpl previousResult,
      ViewComputationResultModelImpl result) {
    for (String calcConfigName : result.getCalculationConfigurationNames()) {
      ViewCalculationResultModel resultCalcModel = result.getCalculationResult(calcConfigName);
      ViewCalculationResultModel previousCalcModel = previousResult.getCalculationResult(calcConfigName);
      
      computeDeltaModel(deltaModel, targetSpec, calcConfigName, previousCalcModel, resultCalcModel);
    }
  }

  private void computeDeltaModel(
      ViewDeltaResultModelImpl deltaModel,
      ComputationTargetSpecification targetSpec,
      String calcConfigName,
      ViewCalculationResultModel previousCalcModel,
      ViewCalculationResultModel resultCalcModel) {
    if (previousCalcModel == null) {
      // Everything is new/delta because this is a new calculation context.
      Map<String, ComputedValue> resultValues = resultCalcModel.getValues(targetSpec);
      for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
        deltaModel.addValue(calcConfigName, resultEntry.getValue());
      }
    } else {
      Map<String, ComputedValue> resultValues = resultCalcModel.getValues(targetSpec);
      Map<String, ComputedValue> previousValues = previousCalcModel.getValues(targetSpec);
      
      if (previousValues == null) {
        // Everything is new/delta because this is a new target.
        for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
          deltaModel.addValue(calcConfigName, resultEntry.getValue());
        }
      } else {
        // Have to individual delta.
        DeltaDefinition deltaDefinition = getDefinition().getCalculationConfiguration(calcConfigName).getDeltaDefinition();
        for (Map.Entry<String, ComputedValue> resultEntry : resultValues.entrySet()) {
          ComputedValue resultValue = resultEntry.getValue();
          ComputedValue previousValue = previousValues.get(resultEntry.getKey());
          // REVIEW jonathan 2010-05-07 -- The previous value that we're comparing with is the value from the last
          // computation cycle, not the value that we last emitted as a delta. It is therefore important that the
          // DeltaComparers take this into account in their implementation of isDelta. E.g. they should compare the
          // values after truncation to the required decimal place, rather than testing whether the difference of the
          // full values is greater than some threshold; this way, there will always be a point beyond which a change
          // is detected, even in the event of gradual creep.
          if (deltaDefinition.isDelta(previousValue, resultValue)) {
            deltaModel.addValue(calcConfigName, resultEntry.getValue());
          }
        }
      }
    }
  }

  // REVIEW kirk 2009-09-11 -- Need to resolve the synchronization on the lifecycle
  // methods.

  @Override
  public synchronized boolean isRunning() {
    return getCalculationState() == ViewCalculationState.RUNNING;
  }
  
  public synchronized void runOneCycle() {
    long snapshotTime = getProcessingContext().getLiveDataSnapshotProvider().snapshot();
    runOneCycle(snapshotTime);
  }
  
  public synchronized void runOneCycle(long snapshotTime) {
    SingleComputationCycle cycle = createCycle(snapshotTime);
    cycle.prepareInputs();
    cycle.executePlans();
    cycle.populateResultModel();
    recalculationPerformed(cycle.getResultModel());
    cycle.releaseResources();
  }

  @Override
  public synchronized void start() {
    s_logger.info("Starting...");
    switch(getCalculationState()) {
      case NOT_STARTED:
      case TERMINATED:
        // Normal state of play. Continue as normal.
        break;
      case TERMINATING:
        // In the middle of termination. This is really bad, as we're now holding the lock
        // that will allow termination to complete successfully. Therefore, we have to throw
        // an exception rather than just waiting or something.
        throw new IllegalStateException("Instructed to start while still terminating.");
      case INITIALIZING:
        // Must have thrown an exception in initialization. Can't start.
        throw new IllegalStateException("Initialization didn't completely successfully. Can't start.");
      case NOT_INITIALIZED:
        throw new IllegalStateException("Must call init() before starting.");
      case STARTING:
        // Must have thrown an exception when start() called previously.
        throw new IllegalStateException("start() already called, but failed to start. Cannot start again.");
      case RUNNING:
        throw new IllegalStateException("Already running.");
    }
    
    setCalculationState(ViewCalculationState.STARTING);
    ViewRecalculationJob recalcJob = new ViewRecalculationJob(this);
    Thread recalcThread = new Thread(recalcJob, "Recalc Thread for " + getDefinition().getName());
    
    setRecalcJob(recalcJob);
    setRecalculationThread(recalcThread);
    setCalculationState(ViewCalculationState.RUNNING);
    recalcThread.start();
    s_logger.info("Started.");
  }

  @Override
  public void stop() {
    s_logger.info("Stopping.....");
    synchronized (this) {
      switch(getCalculationState()) {
        case STARTING:
          // Something went horribly wrong during start, and it must have thrown an exception.
          s_logger.warn("Instructed to stop the ViewImpl, but still starting. Starting must have failed. Doing nothing.");
          break;
        case RUNNING:
          // This is the normal state of play. Do nothing.
          break;
        default:
          throw new IllegalStateException("Cannot stop a ViewImpl that isn't running. State: " + getCalculationState());
      }
    }
    
    assert getRecalcJob() != null;
    assert getRecalculationThread() != null;
    
    synchronized (this) {
      if ((getCalculationState() == ViewCalculationState.TERMINATED)
          || (getCalculationState() == ViewCalculationState.TERMINATING)) {
        s_logger.info("Multiple requests to stop() made, this invocation will do nothing.");
        return;
      }
      setCalculationState(ViewCalculationState.TERMINATING);
    }
    
    getRecalcJob().terminate();
    if (getRecalculationThread().getState() == Thread.State.TIMED_WAITING) {
      // In this case it might be waiting on a recalculation pass. Interrupt it.
      getRecalculationThread().interrupt();
    }
    
    // TODO kirk 2009-09-11 -- Have a heuristic on when to set the timeout based on
    // how long the job is currently taking to cycle.
    long timeout = 100 * 1000L;
    boolean successful = ThreadUtil.safeJoin(getRecalculationThread(), timeout);
    if (!successful) {
      s_logger.warn("Unable to shut down recalc thread in {}ms", timeout);
    }
    
    synchronized (this) {
      setCalculationState(ViewCalculationState.TERMINATED);
      
      setRecalcJob(null);
      setRecalculationThread(null);
    }
    s_logger.info("Stopped.");
  }
  
  /**
   * Reading the static contents of a view, modifying the view, 
   * etc., can sometimes  be performed even by users 
   * who are not entitled to view the results of the view.
   * 
   * @param user User who is requesting access
   * @return true if the user should be able to view the
   * static contents of the view. false otherwise.
   */
  public boolean isEntitledToAccess(UserPrincipal user) {
    try {
      checkIsEntitledToAccess(user);
      return true;
    } catch (ViewAccessException e) {
      return false;
    }
  }
  
  /**
   * Reading the static contents of a view, modifying the view, 
   * etc., can sometimes  be performed even by users 
   * who are not entitled to view the results of the view.
   * 
   * @param user User who is requesting access
   * @throws ViewAccessException If the user is not entitled
   */
  public void checkIsEntitledToAccess(UserPrincipal user) {
    // not done yet    
  }
  
  /**
   * A user is entitled to view the computation results produced
   * by a view only if they are entitled to every market data
   * line required to compute the results of the view.
   * 
   * @param user User who is requesting access
   * @return true if the user should be able to view the
   * computation results produced by the view. false otherwise.
   */
  public boolean isEntitledToResults(UserPrincipal user) {
    try {
      checkIsEntitledToResults(user);
      return true;
    } catch (ViewAccessException e) {
      return false;
    }
  }
  
  /**
   * A user is entitled to view the computation results produced
   * by a view only if they are entitled to every market data
   * line required to compute the results of the view.
   * 
   * @param user User who is requesting access
   * @throws ViewAccessException If the user is not entitled 
   */
  public void checkIsEntitledToResults(UserPrincipal user) {
    Set<ValueRequirement> requiredValues = getPortfolioEvaluationModel().getAllLiveDataRequirements();
    Collection<LiveDataSpecification> requiredLiveData = ValueRequirement.getRequiredLiveData(
        requiredValues, 
        getProcessingContext().getSecurityMaster());
    
    s_logger.info("Checking that {} is entitled to the results of {}", user, this);
    Map<LiveDataSpecification, Boolean> entitlements = getProcessingContext().getLiveDataClient().isEntitled(user, requiredLiveData);
    
    ArrayList<LiveDataSpecification> failures = new ArrayList<LiveDataSpecification>();
    for (Map.Entry<LiveDataSpecification, Boolean> entry : entitlements.entrySet()) {
      if (entry.getValue().booleanValue() == false) {
        failures.add(entry.getKey());
      }
    }
    
    if (!failures.isEmpty()) {
      throw new ViewAccessException(user + " is not entitled to " + this + 
          " because they do not have permissions to " + failures.get(0));
    }
  }
  
  @Override
  public String toString() {
    return "View[" + getDefinition().getName() + "]";
  }
  
  /*package*/ SingleComputationCycle createCycle(long snapshotTime) {
    PortfolioEvaluationModel portfolioEvaluationModel = getPortfolioEvaluationModel();
    ViewComputationResultModelImpl result = new ViewComputationResultModelImpl();
    // REVIEW kirk 2010-03-29 -- Order here is important. This is lame and should be refactored into
    // the constructor.
    result.setCalculationConfigurationNames(portfolioEvaluationModel.getAllCalculationConfigurationNames());
    result.setPortfolio(portfolioEvaluationModel.getPortfolio());
    
    SingleComputationCycle cycle = new SingleComputationCycle(
        getDefinition().getName(),
        getProcessingContext(),
        portfolioEvaluationModel,
        result, 
        getDefinition(),
        snapshotTime);
    return cycle;
  }

}
