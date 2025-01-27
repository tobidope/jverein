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
import de.jost_net.JVerein.gui.view.ZusatzbetragView;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class ZusatzbetraegeAction implements Action
{

  private Mitglied m;

  public ZusatzbetraegeAction(Mitglied m)
  {
    super();
    this.m = m;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Zusatzbetrag z = null;

    if (context != null && (context instanceof Zusatzbetrag))
    {
      z = (Zusatzbetrag) context;
    }
    else
    {
      try
      {
        z = (Zusatzbetrag) Einstellungen.getDBService().createObject(
            Zusatzbetrag.class, null);
        if (m != null  && m.getID() == null)
        {
          throw new ApplicationException(
              "Neues Mitglied bitte erst speichern. Dann k�nnen Zusatzbetr�ge aufgenommen werden.");
        }

        if (m != null)
        {
          z.setMitglied(Integer.valueOf(m.getID()).intValue());
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(
            "Fehler bei der Erzeugung eines neuen Zusatzbetrages", e);
      }
    }
    GUI.startView(ZusatzbetragView.class.getName(), z);
  }
}
