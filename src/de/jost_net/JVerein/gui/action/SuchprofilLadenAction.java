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

import java.io.ByteArrayInputStream;
import java.util.Properties;

import de.jost_net.JVerein.gui.view.MitgliederSucheView;
import de.jost_net.JVerein.rmi.Suchprofil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class SuchprofilLadenAction implements Action
{
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
    {
      throw new ApplicationException("Kein Suchprofil ausgew�hlt!");
    }
    if (!(context instanceof Suchprofil))
    {
      throw new ApplicationException("Programmfehler! Kein Suchprofil!");
    }
    try
    {
      Suchprofil sp = (Suchprofil) context;
      Settings s = new Settings(Class.forName(sp.getClazz()));
      s.setStoreWhenRead(true);
      ByteArrayInputStream bis = new ByteArrayInputStream(sp.getInhalt());
      Properties p = new Properties();
      p.loadFromXML(bis);
      for (Object o : p.keySet())
      {
        String key = (String) o;
        s.setAttribute(key, p.getProperty(key));
      }
      s.setAttribute("id", sp.getID());
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException(e);
    }
    GUI.startView(MitgliederSucheView.class.getName(), null);
  }
}
