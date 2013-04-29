/*
 * Created on Mar 18, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.debug.elements;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class ASTVariable extends ACTRDebugElement implements IVariable, IValue
{

  /**
   * Logger definition
   */

  static private final transient Log            LOGGER    = LogFactory
                                                              .getLog(ASTVariable.class);

  private final CommonTree                      _ast;

  private String                                _variableName;

  private String                                _variableType;

  private String                                _variableValue;

  private SoftReference<IVariable[]> _children = new SoftReference<IVariable[]>(
                                                              null);

  public ASTVariable(CommonTree ast, ACTRDebugTarget target)
  {
    setDebugTarget(target);
    _ast = ast;
    extractDisplayTexts();

    if (_ast == null) LOGGER.error("NULL AST?", new NullPointerException());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Variable created " + _variableName + " ");
  }

  public ASTVariable(CommonTree ast, String variableValue,
      ACTRDebugTarget target)
  {
    this(ast, target);
    setVariableValue(variableValue);
  }

  public CommonTree getCommonTree()
  {
    return _ast;
  }

  public void setReferenceTypeName(String variableType)
  {
    _variableType = variableType;
  }

  public void setName(String name)
  {
    _variableName = name;
  }

  public void setVariableValue(String value)
  {
    _variableValue = value;
  }

  public String getName() throws DebugException
  {
    return _variableName;
  }

  public String getReferenceTypeName() throws DebugException
  {
    return _variableType;
  }

  public IValue getValue() throws DebugException
  {
    return this;
  }

  public boolean hasValueChanged() throws DebugException
  {
    return false;
  }

  public void setValue(String expression) throws DebugException
  {
    

  }

  public void setValue(IValue value) throws DebugException
  {
   

  }

  public boolean supportsValueModification()
  {
    return false;
  }

  public boolean verifyValue(String expression) throws DebugException
  {
    return false;
  }

  public boolean verifyValue(IValue value) throws DebugException
  {
    return false;
  }

  public String getValueString() throws DebugException
  {
    return _variableValue;
  }

 

  public IVariable[] getVariables() throws DebugException
  {
    IVariable[] children = _children.get();
    if (children == null)
    {
      ArrayList<IVariable> variables = new ArrayList<IVariable>();
      for (CommonTree child : Support.getVisibleChildren(_ast))
        variables.add(new ASTVariable(child, _target));

      children = variables.toArray(new IVariable[variables.size()]);
      _children = new SoftReference<IVariable[]>(children);
    }
    
    return children;
  }

  public boolean hasVariables() throws DebugException
  {
    return getVariables().length != 0;
  }

  public boolean isAllocated() throws DebugException
  {
    return true;
  }

  /**
   * we actually rely upon the ACTRModelPresentation to display the icon, and
   * name we just need to set the variable value
   */
  protected void extractDisplayTexts()
  {

    String name = _ast.getText();

    String tokenType = "["
        + JACTRBuilder.tokenNames[_ast.getType()].toLowerCase() + "]";
    setReferenceTypeName(tokenType);

    CommonTree nameNode = null;
    for (int i = 0; i < _ast.getChildCount() && nameNode == null; i++)
    {
      nameNode = (CommonTree) _ast.getChild(i);
      if (nameNode.getType() != JACTRBuilder.NAME) nameNode = null;
    }

    int type = _ast.getType();

    if (nameNode != null) name = nameNode.getText();

    setName(name);

    switch (type)
    {

      case JACTRBuilder.SLOT:
        setVariableValue(_ast.getChild(2).getText());
        break;
      case JACTRBuilder.PARAMETER:
        setVariableValue(_ast.getChild(1).getText());
        break;
      default:
        break;
    }

  }
}
