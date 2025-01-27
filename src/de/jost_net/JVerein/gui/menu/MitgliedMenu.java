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
package de.jost_net.JVerein.gui.menu;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.NichtMitgliedDetailAction;
import de.jost_net.JVerein.gui.action.KontoauszugAction;
import de.jost_net.JVerein.gui.action.MitgliedArbeitseinsatzZuordnungAction;
import de.jost_net.JVerein.gui.action.MitgliedDeleteAction;
import de.jost_net.JVerein.gui.action.NichtMitgliedDeleteAction;
import de.jost_net.JVerein.gui.action.MitgliedDuplizierenAction;
import de.jost_net.JVerein.gui.action.MitgliedEigenschaftZuordnungAction;
import de.jost_net.JVerein.gui.action.MitgliedInZwischenablageKopierenAction;
import de.jost_net.JVerein.gui.action.MitgliedLastschriftAction;
import de.jost_net.JVerein.gui.action.MitgliedMailSendenAction;
import de.jost_net.JVerein.gui.action.MitgliedVCardDateiAction;
import de.jost_net.JVerein.gui.action.MitgliedVCardQRCodeAction;
import de.jost_net.JVerein.gui.action.MitgliedZusatzbetraegeZuordnungAction;
import de.jost_net.JVerein.gui.action.PersonalbogenAction;
import de.jost_net.JVerein.gui.action.SpendenbescheinigungAction;
import de.jost_net.JVerein.gui.view.MitgliedDetailView;
import de.jost_net.JVerein.gui.view.NichtMitgliedDetailView;
import de.jost_net.JVerein.keys.FormularArt;
import de.jost_net.JVerein.keys.Spendenart;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.MitgliedNextBGruppe;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.SimpleDialog;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Kontext-Menu zu den Mitgliedern
 */
public class MitgliedMenu extends ContextMenu
{

  /**
   * Erzeugt ein Kontext-Menu f�r die Liste der Mitglieder.
   * 
   * @throws RemoteException
   */
  public MitgliedMenu(Action detailaction) throws RemoteException
  {
    addItem(new CheckedSingleContextMenuItem("Bearbeiten", detailaction,
        "text-x-generic.png"));
    addItem(new CheckedSingleContextMenuItem("Duplizieren",
        new MitgliedDuplizierenAction(), "edit-copy.png"));
    addItem(new CheckedContextMenuItem("In Zwischenablage kopieren",
        new MitgliedInZwischenablageKopierenAction(), "edit-copy.png"));
    addItem(new CheckedContextMenuItem("Eigenschaften",
        new MitgliedEigenschaftZuordnungAction(), "document-properties.png"));
    addItem(new CheckedContextMenuItem("Zusatzbetr�ge zuweisen",
        new MitgliedZusatzbetraegeZuordnungAction(), "coins.png"));
    if (Einstellungen.getEinstellung().getArbeitseinsatz() && !(detailaction instanceof NichtMitgliedDetailAction))
    {
      addItem(new CheckedContextMenuItem("Arbeitseins�tze zuweisen",
          new MitgliedArbeitseinsatzZuordnungAction(), "screwdriver.png"));
    }
    if (detailaction instanceof NichtMitgliedDetailAction)
    {
      addItem(new CheckedSingleContextMenuItem("Zu Mitglied umwandeln", new Action()
      {

        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          Mitglied m = (Mitglied) context;
          try
          {
            SimpleDialog sd = new SimpleDialog(SimpleDialog.POSITION_CENTER);
            sd.setText(
                "Bitte die f�r Mitglieder erforderlichen Daten nacherfassen.");
            sd.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
            sd.setSize(400, SWT.DEFAULT);
            sd.setTitle("Daten nacherfassen");
            try
            {
              sd.open();
            }
            catch (Exception e)
            {
              Logger.error("Fehler", e);
            }
            m.setAdresstyp(1);
            m.setEingabedatum();
            GUI.startView(MitgliedDetailView.class.getName(), m);
          }
          catch (RemoteException e)
          {
            throw new ApplicationException(e);
          }
        }
      }, "view-refresh.png"));
    }
    else
    {
      addItem(new CheckedSingleContextMenuItem("Zu Nicht-Mitglied umwandeln", new Action()
      {

        @Override
        public void handleAction(Object context) throws ApplicationException
        {
          Mitglied m = (Mitglied) context;
          try
          {
            SimpleDialog sd = new SimpleDialog(SimpleDialog.POSITION_CENTER);
            sd.setText(
                "Bitte den Mitgliedstyp nacherfassen.");
            sd.setSideImage(SWTUtil.getImage("dialog-warning-large.png"));
            sd.setSize(400, SWT.DEFAULT);
            sd.setTitle("Daten nacherfassen");
            try
            {
              sd.open();
            }
            catch (Exception e)
            {
              Logger.error("Fehler", e);
            }
            m.setAdresstyp(2);
            m.setEingabedatum();
            m.setBeitragsgruppe(null);
            m.setExterneMitgliedsnummer(null);
            m.setIndividuellerBeitrag(null);
            m.setEintritt("");
            m.setAustritt("");
            m.setKuendigung("");
            m.setZahlerID(null);
            DBService service = Einstellungen.getDBService();
            // Sekund�re Beitragsgruppen l�schen
            DBIterator<SekundaereBeitragsgruppe> sit = service
                .createList(SekundaereBeitragsgruppe.class);
            sit.addFilter("mitglied = ? ", m.getID());
            while (sit.hasNext())
            {
              sit.next().delete();
            }
            // Zuk�nftige Beitragsgruppen l�schen
            DBIterator<MitgliedNextBGruppe> mit = service
                .createList(MitgliedNextBGruppe.class);
            mit.addFilter(MitgliedNextBGruppe.COL_MITGLIED + " = ? ", m.getID());
            while (mit.hasNext())
            {
              mit.next().delete();
            }
            GUI.startView(NichtMitgliedDetailView.class.getName(), m);
          }
          catch (RemoteException e)
          {
            throw new ApplicationException(e);
          }
        }
      }, "view-refresh.png"));
    }
    if (detailaction instanceof NichtMitgliedDetailAction)
    {
    addItem(new CheckedContextMenuItem("L�schen",
        new NichtMitgliedDeleteAction(), "user-trash-full.png"));
    }
    else
    {
      addItem(new CheckedContextMenuItem("L�schen",
          new MitgliedDeleteAction(), "user-trash-full.png"));
    }
    addItem(ContextMenuItem.SEPARATOR);
    addItem(new CheckedContextMenuItem("Mail senden",
        new MitgliedMailSendenAction(), "envelope-open.png"));
    addItem(new CheckedContextMenuItem("vCard-Datei",
        new MitgliedVCardDateiAction(), "address-card.png"));
    addItem(new CheckedSingleContextMenuItem("vCard QR-Code",
        new MitgliedVCardQRCodeAction(), "qr-code.png"));
    addItem(new CheckedContextMenuItem("Kontoauszug", new KontoauszugAction(),
        "file-invoice.png"));
    addItem(new CheckedSingleContextMenuItem("Geldspendenbescheinigung",
        new SpendenbescheinigungAction(Spendenart.GELDSPENDE), "file-invoice.png"));
    addItem(new CheckedSingleContextMenuItem("Sachspendenbescheinigung",
        new SpendenbescheinigungAction(Spendenart.SACHSPENDE), "file-invoice.png"));
    addItem(new CheckedContextMenuItem("Personalbogen",
        new PersonalbogenAction(), "file-invoice.png"));
    addItem(new CheckedSingleContextMenuItem("Manuelle Lastschrift",
        new MitgliedLastschriftAction(), "file-invoice.png"));
    DBIterator<Formular> it = Einstellungen.getDBService()
        .createList(Formular.class);
    it.addFilter("art = ?",
        new Object[] { FormularArt.FREIESFORMULAR.getKey() });
    if (it.hasNext())
    {
      addItem(ContextMenuItem.SEPARATOR);
      ContextMenu freieformularemenu = new FreieFormulareMenu(it);
      freieformularemenu.setText("Freie Formulare");
      addMenu(freieformularemenu);
    }
  }
}
