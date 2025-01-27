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
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Loeschen einer Buchungsart.
 */
public class KontoDeleteAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null || !(context instanceof Konto))
    {
      throw new ApplicationException("Kein Konto ausgew�hlt");
    }
    try
    {
      Konto k = (Konto) context;
      if (k.isNewObject())
      {
        return;
      }
      DBIterator<Buchung> it = Einstellungen.getDBService()
          .createList(Buchung.class);
      it.addFilter("konto = ?", new Object[] { k.getID() });
      it.setLimit(1);
      if (it.hasNext())
      {
        throw new ApplicationException(String.format(
            "Konto '%s' kann nicht gel�scht werden. Es existieren Buchungen auf diesem Konto.",
            k.getBezeichnung()));
      }
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Konto l�schen");
      d.setText("Wollen Sie dieses Konto wirklich l�schen?");
      try
      {
        Boolean choice = (Boolean) d.open();
        if (!choice.booleanValue())
          return;
      }
      catch (Exception e)
      {
        Logger.error("Fehler beim L�schen des Kontos", e);
        return;
      }

      k.delete();
      GUI.getStatusBar().setSuccessText("Konto gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen des Kontos.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
