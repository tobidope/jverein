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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

public class Suchbetrag
{

  public enum Suchstrategie
  {
    KEINE, GLEICH, GR�SSER, GR�SSERGLEICH, KLEINER, KLEINERGLEICH, BEREICH, UNG�LTIG, BETRAG
  }

  private Suchstrategie suchstrategie = Suchstrategie.KEINE;

  private BigDecimal betrag;

  private BigDecimal betrag2 = null;

  public final static DecimalFormat DECIMALFORMAT = new DecimalFormat(
      "###,###.##");

  public Suchbetrag(String suchbetrag) throws Exception
  {
    if (suchbetrag == null || suchbetrag.length() == 0)
    {
      suchstrategie = Suchstrategie.KEINE;
      return;
    }

    // Suchstring in Einzelteile zerlegen
    ArrayList<String> liste = new ArrayList<>();
    StringTokenizer tok = new StringTokenizer(suchbetrag, "<>=.|", true);
    while (tok.hasMoreTokens())
    {
      liste.add(tok.nextToken().trim());
    }
    for (int i = 0; i < liste.size() - 1; i++)
    {
      if (liste.get(i).equals(">") && liste.get(i + 1).equals("="))
      {
        liste.set(i, ">=");
        liste.remove(i + 1);
      }
      else if (liste.get(i).equals("<") && liste.get(i + 1).equals("="))
      {
        liste.set(i, "<=");
        liste.remove(i + 1);
      }
      else if (liste.get(i).equals(".") && liste.get(i + 1).equals("."))
      {
        liste.set(i, "..");
        liste.remove(i + 1);
      }
    }
    if (liste.get(0).equals(">"))
    {
      suchstrategie = Suchstrategie.GR�SSER;
      liste.remove(0);
    }
    else if (liste.get(0).equals(">="))
    {
      suchstrategie = Suchstrategie.GR�SSERGLEICH;
      liste.remove(0);
    }
    else if (liste.get(0).equals("<"))
    {
      suchstrategie = Suchstrategie.KLEINER;
      liste.remove(0);
    }
    else if (liste.get(0).equals("<="))
    {
      suchstrategie = Suchstrategie.KLEINERGLEICH;
      liste.remove(0);
    }
    else if (liste.get(0).equals("="))
    {
      suchstrategie = Suchstrategie.GLEICH;
      liste.remove(0);
    }
    else if (liste.get(0).equals("|"))
    {
      suchstrategie = Suchstrategie.BETRAG;
      liste.remove(0);
    }
    else if (liste.size() > 1 && liste.get(1).equals(".."))
    {
      suchstrategie = Suchstrategie.BEREICH;
      liste.remove(1);
    }
    else if (liste.size() > 1 && !liste.get(1).equals(".."))
    {
      throw new Exception("Wert ung�ltig");
    }
    else if (liste.size() == 1)
    {
      suchstrategie = Suchstrategie.GLEICH; // Nur Betrag angegeben.
    }

    // jetzt muss ein Decimalwert kommen
    try
    {
      NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
      betrag = new BigDecimal(nf.parse(liste.get(0)).toString());

      if (suchstrategie == Suchstrategie.BEREICH)
      {
        betrag2 = new BigDecimal(nf.parse(liste.get(1)).toString());
      }
    }
    catch (ParseException e)
    {
      suchstrategie = Suchstrategie.UNG�LTIG;
      throw new Exception("Wert ung�ltig");
    }
  }

  public Suchstrategie getSuchstrategie()
  {
    return suchstrategie;
  }

  public BigDecimal getBetrag()
  {
    return betrag;
  }

  public BigDecimal getBetrag2()
  {
    return betrag2;
  }
}
