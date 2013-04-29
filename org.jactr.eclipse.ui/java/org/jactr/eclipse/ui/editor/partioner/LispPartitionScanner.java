/*
 * Created on Apr 17, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.editor.partioner;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * jactr partition scanner
 * 
 * @author developer
 */
public class LispPartitionScanner extends RuleBasedPartitionScanner
{

  static private LispPartitionScanner _default = new LispPartitionScanner();

  static public LispPartitionScanner getInstance()
  {
    return _default;
  }

  public LispPartitionScanner()
  {
    super();
    IToken comment = new Token(LispPartitions.COMMENT);
    IToken string = new Token(LispPartitions.STRING);

    ArrayList<IPredicateRule> rules = new ArrayList<IPredicateRule>();
    rules.add(new MultiLineRule("#|", "|#", comment));
    rules.add(new EndOfLineRule(";", comment));

    rules.add(new SingleLineRule("\"", "\"", string));
    setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
  }
}
