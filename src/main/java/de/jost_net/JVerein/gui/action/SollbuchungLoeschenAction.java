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
import de.jost_net.JVerein.Messaging.MitgliedskontoMessage;
import de.jost_net.JVerein.gui.control.MitgliedskontoNode;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SollbuchungLoeschenAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !((context instanceof MitgliedskontoNode)
        || context instanceof Mitgliedskonto))
    {
      throw new ApplicationException("Keine Sollbuchung ausgew�hlt");
    }
  	
    YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
    d.setTitle("Sollbuchung l�schen");
    d.setText("Wollen Sie die Sollbuchung wirklich l�schen?");

    try
    {
      Boolean choice = (Boolean) d.open();
      if (!choice.booleanValue())
      {
        return;
      }
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      return;
    }
    MitgliedskontoNode mkn = null;
    Mitgliedskonto mk = null;
    try
    {
      if (context instanceof MitgliedskontoNode)
      {
        mkn = (MitgliedskontoNode) context;
        mk = (Mitgliedskonto) Einstellungen.getDBService().createObject(
            Mitgliedskonto.class, mkn.getID());
      }
      else
      {
        mk = (Mitgliedskonto) context;
      }
      Mitglied mitglied = mk.getMitglied();
      mk.delete();
      GUI.getStatusBar().setSuccessText("Sollbuchung gel�scht.");
      Application.getMessagingFactory().sendMessage(
          new MitgliedskontoMessage(mitglied));
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(
          "Fehler beim L�schen einer Sollbuchung");
    }
  }
}
