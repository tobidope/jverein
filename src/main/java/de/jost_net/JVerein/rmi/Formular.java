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
package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.jost_net.JVerein.keys.FormularArt;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

public interface Formular extends DBObject
{
  public void setID(String id) throws RemoteException;

  public byte[] getInhalt() throws RemoteException;

  public void setInhalt(byte[] inhalt) throws RemoteException;

  public String getBezeichnung() throws RemoteException;

  public void setBezeichnung(String bezeichnung) throws RemoteException;

  public FormularArt getArt() throws RemoteException;

  public void setArt(FormularArt formularArtEnum) throws RemoteException;

  public int getZaehler() throws RemoteException;

  public void setZaehler(int zaehler) throws RemoteException;

  public Integer getFormlink() throws RemoteException;

  public void setFormlink(Integer formlink) throws RemoteException;
  
  public boolean hasFormlinks() throws RemoteException;
  
  public DBIterator<Formular> getLinked() throws RemoteException;

  public void setZaehlerToFormlink(int zaehler) throws RemoteException;

}
