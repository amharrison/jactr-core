/*
 * Created on Feb 2, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.core.production.action;

import java.util.Collection;
import java.util.Collections;

import org.jactr.core.production.CannotInstantiateException;
import org.jactr.core.production.IInstantiation;
import org.jactr.core.production.VariableBindings;
import org.jactr.core.slot.ISlot;
import org.jactr.core.slot.ISlotContainer;
import org.slf4j.LoggerFactory;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProxyAction extends AddAction
{

  /**
   * Logger definition
   */

  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ProxyAction.class);

  String                             _className;

  IAction                            _delegateAction;

  public ProxyAction(String className) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException
  {
    super();
    setDelegateClassName(className);
  }

  public ProxyAction(Class<? extends IAction> proxyClass)
      throws InstantiationException, IllegalAccessException
  {
    this(proxyClass, Collections.EMPTY_LIST);
  }

  private ProxyAction(Class<? extends IAction> proxyClass,
      Collection<? extends ISlot> slots) throws InstantiationException,
      IllegalAccessException
  {
    super();
    setDelegateClass(proxyClass);
    for (ISlot slot : slots)
      addSlot(slot);
  }

  private ProxyAction(IAction proxy, VariableBindings variableBindings,
      Collection<? extends ISlot> slots) throws CannotInstantiateException
  {
    super();
    _className = proxy.getClass().getName();
    _delegateAction = proxy.bind(variableBindings);
    for (ISlot slot : slots)
      addSlot(slot);
  }

  public IAction getDelegate()
  {
    return _delegateAction;
  }

  @Override
  public void addSlot(ISlot slot)
  {
    if (_delegateAction instanceof ISlotContainer)
      ((ISlotContainer) _delegateAction).addSlot(slot);
    else
      super.addSlot(slot);
  }

  @Override
  public Collection<ISlot> getSlots(Collection<ISlot> container)
  {
    if (_delegateAction instanceof ISlotContainer)
      return ((ISlotContainer) _delegateAction).getSlots(container);
    else
      return super.getSlots(container);
  }

  @Override
  public void removeSlot(ISlot slot)
  {
    if (_delegateAction instanceof ISlotContainer)
      ((ISlotContainer) _delegateAction).removeSlot(slot);
    else
      super.removeSlot(slot);
  }

  @Override
  public void dispose()
  {
    super.dispose();
    if (_delegateAction != null) _delegateAction.dispose();
    _delegateAction = null;
  }

  public String getDelegateClassName()
  {
    return _className;
  }

  public void setDelegateClassName(String name) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException
  {
    Class<? extends IAction> proxyClass = (Class<? extends IAction>) getClass()
        .getClassLoader().loadClass(name);
    setDelegateClass(proxyClass);
  }

  public void setDelegateClass(Class<? extends IAction> proxyClass)
      throws InstantiationException, IllegalAccessException
  {
    _delegateAction = proxyClass.newInstance();
    _className = proxyClass.getName();
  }


  @Override
  public String toString()
  {
    return "[ProxyAction : " + _className + "]";
  }

  @Override
  public IAction bind(VariableBindings variableBindings)
      throws CannotInstantiateException
  {
    ProxyAction pa = null;
    try
    {
      pa = new ProxyAction(_delegateAction, variableBindings,
          getSlotsInternal());
    }
    catch (CannotInstantiateException cie)
    {
      throw cie;
    }
    catch (Exception e)
    {
      throw new CannotInstantiateException(String.format(
          "Could not instantiate %s because %s", getDelegateClassName(), e
              .getMessage()), e);
    }

    pa.bindSlotValues(variableBindings, pa.getSlotsInternal());
    return pa;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jactr.core.production.action.IAction#fire(org.jactr.core.model.IModel,
   * org.jactr.core.production.IProduction, java.util.Map)
   */
  @Override
  public double fire(IInstantiation instantiation, double firingTime)
  {
    VariableBindings variableBindings = instantiation.getVariableBindings();

    /*
     * we create an additional bindings to pass the slots (aka parameters) to
     * the proxy action. If the delegate is already a slot container, it will
     * already have the slots so this wont do anything
     */
    if (!(_delegateAction instanceof ISlotContainer))
    for (ISlot slot : getSlots())
    {
        variableBindings.bind(slot.getName(), slot.getValue());
        variableBindings.bind("=" + slot.getName(), slot.getValue());
    }

    return getDelegate().fire(instantiation, firingTime);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_className == null ? 0 : _className.hashCode());
    result = prime * result
        + (_delegateAction == null ? 0 : _delegateAction.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    ProxyAction other = (ProxyAction) obj;
    if (_className == null)
    {
      if (other._className != null) return false;
    }
    else if (!_className.equals(other._className)) return false;
    if (_delegateAction == null)
    {
      if (other._delegateAction != null) return false;
    }
    else if (!_delegateAction.equals(other._delegateAction)) return false;
    return true;
  }

}