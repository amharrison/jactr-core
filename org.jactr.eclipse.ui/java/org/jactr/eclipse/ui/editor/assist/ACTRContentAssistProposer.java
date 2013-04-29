/*
 * Created on Apr 19, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.editor.assist;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Point;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.markers.ASTPosition;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.eclipse.ui.preferences.UIPreferences;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ACTRContentAssistProposer implements IContentAssistProcessor
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER                 = LogFactory
                                                                .getLog(ACTRContentAssistProposer.class);

  private final ACTRModelEditor      _editor;

  private boolean                    _autoActivationEnabled = false;

  static private char[]              AUTO_ACTIVATION_CHARS  = "abcdefghijklmnopqrstuvwxyz=-:.ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
                                                                .toCharArray();

  public ACTRContentAssistProposer(ACTRModelEditor editor)
  {
    _editor = editor;
    _autoActivationEnabled = UIPlugin.getDefault().getPluginPreferences()
        .getBoolean(UIPreferences.ENABLE_AUTO_ACTIVATE_PREF);
  }

  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
      int offset)
  {
    try
    {
      IRegion region = getPrefixRegion(viewer, offset);
      String prefix = getPrefix(viewer, region).toLowerCase();

      ASTPosition position = getContextualPosition(viewer, offset);

      if (LOGGER.isDebugEnabled())
        LOGGER
            .debug(String
                .format(
                    "computing proposals for %d in (%d-%d), prefixed:%s, ASTPosition:%d",
                    offset, region.getOffset(),
                    region.getOffset() + region.getLength(), prefix,
                    position != null ? position.getNode().getType() : -1));

      Map<String, CommonTree> recommendations = getRecommendationsUsingPositions(
          position, viewer, offset, prefix);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Yielded %d recommendations",
            recommendations.size()));

      if (recommendations.size() == 0) return null;

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Proposing " + recommendations);

      Point selection = viewer.getSelectedRange();

      TreeMap<CommonTree, ICompletionProposal> proposals = new TreeMap<CommonTree, ICompletionProposal>(
          new Comparator<CommonTree>() {

            public int compare(CommonTree o1, CommonTree o2)
            {
              if (o1 == o2) return 0;
              try
              {
                int compare = ASTSupport.getName(o1).compareToIgnoreCase(
                    ASTSupport.getName(o2));
                if (compare != 0) return compare;
              }
              catch (Exception e)
              {
                // its a variable (no name node)
              }
              // compare types..
              int o1Type = o1.getType();
              int o2Type = o2.getType();
              if (o1Type < o2Type) return -1;
              if (o1Type > o2Type) return 1;
              return o1.hashCode() < o2.hashCode() ? -1 : 1;
            }

          });
      // TreeMap<String, ICompletionProposal> proposals = new TreeMap<String,
      // ICompletionProposal>();

      for (Map.Entry<String, CommonTree> entry : recommendations.entrySet())
      {
        int start = Math.min(region.getOffset(), selection.x);
        int length = region.getLength() + selection.y;
        CommonTree node = entry.getValue();
        String textToInsert = entry.getKey();
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Initial proposal (" + prefix + ") : " + textToInsert
              + " at " + start + " replacing " + length);

        if (node != null)
          try
          {
            textToInsert = ASTSupport.getName(entry.getValue());
            if (prefix.length() > 0 && textToInsert.startsWith(prefix))
            {
              if (LOGGER.isDebugEnabled())
                LOGGER.debug("shifting start and length " + region.getLength());
              textToInsert = textToInsert.substring(region.getLength());
              start += region.getLength();
              length -= region.getLength();
            }
          }
          catch (Exception e)
          {
          }

        if (textToInsert.length() == 0) continue;

        if (textToInsert.equals(prefix)) continue;

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Proposing : " + textToInsert + " at " + start
              + " replacing " + length);

        /*
         * note: the displayString is not being used, but rather the
         * textToInsert. this is because for some reason, the display string is
         * being used for the intermediary completion (say, when there are
         * multiple completions that might work). don't know why..
         */
        ACTRCompletionProposal proposal = new ACTRCompletionProposal(
            textToInsert, start, length, textToInsert.length(),
            ACTRLabelProvider.getImageOfAST(node), textToInsert, null, null,
            true);
        proposals.put(node, proposal);
      }

      return proposals.values().toArray(
          new ICompletionProposal[proposals.size()]);
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("something went wrong ", e);
    }

    return null;
  }

  protected ASTPosition getContextualPosition(ITextViewer viewer, int offset)
  {
    ASTPosition position = PositionMarker.getPosition(viewer.getDocument(),
        _editor.getBase(), offset);
    return position;
  }

  protected Map<String, CommonTree> getRecommendationsUsingPositions(
      ASTPosition position, ITextViewer viewer, int offset, String prefix)
  {

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("returned " + position + " for offset: " + offset
          + " in document " + _editor.getBase());

    if (position == null) return Collections.EMPTY_MAP;

    Map<String, CommonTree> props = ProposalGenerator.generateProposals(
        position, offset, _editor.getCompilationUnit());

    Iterator<String> keys = props.keySet().iterator();
    while (keys.hasNext())
    {
      String key = keys.next();
      if (key.length() == 0 || prefix != null && prefix.length() != 0
          && !key.toLowerCase().startsWith(prefix)) keys.remove();
    }

    return props;
  }

  private IRegion getPrefixRegion(ITextViewer viewer, int offset)
      throws BadLocationException
  {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) return null;

    int length = 0;
    char current = 0;
    while (--offset >= 0
        && (current = doc.getChar(offset)) != 0
        && (Character.isJavaIdentifierPart(current) || current == '-' || current == '='))
      length++;

    return new Region(offset + 1, length);
  }

  private String getPrefix(ITextViewer viewer, IRegion region)
      throws BadLocationException
  {
    return viewer.getDocument().get(region.getOffset(), region.getLength());
  }

  public IContextInformation[] computeContextInformation(ITextViewer viewer,
      int offset)
  {
    return null;
  }

  public char[] getCompletionProposalAutoActivationCharacters()
  {
    if (_autoActivationEnabled) return AUTO_ACTIVATION_CHARS;
    return null;
  }

  public char[] getContextInformationAutoActivationCharacters()
  {
    return null;
  }

  public IContextInformationValidator getContextInformationValidator()
  {
    return null;
  }

  public String getErrorMessage()
  {
    return null;
  }

}
