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
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedNextBGruppe;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * L�schen einer Beitragsgruppe
 */
public class BeitragsgruppeDeleteAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context instanceof TablePart)
    {
      TablePart tp = (TablePart) context;
      context = tp.getSelection();
    }
    if (context == null || !(context instanceof Beitragsgruppe))
    {
      throw new ApplicationException("Keine Beitragsgruppe ausgew�hlt");
    }
    try
    {
      Beitragsgruppe bg = (Beitragsgruppe) context;
      if (bg.isNewObject())
      {
        return;
      }
      if(bg.getSekundaer())
      {
        DBIterator<SekundaereBeitragsgruppe> sek = Einstellungen.getDBService()
            .createList(SekundaereBeitragsgruppe.class);
        sek.addFilter("beitragsgruppe = ?", new Object[] { bg.getID() });
        if (sek.size() > 0)
        {
          throw new ApplicationException(String.format(
              "Beitragsgruppe '%s' kann nicht gel�scht werden. %d Mitglied(er) sind dieser sekund�ren Beitragsgruppe zugeordnet.",
              bg.getBezeichnung(), sek.size()));
        }
      }
      else
      {
        DBIterator<Mitglied> mitgl = Einstellungen.getDBService()
            .createList(Mitglied.class);
        mitgl.addFilter("beitragsgruppe = ?", new Object[] { bg.getID() });
        if (mitgl.size() > 0)
        {
          throw new ApplicationException(String.format(
              "Beitragsgruppe '%s' kann nicht gel�scht werden. %d Mitglied(er) sind dieser Beitragsgruppe zugeordnet.",
              bg.getBezeichnung(), mitgl.size()));
        }
      }
      DBIterator<MitgliedNextBGruppe> nextbg = Einstellungen.getDBService()
          .createList(MitgliedNextBGruppe.class);
      nextbg.addFilter("beitragsgruppe = ?", new Object[] { bg.getID() });
      if (nextbg.size() > 0)
      {
        throw new ApplicationException(String.format(
            "Beitragsgruppe '%s' kann nicht gel�scht werden. Bei %d Mitglied(er) ist diese Beitragsgruppe als zuk�nftige Beitragsgrupe hinterlegt.",
            bg.getBezeichnung(), nextbg.size()));
      }
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Beitragsgruppe l�schen");
      d.setText("Wollen Sie diese Beitragsgruppe wirklich l�schen?");

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
        Logger.error(String.format("Fehler beim L�schen der Beitragsgruppe: %s",
            new Object[] { e.getMessage() }));
        return;
      }
      bg.delete();
      GUI.getStatusBar().setSuccessText("Beitragsgruppe gel�scht.");
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim L�schen der Beitragsgruppe";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
  }
}
