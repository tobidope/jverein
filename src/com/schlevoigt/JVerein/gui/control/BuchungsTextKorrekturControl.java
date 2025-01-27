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
package com.schlevoigt.JVerein.gui.control;

import java.rmi.RemoteException;

import com.schlevoigt.JVerein.Queries.BuchungsKorrekturQuery;
import com.schlevoigt.JVerein.util.Misc;

import de.jost_net.JVerein.DBTools.DBTransaction;
import de.jost_net.JVerein.Messaging.BuchungMessage;
import de.jost_net.JVerein.gui.action.BuchungAction;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Konto;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class BuchungsTextKorrekturControl extends AbstractControl {

	private TablePart buchungsList;

	private BuchungsKorrekturQuery query;

	private BuchungMessageConsumer mc = null;

	public BuchungsTextKorrekturControl(AbstractView view) {
		super(view);
	}

	public Button getStartKorrekturButton() {
		Button b = new Button("Korrektur", new Action() {

			@Override
			public void handleAction(Object context) {
				starteKorrektur();
			}
		}, null, true, "walking.png"); // "true" defines this button as the
											// default
		return b;
	}

	public Part getBuchungsList() throws RemoteException {
		// Buchungen holen
		query = new BuchungsKorrekturQuery();
		if (buchungsList == null) {
			buchungsList = new TablePart(query.get(), new BuchungAction(false));
			buchungsList.addColumn("Nr", "id-int");
			buchungsList.addColumn("S", "splitid", new Formatter() {
				@Override
				public String format(Object o) {
					return (o != null ? "S" : " ");
				}
			});
			buchungsList.addColumn("Konto", "konto", new Formatter() {

				@Override
				public String format(Object o) {
					Konto k = (Konto) o;
					if (k != null) {
						try {
							return k.getBezeichnung();
						} catch (RemoteException e) {
							Logger.error("Fehler", e);
						}
					}
					return "";
				}
			});
			buchungsList.addColumn("Datum", "datum", new DateFormatter(new JVDateFormatTTMMJJJJ()));

			buchungsList.addColumn("Verwendungszweck neu", "zweck", new Formatter() {
				@Override
				public String format(Object value) {
					if (value == null) {
						return null;
					}
					return Misc.getBuchungsZweckKorrektur(value.toString(), false);
				}
			});
			buchungsList.addColumn("Verwendungszweck alt", "zweck", new Formatter() {
				@Override
				public String format(Object value) {
					if (value == null) {
						return null;
					}
					String s = value.toString();
					s = s.replaceAll("\r\n", "|");
					s = s.replaceAll("\r", "|");
					s = s.replaceAll("\n", "|");
					return s;
				}
			});
			buchungsList.setMulti(true);
			buchungsList.setRememberColWidths(true);
			buchungsList.setRememberOrder(true);
			buchungsList.setRememberState(true);
			this.mc = new BuchungMessageConsumer();
			Application.getMessagingFactory().registerMessageConsumer(this.mc);
		} else {
			buchungsList.removeAll();

			for (Buchung bu : query.get()) {
				buchungsList.addItem(bu);
			}
			buchungsList.sort();
		}

		return buchungsList;
	}

	public void refreshBuchungen() throws RemoteException {
		if (buchungsList == null) {
			return;
		}
		buchungsList.removeAll();

		for (Buchung b : query.get()) {
			buchungsList.addItem(b);
		}
		buchungsList.sort();
	}

	private void starteKorrektur() {
		try {
			int count = 0;
			DBTransaction.starten();

			for (Object item : buchungsList.getItems()) {
				Buchung b = (Buchung) item;
				if (b.getJahresabschluss() != null) {
					continue;
				}
				String zweck = b.getZweck();
				zweck = Misc.getBuchungsZweckKorrektur(zweck, true);
				b.setZweck(zweck);
				b.store();
				count++;
			}

			DBTransaction.commit();
			GUI.getStatusBar().setSuccessText(count + " Buchungen korrigiert");
			refreshBuchungen();
		} catch (ApplicationException e) {
			DBTransaction.rollback();
			GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
		} catch (RemoteException e) {
			DBTransaction.rollback();
			GUI.getStatusBar().setErrorText(e.getLocalizedMessage());
		}
	}

	/**
	 * Wird benachrichtigt um die Anzeige zu aktualisieren.
	 */
	private class BuchungMessageConsumer implements MessageConsumer {

		/**
		 * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
		 */
		@Override
		public boolean autoRegister() {
			return false;
		}

		/**
		 * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
		 */
		@Override
		public Class<?>[] getExpectedMessageTypes() {
			return new Class[] { BuchungMessage.class };
		}

		/**
		 * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
		 */
		@Override
		public void handleMessage(final Message message) throws Exception {
			GUI.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						if (buchungsList == null) {
							// Eingabe-Feld existiert nicht. Also abmelden
							Application.getMessagingFactory().unRegisterMessageConsumer(BuchungMessageConsumer.this);
							return;
						}
						refreshBuchungen();
					} catch (Exception e) {
						// Wenn hier ein Fehler auftrat, deregistrieren wir uns
						// wieder
						Logger.error("unable to refresh Splitbuchungen", e);
						Application.getMessagingFactory().unRegisterMessageConsumer(BuchungMessageConsumer.this);
					}
				}
			});
		}

	}
}
