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
package org.jactr.eclipse.ui.editor.markers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.parser.ITreeTracker;

/**
 * @author developer
 */
public class PositionMarker implements ITreeTracker
{

  static public final String                       POSITION_ID       = "__jactr__default__positions";

  /**
   * Logger definition
   */

  static private final transient Log               LOGGER            = LogFactory
                                                                         .getLog(PositionMarker.class);

  protected IDocument                              _document;

  protected String                                 _locationID;

  protected Collection<IPositionMarkerParticipant> _participants     = new ArrayList<IPositionMarkerParticipant>();

  protected Set<Integer>                           _relevantTypes    = new TreeSet<Integer>();

  private IPositionUpdater                         _positionUpdater;

  private URL                                      _baseURL;

  private final Collection<ASTPosition>            _createdPositions = FastList
                                                                         .newInstance();

  private final Collection<ASTPosition>            _orphanPositions  = FastList
                                                                         .newInstance();
  
  private Collection<ASTPosition>                  _oldPositions     = FastList
                                                                         .newInstance();

  public PositionMarker()
  {
    setLocationID(POSITION_ID);
    _relevantTypes.add(JACTRBuilder.MODEL);
    _relevantTypes.add(JACTRBuilder.MODULES);
    _relevantTypes.add(JACTRBuilder.MODULE);
    _relevantTypes.add(JACTRBuilder.EXTENSIONS);
    _relevantTypes.add(JACTRBuilder.EXTENSION);
    _relevantTypes.add(JACTRBuilder.DECLARATIVE_MEMORY);
    _relevantTypes.add(JACTRBuilder.PROCEDURAL_MEMORY);

    _relevantTypes.add(JACTRBuilder.PARAMETERS);

    _relevantTypes.add(JACTRBuilder.CHUNK);
    _relevantTypes.add(JACTRBuilder.CHUNK_TYPE);
    _relevantTypes.add(JACTRBuilder.BUFFER);

    _relevantTypes.add(JACTRBuilder.PRODUCTION);
    _relevantTypes.add(JACTRBuilder.ACTIONS);
    _relevantTypes.add(JACTRBuilder.CONDITIONS);

  }

  public void setBase(URL url)
  {
    _baseURL = url;
  }

  public IDocument getDocument()
  {
    return _document;
  }

  public void setDocument(IDocument document)
  {
    _document = document;
  }

  public void setLocationID(String id)
  {
    _locationID = id;
  }

  public String getLocationID()
  {
    return _locationID;
  }

  public Collection<Integer> getRelevantTypes()
  {
    return _relevantTypes;
  }

  public void addParticipant(IPositionMarkerParticipant participant)
  {
    _relevantTypes.addAll(participant.getRelevantTypes());
    _participants.add(participant);
  }

  public void treeAssembled(CommonTree node)
  {
    /*
     * by default this will mark the position
     */
    Region span = getTreeSpan(node, _baseURL);
    if (span != null)
    {
      ASTPosition position = createPosition(span, node);
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("creating position(" + getLocationID() + ") " + position);

      if (position == null)
      {
        CorePlugin.error("Null position was create for " + node,
            new RuntimeException());
        return;
      }

      _createdPositions.add(position);

      /*
       * check the created positions and set up the child hierarchies, but only
       * if the node is not an import
       */
      Iterator<ASTPosition> previous = _orphanPositions.iterator();
      while (previous.hasNext())
      {
        ASTPosition pos = previous.next();
        URL posURL = pos.getBase();
        if (posURL != null && _baseURL.equals(posURL)
            && position.contains(pos.getOffset()))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(position + " contains " + pos);
          position.addChild(pos);
          previous.remove();
        }
      }

      _orphanPositions.add(position);

      /*
       * notify
       */
      for (IPositionMarkerParticipant participant : _participants)
        if (participant.getRelevantTypes().contains(node.getType()))
          participant.positionCreated(position);
    }
  }

  public void postParse(boolean parseHadErrors)
  {
    _orphanPositions.clear();
    
    if (parseHadErrors) _createdPositions.clear();
    
    if (_createdPositions.size() != 0)
    {
      removePositions();
      
      /*
       * install the positions in the document
       */

      _oldPositions.addAll(_createdPositions);
      _createdPositions.clear();

      Runnable adder = new Runnable() {
        public void run()
        {
          String id = getLocationID();
          for (Position position : _oldPositions)
            try
            {
              _document.addPosition(id, position);
            }
            catch (BadLocationException e)
            {
              CorePlugin.debug(
                  "PositionMarker.postParse.run threw BadLocationException : ",
                  e);
            }
            catch (BadPositionCategoryException e)
            {
              CorePlugin.debug(".run threw BadPositionCategoryException : ", e);
            }
        }
      };

      UIPlugin.getStandardDisplay().asyncExec(adder);
    }

    for (IPositionMarkerParticipant participant : _participants)
      participant.postParse();
  }

  public void preParse()
  {
    /*
     * reset everthing
     */
    if (_positionUpdater == null)
      _positionUpdater = installUpdater();

    for (IPositionMarkerParticipant participant : _participants)
      participant.preParse();
  }

  protected IPositionUpdater installUpdater()
  {
    try
    {
      IPositionUpdater updater = new DefaultPositionUpdater(getLocationID());
      _document.addPositionCategory(getLocationID());
      _document.addPositionUpdater(updater);
      return updater;
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not install position information", e);
      return null;
    }
  }

  private void removePositions()
  {

    if (_oldPositions.size() != 0)
    {
      /*
       * install the positions in the document
       */
      final Position[] positions = _oldPositions
          .toArray(new Position[_oldPositions.size()]);

      _oldPositions.clear();

      Runnable remover = new Runnable() {
        public void run()
        {
          String id = getLocationID();
          for (Position position : positions)
            try
            {
              _document.removePosition(id, position);
            }
            catch (BadPositionCategoryException e)
            {
              CorePlugin.error(".run threw BadPositionCategoryException : ", e);
            }
        }
      };

      UIPlugin.getStandardDisplay().asyncExec(remover);

      for (IPositionMarkerParticipant participant : _participants)
        participant.positionsCleared(positions);
    }

  }

  protected ASTPosition createPosition(Region span, CommonTree node)
  {
    return new ASTPosition(span.getOffset(), span.getLength(), node);
  }

  /**
   * return the smallest position containing offset
   */
  static public ASTPosition getPosition(IDocument document, URL base, int offset)
  {
    ASTPosition bestPosition = null;
    int size = Integer.MAX_VALUE;
    try
    {
      Position[] positions = document.getPositions(POSITION_ID);
      for (Position position : positions)
        if (position instanceof ASTPosition)
        {
          ASTPosition ap = (ASTPosition) position;

          if (base != null && !base.equals(ap.getBase())) continue;

          if (ap.contains(offset) && ap.getLength() < size)
          {
            bestPosition = ap;
            size = position.getLength();
          }
        }
    }
    catch (BadPositionCategoryException bpce)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Could not get position information", bpce);
    }
    return bestPosition;
  }

  static public Region getNodeSpan(CommonTree node, URL base)
  {
    int[] rtn = Support.getNodeOffsets(node, base);
    if (rtn == null) return null;
    return new Region(rtn[0], rtn[1] - rtn[0]);
  }

  static public Region getTreeSpan(CommonTree node, URL base)
  {
    int[] rtn = Support.getTreeOffsets(node, base);
    if (rtn == null) return null;
    return new Region(rtn[0], rtn[1] - rtn[0]);
  }

}
