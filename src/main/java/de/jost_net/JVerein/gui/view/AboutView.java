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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.JVereinPlugin;
import de.jost_net.JVerein.rmi.Version;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

public class AboutView extends AbstractDialog<Object>
{

  public AboutView(int position)
  {
    super(position);
    this.setSize(460, SWT.DEFAULT);
    this.setTitle("�ber");
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    Label l = GUI.getStyleFactory().createLabel(parent, SWT.BORDER);
    l.setImage(SWTUtil.getImage("JVerein.png"));

    SimpleContainer container = new SimpleContainer(parent);
    FormTextPart text = new FormTextPart();
    text.setText("<form>"
        + "<p><b>Plugin f�r die Vereinsverwaltung unter Jameica</b></p>"
        + "<p>Lizenz: GPLv3 https://www.gnu.org/licenses/gpl-3.0.html</p>"
        + "<p>Web: https://openjverein.github.io</p>"
        + "<p>Forum: https://jverein-forum.de</p>"
    	+ "<p>GitHub: https://github.com/openjverein/jverein</p>"
        + "<p>Dokumentation: https://openjverein.gitbook.io/doku</p></form>");

    text.paint(container.getComposite());

    LabelGroup group = new LabelGroup(parent, "Information");

    AbstractPlugin p = Application.getPluginLoader()
        .getPlugin(JVereinPlugin.class);

    group.addLabelPair("Version",
        new LabelInput("" + p.getManifest().getVersion()));

    group.addLabelPair("Build-Date",
        new LabelInput("" + p.getManifest().getBuildDate()));

    group.addLabelPair("Build-Nr",
        new LabelInput("" + p.getManifest().getBuildnumber()));

    Version v = (Version) Einstellungen.getDBService()
        .createObject(Version.class, "1");
    group.addLabelPair("Datenbank-Version",
        new LabelInput("" + v.getVersion()));
    group.addLabelPair("Arbeitsverzeichnis",
        new LabelInput("" + p.getResources().getWorkPath()));

    ButtonArea buttons = group.createButtonArea(1);
    buttons.addButton(i18n.tr("Schlie�en"), new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        close();
      }
    }, null, true, "window-close.png");

    getShell().setSize(getShell().computeSize(SWT.DEFAULT,SWT.DEFAULT));
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }

  public String getHelp()
  {
    return "<form><p><span color=\"header\" font=\"header\">�ber</span></p></form>";
  }
}
