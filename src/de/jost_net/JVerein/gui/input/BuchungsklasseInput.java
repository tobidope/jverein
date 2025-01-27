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
package de.jost_net.JVerein.gui.input;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.BuchungsartSort;
import de.jost_net.JVerein.rmi.Buchungsklasse;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.input.SelectInput;

public class BuchungsklasseInput
{
  public SelectInput getBuchungsklasseInput(SelectInput buchungsklasse,
      Buchungsklasse klasse) throws RemoteException
  {
    DBIterator<Buchungsklasse> it = Einstellungen.getDBService()
        .createList(Buchungsklasse.class);
    if (Einstellungen.getEinstellung()
        .getBuchungsartSort() == BuchungsartSort.NACH_NUMMER)
    {
      it.setOrder("ORDER BY nummer");
    }
    else
    {
      it.setOrder("ORDER BY bezeichnung");
    }
    buchungsklasse = new SelectInput(it != null ? PseudoIterator.asList(it) : null, 
        klasse);

    switch (Einstellungen.getEinstellung().getBuchungsartSort())
    {
      case BuchungsartSort.NACH_NUMMER:
        buchungsklasse.setAttribute("nrbezeichnung");
        break;
      case BuchungsartSort.NACH_BEZEICHNUNG_NR:
        buchungsklasse.setAttribute("bezeichnungnr");
        break;
      default:
        buchungsklasse.setAttribute("bezeichnung");
        break;
    }
    buchungsklasse.setPleaseChoose("Bitte ausw�hlen");
    buchungsklasse.setValue(klasse);
    return buchungsklasse;
  }

}
