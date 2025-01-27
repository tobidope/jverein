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
package de.jost_net.JVerein.gui.view;

import java.rmi.RemoteException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.DokumentationAction;
import de.jost_net.JVerein.gui.control.BeitragsgruppeControl;
import de.jost_net.JVerein.gui.util.SimpleVerticalContainer;
import de.jost_net.JVerein.keys.Beitragsmodel;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;

public class BeitragsgruppeDetailView extends AbstractView
{

  final BeitragsgruppeControl control = new BeitragsgruppeControl(this);

  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle("Beitragsgruppe");

    LabelGroup group = new LabelGroup(getParent(), "Beitrag");
    group.addLabelPair("Bezeichnung", control.getBezeichnung(true));

    if (Einstellungen.getEinstellung().getSekundaereBeitragsgruppen())
    {
      group.addLabelPair("Sekund�re Beitragsgruppe", control.getSekundaer());
    }

    switch (Einstellungen.getEinstellung().getBeitragsmodel())
    {
      case GLEICHERTERMINFUERALLE:
      case MONATLICH12631:
      {
        group.addLabelPair("Betrag", control.getBetrag());
        break;
      }
      case FLEXIBEL:
      {
        group.addLabelPair("Betrag monatlich", control.getBetragMonatlich());
        group.addLabelPair("Betrag viertelj�hrlich",
            control.getBetragVierteljaehrlich());
        group.addLabelPair("Betrag halbj�hrlich",
            control.getBetragHalbjaehrlich());
        group.addLabelPair("Betrag j�hrlich", control.getBetragJaehrlich());
        break;
      }
    }
    
    group.addLabelPair("Beitragsart", control.getBeitragsArt());
    group.addLabelPair("Buchungsart", control.getBuchungsart());
    if (Einstellungen.getEinstellung().getBuchungsklasseInBuchung())
    {
      group.addLabelPair("Buchungsklasse", control.getBuchungsklasse());
    }

    if(Einstellungen.getEinstellung().getBeitragsmodel() != Beitragsmodel.FLEXIBEL
        && Einstellungen.getEinstellung().getGeburtsdatumPflicht())
    {
      Input[] altersstaffel = control.getAltersstaffel();
      if (altersstaffel != null)
      {
        Container cont = new LabelGroup(getParent(), "Altersstaffel");
        SimpleVerticalContainer svc = new SimpleVerticalContainer(
            cont.getComposite(), true, 3);
        svc.addCheckbox(control.getIsAltersstaffel(), "Nach Alter gestaffelte Beitr�ge verwenden");
        for (Input inp : altersstaffel)
        {
          svc.addInput(inp);
        }
        svc.arrangeVertically();
      }
    }
    
    if (Einstellungen.getEinstellung().getArbeitseinsatz())
    {
      LabelGroup groupAe = new LabelGroup(getParent(), "Arbeitseinsatz");
      groupAe.addLabelPair("Stunden", control.getArbeitseinsatzStunden());
      groupAe.addLabelPair("Betrag", control.getArbeitseinsatzBetrag());
    }

    group.addLabelPair("Notiz", control.getNotiz());

    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Hilfe", new DokumentationAction(),
        DokumentationUtil.BEITRAGSGRUPPEN, false, "question-circle.png");
    buttons.addButton("Speichern", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        control.handleStore();
      }
    }, null, true, "document-save.png");
    buttons.paint(this.getParent());
  }

  @Override
  public void unbind() throws ApplicationException
  {
    try
    {
      if (control.getBezeichnung(true).hasChanged()
          || control.getBetrag().hasChanged())
      {
        YesNoDialog dialog = new YesNoDialog(AbstractDialog.POSITION_CENTER);
        dialog.setText("Soll die �nderung gespeichert werden?");
        try
        {
          Boolean yesno = (Boolean) dialog.open();
          if (yesno)
          {
            throw new ApplicationException("�nderungen bitte speichern.");
          }
        }
        catch (Exception e)
        {
          throw new ApplicationException(e);
        }
      }
    }
    catch (RemoteException e)
    {
      throw new ApplicationException(e);
    }
  }
}
