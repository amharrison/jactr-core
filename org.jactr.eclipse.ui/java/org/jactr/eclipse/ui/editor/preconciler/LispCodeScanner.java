/*
 * Created on Apr 18, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.editor.preconciler;

import java.util.ArrayList;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.jactr.eclipse.ui.editor.config.ACTRSourceViewerConfiguration;

public class LispCodeScanner extends RuleBasedScanner
{

  static private final String[] KEYWORDS = { "define-model", "add-dm", "chunk-type",
 "p", "sdp", "sgp", "spp", "extension", "import",
      "!output!", "!eval!", "!bind!", "module", "isa", "!stop!", "clear-all", "goal-focus", "==>"};

  public LispCodeScanner()
  {
    super();
    IToken keyword = new Token(new TextAttribute(ACTRSourceViewerConfiguration
        .getKeywordColor()));
    IToken string = new Token(new TextAttribute(ACTRSourceViewerConfiguration
        .getStringColor()));
    IToken comment = new Token(new TextAttribute(ACTRSourceViewerConfiguration
        .getCommentColor()));
    IToken other = new Token(new TextAttribute(ACTRSourceViewerConfiguration
        .getDefaultColor()));

    ArrayList<IRule> rules = new ArrayList<IRule>();
    rules.add(new SingleLineRule("\"", "\"", string));
    rules.add(new MultiLineRule("#|", "|#", comment));
    rules.add(new EndOfLineRule(";",comment));

    WordRule rule = new WordRule(new LispWordDetector(), other, true);
    for (String word : KEYWORDS)
      rule.addWord(word, keyword);

    rules.add(rule);
    setRules(rules.toArray(new IRule[rules.size()]));
  }
}
