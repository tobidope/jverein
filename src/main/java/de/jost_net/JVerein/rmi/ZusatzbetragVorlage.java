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
import java.util.Date;

import de.jost_net.JVerein.keys.Zahlungsweg;
import de.willuhn.datasource.rmi.DBObject;

public interface ZusatzbetragVorlage extends DBObject
{

  public Date getFaelligkeit() throws RemoteException;

  public void setFaelligkeit(Date faelligkeit) throws RemoteException;

  public String getBuchungstext() throws RemoteException;

  public void setBuchungstext(String buchungstext) throws RemoteException;

  public double getBetrag() throws RemoteException;

  public void setBetrag(double betrag) throws RemoteException;

  public Date getStartdatum() throws RemoteException;

  public void setStartdatum(Date value) throws RemoteException;

  public Integer getIntervall() throws RemoteException;

  public String getIntervallText() throws RemoteException;

  public void setIntervall(Integer value) throws RemoteException;

  public Date getEndedatum() throws RemoteException;

  public void setEndedatum(Date value) throws RemoteException;

  public void setBuchungsart(Buchungsart buchungsart) throws RemoteException;

  public Buchungsart getBuchungsart() throws RemoteException;
  
  public Buchungsklasse getBuchungsklasse() throws RemoteException;
  
  public Long getBuchungsklasseId() throws RemoteException;

  public void setBuchungsklasseId(Long buchungsklasseId) throws RemoteException;

  public Zahlungsweg getZahlungsweg() throws RemoteException;

  public void setZahlungsweg(Zahlungsweg value) throws RemoteException;
}
