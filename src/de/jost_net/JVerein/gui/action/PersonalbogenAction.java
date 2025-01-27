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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.io.BeitragsUtil;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.ArtBeitragsart;
import de.jost_net.JVerein.keys.Zahlungsweg;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Beitragsgruppe;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Eigenschaften;
import de.jost_net.JVerein.rmi.Felddefinition;
import de.jost_net.JVerein.rmi.Lehrgang;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedfoto;
import de.jost_net.JVerein.rmi.Mitgliedskonto;
import de.jost_net.JVerein.rmi.SekundaereBeitragsgruppe;
import de.jost_net.JVerein.rmi.Wiedervorlage;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.rmi.Zusatzfelder;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class PersonalbogenAction implements Action
{

  private de.willuhn.jameica.system.Settings settings;

  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    Mitglied[] m = null;
    if (context != null
        && (context instanceof Mitglied || context instanceof Mitglied[]))
    {
      if (context instanceof Mitglied)
      {
        m = new Mitglied[] { (Mitglied) context };
      }
      else if (context instanceof Mitglied[])
      {
        m = (Mitglied[]) context;
      }
      try
      {
        generierePersonalbogen(m);
      }
      catch (IOException e)
      {
        Logger.error("Fehler", e);
        throw new ApplicationException("Fehler bei der Aufbereitung", e);
      }
    }
    else
    {
      throw new ApplicationException("Kein Mitglied ausgew�hlt");
    }
  }

  private void generierePersonalbogen(Mitglied[] m) throws IOException
  {
    final Mitglied[] mitglied = m;
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei w�hlen.");

    settings = new de.willuhn.jameica.system.Settings(this.getClass());
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("personalbogen", "",
        Einstellungen.getEinstellung().getDateinamenmuster(), "pdf").get());
    fd.setFilterExtensions(new String[] { "*.pdf" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith(".pdf"))
    {
      s = s + ".pdf";
    }
    final File file = new File(s);
    settings.setAttribute("lastdir", file.getParent());
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          Reporter rpt = new Reporter(new FileOutputStream(file), "",
              "Personalbogen", mitglied.length);

          GUI.getStatusBar().setSuccessText("Auswertung gestartet");
          GUI.getCurrentView().reload();

          boolean first = true;

          for (Mitglied m : mitglied)
          {
            if (!first)
            {
              rpt.newPage();
            }
            first = false;

            rpt.add(
                "Personalbogen" + " " + Adressaufbereitung.getVornameName(m),
                14);

            generiereMitglied(rpt, m);

            if (Einstellungen.getEinstellung().getZusatzbetrag())
            {
              generiereZusatzbetrag(rpt, m);
            }
            generiereMitgliedskonto(rpt, m);
            if (Einstellungen.getEinstellung().getVermerke()
                && ((m.getVermerk1() != null && m.getVermerk1().length() > 0)
                    || (m.getVermerk2() != null
                        && m.getVermerk2().length() > 0)))
            {
              generiereVermerke(rpt, m);
            }
            if (Einstellungen.getEinstellung().getWiedervorlage())
            {
              generiereWiedervorlagen(rpt, m);
            }
            if (Einstellungen.getEinstellung().getLehrgaenge())
            {
              generiereLehrgaenge(rpt, m);
            }
            generiereZusatzfelder(rpt, m);
            generiereEigenschaften(rpt, m);
            if (Einstellungen.getEinstellung().getArbeitseinsatz())
            {
              generiereArbeitseinsaetze(rpt, m);
            }
          }
          rpt.close();
          FileViewer.show(file);
        }
        catch (Exception re)
        {
          Logger.error("Fehler", re);
          GUI.getStatusBar().setErrorText(re.getMessage());
          throw new ApplicationException(re);
        }
      }

      @Override
      public void interrupt()
      {
        //
      }

      @Override
      public boolean isInterrupted()
      {
        return false;
      }
    };
    Application.getController().start(t);

  }

  private void generiereMitglied(Reporter rpt, Mitglied m)
      throws DocumentException, MalformedURLException, IOException, ApplicationException
  {
    rpt.addHeaderColumn("Feld", Element.ALIGN_LEFT, 50, BaseColor.LIGHT_GRAY);
    rpt.addHeaderColumn("Inhalt", Element.ALIGN_LEFT, 140,
        BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    DBIterator<Mitgliedfoto> it = Einstellungen.getDBService()
        .createList(Mitgliedfoto.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    if (it.size() > 0)
    {
      Mitgliedfoto foto = it.next();
      if (foto.getFoto() != null)
      {
        rpt.addColumn("Foto", Element.ALIGN_LEFT);
        rpt.addColumn(foto.getFoto(), 100, 100, Element.ALIGN_RIGHT);
      }
    }
    if (Einstellungen.getEinstellung().getExterneMitgliedsnummer())
    {
      rpt.addColumn("Ext. Mitgliedsnummer", Element.ALIGN_LEFT);
      rpt.addColumn(m.getExterneMitgliedsnummer() != null
          ? m.getExterneMitgliedsnummer() + ""
          : "", Element.ALIGN_LEFT);
    }
    else
    {
      rpt.addColumn("Mitgliedsnummer", Element.ALIGN_LEFT);
      rpt.addColumn(m.getID(), Element.ALIGN_LEFT);
    }
    rpt.addColumn("Name, Vorname", Element.ALIGN_LEFT);
    rpt.addColumn(Adressaufbereitung.getNameVorname(m), Element.ALIGN_LEFT);
    rpt.addColumn("Anschrift", Element.ALIGN_LEFT);
    rpt.addColumn(Adressaufbereitung.getAnschrift(m), Element.ALIGN_LEFT);
    rpt.addColumn("Geburtsdatum", Element.ALIGN_LEFT);
    rpt.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT);
    if (m.getSterbetag() != null)
    {
      rpt.addColumn("Sterbetag", Element.ALIGN_LEFT);
      rpt.addColumn(m.getSterbetag(), Element.ALIGN_LEFT);
    }
    rpt.addColumn("Geschlecht", Element.ALIGN_LEFT);
    rpt.addColumn(m.getGeschlecht(), Element.ALIGN_LEFT);
    rpt.addColumn("Kommunikation", Element.ALIGN_LEFT);
    String kommunikation = "";
    if (m.getTelefonprivat().length() != 0)
    {
      kommunikation += "privat: " + m.getTelefonprivat();
    }
    if (m.getTelefondienstlich().length() != 0)
    {
      if (kommunikation.length() > 0)
      {
        kommunikation += "\n";
      }
      kommunikation += "dienstlich: " + m.getTelefondienstlich();
    }
    if (m.getHandy().length() != 0)
    {
      if (kommunikation.length() > 0)
      {
        kommunikation += "\n";
      }
      kommunikation += "Handy: " + m.getHandy();
    }
    if (m.getEmail().length() != 0)
    {
      if (kommunikation.length() > 0)
      {
        kommunikation += "\n";
      }
      kommunikation += "Email: " + m.getEmail();
    }
    rpt.addColumn(kommunikation, Element.ALIGN_LEFT);
    if (m.getAdresstyp().getID().equals("1"))
    {
      rpt.addColumn("Eintritt", Element.ALIGN_LEFT);
      rpt.addColumn(m.getEintritt(), Element.ALIGN_LEFT);
      printBeitragsgruppe(rpt, m, m.getBeitragsgruppe(), false);
      if (Einstellungen.getEinstellung().getSekundaereBeitragsgruppen())
      {
        DBIterator<SekundaereBeitragsgruppe> sb = Einstellungen.getDBService()
            .createList(SekundaereBeitragsgruppe.class);
        sb.addFilter("mitglied = ?", m.getID());
        while (sb.hasNext())
        {
          SekundaereBeitragsgruppe sebe = sb.next();
          printBeitragsgruppe(rpt, m, sebe.getBeitragsgruppe(), true);
        }
      }

      if (Einstellungen.getEinstellung().getIndividuelleBeitraege())
      {
        rpt.addColumn("Individueller Beitrag", Element.ALIGN_LEFT);
        if (m.getIndividuellerBeitrag() != null)
        {
          rpt.addColumn(
              Einstellungen.DECIMALFORMAT.format(m.getIndividuellerBeitrag())
                  + " EUR",
              Element.ALIGN_LEFT);
        }
        else
        {
          rpt.addColumn("", Element.ALIGN_LEFT);
        }
      }
      if (m.getBeitragsgruppe()
          .getBeitragsArt() != ArtBeitragsart.FAMILIE_ANGEHOERIGER)
      {
        DBIterator<Mitglied> itbg = Einstellungen.getDBService()
            .createList(Mitglied.class);
        itbg.addFilter("zahlerid = ?", m.getID());
        rpt.addColumn("Vollzahler mit Angeh�rigen", Element.ALIGN_LEFT);
        String zahltfuer = "";
        while (itbg.hasNext())
        {
          Mitglied mz = itbg.next();
          if (zahltfuer.length() > 0)
          {
            zahltfuer += "\n";
          }
          zahltfuer += Adressaufbereitung.getNameVorname(mz);
        }
        rpt.addColumn(zahltfuer, Element.ALIGN_LEFT);
      }
      else if (m.getBeitragsgruppe()
          .getBeitragsArt() == ArtBeitragsart.FAMILIE_ANGEHOERIGER)
      {
        Mitglied mfa = (Mitglied) Einstellungen.getDBService()
            .createObject(Mitglied.class, m.getZahlerID() + "");
        rpt.addColumn("Vollzahlendes Familienmitglied", Element.ALIGN_LEFT);
        rpt.addColumn(Adressaufbereitung.getNameVorname(mfa),
            Element.ALIGN_LEFT);
      }
      rpt.addColumn("Austritts-/K�ndigungsdatum", Element.ALIGN_LEFT);
      String akdatum = "";
      if (m.getAustritt() != null)
      {
        akdatum += new JVDateFormatTTMMJJJJ().format(m.getAustritt());
      }
      if (m.getKuendigung() != null)
      {
        if (akdatum.length() != 0)
        {
          akdatum += " / ";
        }
        akdatum += new JVDateFormatTTMMJJJJ().format(m.getKuendigung());
      }
      rpt.addColumn(akdatum, Element.ALIGN_LEFT);
    }
    rpt.addColumn("Zahlungsweg", Element.ALIGN_LEFT);
    rpt.addColumn(Zahlungsweg.get(m.getZahlungsweg()), Element.ALIGN_LEFT);
    if (m.getBic() != null && m.getBic().length() > 0
        && m.getIban().length() > 0)
    {
      rpt.addColumn("Bankverbindung", Element.ALIGN_LEFT);
      rpt.addColumn(m.getBic() + "/" + m.getIban(), Element.ALIGN_LEFT);
    }
    rpt.addColumn("Datum Erstspeicherung", Element.ALIGN_LEFT);
    rpt.addColumn(m.getEingabedatum(), Element.ALIGN_LEFT);
    rpt.addColumn("Datum letzte �nderung", Element.ALIGN_LEFT);
    rpt.addColumn(m.getLetzteAenderung(), Element.ALIGN_LEFT);
    rpt.closeTable();
  }

  private void printBeitragsgruppe(Reporter rpt, Mitglied m, Beitragsgruppe bg,
      boolean sek) throws RemoteException, ApplicationException
  {
    rpt.addColumn((sek ? "Sekund�re " : "") + "Beitragsgruppe",
        Element.ALIGN_LEFT);
    String beitragsgruppe = bg.getBezeichnung() + " - "
        + Einstellungen.DECIMALFORMAT.format(BeitragsUtil.getBeitrag(
            Einstellungen.getEinstellung().getBeitragsmodel(),
            m.getZahlungstermin(), m.getZahlungsrhythmus().getKey(), bg,
            new Date(), m))
        + " EUR";
    rpt.addColumn(beitragsgruppe, Element.ALIGN_LEFT);
  }

  private void generiereZusatzbetrag(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Zusatzbetrag> it = Einstellungen.getDBService()
        .createList(Zusatzbetrag.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("ORDER BY faelligkeit DESC");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Zusatzbetrag", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Start", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("n�chste F�ll.", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("letzte Ausf.", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Intervall", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Ende", Element.ALIGN_LEFT, 30, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Buchungstext", Element.ALIGN_LEFT, 60,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag", Element.ALIGN_RIGHT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      while (it.hasNext())
      {
        Zusatzbetrag z = it.next();
        rpt.addColumn(z.getStartdatum(), Element.ALIGN_LEFT);
        rpt.addColumn(z.getFaelligkeit(), Element.ALIGN_LEFT);
        rpt.addColumn(z.getAusfuehrung(), Element.ALIGN_LEFT);
        rpt.addColumn(z.getIntervallText(), Element.ALIGN_LEFT);
        rpt.addColumn(z.getEndedatum(), Element.ALIGN_LEFT);
        rpt.addColumn(z.getBuchungstext(), Element.ALIGN_LEFT);
        rpt.addColumn(z.getBetrag());
      }
    }
    rpt.closeTable();
  }

  private void generiereMitgliedskonto(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Mitgliedskonto> it = Einstellungen.getDBService()
        .createList(Mitgliedskonto.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("order by datum desc");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Mitgliedskonto", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Text", Element.ALIGN_LEFT, 12, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Zweck", Element.ALIGN_LEFT, 50,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Zahlungsweg", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Betrag", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      while (it.hasNext())
      {
        Mitgliedskonto mk = it.next();
        rpt.addColumn("Soll", Element.ALIGN_LEFT);
        rpt.addColumn(mk.getDatum(), Element.ALIGN_LEFT);
        rpt.addColumn(mk.getZweck1(), Element.ALIGN_LEFT);
        rpt.addColumn(Zahlungsweg.get(mk.getZahlungsweg()), Element.ALIGN_LEFT);
        rpt.addColumn(mk.getBetrag());
        DBIterator<Buchung> it2 = Einstellungen.getDBService()
            .createList(Buchung.class);
        it2.addFilter("mitgliedskonto = ?", new Object[] { mk.getID() });
        it2.setOrder("order by datum desc");
        while (it2.hasNext())
        {
          Buchung bu = it2.next();
          rpt.addColumn("Ist", Element.ALIGN_RIGHT);
          rpt.addColumn(bu.getDatum(), Element.ALIGN_LEFT);
          rpt.addColumn(bu.getZweck(), Element.ALIGN_LEFT);
          rpt.addColumn("", Element.ALIGN_LEFT);
          rpt.addColumn(bu.getBetrag());
        }
      }
    }
    rpt.closeTable();

  }

  private void generiereVermerke(Reporter rpt, Mitglied m)
      throws DocumentException, RemoteException
  {
    rpt.add(new Paragraph("Vermerke", Reporter.getFreeSans(12)));
    rpt.addHeaderColumn("Text", Element.ALIGN_LEFT, 100, BaseColor.LIGHT_GRAY);
    rpt.createHeader();
    if (m.getVermerk1() != null && m.getVermerk1().length() > 0)
    {
      rpt.addColumn(m.getVermerk1(), Element.ALIGN_LEFT);
    }
    if (m.getVermerk2() != null && m.getVermerk2().length() > 0)
    {
      rpt.addColumn(m.getVermerk2(), Element.ALIGN_LEFT);
    }
    rpt.closeTable();

  }

  private void generiereWiedervorlagen(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Wiedervorlage> it = Einstellungen.getDBService()
        .createList(Wiedervorlage.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("order by datum desc");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Wiedervorlage", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 50,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Vermerk", Element.ALIGN_LEFT, 100,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Erledigung", Element.ALIGN_LEFT, 50,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      while (it.hasNext())
      {
        Wiedervorlage w = it.next();
        rpt.addColumn(w.getDatum(), Element.ALIGN_LEFT);
        rpt.addColumn(w.getVermerk(), Element.ALIGN_LEFT);
        rpt.addColumn(w.getErledigung(), Element.ALIGN_LEFT);
      }
    }
    rpt.closeTable();

  }

  private void generiereLehrgaenge(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Lehrgang> it = Einstellungen.getDBService()
        .createList(Lehrgang.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("order by von");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Lehrg�nge", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Lehrgangsart", Element.ALIGN_LEFT, 50,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("am/vom", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("bis", Element.ALIGN_LEFT, 30, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Veranstalter", Element.ALIGN_LEFT, 60,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Ergebnis", Element.ALIGN_LEFT, 60,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      while (it.hasNext())
      {
        Lehrgang l = it.next();
        rpt.addColumn(l.getLehrgangsart().getBezeichnung(), Element.ALIGN_LEFT);
        rpt.addColumn(l.getVon(), Element.ALIGN_LEFT);
        rpt.addColumn(l.getBis(), Element.ALIGN_LEFT);
        rpt.addColumn(l.getVeranstalter(), Element.ALIGN_LEFT);
        rpt.addColumn(l.getErgebnis(), Element.ALIGN_LEFT);
      }
    }
    rpt.closeTable();
  }

  private void generiereZusatzfelder(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Felddefinition> it = Einstellungen.getDBService()
        .createList(Felddefinition.class);
    it.setOrder("order by label");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Zusatzfelder", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Feld", Element.ALIGN_LEFT, 50, BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Inhalt", Element.ALIGN_LEFT, 130,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      while (it.hasNext())
      {
        Felddefinition fd = it.next();
        rpt.addColumn(fd.getLabel(), Element.ALIGN_LEFT);
        DBIterator<Zusatzfelder> it2 = Einstellungen.getDBService()
            .createList(Zusatzfelder.class);
        it2.addFilter("mitglied = ? and felddefinition = ?",
            new Object[] { m.getID(), fd.getID() });
        if (it2.size() > 0)
        {
          Zusatzfelder zf = it2.next();
          rpt.addColumn(zf.getString(), Element.ALIGN_LEFT);
        }
        else
        {
          rpt.addColumn("", Element.ALIGN_LEFT);
        }
      }
      rpt.closeTable();
    }
  }

  private void generiereEigenschaften(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    ResultSetExtractor rs = new ResultSetExtractor()
    {

      @Override
      public Object extract(ResultSet rs) throws SQLException
      {
        List<String> ids = new ArrayList<>();
        while (rs.next())
        {
          ids.add(rs.getString(1));
        }
        return ids;
      }
    };
    String sql = "select eigenschaften.id from eigenschaften, eigenschaft "
        + "where eigenschaften.eigenschaft = eigenschaft.id and mitglied = ? "
        + "order by eigenschaft.bezeichnung";
    @SuppressWarnings("unchecked")
    ArrayList<String> idliste = (ArrayList<String>) Einstellungen.getDBService()
        .execute(sql, new Object[] { m.getID() }, rs);
    if (idliste.size() > 0)
    {
      rpt.add(new Paragraph("Eigenschaften", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Eigenschaftengruppe", Element.ALIGN_LEFT, 100,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Eigenschaft", Element.ALIGN_LEFT, 100,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      for (String id : idliste)
      {
        DBIterator<Eigenschaften> it = Einstellungen.getDBService()
            .createList(Eigenschaften.class);
        it.addFilter("id = ?", new Object[] { id });
        while (it.hasNext())
        {
          Eigenschaften ei = it.next();
          rpt.addColumn(
              ei.getEigenschaft().getEigenschaftGruppe().getBezeichnung(),
              Element.ALIGN_LEFT);
          rpt.addColumn(ei.getEigenschaft().getBezeichnung(),
              Element.ALIGN_LEFT);
        }
      }
      rpt.closeTable();
    }
  }

  private void generiereArbeitseinsaetze(Reporter rpt, Mitglied m)
      throws RemoteException, DocumentException
  {
    DBIterator<Arbeitseinsatz> it = Einstellungen.getDBService()
        .createList(Arbeitseinsatz.class);
    it.addFilter("mitglied = ?", new Object[] { m.getID() });
    it.setOrder("ORDER BY datum");
    if (it.size() > 0)
    {
      rpt.add(new Paragraph("Arbeitseins�tze", Reporter.getFreeSans(12)));
      rpt.addHeaderColumn("Datum", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Stunden", Element.ALIGN_LEFT, 30,
          BaseColor.LIGHT_GRAY);
      rpt.addHeaderColumn("Bemerkung", Element.ALIGN_LEFT, 90,
          BaseColor.LIGHT_GRAY);
      rpt.createHeader();
      while (it.hasNext())
      {
        Arbeitseinsatz ae = it.next();
        rpt.addColumn(ae.getDatum(), Element.ALIGN_LEFT);
        rpt.addColumn(ae.getStunden());
        rpt.addColumn(ae.getBemerkung(), Element.ALIGN_LEFT);
      }
    }
    rpt.closeTable();
  }
}
