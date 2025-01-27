/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung f�r Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2014 by Thomas Hooge
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
package de.jost_net.JVerein.gui.action;

import java.rmi.RemoteException;

import de.jost_net.JVerein.gui.control.FormularPartControl;
import de.jost_net.JVerein.gui.dialogs.ImportDialog;
import de.jost_net.JVerein.gui.view.DokumentationUtil;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class FormularfelderImportAction implements Action
{
  FormularPartControl control;

  public FormularfelderImportAction(FormularPartControl control)
  {
    this.control = control;
  }

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context != null && (context instanceof Formular))
    {
      Formular f = (Formular) context;
      try
      {
        if (f.isNewObject())
        {
          throw new ApplicationException(
              "Vor dem Import der Formularfelder muss das Formular gespeichert werden!");
        }
      }
      catch (RemoteException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException(
            "Fehler beim Import der Formularfelder", e);
      }
    }
    else
    {
      throw new ApplicationException(
          "Es wurde kein Formular ausgew�hlt!");
    }
    
    // Nachfrage, da alle Daten gel�scht werden!
    YesNoDialog ynd = new YesNoDialog(AbstractDialog.POSITION_CENTER);
    ynd.setText("Achtung! Alle momentan vorhandenen Formularfelder f�r\n"
        + "dieses Formular werden gel�scht.\n"
        + "Dieser Vorgang kann nicht r�ckg�ngig gemacht werden.\n\n"
        + "Wirklich fortfahren?");
    ynd.setTitle("Import: Formularfelder");
    Boolean choice;
    try
    {
      choice = (Boolean) ynd.open();
      if (!choice.booleanValue())
        return;
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }

    try
    {
      ImportDialog d = new ImportDialog((GenericObject) context,
          Formularfeld.class, true, DokumentationUtil.FORMULARE);
      d.open();
    }
    catch (OperationCanceledException oce)
    {
      Logger.info(oce.getMessage());
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while importing form  fields", e);
      GUI.getStatusBar().setErrorText(
          "Fehler beim Importieren von Formularfeldern");
    }

    try
    {
      control.refreshTable();
    }
    catch (RemoteException re)
    {
      Logger.error("Fehler: ", re);
      throw new ApplicationException("Fehler anzeigen der Formularfelder", re);
    }

  }
}
