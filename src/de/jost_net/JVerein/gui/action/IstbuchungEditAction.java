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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.gui.view.BuchungView;
import de.jost_net.JVerein.rmi.Buchung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class IstbuchungEditAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof MitgliedskontoNode))
    {
      throw new ApplicationException("Keine Istbuchung ausgew�hlt");
    }

    try
    {
      MitgliedskontoNode mkn = (MitgliedskontoNode) context;
      Buchung bu = (Buchung) Einstellungen.getDBService().createObject(Buchung.class,
          mkn.getID());
      GUI.startView(BuchungView.class.getName(), bu);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException("Fehler beim Editieren der Istbuchung");
    }

  }
}
