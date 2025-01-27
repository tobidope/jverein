/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung f�r Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2015 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify 
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.server.DDLTool.Updates;

import java.sql.Connection;

import de.jost_net.JVerein.server.DDLTool.AbstractDDLUpdate;
import de.jost_net.JVerein.server.DDLTool.Column;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class Update0395 extends AbstractDDLUpdate
{
  public Update0395(String driver, ProgressMonitor monitor, Connection conn)
  {
    super(driver, monitor, conn);
  }

  @Override
  public void run() throws ApplicationException
  {

    // Ein Abrechnungslauf kann abgeschlossen werden, damit
    // er nicht mehr �ber die Oberfl�che gel�scht werden kann
    // Zus�tzlich gibt es noch ein neues Bemerkungsfeld

    execute(addColumn("abrechnungslauf",
        new Column("bemerkung", COLTYPE.VARCHAR, 80, null, false, false)));

    execute(addColumn("abrechnungslauf",
        new Column("abgeschlossen", COLTYPE.BOOLEAN, 0, "FALSE", true, false)));

    // Ein- uns Ausschalten der Abschlie�en-Funktion erm�glichen
    // Hat keine Auswirkung auf bereits abhgeschlossene Abrechnungsl�ufe,
    // bewirkt nur die Anzeige der Funktion im Kontextmen�
    execute(addColumn("einstellung", new Column("abrlabschliessen",
        COLTYPE.BOOLEAN, 0, "FALSE", false, false)));

  }
}