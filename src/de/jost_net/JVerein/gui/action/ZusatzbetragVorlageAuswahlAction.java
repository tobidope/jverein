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

import de.jost_net.JVerein.gui.dialogs.ZusatzbetragVorlageDialog;
import de.jost_net.JVerein.gui.parts.ZusatzbetragPart;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.ZusatzbetragVorlage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ZusatzbetragVorlageAuswahlAction implements Action
{

  private ZusatzbetragPart part;

  public ZusatzbetragVorlageAuswahlAction(ZusatzbetragPart part)
  {
    this.part = part;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    try
    {
      ZusatzbetragVorlageDialog zbvd = new ZusatzbetragVorlageDialog();
      ZusatzbetragVorlage zbv = zbvd.open();
      if (zbv != null)
      {
        part.getBetrag().setValue(zbv.getBetrag());
        part.getBuchungstext().setValue(zbv.getBuchungstext());
        part.getEndedatum().setValue(zbv.getEndedatum());
        part.getFaelligkeit().setValue(zbv.getFaelligkeit());
        part.getIntervall().setValue(zbv.getIntervall());
        part.getBuchungsart().setValue(zbv.getBuchungsart());
        if (part.isBuchungsklasseActive())
        {
          part.getBuchungsklasse().setValue(zbv.getBuchungsklasse());
        }
        for (Object obj : part.getIntervall().getList())
        {
          IntervallZusatzzahlung ivz = (IntervallZusatzzahlung) obj;
          if (zbv.getIntervall() == ivz.getKey())
          {
            part.getIntervall().setPreselected(ivz);
            break;
          }
        }
        if (zbv.getZahlungsweg().getKey() == Zahlungsweg.STANDARD)
        {
          part.getZahlungsweg().setValue(null);
        }
        else
        {
          part.getZahlungsweg().setValue(zbv.getZahlungsweg());
        }
        part.getStartdatum(false).setValue(zbv.getStartdatum());
      }
    }
    catch (OperationCanceledException oce)
    {
      throw oce;
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      GUI.getStatusBar()
          .setErrorText("Fehler bei der Zusatzbeitrag Vorlagen Auswahl");
    }
  }
}
