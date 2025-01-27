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
import de.jost_net.JVerein.rmi.Adresstyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen eines Mitgliedstyp.
 */
public class MitgliedstypDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Adresstyp))
    {
      throw new ApplicationException("Kein Mitgliedstyp ausgew�hlt");
    }
    try
    {
      Adresstyp at = (Adresstyp) context;
      if (at.getJVereinid() > 0)
      {
        throw new ApplicationException(
            "Dieser Mitgliedstyp darf nicht gel�scht werden");
      }
      if (at.isNewObject())
      {
        return;
      }
      DBIterator<Mitglied> it = Einstellungen.getDBService()
          .createList(Mitglied.class);
      it.addFilter("adresstyp = ?", new Object[] { at.getID() });
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(String.format(
            "Mitgliedstyp '%s' kann nicht gel�scht werden. Es existieren Nicht-Mitglieder dieses Typs.",
            at.getBezeichnung()));
      }
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Mitgliedstyp l�schen");
      d.setText("Wollen Sie diesen Mitgliedstyp wirklich l�schen?");
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim L�schen eines Mitgliedstyp", e);
        return;
      }

      at.delete();
      GUI.getStatusBar().setSuccessText("Mitgliedstyp gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen des Mitgliedstyp.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
