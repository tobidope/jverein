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

/**
 * Combo-Box, fuer die Auswahl des Abbuchungsmodus.
 */
public class PersonenartInput extends SelectNoScrollInput
{

  public final static String NATUERLICHE_PERSON = "Nat�rliche Person";

  public final static String JURISTISCHE_PERSON = "Juristische Person (Firma, Organisation, Beh�rde)";

  public PersonenartInput(final String personenart)
  {
    super(new Object[] { NATUERLICHE_PERSON, JURISTISCHE_PERSON },
        init(personenart));
  }

  /**
   * @return initialisiert die Liste der Optionen.
   * @throws RemoteException
   */
  private static String init(String personenart)
  {
    if (personenart.equalsIgnoreCase("n"))
    {
      return NATUERLICHE_PERSON;
    }
    else if (personenart.equalsIgnoreCase("j"))
    {
      return JURISTISCHE_PERSON;
    }
    else
      return null;
  }

}
