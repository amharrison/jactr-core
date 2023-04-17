/*
 * Created on Oct 25, 2006 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.core.module.procedural.six.learning;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import org.jactr.core.event.ACTREventDispatcher;
import org.jactr.core.logging.Logger;
import org.jactr.core.model.IModel;
import org.jactr.core.module.AbstractModule;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.module.procedural.event.ProceduralModuleEvent;
import org.jactr.core.module.procedural.event.ProceduralModuleListenerAdaptor;
import org.jactr.core.module.procedural.five.learning.IProductionCompiler;
import org.jactr.core.module.procedural.six.learning.event.IProceduralLearningModule6Listener;
import org.jactr.core.module.procedural.six.learning.event.ProceduralLearningEvent;
import org.jactr.core.production.IInstantiation;
import org.jactr.core.production.IProduction;
import org.jactr.core.production.action.IAction;
import org.jactr.core.production.action.IBufferAction;
import org.jactr.core.production.condition.IBufferCondition;
import org.jactr.core.production.condition.ICondition;
import org.jactr.core.production.six.ISubsymbolicProduction6;
import org.jactr.core.utils.parameter.IParameterized;
import org.jactr.core.utils.parameter.ParameterHandler;
import org.slf4j.LoggerFactory;

/**
 * production learning is accomplished by listening to the procedural module for
 * firing events..<br>
 * <br>
 * This module will update a production's 'ExpectedUtility' based on reward
 * signals (if any), and it's Utility. A production may have a 'Reward' value
 * which is the reward applied when that production fires and back propogates a
 * discounted reward signal all the way back to the most recently rewarded
 * production. A 'Reward' value of NaN/'default' is the default and merely marks
 * this production as participating in the reward process, but not to start it.
 * 'Reward' of 'skip'/-Infinity will allow the production to be skipped during
 * reward, or 'stop'/+Inf will permit the production to terminate the reward
 * sequence.
 * 
 * @see http://jactr.org/node/67
 * @author developer
 */
public class DefaultProceduralLearningModule6 extends AbstractModule
    implements IProceduralLearningModule6, IParameterized
{
  static public final double                                                                  SKIP_REWARD                   = Double.NEGATIVE_INFINITY;

  static public final double                                                                  STOP_REWARD                   = Double.POSITIVE_INFINITY;

  static public final double                                                                  PARTICIPATE                   = Double.NaN;

  static public final String                                                                  INCLUDE_BUFFERS_PARAM         = "IncludeBuffers";

  static final public String                                                                  PRODUCTION_COMPILER_PARAM     = "ProductionCompiler";

  /**
   * logger definition
   */
  static final transient org.slf4j.Logger                                                     LOGGER                        = LoggerFactory
      .getLogger(DefaultProceduralLearningModule6.class);

  protected boolean                                                                           _productionCompilationEnabled = false;

  protected double                                                                            _parameterLearningRate        = Double.NaN;

  protected int                                                                               _optimizationLevel            = 10;

  private double                                                                              _initialUtility               = 0;

  protected IExpectedUtilityEquation                                                          _utilityEquation;

  protected IProductionCompiler                                                               _productionCompiler;

  protected SortedMap<Double, IProduction>                                                    _firedProductions;

  private IProduction                                                                         _justFired;

  private IProduction                                                                         _oneBack;

  private IProceduralModule                                                                   _proceduralModule;

  private ACTREventDispatcher<IProceduralLearningModule6, IProceduralLearningModule6Listener> _dispatcher                   = new ACTREventDispatcher<IProceduralLearningModule6, IProceduralLearningModule6Listener>();

  /**
   * we only credit productions that reference one of these buffers on the LHS
   * or RHS
   */
  private Set<String>                                                                         _includeBuffers;

  public DefaultProceduralLearningModule6()
  {
    super("ProceduralLearningV6");
    _firedProductions = new TreeMap<Double, IProduction>();

    _includeBuffers = new TreeSet<String>();
    _utilityEquation = new DefaultExpectedUtilityEquation();

    // setExecutor(ExecutorServices.getExecutor(ExecutorServices.POOL));
  }

  public boolean isProductionCompilationEnabled()
  {
    return _productionCompilationEnabled && _productionCompiler != null;
  }

  public void setProductionCompilationEnabled(boolean enabled)
  {
    _productionCompilationEnabled = enabled;
    if (enabled = true && _productionCompiler == null)
      _productionCompiler = new DefaultProductionCompiler6();
  }

  public double getParameterLearning()
  {
    return _parameterLearningRate;
  }

  public boolean isParameterLearningEnabled()
  {
    return !Double.isNaN(_parameterLearningRate);
  }

  public void setParameterLearning(double rate)
  {
    _parameterLearningRate = rate;
  }

  public boolean isLearningEnabled()
  {
    return isParameterLearningEnabled();
  }

  @Override
  public void install(IModel model)
  {
    super.install(model);

    _proceduralModule = getModel().getProceduralModule();
    _proceduralModule.addListener(new ProceduralModuleListenerAdaptor() {

      @Override
      public void productionAdded(ProceduralModuleEvent pme)
      {
        ISubsymbolicProduction6 ssp = pme.getProduction()
            .getAdapter(ISubsymbolicProduction6.class);
        ssp.setUtility(_initialUtility);
      }

      @Override
      public void productionFired(ProceduralModuleEvent pme)
      {
        IInstantiation instantiation = (IInstantiation) pme.getProduction();

        DefaultProceduralLearningModule6.this.productionFired(
            instantiation.getProduction(), pme.getSimulationTime());

        if (isProductionCompilationEnabled()) try
        {
          IProduction newProduction = getProductionCompiler()
              .productionFired(instantiation, pme.getSource());

          if (newProduction == null && _oneBack != null)
            throw new CannotCompileException("Other unknown reason.");

          if (newProduction == null) return;

          if (_dispatcher.hasListeners()) _dispatcher.fire(
              new ProceduralLearningEvent(DefaultProceduralLearningModule6.this,
                  _oneBack, _justFired, newProduction));

          ISubsymbolicProduction6 ssp = newProduction
              .getAdapter(ISubsymbolicProduction6.class);
          ssp.setPrimaryParent(_oneBack);

          /**
           * add the new production
           */
          pme.getSource().addProduction(newProduction);
        }
        catch (CannotCompileException cce)
        {
          if (_dispatcher.hasListeners()) _dispatcher.fire(
              new ProceduralLearningEvent(DefaultProceduralLearningModule6.this,
                  _oneBack, _justFired, cce.getMessage()));
        }
      }

      @Override
      public void productionsMerged(ProceduralModuleEvent pme)
      {
        /*
         * each time we merge, we apply the expected utility equation using the
         * primary parent's current utility as the reward value
         */
        IExpectedUtilityEquation equation = getExpectedUtilityEquation();
        IProduction production = pme.getProduction(); // first is the older
        ISubsymbolicProduction6 ssp = production
            .getAdapter(ISubsymbolicProduction6.class);
        IProduction primaryParent = ssp.getPrimaryParent();
        double oldUtility = ssp.getExpectedUtility();
        double reward = primaryParent.getAdapter(ISubsymbolicProduction6.class)
            .getExpectedUtility();

        if (Double.isNaN(reward)) // possible if we haven't been rewarded yet
          reward = primaryParent.getAdapter(ISubsymbolicProduction6.class)
              .getUtility();

        double newUtility = equation.computeExpectedUtility(production,
            getModel(), reward);
        ssp.setExpectedUtility(newUtility);

        boolean log = LOGGER.isDebugEnabled() || Logger.hasLoggers(getModel());

        if (log)
        {
          String msg = String.format(
              "%s merged, expected utility was %.2f, now %.2f.", production,
              oldUtility, newUtility);
          LOGGER.debug(msg);
          Logger.log(getModel(), Logger.Stream.PROCEDURAL, msg);
        }
      }

    }, getExecutor());
  }

  @Override
  public void initialize()
  {
    /*
     * use default set of buffer includes
     */
    if (_includeBuffers.size() == 0)
      getModel().getActivationBuffers().forEach(b -> {
        _includeBuffers.add(b.getName());
      });
  }

  protected void productionFired(IProduction production, double when)
  {
    _oneBack = _justFired;
    _justFired = production;

    if (isParameterLearningEnabled())
    {
      _firedProductions.put(when, production);

      /*
       * if the production has a reward value that is finite, we start the
       * rewarding
       */
      double reward = ((ISubsymbolicProduction6) production
          .getSubsymbolicProduction()).getReward();

      if (!Double.isNaN(reward) && Double.isFinite(reward)) reward(reward);
    }
  }

  public IProductionCompiler getProductionCompiler()
  {
    return _productionCompiler;
  }

  public void setProductionCompiler(IProductionCompiler compiler)
  {
    _productionCompiler = compiler;
  }

  public void setExpectedUtilityEquation(IExpectedUtilityEquation equation)
  {
    _utilityEquation = equation;
  }

  public IExpectedUtilityEquation getExpectedUtilityEquation()
  {
    return _utilityEquation;
  }

  public int getOptimizationLevel()
  {
    return _optimizationLevel;
  }

  public void setOptimizationLevel(int level)
  {
    _optimizationLevel = level;
  }

  public void reward(double initialReward)
  {
    IModel model = getModel();
    boolean log = LOGGER.isDebugEnabled() || Logger.hasLoggers(model);

    if (log)
    {
      String msg = "Rewarding " + _firedProductions.size() + " productions by "
          + initialReward;
      LOGGER.debug(msg);
      Logger.log(model, Logger.Stream.PROCEDURAL, msg);
    }

    // to be consistent with the Lisp, which applies the reward after time has
    // advanced, we fudge
    // our current time to be one cycle ahead for the calculations.
    double now = model.getAge()
        + model.getProceduralModule().getDefaultProductionFiringTime();
    IExpectedUtilityEquation equation = getExpectedUtilityEquation();

    if (_dispatcher.hasListeners())
      _dispatcher.fire(new ProceduralLearningEvent(this,
          ProceduralLearningEvent.Type.START_REWARDING, initialReward));
    try
    {

      for (Map.Entry<Double, IProduction> entry : _firedProductions.entrySet())
      {
        double discountedReward = initialReward - (now - entry.getKey());
        IProduction p = entry.getValue();

        if (!shouldInclude(p))
        {
          if (log)
          {
            String msg = String.format(
                "Excluding %s from rewarding since it doesn't reference %s", p,
                _includeBuffers);
            if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
            if (Logger.hasLoggers(model))
              Logger.log(model, Logger.Stream.PROCEDURAL, msg);
          }
          continue;
        }

        ISubsymbolicProduction6 ssp = (ISubsymbolicProduction6) p
            .getSubsymbolicProduction();

        double productionsReward = ssp.getReward();

        /*
         * we only apply the utility learning if the production's reward is a
         * discrete or NaN value.
         */
        if (Double.isFinite(productionsReward)
            || Double.isNaN(productionsReward))
        {
          double utility = equation.computeExpectedUtility(p, model,
              discountedReward);

          if (!(Double.isNaN(utility) || Double.isInfinite(utility)))
            ssp.setExpectedUtility(utility);

          if (log)
          {
            String msg = String.format(
                "Discount reward for %s to %.2f for a learned utility of %.2f",
                p, discountedReward, utility);
            if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
            if (Logger.hasLoggers(model))
              Logger.log(model, Logger.Stream.PROCEDURAL, msg);
          }

          if (_dispatcher.hasListeners()) _dispatcher
              .fire(new ProceduralLearningEvent(this, p, discountedReward));
        }
        else if (productionsReward < 0) // negative inf, skip
        {
          if (log)
          {
            String msg = String.format("Skipping rewarding of %s", p);
            if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
            if (Logger.hasLoggers(model))
              Logger.log(model, Logger.Stream.PROCEDURAL, msg);
          }
          continue; // skip
        }
        else
        // pos inf, stop
        {
          if (log)
          {
            String msg = String.format("Stopping reward crediation at %s", p);
            if (LOGGER.isDebugEnabled()) LOGGER.debug(msg);
            if (Logger.hasLoggers(model))
              Logger.log(model, Logger.Stream.PROCEDURAL, msg);
          }
          break; // stop entirely
        }

      }
    }
    finally
    {
      if (_dispatcher.hasListeners())
        _dispatcher.fire(new ProceduralLearningEvent(this,
            ProceduralLearningEvent.Type.END_REWARDING, initialReward));

      _firedProductions.clear();
    }
  }

  protected boolean shouldInclude(IProduction production)
  {
    for (ICondition condition : production.getSymbolicProduction()
        .getConditions())
      if (condition instanceof IBufferCondition) if (_includeBuffers.contains(
          ((IBufferCondition) condition).getBufferName()))
        return true;

    for (IAction action : production.getSymbolicProduction().getActions())
      if (action instanceof IBufferAction)
        if (_includeBuffers.contains(((IBufferAction) action).getBufferName()))
          return true;

    return false;
  }

  public String getParameter(String key)
  {
    if (PARAMETER_LEARNING_RATE.equalsIgnoreCase(key))
      return "" + getParameterLearning();
    else if (OPTIMIZED_LEARNING.equalsIgnoreCase(key))
      return "" + getOptimizationLevel();
    else if (PRODUCTION_COMPILATION_PARAM.equalsIgnoreCase(key))
      return "" + isProductionCompilationEnabled();
    else if (EXPECTED_UTILITY_EQUATION_PARAM.equalsIgnoreCase(key))
      return "" + getExpectedUtilityEquation().getClass().getName();
    else if (INCLUDE_BUFFERS_PARAM.equalsIgnoreCase(key))
    {
      StringBuilder sb = new StringBuilder();
      for (String bufferName : _includeBuffers)
        sb.append(bufferName).append(", ");

      if (sb.length() > 2) sb.delete(sb.length() - 2, sb.length());
      return sb.toString();
    }
    else if (INITIAL_UTILITY_PARAM.equalsIgnoreCase(key))
      return "" + _initialUtility;

    return null;
  }

  public Collection<String> getPossibleParameters()
  {
    return getSetableParameters();
  }

  public Collection<String> getSetableParameters()
  {
    return Arrays.asList(PARAMETER_LEARNING_RATE, PRODUCTION_COMPILATION_PARAM,
        EXPECTED_UTILITY_EQUATION_PARAM, INCLUDE_BUFFERS_PARAM,
        PRODUCTION_COMPILER_PARAM, INITIAL_UTILITY_PARAM);
  }

  public void setParameter(String key, String value)
  {
    if (EXPECTED_UTILITY_EQUATION_PARAM.equalsIgnoreCase(key))
      try
      {
        setExpectedUtilityEquation((IExpectedUtilityEquation) ParameterHandler
            .classInstance().coerce(value).newInstance());
      }
      catch (Exception e)
      {
        if (LOGGER.isWarnEnabled()) LOGGER.warn(
            String.format("Could not instantiate %s, using default", value));
        setExpectedUtilityEquation(new DefaultExpectedUtilityEquation());
      }
    else if (PRODUCTION_COMPILER_PARAM.equalsIgnoreCase(key))
      try
      {
        setProductionCompiler((IProductionCompiler) ParameterHandler
            .classInstance().coerce(value).newInstance());
      }
      catch (Exception e)
      {
        if (LOGGER.isWarnEnabled()) LOGGER.warn(
            String.format("Could not instantiate %s, using default", value));
        setProductionCompiler(new DefaultProductionCompiler6());
      }
    else if (INITIAL_UTILITY_PARAM.equalsIgnoreCase(key))
      setInitialLearnedUtility(
          ParameterHandler.numberInstance().coerce(value).doubleValue());
    else if (PARAMETER_LEARNING_RATE.equalsIgnoreCase(key))
      setParameterLearning(
          ParameterHandler.numberInstance().coerce(value).doubleValue());
    else if (OPTIMIZED_LEARNING.equalsIgnoreCase(key))
      setOptimizationLevel(
          ParameterHandler.numberInstance().coerce(value).intValue());
    else if (PRODUCTION_COMPILATION_PARAM.equalsIgnoreCase(key))
      setProductionCompilationEnabled(
          ParameterHandler.booleanInstance().coerce(value).booleanValue());
    else if (INCLUDE_BUFFERS_PARAM.equalsIgnoreCase(key))
    {
      String[] buffers = value.split(",");
      for (String bufferName : buffers)
      {
        bufferName = bufferName.trim().toLowerCase();
        if (bufferName.length() == 0) continue;
        _includeBuffers.add(bufferName);
      }
    }
    else if (LOGGER.isWarnEnabled()) LOGGER.warn(
        String.format("%s doesn't recognize %s. Available parameters : %s",
            getClass().getSimpleName(), key, getSetableParameters()));
  }

  public void addListener(IProceduralLearningModule6Listener listener,
      Executor executor)
  {
    _dispatcher.addListener(listener, executor);
  }

  public void removeListener(IProceduralLearningModule6Listener listener)
  {
    _dispatcher.removeListener(listener);
  }

  public void reset()
  {
    // noop
  }

  @Override
  public void setInitialLearnedUtility(double initialUtility)
  {
    _initialUtility = initialUtility;
  }

  @Override
  public double getInitialLearnedUtility()
  {
    return _initialUtility;
  }
}
