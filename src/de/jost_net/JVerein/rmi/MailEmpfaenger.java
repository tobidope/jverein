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
import java.sql.Timestamp;

import de.willuhn.datasource.rmi.DBObject;

public interface MailEmpfaenger extends DBObject
{
  @Override
  public String getID() throws RemoteException;

  /**
   * ID der zugeh�rigen Mail
   */
  public Mail getMail() throws RemoteException;

  /**
   * ID der zugeh�rigen Mail
   */
  public void setMail(Mail mail) throws RemoteException;

  /**
   * Mitglied ist Mail-Empf�nger
   */
  public Mitglied getMitglied() throws RemoteException;

  /**
   * Mitglied ist Mail-Empf�nger
   */

  public void setMitglied(Mitglied mitglied) throws RemoteException;

  /**
   * Gibt entweder die Mailadresse des Mitgliedes oder die "nackte" Adresse
   * zur�ck
   */
  public String getMailAdresse() throws RemoteException;

  /**
   * Gibt das Datum des letzten Versand der Mail zur�ck.
   */
  public Timestamp getVersand() throws RemoteException;

  /**
   * Setzt das Datum des letzten Versand der Mail.
   */
  public void setVersand(Timestamp versand) throws RemoteException;

}
