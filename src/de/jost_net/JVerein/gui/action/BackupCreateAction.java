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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.server.AbrechnungslaufImpl;
import de.jost_net.JVerein.server.AdresstypImpl;
import de.jost_net.JVerein.server.AltersstaffelImpl;
import de.jost_net.JVerein.server.AnfangsbestandImpl;
import de.jost_net.JVerein.server.ArbeitseinsatzImpl;
import de.jost_net.JVerein.server.BeitragsgruppeImpl;
import de.jost_net.JVerein.server.BuchungDokumentImpl;
import de.jost_net.JVerein.server.BuchungImpl;
import de.jost_net.JVerein.server.BuchungsartImpl;
import de.jost_net.JVerein.server.BuchungsklasseImpl;
import de.jost_net.JVerein.server.EigenschaftGruppeImpl;
import de.jost_net.JVerein.server.EigenschaftImpl;
import de.jost_net.JVerein.server.EigenschaftenImpl;
import de.jost_net.JVerein.server.EinstellungImpl;
import de.jost_net.JVerein.server.FelddefinitionImpl;
import de.jost_net.JVerein.server.FormularImpl;
import de.jost_net.JVerein.server.FormularfeldImpl;
import de.jost_net.JVerein.server.JahresabschlussImpl;
import de.jost_net.JVerein.server.KontoImpl;
import de.jost_net.JVerein.server.KursteilnehmerImpl;
import de.jost_net.JVerein.server.LastschriftImpl;
import de.jost_net.JVerein.server.LehrgangImpl;
import de.jost_net.JVerein.server.LehrgangsartImpl;
import de.jost_net.JVerein.server.LesefeldImpl;
import de.jost_net.JVerein.server.MailAnhangImpl;
import de.jost_net.JVerein.server.MailEmpfaengerImpl;
import de.jost_net.JVerein.server.MailImpl;
import de.jost_net.JVerein.server.MailVorlageImpl;
import de.jost_net.JVerein.server.MitgliedDokumentImpl;
import de.jost_net.JVerein.server.MitgliedImpl;
import de.jost_net.JVerein.server.MitgliedNextBGruppeImpl;
import de.jost_net.JVerein.server.MitgliedfotoImpl;
import de.jost_net.JVerein.server.MitgliedskontoImpl;
import de.jost_net.JVerein.server.ProjektImpl;
import de.jost_net.JVerein.server.QIFImportHeadImpl;
import de.jost_net.JVerein.server.QIFImportPosImpl;
import de.jost_net.JVerein.server.SekundaereBeitragsgruppeImpl;
import de.jost_net.JVerein.server.SpendenbescheinigungImpl;
import de.jost_net.JVerein.server.SuchprofilImpl;
import de.jost_net.JVerein.server.VersionImpl;
import de.jost_net.JVerein.server.WiedervorlageImpl;
import de.jost_net.JVerein.server.ZusatzbetragAbrechnungslaufImpl;
import de.jost_net.JVerein.server.ZusatzbetragImpl;
import de.jost_net.JVerein.server.ZusatzbetragVorlageImpl;
import de.jost_net.JVerein.server.ZusatzfelderImpl;
import de.jost_net.JVerein.util.JVDateFormatJJJJMMTT;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;
import de.willuhn.datasource.serialize.Writer;
import de.willuhn.datasource.serialize.XmlWriter;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Action zum Erstellen eines Komplett-Backups im XML-Format.
 */
public class BackupCreateAction implements Action
{
  // Die Versionstabelle wird nicht mit kopiert

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setFilterPath(System.getProperty("user.home"));
    fd.setFileName("jverein-backup-"
        + new JVDateFormatJJJJMMTT().format(new Date()) + ".xml");
    fd.setFilterExtensions(new String[] { "*.xml" });
    fd.setText(
        "Bitte w�hlen Sie die Datei, in der das Backup gespeichert wird");
    String f = fd.open();
    if (f == null || f.length() == 0)
      return;

    final File file = new File(f);
    try
    {
      if (file.exists() && !Application.getCallback()
          .askUser("Datei existiert bereits. �berschreiben?"))
        return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while asking user", e);
      Application.getMessagingFactory().sendMessage(
          new StatusBarMessage("Fehler beim Erstellen der Backup-Datei",
              StatusBarMessage.TYPE_ERROR));
      return;
    }

    Application.getController().start(new BackgroundTask()
    {

      private boolean cancel = false;

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        Writer writer = null;
        try
        {
          Logger.info("creating xml backup to " + file.getAbsolutePath());

          writer = new XmlWriter(
              new BufferedOutputStream(new FileOutputStream(file)));

          monitor.setStatusText("Speichere Version");
          backup(VersionImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Einstellungen");
          backup(EinstellungImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Abrechnungslauf-Informationen");
          backup(AbrechnungslaufImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mitgliedstypen");
          backup(AdresstypImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Buchungsklassen");
          backup(BuchungsklasseImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Buchungsarten");
          backup(BuchungsartImpl.class, writer, monitor, "steuer_buchungsart, id");
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Eigenschaftengruppen");
          backup(EigenschaftGruppeImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Felddefinitionen");
          backup(FelddefinitionImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Formulare");
          backup(FormularImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Formularfelder");
          backup(FormularfeldImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Konten");
          backup(KontoImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Kursteilnehmer");
          backup(KursteilnehmerImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Lehrgangsarten");
          backup(LehrgangsartImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Lesefelder");
          backup(LesefeldImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mails");
          backup(MailImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mailanh�nge");
          backup(MailAnhangImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mailvorlagen");
          backup(MailVorlageImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Projekte");
          backup(ProjektImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Informationen �ber QIF-Header");
          backup(QIFImportHeadImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Informationen �ber QIF-Positionen");
          backup(QIFImportPosImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Anfangsbest�nde");
          backup(AnfangsbestandImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Eingenschaften");
          backup(EigenschaftImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Beitragsgruppen");
          backup(BeitragsgruppeImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Altersstaffel");
          backup(AltersstaffelImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mitglieder");
          backup(MitgliedImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Dokumente der Mitglieder");
          backup(MitgliedDokumentImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Fotos der Mitglieder");
          backup(MitgliedfotoImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText(
              "Speichere Informationen �ber zuk�nftige Beitragsgruppen");
          backup(MitgliedNextBGruppeImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mitgliedskonten");
          backup(MitgliedskontoImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Spendenbescheinigungen");
          backup(SpendenbescheinigungImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Wiedervorlagen");
          backup(WiedervorlageImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Zusatzbetr�ge");
          backup(ZusatzbetragImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText(
              "Speichere Informatioen �ber Abrechnungsl�ufe von Zusatzbetr�gen");
          backup(ZusatzbetragAbrechnungslaufImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Zusatzfelder");
          backup(ZusatzfelderImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Arbeitseins�tze");
          backup(ArbeitseinsatzImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Jahresabschl�sse");
          backup(JahresabschlussImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Buchungen");
          backup(BuchungImpl.class, writer, monitor, "splitid, id");
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Dokumente zu Buchungen");
          backup(BuchungDokumentImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Eigenschaften der Mitglieder");
          backup(EigenschaftenImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Lastschriften");
          backup(LastschriftImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Lehrg�nge");
          backup(LehrgangImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Mailempf�nger");
          backup(MailEmpfaengerImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatusText("Speichere Sekund�re Beitragsgruppen der Mitglieder");
          backup(SekundaereBeitragsgruppeImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Suchprofile");
          backup(SuchprofilImpl.class, writer, monitor);
          monitor.addPercentComplete(1);
          
          monitor.setStatusText("Speichere Zusatzbetrag Vorlagen");
          backup(ZusatzbetragVorlageImpl.class, writer, monitor);
          monitor.addPercentComplete(1);

          monitor.setStatus(ProgressMonitor.STATUS_DONE);
          monitor.setStatusText("Backup erstellt");
          monitor.setPercentComplete(100);
        }
        catch (Exception e)
        {
          throw new ApplicationException(e.getMessage());
        }
        finally
        {
          if (writer != null)
          {
            try
            {
              writer.close();
              Logger.info("backup created");
            }
            catch (Exception e)
            {
              /* useless */}
          }
        }
      }

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      @Override
      public boolean isInterrupted()
      {
        return this.cancel;
      }

      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      @Override
      public void interrupt()
      {
        this.cancel = true;
      }

    });
  }

  private static void backup(Class<? extends DBObject> type, Writer writer,
      ProgressMonitor monitor) throws Exception
  {
    backup(type, writer, monitor, "id");
  }
  
  private static void backup(Class<? extends DBObject> type, Writer writer,
      ProgressMonitor monitor, String order) throws Exception
  {
    DBIterator<?> list = Einstellungen.getDBService().createList(type);
    list.setOrder("order by " + order);
    long count = 1;
    while (list.hasNext())
    {
      GenericObject o = null;
      try
      {
        o = list.next();
        writer.write(o);
        if (count++ % 200 == 0)
          monitor.addPercentComplete(1);
      }
      catch (Exception e)
      {
        Logger.error("error while writing object " + BeanUtil.toString(o)
            + " - skipping", e);
        monitor.log(String.format("  %s fehlerhaft (%s), �berspringe",
            BeanUtil.toString(o), e.getMessage()));
      }
    }
  }
}
