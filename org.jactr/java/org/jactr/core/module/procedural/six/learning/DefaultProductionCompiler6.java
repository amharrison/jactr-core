package org.jactr.core.module.procedural.six.learning;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections.map.LRUMap;
import org.eclipse.collections.impl.factory.Lists;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.module.procedural.five.learning.ICompilableBuffer;
import org.jactr.core.module.procedural.five.learning.IProductionCompiler;
import org.jactr.core.production.IInstantiation;
import org.jactr.core.production.IProduction;
import org.jactr.core.production.VariableBindings;
import org.jactr.core.production.action.AddAction;
import org.jactr.core.production.action.IAction;
import org.jactr.core.production.action.IBufferAction;
import org.jactr.core.production.action.ModifyAction;
import org.jactr.core.production.action.OutputAction;
import org.jactr.core.production.action.RemoveAction;
import org.jactr.core.production.action.SetAction;
import org.jactr.core.production.condition.ChunkTypeCondition;
import org.jactr.core.production.condition.IBufferCondition;
import org.jactr.core.production.condition.ICondition;
import org.jactr.core.production.condition.QueryCondition;
import org.jactr.core.production.condition.VariableCondition;
import org.jactr.core.production.request.ChunkRequest;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.production.request.SlotBasedRequest;
import org.jactr.core.production.six.ISubsymbolicProduction6;
import org.jactr.core.slot.BasicSlot;
import org.jactr.core.slot.DefaultMutableSlot;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.core.slot.IMutableVariableNameSlot;
import org.jactr.core.slot.ISlot;
import org.jactr.core.slot.ISlotContainer;
import org.slf4j.LoggerFactory;

public class DefaultProductionCompiler6 implements IProductionCompiler
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER     = LoggerFactory
      .getLogger(DefaultProductionCompiler6.class);

  private IInstantiation                          _lastInstantiation;

  // private ICodeGenerator _codeGenerator = new JACTRCodeGenerator();

  private ProductionCompilerEvaluator             evaluator;

  List<ISlot>                                     __slotList = Lists.mutable
      .empty();                                                             // for
                                                                            // fast
                                                                            // iterating
                                                                            // through
                                                                            // slot
                                                                            // lists

  public DefaultProductionCompiler6()
  {
    evaluator = new ProductionCompilerEvaluator();
  }
  
  public ProductionCompilerEvaluator getEvaluator() {
    return evaluator;
  }

  protected LRUMap getLRUMap(IInstantiation instantiation)
  {
    LRUMap map = (LRUMap) instantiation.getProduction()
        .getMetaData("compilation.LRUMap");
    if (map == null)
    {
      map = new LRUMap(5);
      instantiation.getProduction().setMetaData("compilation.LRUMap", map);
    }
    return map;
  }

  protected boolean quickTest(IInstantiation current, IInstantiation last)
      throws CannotCompileException
  {
    IProduction lastProduction = last.getProduction();
    LRUMap currentLRU = getLRUMap(current);

    CannotCompileException canCompile = (CannotCompileException) currentLRU
        .get(lastProduction);
    if (canCompile == null) return true;

    throw canCompile;
  }

  protected void updateQuickTest(IInstantiation current, IInstantiation last,
      IProduction compiled)
  {
    IProduction lastProduction = last.getProduction();
    LRUMap currentLRU = getLRUMap(current);

    currentLRU.remove(lastProduction);
  }

  protected void updateQuickTest(IInstantiation current, IInstantiation last,
      CannotCompileException cce)
  {
    IProduction lastProduction = last.getProduction();
    LRUMap currentLRU = getLRUMap(current);

    if (cce instanceof CanNeverCompileException)
      currentLRU.put(lastProduction, cce);
    // cannot compile could be due to anything else, so we don't cache it
  }

  public IProduction productionFired(IInstantiation instantiation,
      IProceduralModule proceduralModule) throws CannotCompileException
  {
    LOGGER.debug("Got that production " + instantiation
        + " fired; last fired was " + _lastInstantiation);

    IProduction compiled = null;
    Map<ICompilableBuffer, BufferStruct> compilationMapOne = new HashMap<ICompilableBuffer, BufferStruct>();
    Map<ICompilableBuffer, BufferStruct> compilationMapTwo = new HashMap<ICompilableBuffer, BufferStruct>();

    try
    {
      if (_lastInstantiation != null
          && quickTest(instantiation, _lastInstantiation)
          && canCompile(_lastInstantiation, instantiation, compilationMapOne,
              compilationMapTwo))
      {
        compiled = doCompilation(_lastInstantiation, instantiation,
            compilationMapOne, compilationMapTwo);

        updateQuickTest(instantiation, _lastInstantiation, compiled);
      }
    }
    catch (CannotCompileException cce)
    {
      updateQuickTest(instantiation, _lastInstantiation, cce);
      throw cce;
    }
    catch (Exception e)
    {
      LOGGER.warn("got an exception: ", e);
    }
    finally
    {
      _lastInstantiation = instantiation;
    }
    return compiled; // for now, for testing
  }

  protected boolean canCompile(IInstantiation instantiationOne,
      IInstantiation instantiationTwo,
      Map<ICompilableBuffer, BufferStruct> compilationMapOne,
      Map<ICompilableBuffer, BufferStruct> compilationMapTwo)
      throws CannotCompileException
  {
    HashSet<ICompilableBuffer> buffers = new HashSet<ICompilableBuffer>();
    // check individually; return false if buffers involved aren't
    // ICompilableBuffers
    if (!checkBuffers(instantiationOne, compilationMapOne, false))
      throw new CanNeverCompileException(
          String.format("parentA %s can never compile due to contents.",
              instantiationOne.getProduction()));

    if (!checkBuffers(instantiationTwo, compilationMapTwo, true))
      throw new CanNeverCompileException(
          String.format("parentB %s can never compile due to contents.",
              instantiationTwo.getProduction()));

    // now check the maps
    buffers.addAll(compilationMapOne.keySet());
    buffers.addAll(compilationMapTwo.keySet());
    for (ICompilableBuffer buffer : buffers)
    {
      if (compilationMapOne.get(buffer) == null) compilationMapOne.put(buffer,
          new BufferStruct(buffer.getName(), buffer.isStrictHarvestingEnabled(),
              instantiationOne.getVariableBindings()));
      if (compilationMapTwo.get(buffer) == null) compilationMapTwo.put(buffer,
          new BufferStruct(buffer.getName(), buffer.isStrictHarvestingEnabled(),
              instantiationTwo.getVariableBindings()));
      if (!evaluator.canCompile(compilationMapOne.get(buffer),
          compilationMapTwo.get(buffer), buffer))
      {
        String type = evaluator.getType(buffer.getCompilableContext(),
            compilationMapOne.get(buffer).getIRequest());

        throw new CanNeverCompileException(String.format(
            "Cannot compile due to LHS/RHS conditions on %s (%s, %d, %d)",
            buffer, type, compilationMapOne.get(buffer).index,
            compilationMapTwo.get(buffer).index));
      }
    }
    // LOGGER.debug("can compile!");
    return true;
  }

  protected IProduction doCompilation(IInstantiation instantiationOne,
      IInstantiation instantiationTwo,
      Map<ICompilableBuffer, BufferStruct> compilationMapOne,
      Map<ICompilableBuffer, BufferStruct> compilationMapTwo)
      throws InterruptedException, ExecutionException, CannotCompileException
  {
    ArrayList<ICondition> tempTwoConditions = new ArrayList<ICondition>();
    ArrayList<IAction> tempTwoActions = new ArrayList<IAction>();

    // FIRST: Unification
    // Step 1: rename duplicate variables
    HashMap<String, Object> mapping = findDuplicateVariableMapping(
        compilationMapOne, compilationMapTwo);
    if (mapping == null) throw new CannotCompileException(
        "Duplicate variable mappings returned null");
    if (!renameVariables(compilationMapTwo, mapping))
      throw new CannotCompileException("Rename variables returned falsed");

    // Step 2: do buffer mappings
    HashMap<ICompilableBuffer, Map<String, Object>> bufferMappings = new HashMap<ICompilableBuffer, Map<String, Object>>();
    if (!extractBufferMappings(bufferMappings, compilationMapOne,
        compilationMapTwo, instantiationOne, tempTwoConditions, tempTwoActions))
      throw new CannotCompileException("extractBufferMappings returned false");

    // Step 3: Merge Mappings
    Map<String, Object> mergedMapping = mergeBufferMappings(bufferMappings);
    if (mergedMapping == null)
      throw new CannotCompileException("mergeBufferMappings returned null");

    // SECOND: Substitution - just reuse earlier code.
    if (!renameVariables(compilationMapOne, mergedMapping))
      throw new CannotCompileException("renameVariables one returned false");
    if (!renameVariables(compilationMapTwo, mergedMapping))
      throw new CannotCompileException("renameVariables two returned false");

    // THIRD: Collapsing
    IProduction p = collapseProductions(compilationMapOne, compilationMapTwo,
        instantiationOne.getModel().getProceduralModule()
            .createProduction(instantiationOne.getSymbolicProduction().getName()
                + "-" + instantiationTwo.getSymbolicProduction().getName())
            .get());
    if (p == null)
      throw new CannotCompileException("collapseProductions returned null");

    // FOURTH: set reward of new production to be the reward of the second
    // parent
    ((ISubsymbolicProduction6) p.getSubsymbolicProduction())
        .setReward(((ISubsymbolicProduction6) instantiationTwo.getProduction()
            .getSubsymbolicProduction()).getReward());

    return bindFreeVariables(p, compilationMapTwo, instantiationTwo);
  }

  private IProduction bindFreeVariables(IProduction p,
      Map<ICompilableBuffer, BufferStruct> compilationMapTwo,
      IInstantiation instantiation) throws CannotCompileException
  {
    HashSet<String> boundVariables = new HashSet<String>();
    if (!getConditionVariables(p.getSymbolicProduction().getConditions(),
        boundVariables))
      return null;

    for (IAction action : p.getSymbolicProduction().getActions())
      if (action instanceof AddAction
          && ((AddAction) action).getChunkName() != null)
      {
        if (!boundVariables.contains(((AddAction) action).getChunkName()))
          throw new CannotCompileException(
              "Should never be here where AddAction's chunk name isn't resolved "
                  + action);
      }
      else if (action instanceof ISlotContainer)
      {
        __slotList.clear();
        for (ISlot s : ((ISlotContainer) action).getSlots(__slotList))
        {
          if (s.getName().startsWith("=")
              && !boundVariables.contains(s.getName()))
            throw new CannotCompileException(
                "Variable name in condition slot name... we don't currently handle this. "
                    + s);
          if (s.getValue() != null && s.getValue().toString().startsWith("=")
              && !boundVariables.contains(s.getValue().toString()))
          {
            // LOGGER.debug("found an unbound variable..." + s);
            // get value from second instantiation
            ((ISlotContainer) action).removeSlot(s);
            IMutableSlot newSlot = ((DefaultMutableSlot) s).clone();
            newSlot.setValue(compilationMapTwo
                .get(instantiation.getModel().getActivationBuffer(
                    ((IBufferAction) action).getBufferName())).bindings
                        .get(s.getValue().toString()));
            ((ISlotContainer) action).addSlot(newSlot);
          }
        }
      } // ignoring here Add production chunk name variables
    return p;
  }

  private IProduction collapseProductions(
      Map<ICompilableBuffer, BufferStruct> compilationMapOne,
      Map<ICompilableBuffer, BufferStruct> compilationMapTwo,
      IProduction newProduction)
  {
    HashSet<ICompilableBuffer> buffers = new HashSet<ICompilableBuffer>();
    buffers.addAll(compilationMapOne.keySet());
    buffers.addAll(compilationMapTwo.keySet());
    BufferStruct result;
    for (ICompilableBuffer buffer : buffers)
    {
      result = evaluator.collapseBuffer(compilationMapOne.get(buffer),
          compilationMapTwo.get(buffer), buffer);
      if (result == null)
      {
        LOGGER.info("Couldn't compile productions for " + buffer
            + " so stopping compilation process.  If your Evaluator/Mapper/Collapser tables were consistent, and all your Collapser functions were being called correctly, you probably wouldn't be here.");
        return null;
      }
      else
      {
        for (ICondition condition : result.conditions)
          newProduction.getSymbolicProduction().addCondition(condition);
        for (IAction action : result.actions)
        {
          if (action instanceof ISlotContainer) LOGGER.debug(
              "adding action " + action + ((ISlotContainer) action).getSlots());
          newProduction.getSymbolicProduction().addAction(action);
        }
      }

    }

    return newProduction;
  }

  private Map<String, Object> mergeBufferMappings(
      Map<ICompilableBuffer, Map<String, Object>> bufferMappings)
  {
    Map<String, Object> mergedMappings = new HashMap<String, Object>();

    for (Map<String, Object> bufferMap : bufferMappings.values())
      for (String s : bufferMap.keySet())
        if (mergedMappings.containsKey(s))
        {
          LOGGER.debug("merged mappings contains " + s);
          Object val1 = mergedMappings.get(s);
          Object val2 = bufferMap.get(s);

          if (val1.equals(val2))
            // they are the same; just move on.
            continue;
          else if (val1.toString().startsWith("="))
          {
            if (val2.toString().startsWith("="))
            {
              LOGGER.warn("can't merge mappings because " + s + " maps to both "
                  + val1 + " and " + val2);
              return null;
            }
            else
            {
              // val1 is a variable and val2 is a value; update s entry so that
              // these can be applied in any order.
              mergedMappings.put(s, val2);
              mergedMappings.put(val1.toString(), val2);
            }
          }
          else if (val2.toString().startsWith("="))
            // val1 is a value and val2 is a variable
            mergedMappings.put(val2.toString(), val1);
          else
          {
            LOGGER.warn("can't merge mappings because " + s + " maps to both "
                + val1 + " and " + val2);
            return null;
          }
        }
        else
          mergedMappings.put(s, bufferMap.get(s));

    return mergedMappings;
  }

  private boolean extractBufferMappings(
      HashMap<ICompilableBuffer, Map<String, Object>> bufferMappings,
      Map<ICompilableBuffer, BufferStruct> compilationMapOne,
      Map<ICompilableBuffer, BufferStruct> compilationMapTwo,
      IInstantiation instantiationOne, ArrayList<ICondition> tempTwoConditions,
      ArrayList<IAction> tempTwoActions)
  {

    HashSet<ICompilableBuffer> buffers = new HashSet<ICompilableBuffer>();
    buffers.addAll(compilationMapOne.keySet());
    buffers.addAll(compilationMapTwo.keySet());
    for (ICompilableBuffer buffer : buffers)
    {

      Map<String, Object> bufferMap = evaluator.extractMap(
          compilationMapOne.get(buffer), compilationMapTwo.get(buffer), buffer);
      if (bufferMap == null)
      {
        LOGGER.info("Couldn't extract buffer mappings for " + buffer
            + " so stopping compilation process.  If your Evaluator/Mapper tables were consistent, and all your Mapper functions were being called correctly, you probably wouldn't be here.");
        return false;
      }
      else
      {
        LOGGER.debug("got buffer mappings: " + bufferMap);
        bufferMappings.put(buffer, bufferMap);
      }

    }

    return true;
  }

  private boolean getConditionVariables(Collection<ICondition> conditions,
      HashSet<String> variables)
  {
    for (ICondition condition : conditions)
      if (condition instanceof ChunkTypeCondition)
        for (ISlot s : ((ChunkTypeCondition) condition).getSlots(__slotList))
        {
          // first, add variable for the buffer itself
          variables.add("=" + ((ChunkTypeCondition) condition).getBufferName());
          if (s.getName().startsWith("="))
          {
            // oneVariables.add(s.getName()); //ALTHOUGH this should NEVER be
            // the case...
            LOGGER.warn(
                "Variable name in condition slot name... we don't currently handle this. "
                    + s);
            return false;
          }
          if (s.getValue() != null && s.getValue().toString().startsWith("="))
            variables.add(s.getValue().toString());
        }
      else if (condition instanceof VariableCondition)
        variables.add(((VariableCondition) condition).getVariableName());

    LOGGER.debug("condition variables are " + variables);
    return true;

  }

  // find variables that are in both production one and production 2; provide
  // mapping for renaming variables in p2
  // Hash second param is an object for compatibility with renameVariables.
  private HashMap<String, Object> findDuplicateVariableMapping(
      Map<ICompilableBuffer, BufferStruct> compilationMapOne,
      Map<ICompilableBuffer, BufferStruct> compilationMapTwo)
  {
    // Assumes (correctly?) that all variables are initially "declared" in the
    // conditions
    HashSet<String> oneVariables = new HashSet<String>();
    HashSet<String> twoVariables = new HashSet<String>();
    for (BufferStruct struct : compilationMapOne.values())
      if (!getConditionVariables(struct.conditions, oneVariables)) return null;

    for (BufferStruct struct : compilationMapTwo.values())
      if (!getConditionVariables(struct.conditions, twoVariables)) return null;

    // LOGGER.debug("variables one are " + oneVariables + " and two are " +
    // twoVariables);
    // Now find duplicates and rename them in production 2
    HashSet<String> commonVariables = new HashSet<String>();
    commonVariables.addAll(oneVariables);
    commonVariables.retainAll(twoVariables);

    // LOGGER.debug("common variables are " + commonVariables);
    HashMap<String, Object> mapping = new HashMap<String, Object>();
    String newS;
    for (String s : commonVariables)
    {
      newS = s + "-0";
      while (oneVariables.contains(newS) || twoVariables.contains(newS))
        newS = newS + "-0";

      mapping.put(s, newS);
    }

    return mapping;

  }

  /**
   * @param compilationMap
   *          - map of the production whose variables should be renamed
   * @param mapping
   *          - mapping of old->new variable names
   * @return
   */
  private boolean renameVariables(
      Map<ICompilableBuffer, BufferStruct> compilationMap,
      Map<String, Object> mapping)
  {

    if (mapping.size() > 0)
    {
      ArrayList<ICondition> newConds = new ArrayList<ICondition>();
      ArrayList<IAction> newActs = new ArrayList<IAction>();
      String name;
      Object value;
      for (BufferStruct struct : compilationMap.values())
      {

        newConds.clear();
        newActs.clear();
        ArrayList<ISlot> newSlots = new ArrayList<ISlot>();

        // First do in conditions
        for (ICondition condition : struct.conditions)
        {
          newSlots.clear();
          if (condition instanceof ChunkTypeCondition)
          {
            __slotList.clear();
            for (ISlot s : ((ChunkTypeCondition) condition)
                .getSlots(__slotList))
            {
              if (mapping.containsKey(s.getName()))
              {
                // name = mapping.get(s.getName()); //Should NEVER be the case
                // for conditions.
                LOGGER.warn(
                    "Variable name in condition slot name... we don't handle this. "
                        + s);
                return false;
              }
              else
                name = s.getName();

              if (mapping.containsKey(s.getValue())) // LOGGER.debug("mapping
                                                     // includes " +
                                                     // s.getValue() + " so
                // putting that in");
                value = mapping.get(s.getValue());
              else
                // LOGGER.debug("mapping doesn't include " + s.getValue());
                value = s.getValue();
              // don't handle the first case for conditions.
              // if(s instanceof IMutableVariableNameSlot && s instanceof
              // IMutableSlot && s instanceof BasicSlot) {
              // IMutableVariableNameSlot newSlot = (IMutableVariableNameSlot)
              // ((BasicSlot)s).clone();
              // newSlot.setName(name);
              // ((IMutableSlot)newSlot).setValue(value);
              // newSlots.add(newSlot);
              // } else
              if (s instanceof IMutableSlot && s instanceof BasicSlot)
              {
                IMutableSlot newSlot = (IMutableSlot) ((BasicSlot) s).clone();
                newSlot.setValue(value);
                newSlots.add(newSlot);
              }
              else
              {
                LOGGER.warn("Slot " + s
                    + " is not the type expected (IMutableSlot and BasicSlot), but is "
                    + s.getClass());
                return false;
              }
            }

            newConds.add(new ChunkTypeCondition(struct.bufferName,
                ((ChunkTypeCondition) condition).getChunkType(), newSlots));
          }
          else if (condition instanceof VariableCondition)
          {
            if (mapping
                .containsKey(((VariableCondition) condition).getVariableName()))
            {
              if (mapping.get(((VariableCondition) condition)
                  .getVariableName()) instanceof String)
                newConds.add(new VariableCondition(struct.bufferName,
                    (String) mapping.get(
                        ((VariableCondition) condition).getVariableName())));
              else
              {
                LOGGER.warn(
                    "Can't map a variable condition variable name to a non-string object: "
                        + condition);
                return false;
              }
            }
            else
              newConds.add(new VariableCondition(struct.bufferName,
                  ((VariableCondition) condition).getVariableName()));
          }
          else
            newConds.add(condition); // else - no variables. most likely....
                                     // hopefully.
        }
        struct.conditions.clear();
        struct.conditions.addAll(newConds);

        // Now do for actions
        for (IAction action : struct.actions)
        {
          newSlots.clear();
          if (action instanceof ModifyAction
              && !(action instanceof RemoveAction)
              || action instanceof AddAction)
          {
            __slotList.clear();
            for (ISlot s : ((org.jactr.core.slot.ISlotContainer) action)
                .getSlots(__slotList))
            {
              if (mapping.containsKey(s.getName()))
              {
                if (mapping.get(s.getName()).toString().startsWith("="))
                {
                  // name = (String) mapping.get(s.getName());
                  LOGGER.warn(
                      "Variable name in action slot name... we don't currently handle this. "
                          + s);
                  return false;
                }
                else
                {
                  LOGGER.warn(
                      "Can't map a slot name to a non-string object (and don't currently handle string object anyway): "
                          + s);
                  return false;
                }
              }
              else
                name = s.getName();

              if (mapping.containsKey(s.getValue()))
              {
                LOGGER.debug("in actions, mapping includes " + s.getValue()
                    + " so putting that in.");
                value = mapping.get(s.getValue());
              }
              else
              {
                LOGGER
                    .debug("in actions, mapping doesn't include " + s.getValue()
                        + " so putting " + name + " as " + s.getValue());
                value = s.getValue();
              }

              if (s instanceof IMutableVariableNameSlot
                  && s instanceof IMutableSlot && s instanceof BasicSlot)
              {
                IMutableVariableNameSlot newSlot = (IMutableVariableNameSlot) ((BasicSlot) s)
                    .clone();
                newSlot.setName(name);
                ((IMutableSlot) newSlot).setValue(value);
                newSlots.add(newSlot);
              }
              else if (s instanceof IMutableSlot && s instanceof BasicSlot)
              {
                IMutableSlot newSlot = (IMutableSlot) ((BasicSlot) s).clone();
                newSlot.setValue(value);
                newSlots.add(newSlot);
              }
              else
              {
                LOGGER.warn("Slot " + s
                    + " is not the type expected (IMutableSlot and BasicSlot), but is "
                    + s.getClass());
                return false;
              }
            }

            IAction newAct = null;
            if (action instanceof ModifyAction)
              newAct = new ModifyAction(struct.bufferName, newSlots);
            else
              newAct = new AddAction(struct.bufferName,
                  ((AddAction) action).getReferant(), newSlots);
            LOGGER.debug("newAct is " + newAct);
            newActs.add(newAct);
          }
          else
            // nothing to do for the others; Remove doesn't refer to anything
            // and Set is excluded from compilation
            newActs.add(action);
        }
        struct.actions.clear();
        struct.actions.addAll(newActs);

        LOGGER
            .debug("updating bindings " + struct.bindings + " with " + mapping);
        // Now do for variable bindings
        Collection<String> vars = new ArrayList<String>();
        vars.addAll(struct.bindings.getVariables());
        for (String s : vars)
          if (mapping.containsKey(s)) if (mapping.get(s) != null
              && mapping.get(s).toString().startsWith("=")) // LOGGER.debug("binding"
                                                            // +
                                                            // mapping.get(s).toString()
                                                            // + " to " +
            // struct.bindings.get(s) + " and " + struct.bindings.getSource(s));
            struct.bindings.bind(mapping.get(s).toString(),
                struct.bindings.get(s), struct.bindings.getSource(s));
          else
            // otherwise, might be a different value so update. doesn't really
            // matter though since we're getting rid of this variable anyway.
            // LOGGER.debug("binding " + s + " to " + mapping.get(s));
            struct.bindings.bind(s, mapping.get(s));
      }
    }

    return true;
  }

  protected boolean checkBuffers(IInstantiation instantiation,
      Map<ICompilableBuffer, BufferStruct> compilationMap,
      boolean canHaveReward)
  {

    if (Double.isFinite(((ISubsymbolicProduction6) instantiation.getProduction()
        .getSubsymbolicProduction()).getReward()) && !canHaveReward)
    {
      LOGGER.debug(
          "returning false because production has a finite reward and we aren't allowing rewards for this production");
      return false;
    }

    for (ICondition condition : instantiation.getProduction()
        .getSymbolicProduction().getConditions())
      if (condition instanceof IBufferCondition)
      {
        IActivationBuffer buffer = instantiation.getModel().getActivationBuffer(
            ((IBufferCondition) condition).getBufferName());
        if (!(buffer instanceof ICompilableBuffer)
            || ((ICompilableBuffer) buffer).getCompilableContext() == null)
        {
          LOGGER.debug("returning false because condition " + condition
              + " has a non-compilable buffer " + buffer);
          return false; // definitely can't compile
        }

        ICompilableBuffer cBuffer = (ICompilableBuffer) buffer;
        if (!compilationMap.containsKey(cBuffer)) compilationMap.put(cBuffer,
            new BufferStruct(cBuffer.getName(),
                buffer.isStrictHarvestingEnabled(),
                instantiation.getVariableBindings()));

        if (condition instanceof QueryCondition)
          compilationMap.put(cBuffer, compilationMap.get(cBuffer)
              .update(ProductionCompilerEvaluator.query, condition));
        else if (condition instanceof ChunkTypeCondition)
          compilationMap.put(cBuffer, compilationMap.get(cBuffer)
              .update(ProductionCompilerEvaluator.match, condition));
        else
        {
          LOGGER.debug(
              "returning false because don't currently handle conditions like "
                  + condition);
          return false;
        }
      }
      else
      {
        LOGGER.debug("returning false because condition " + condition
            + " is not a buffer condition");
        return false;
      }

    for (IAction action : instantiation.getProduction().getSymbolicProduction()
        .getActions())
      if (action instanceof IBufferAction)
      {
        IActivationBuffer buffer = instantiation.getModel()
            .getActivationBuffer(((IBufferAction) action).getBufferName());
        if (!(buffer instanceof ICompilableBuffer))
        {
          LOGGER.debug("returning false because action " + action
              + " has a non-compilable buffer " + buffer);
          return false; // definitely can't compile
        }

        ICompilableBuffer cBuffer = (ICompilableBuffer) buffer;
        if (!compilationMap.containsKey(cBuffer)) compilationMap.put(cBuffer,
            new BufferStruct(cBuffer.getName(),
                buffer.isStrictHarvestingEnabled(),
                instantiation.getVariableBindings()));

        if (action instanceof RemoveAction)
          compilationMap.put(cBuffer, compilationMap.get(cBuffer)
              .update(ProductionCompilerEvaluator.remove, action));
        else if (action instanceof ModifyAction)
          compilationMap.put(cBuffer, compilationMap.get(cBuffer)
              .update(ProductionCompilerEvaluator.modify, action));
        else if (action instanceof AddAction
            && ((AddAction) action).isDelayedRequest())
          compilationMap.put(cBuffer, compilationMap.get(cBuffer)
              .update(ProductionCompilerEvaluator.delayed_modify, action));
        else if (action instanceof AddAction
            && ((AddAction) action).getReferant() instanceof IChunkType)
          compilationMap.put(cBuffer, compilationMap.get(cBuffer)
              .update(ProductionCompilerEvaluator.add, action));
        else if (action instanceof SetAction)
        {
          LOGGER.debug(
              "returning false because not allowing set action" + action);
          return false;
        }
        else
        {
          LOGGER.debug(
              "returning false because don't currently handle actions like "
                  + action);
          return false;
        }
      }
      else if (action instanceof OutputAction)
        LOGGER.debug(
            "found OutputAction, ignoring it for production compilation.");
      else
      {
        LOGGER.debug("returning false because action " + action
            + " is not a buffer action");
        return false;
      }

    return true;
  }

  /*
   * private static Integer empty = 0; private static Integer match = 8; private
   * static Integer query = 16; private static Integer modify = 1; private
   * static Integer add = 4; private static Integer remove = 2;
   */
  /** helper struct to organize buffer-dependent knowledge **/
  public static class BufferStruct
  {
    public Integer                index;            // hash of what types of
                                                    // actions/conditions this
                                                    // buffer has

    public Collection<ICondition> conditions;

    public Collection<IAction>    actions;

    public String                 bufferName;       // just for good measure

    public boolean                strict_harvesting;

    public VariableBindings       bindings = null;

    public BufferStruct(String name, Integer i, boolean sh,
        Collection<ICondition> conds, Collection<IAction> acts,
        VariableBindings vb)
    {
      bufferName = name;
      index = i;
      conditions = conds;
      actions = acts;
      strict_harvesting = sh;
      bindings = vb;
    }

    public BufferStruct(String name, boolean sh, VariableBindings vb)
    {
      bufferName = name;
      strict_harvesting = sh;
      index = ProductionCompilerEvaluator.empty;
      conditions = new ArrayList<ICondition>();
      actions = new ArrayList<IAction>();
      bindings = vb;
    }

    public BufferStruct update(int i, ICondition c)
    {
      index = index + i;
      conditions.add(c);
      return this;
    }

    public BufferStruct update(int i, IAction a)
    {
      index = index + i;
      actions.add(a);
      return this;
    }

    public String getName()
    {
      return bufferName;
    }

    public Collection<ICondition> getConditions()
    {
      return conditions;
    }

    public Collection<IAction> getActions()
    {
      return actions;
    }

    public boolean hasStrictHarvesting()
    {
      return strict_harvesting;
    }

    public VariableBindings getVariableBindings()
    {
      return bindings;
    }

    public String bindingsToString()
    {
      String result = "[";
      for (String s : bindings.getVariables())
        result += s + "," + bindings.get(s) + ";";
      result += "]";
      return result;
    }

    public IRequest getIRequest()
    {
      for (IAction a : actions)
        if (a instanceof AddAction)
        {
          // copied from AddAction
          IRequest request = null;
          Object referant = ((AddAction) a).getReferant();

          if (referant instanceof IRequest)
            request = (IRequest) referant;
          else if (referant instanceof IChunk)
            /*
             * +buffer> chunk (or =chunk)
             */
            request = new ChunkRequest((IChunk) referant,
                ((AddAction) a).getSlots());
          else if (referant instanceof IChunkType) /*
                                                    * +buffer> isa chunk
                                                    */
            request = new ChunkTypeRequest((IChunkType) referant,
                ((AddAction) a).getSlots());
          else
            /*
             * +buffer> slot value
             */
            request = new SlotBasedRequest(((AddAction) a).getSlots());

          return request;
        }
      return null;
    }
  }

}
