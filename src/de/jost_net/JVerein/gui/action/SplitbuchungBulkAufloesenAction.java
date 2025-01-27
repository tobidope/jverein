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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Messaging.BuchungMessage;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Jahresabschluss;
import de.jost_net.JVerein.rmi.Spendenbescheinigung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SplitbuchungBulkAufloesenAction implements Action
{
  private ArrayList<Long> geloescht = new ArrayList<>();
  private ArrayList<Long> schongeprueft = new ArrayList<>();
  private Long splitid;
  
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null
        || (!(context instanceof Buchung) && !(context instanceof Buchung[])))
    {
      throw new ApplicationException("Keine Buchung ausgew�hlt");
    }
    try
    {
      Buchung[] b = null;
      if (context instanceof Buchung)
      {
        b = new Buchung[1];
        b[0] = (Buchung) context;
      }
      else if (context instanceof Buchung[])
      {
        b = (Buchung[]) context;
      }
      if (b == null)
      {
        return;
      }
      if (b.length == 0)
      {
        return;
      }
      if (b[0].isNewObject())
      {
        return;
      }
      
      boolean spendenbescheinigung = false;
      for (Buchung splitbu : b)
      {
        // Es k�nnen mehrere der gleichen Splittbuchung selektiert worden sein
        // Die Pr�fung reicht einmal pro SplitId
        if (!schongeprueft.contains(splitbu.getSplitId()))
        {
          // Check ob einer der Buchungen der Splittbuchung
          // eine Spendenbescheinigung zugeordnet ist
          final DBService service = Einstellungen.getDBService();
          String sql = "SELECT DISTINCT buchung.id from buchung "
              + "WHERE (splitid = ? and spendenbescheinigung IS NOT NULL) ";
          spendenbescheinigung = (boolean) service.execute(sql,
              new Object[] { splitbu.getSplitId() }, new ResultSetExtractor()
          {
            @Override
            public Object extract(ResultSet rs)
                throws RemoteException, SQLException
            {
              if (rs.next())
              {
                return true;
              }
              return false;
            }
          });
          if (spendenbescheinigung)
            break;
          schongeprueft.add(splitbu.getSplitId());
        }
      }
      
      String text = "";
      if (!spendenbescheinigung)
      {
        text = "Wollen Sie diese Splituchung" + (b.length > 1 ? "en" : "")
            + " wirklich aufl�sen?";
      }
      else
      {
        if (b.length == 1)
        {
          text = "Die Splitbuchung enth�lt Buchungen denen eine "
              + "Spendenbescheinigung zugeordnet ist.\n"
              + "Sie k�nnen nur zusammen gel�scht werden.\n"
              + "Splitbuchung aufl�sen und Spendenbescheinigungen l�schen?";
        }
        else
        {
          text = "Mindestens eine Splitbuchung enth�lt Buchungen denen "
              + "eine Spendenbescheinigung zugeordnet ist.\n"
              + "Sie k�nnen nur zusammen gel�scht werden.\n"
              + "Splitbuchungen aufl�sen und Spendenbescheinigungen l�schen?";
        }
      }
      
      YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
      d.setTitle("Splitbuchung" + (b.length > 1 ? "en" : "") + " aufl�sen");
      d.setText(text);
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
        Logger.error("Fehler beim Aufl�sen der Splituchung", e);
        return;
      }
      
      for (Buchung bu : b)
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
        splitid = bu.getSplitId();
        if (!geloescht.contains(splitid))
        {
          SplitbuchungsContainer.init(bu);
          SplitbuchungsContainer.aufloesen();
          geloescht.add(splitid);
        }
      }
      int count = geloescht.size();
      if (count > 0)
      {
        GUI.getStatusBar().setSuccessText(String.format(
            "%d Splituchung" + (count != 1 ? "en" : "") + " aufgel�st.", count));
      }
      else
      {
        GUI.getStatusBar().setErrorText("Keine Splituchung aufgel�st");
      }
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler beim Aufl�sen einer Splituchung.";
      GUI.getStatusBar().setErrorText(fehler);
      Logger.error(fehler, e);
    }
    finally
    {
      Application.getMessagingFactory().sendMessage(new BuchungMessage(null));
    }
  }
}
