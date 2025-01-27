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
package de.jost_net.JVerein.io;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.Date;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.jost_net.JVerein.keys.Zahlungstermin;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.util.VonBis;
import de.willuhn.util.ApplicationException;

public class BeitragsUtil
{
  public static double getBeitrag(Beitragsmodel bm, Zahlungstermin zt, int zr,
      Beitragsgruppe bg, Date stichtag, Mitglied m)
      throws RemoteException, ApplicationException
  {
    double betr = 0;
    if (m.getEintritt() != null && m.getEintritt().after(stichtag))
    {
      return 0;
    }
    if (m.getAustritt() != null && m.getAustritt().before(stichtag))
    {
      return 0;
    }
    switch (bm)
    {
      case GLEICHERTERMINFUERALLE:
        if(Einstellungen.getEinstellung().getGeburtsdatumPflicht() && bg.getHasAltersstaffel())
        {
          Einstellungen.getEinstellung().getAltersModel();
          String stufen = Einstellungen.getEinstellung().getBeitragAltersstufen();
          if(stufen != null && stufen != "")
          {
        	if(m.getAlter() == null)
        		throw new ApplicationException(m.getName() + ", " + m.getVorname() + ": Geburtsdatum nicht vorhanden");
            AltersgruppenParser ap = new AltersgruppenParser(stufen);
            int i = 0;
            int nummer = -1;
            VonBis vb = null;
            while(ap.hasNext())
            {
              vb = ap.getNext();
              if(m.getAlter() >= vb.getVon() && m.getAlter() <= vb.getBis())
              {
                nummer = i;
                break;
              }
              i++;
            }
            if(nummer == -1)
            	throw new ApplicationException(m.getName() + ", " + m.getVorname() + ": Keine passende Altersstufe gefunden: " + m.getAlter() + " Jahre");
            try
            {
            	betr = bg.getAltersstaffel(nummer).getBetrag();
            }
            catch (NullPointerException e)
            {
            	throw new ApplicationException(
            	          "Altersstufe " + vb.getVon() + "-" + vb.getBis() + " in Beitragsgruppe " + bg.getBezeichnung() + " nicht vorhanden");
            }
          }
          else
            betr = bg.getBetrag();
        }
        else
        {
          betr = bg.getBetrag();
        }
        break;
      case MONATLICH12631:
        if(Einstellungen.getEinstellung().getGeburtsdatumPflicht() && bg.getHasAltersstaffel())
        {
          Einstellungen.getEinstellung().getAltersModel();
          String stufen = Einstellungen.getEinstellung().getBeitragAltersstufen();
          if(stufen != null && stufen != "")
          {
        	if(m.getAlter() == null)
          	  throw new ApplicationException(m.getName() + ", " + m.getVorname() + ": Geburtsdatum nicht vorhanden");
            AltersgruppenParser ap = new AltersgruppenParser(stufen);
            int i = 0;
            int nummer = -1;
            VonBis vb = null;
            while(ap.hasNext())
            {
              vb = ap.getNext();
              if(m.getAlter() >= vb.getVon() && m.getAlter() <= vb.getBis())
              {
                nummer = i;
                break;
              }
              i++;
            }
            if(nummer == -1)
            	throw new ApplicationException(m.getName() + ", " + m.getVorname() + ": Keine passende Altersstufe gefunden: " + m.getAlter() + " Jahre");
            try
            {
            	betr = bg.getAltersstaffel(nummer).getBetrag();
            }
            catch (NullPointerException e)
            {
            	throw new ApplicationException(
            	          "Altersstufe " + vb.getVon() + "-" + vb.getBis() + " in Beitragsgruppe " + bg.getBezeichnung() + " nicht vorhanden");
            }
          }
          else
            betr = bg.getBetrag();
        }
        else
        {
          betr = bg.getBetrag();
        }
        BigDecimal bbetr = BigDecimal.valueOf(betr);
        bbetr = bbetr.setScale(2, RoundingMode.HALF_UP);
        BigDecimal bmonate = BigDecimal.valueOf(zr);
        bbetr = bbetr.multiply(bmonate);
        betr = bbetr.doubleValue();
        break;
      case FLEXIBEL:
        switch (zt)
        {
          case MONATLICH:
            betr = bg.getBetragMonatlich();
            break;
          case VIERTELJAEHRLICH1:
          case VIERTELJAEHRLICH2:
          case VIERTELJAEHRLICH3:
            betr = bg.getBetragVierteljaehrlich();
            break;
          case HALBJAEHRLICH1:
          case HALBJAEHRLICH2:
          case HALBJAEHRLICH3:
          case HALBJAEHRLICH4:
          case HALBJAEHRLICH5:
          case HALBJAEHRLICH6:
            betr = bg.getBetragHalbjaehrlich();
            break;
          case JAERHLICH01:
          case JAERHLICH02:
          case JAERHLICH03:
          case JAERHLICH04:
          case JAERHLICH05:
          case JAERHLICH06:
          case JAERHLICH07:
          case JAERHLICH08:
          case JAERHLICH09:
          case JAERHLICH10:
          case JAERHLICH11:
          case JAERHLICH12:
            betr = bg.getBetragJaehrlich();
            break;
        }
        break;
    }
    return betr;
  }
}
