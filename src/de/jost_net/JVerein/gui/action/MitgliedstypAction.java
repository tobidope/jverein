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
import de.jost_net.JVerein.gui.view.MitgliedstypView;
import de.jost_net.JVerein.rmi.Adresstyp;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

public class MitgliedstypAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Adresstyp at = null;

    if (context != null && (context instanceof Adresstyp))
    {
      at = (Adresstyp) context;
      try
      {
        if (at.getJVereinid() > 0)
        {
          throw new ApplicationException(
              "Dieser Mitgliedstyp ist reserviert und darf durch den Benutzer nicht ver�ndert werden.");
        }
      }
      catch (RemoteException e)
      {
        throw new ApplicationException("Fehler", e);
      }
    }
    else
    {
      try
      {
        at = (Adresstyp) Einstellungen.getDBService().createObject(
            Adresstyp.class, null);
      }
      catch (RemoteException e)
      {
        throw new ApplicationException(
            "Fehler bei der Erzeugung eines neuen Mitgliedstypen", e);
      }
    }
    GUI.startView(MitgliedstypView.class.getName(), at);
  }
}
