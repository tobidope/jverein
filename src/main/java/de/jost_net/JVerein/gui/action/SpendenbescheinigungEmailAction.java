/**********************************************************************
* Copyright (c) by Alexander Dippe
* This program is free software: you can redistribute it and/or modify it under the terms of the 
* GNU General Public License as published by the Free Software Foundation, either version 3 of the 
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
* even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
* the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with this program.  If not, 
* see <http://www.gnu.org/licenses/>.
* 
* https://openjverein.github.io
**********************************************************************/
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;
import java.util.ArrayList;

import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * E-Mail senden Formular anhand des zugewiesenen Spenders
 */
public class SpendenbescheinigungEmailAction implements Action
{

  /**
   * Versenden einer E-Mail anhand der Spendenbescheinigung die in der View
   * markiert ist.
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    ArrayList<Mitglied> mitglieder = new ArrayList<>();
    Spendenbescheinigung[] bescheinigungen = null;
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null)
    {
      throw new ApplicationException("Keine Spendenbescheinigung ausgew�hlt.");
    }
    else if (context instanceof Spendenbescheinigung)
    {
      bescheinigungen = new Spendenbescheinigung[] {(Spendenbescheinigung) context};
    }
    else if (context instanceof Spendenbescheinigung[])
    {
      bescheinigungen = (Spendenbescheinigung[]) context;
    }
    else
    {
      return;
    }
    try
    {
      for(Spendenbescheinigung spb:bescheinigungen)
      {
        Mitglied member = spb.getMitglied();
        if (member == null)
        {
          String fehler = spb.getZeile1() + spb.getZeile2() + ": Kein Mitglied zugewiesen";
          GUI.getStatusBar().setErrorText(fehler);
          Logger.error(fehler);
          return;
        }
        mitglieder.add(spb.getMitglied());
      }
      MitgliedMailSendenAction mailSendenAction = new MitgliedMailSendenAction();
      mailSendenAction.handleAction(mitglieder.toArray(new Mitglied[mitglieder.size()]));
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler senden der Spendenbescheinigung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
