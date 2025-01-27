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

import de.jost_net.JVerein.gui.view.SplitBuchungView;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.keys.SplitbuchungTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class SplitBuchungAction implements Action
{

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Buchung) && !(context instanceof Buchung[])))
    {
      throw new ApplicationException("Keine Buchung(en) ausgew�hlt");
    }
    Buchung[] bl = null;
    try
    {
      if (context instanceof Buchung)
      {
        bl = new Buchung[1];
        bl[0] = (Buchung) context;
      }
      if (context instanceof Buchung[])
      {
        bl = (Buchung[]) context;
      }
      if (bl == null)
      {
        return;
      }
      if (bl.length == 0)
      {
        return;
      }
      if (bl[0].isNewObject())
      {
        return;
      }

      for (Buchung bu : bl)
      {
        Jahresabschluss ja = bu.getJahresabschluss();
        if (ja != null)
        {
          throw new ApplicationException(String.format(
              "Buchung wurde bereits am %s von %s abgeschlossen.",
              new JVDateFormatTTMMJJJJ().format(ja.getDatum()), ja.getName()));
        }
        Spendenbescheinigung spb = bu.getSpendenbescheinigung();
        if(spb != null)
        {
          throw new ApplicationException(
              "Buchung kann nicht bearbeitet werden. Sie ist einer Spendenbescheinigung zugeordnet.");
        }
        if (bu.getBuchungsart() == null)
        {
          throw new ApplicationException(
              "Allen Buchungen muss zun�chst eine Buchungsart zugeordnet werden.");
        }
      }
      bl[0].setSplitTyp(SplitbuchungTyp.HAUPT);
      SplitbuchungsContainer.init(bl);
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e.getMessage());
    }
    GUI.startView(SplitBuchungView.class.getName(), bl[0]);
  }
}
