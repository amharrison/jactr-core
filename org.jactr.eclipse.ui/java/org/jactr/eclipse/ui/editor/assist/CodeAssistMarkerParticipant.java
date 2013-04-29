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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.text.Position;
import org.jactr.eclipse.ui.editor.markers.ASTPosition;
import org.jactr.eclipse.ui.editor.markers.IPositionMarkerParticipant;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class CodeAssistMarkerParticipant implements IPositionMarkerParticipant
{

  static private final Collection<Integer> _types = new ArrayList<Integer>();

  static
  {
    _types.add(JACTRBuilder.SLOT);
    _types.add(JACTRBuilder.QUERY_CONDITION);
    _types.add(JACTRBuilder.ADD_ACTION);
    _types.add(JACTRBuilder.REMOVE_ACTION);
    _types.add(JACTRBuilder.MODIFY_ACTION);
    _types.add(JACTRBuilder.OUTPUT_ACTION);
    _types.add(JACTRBuilder.MATCH_CONDITION);
    _types.add(JACTRBuilder.PROXY_ACTION);
    _types.add(JACTRBuilder.PROXY_CONDITION);
    _types.add(JACTRBuilder.SCRIPTABLE_ACTION);
    _types.add(JACTRBuilder.SCRIPTABLE_CONDITION);
  }

  public Collection<Integer> getRelevantTypes()
  {
    return Collections.unmodifiableCollection(_types);
  }

  public void positionCreated(ASTPosition position)
  {
    // TODO Auto-generated method stub

  }

  public void positionsCleared(Position[] positions)
  {
    // TODO Auto-generated method stub

  }

  public void postParse()
  {
    // TODO Auto-generated method stub

  }

  public void preParse()
  {
    // TODO Auto-generated method stub

  }
}
