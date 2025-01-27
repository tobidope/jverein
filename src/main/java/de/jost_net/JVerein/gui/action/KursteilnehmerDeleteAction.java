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

import de.jost_net.JVerein.rmi.Kursteilnehmer;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Einmal-Abbuchung.
 */
public class KursteilnehmerDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Kursteilnehmer[] kursteilnehmer = null;
    if (context == null)
    {
      throw new ApplicationException("Keinen Kursteilnehmer ausgew�hlt");
    }
    else if(context instanceof Kursteilnehmer)
    {
      kursteilnehmer = new Kursteilnehmer[] {(Kursteilnehmer)context};
    }
    else if(context instanceof Kursteilnehmer[])
    {
      kursteilnehmer = (Kursteilnehmer[])context;
    }
    else
    {
      return;
    }
    try
    {
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Kursteilnehmer l�schen");
      d.setText("Wollen Sie diese" + (kursteilnehmer.length > 1?"":"n")+ " Kursteilnehmer wirklich l�schen?");

      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim L�schen des Kursteilnehmers", e);
        return;
      }
      for(Kursteilnehmer kt:kursteilnehmer)
      {
        if(kt.isNewObject())
          continue;
        kt.delete();
      }
      GUI.getStatusBar().setSuccessText("Kursteilnehmer gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen des Kursteilnehmers";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
