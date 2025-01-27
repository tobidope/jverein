/**********************************************************************
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/
package de.jost_net.JVerein.gui.view;

import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeAction;
import de.jost_net.JVerein.gui.action.ZusatzbetraegeImportAction;
import de.jost_net.JVerein.gui.control.ZusatzbetragControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;

public class ZusatzbetraegelisteView extends AbstractView
{

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Zusatzbetr�ge");

    final ZusatzbetragControl control = new ZusatzbetragControl(this);

    LabelGroup group = new LabelGroup(getParent(), "Ausf�hrungstag");
    group.addLabelPair("Ausf�hrungstag", control.getAusfuehrungSuch());

    control.getZusatzbetraegeList().paint(this.getParent());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.ZUSATZBETRAEGE, false, "question-circle.png");
    buttons.addButton("Import",
        new ZusatzbetraegeImportAction(),null,false, "file-import.png");
    buttons.addButton(control.getPDFAusgabeButton());
    buttons.addButton("Neu", new ZusatzbetraegeAction(null), 
        control, false, "document-new.png");
    buttons.paint(this.getParent());
  }
}
