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
package de.jost_net.JVerein.gui.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.gui.action.ArbeitseinsatzAction;
import de.jost_net.JVerein.gui.input.ArbeitseinsatzUeberpruefungInput;
import de.jost_net.JVerein.gui.menu.ArbeitseinsatzMenu;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzPart;
import de.jost_net.JVerein.gui.parts.ArbeitseinsatzUeberpruefungList;
import de.jost_net.JVerein.io.ArbeitseinsatzZeile;
import de.jost_net.JVerein.io.FileViewer;
import de.jost_net.JVerein.io.Reporter;
import de.jost_net.JVerein.keys.IntervallZusatzzahlung;
import de.jost_net.JVerein.rmi.Arbeitseinsatz;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Zusatzbetrag;
import de.jost_net.JVerein.util.Dateiname;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class ArbeitseinsatzControl extends FilterControl
{
  private ArbeitseinsatzPart part = null;

  private Arbeitseinsatz aeins = null;

  private ArbeitseinsatzUeberpruefungList arbeitseinsatzueberpruefungList;

  private SelectInput suchjahr = null;

  private ArbeitseinsatzUeberpruefungInput auswertungschluessel = null;
  
  private TablePart arbeitseinsatzList;

  public ArbeitseinsatzControl(AbstractView view)
  {
    super(view);
    settings = new Settings(this.getClass());
    settings.setStoreWhenRead(true);
  }

  public Arbeitseinsatz getArbeitseinsatz()
  {
    if (aeins != null)
    {
      return aeins;
    }
    aeins = (Arbeitseinsatz) getCurrentObject();
    return aeins;
  }

  public ArbeitseinsatzPart getPart()
  {
    if (part != null)
    {
      return part;
    }
    part = new ArbeitseinsatzPart(getArbeitseinsatz(), true);
    return part;
  }

  public ArbeitseinsatzUeberpruefungInput getAuswertungSchluessel()
      throws RemoteException
  {
    if (auswertungschluessel != null)
    {
      return auswertungschluessel;
    }
    auswertungschluessel = new ArbeitseinsatzUeberpruefungInput(1);
    auswertungschluessel.addListener(new FilterListener());
    return auswertungschluessel;
  }

  public void handleStore()
  {
    try
    {
      Arbeitseinsatz ae = getArbeitseinsatz();
      if (ae.isNewObject())
      {
        if (getPart().getMitglied().getValue() != null)
        {
          Mitglied m = (Mitglied) getPart().getMitglied().getValue();
          ae.setMitglied(Integer.parseInt(m.getID()));
        }
        else
        {
          throw new ApplicationException("Bitte Mitglied eingeben");
        }
      }
      ae.setDatum((Date) part.getDatum().getValue());
      ae.setStunden((Double) part.getStunden().getValue());
      ae.setBemerkung((String) part.getBemerkung().getValue());
      ae.store();
      GUI.getStatusBar().setSuccessText("Arbeitseinsatz gespeichert");
    }
    catch (ApplicationException e)
    {
      GUI.getStatusBar().setErrorText(e.getMessage());
    }
    catch (RemoteException e)
    {
      String fehler = "Fehler bei speichern des Arbeitseinsatzes";
      Logger.error(fehler, e);
      GUI.getStatusBar().setErrorText(fehler);
    }
  }

  public SelectInput getSuchJahr() throws RemoteException
  {
    if (suchjahr != null)
    {
      return suchjahr;
    }
    DBIterator<Arbeitseinsatz> list = Einstellungen.getDBService()
        .createList(Arbeitseinsatz.class);
    list.setOrder("ORDER BY datum");
    Arbeitseinsatz ae = null;
    Calendar von = Calendar.getInstance();
    if (list.hasNext())
    {
      ae = list.next();
      von.setTime(ae.getDatum());
    }
    Calendar bis = Calendar.getInstance();
    ArrayList<Integer> jahre = new ArrayList<>();

    for (int i = von.get(Calendar.YEAR); i <= bis.get(Calendar.YEAR); i++)
    {
      jahre.add(i);
    }

    suchjahr = new SelectInput(jahre, settings.getInt("jahr", jahre.get(0)));
    // suchjahr.setPleaseChoose("Bitte ausw�hlen");
    suchjahr.setPreselected(settings.getInt("jahr", bis.get(Calendar.YEAR)));
    suchjahr.addListener(new FilterListener());
    return suchjahr;
  }

  public Button getPDFAusgabeButton()
  {
    Button b = new Button("PDF", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          startePDFAuswertung();
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler beim Start der PDF-Ausgabe der Arbeitseinsatz�berpr�fung");
        }
      }
    }, null, false, "file-pdf.png");
    return b;
  }

  public Button getCSVAusgabeButton()
  {
    Button b = new Button("CSV", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          starteCSVAuswertung();
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler beim Start der CSV-Ausgabe der Arbeitseinsatz�berpr�fung");
        }
      }
    }, null, false, "xsd.png");
    return b;
  }

  public Button getArbeitseinsatzAusgabeButton()
  {
    Button b = new Button("Zusatzbetr�ge generieren", new Action()
    {

      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        try
        {
          starteArbeitseinsatzGenerierung();
        }
        catch (RemoteException e)
        {
          Logger.error(e.getMessage());
          throw new ApplicationException(
              "Fehler beim der Zusatzbetragsgenerierung");
        }
      }
    }, null, false, "euro-sign.png");
    return b;
  }

  private void startePDFAuswertung() throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei w�hlen.");
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("arbeitseinsaetze", "",
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
    final GenericIterator<ArbeitseinsatzZeile> it = getIterator();
    final int jahr = (Integer) getSuchJahr().getValue();
    final String sub = getAuswertungSchluessel().getText();
    settings.setAttribute("lastdir", file.getParent());
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          FileOutputStream fos = new FileOutputStream(file);
          Reporter reporter = new Reporter(fos,
              String.format("Arbeitseins�tze %d", jahr), sub, it.size());
          reporter.addHeaderColumn("Mitglied", Element.ALIGN_LEFT, 60,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Sollstunden", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Iststunden", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Differenz", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Stundensatz", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.addHeaderColumn("Gesamtbetrag", Element.ALIGN_RIGHT, 30,
              BaseColor.LIGHT_GRAY);
          reporter.createHeader();
          while (it.hasNext())
          {
            ArbeitseinsatzZeile z = (ArbeitseinsatzZeile) it.next();
            reporter.addColumn((String) z.getAttribute("namevorname"),
                Element.ALIGN_LEFT);
            reporter.addColumn((Double) z.getAttribute("soll"));
            reporter.addColumn((Double) z.getAttribute("ist"));
            reporter.addColumn((Double) z.getAttribute("differenz"));
            reporter.addColumn((Double) z.getAttribute("stundensatz"));
            reporter.addColumn((Double) z.getAttribute("gesamtbetrag"));
          }
          reporter.closeTable();
          reporter.close();
          fos.close();
          GUI.getStatusBar().setSuccessText("Auswertung gestartet");
          GUI.getCurrentView().reload();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
        }
        FileViewer.show(file);
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

  private void starteCSVAuswertung() throws RemoteException
  {
    FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
    fd.setText("Ausgabedatei w�hlen.");
    String path = settings.getString("lastdir",
        System.getProperty("user.home"));
    if (path != null && path.length() > 0)
    {
      fd.setFilterPath(path);
    }
    fd.setFileName(new Dateiname("arbeitseinsaetze", "",
        Einstellungen.getEinstellung().getDateinamenmuster(), "csv").get());
    fd.setFilterExtensions(new String[] { "*.csv" });

    String s = fd.open();
    if (s == null || s.length() == 0)
    {
      return;
    }
    if (!s.toLowerCase().endsWith(".csv"))
    {
      s = s + ".csv";
    }
    final File file = new File(s);
    final GenericIterator<ArbeitseinsatzZeile> it = getIterator();
    settings.setAttribute("lastdir", file.getParent());
    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
              CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

          final String[] header = new String[] { "name", "vorname", "strasse",
              "adressierungszusatz", "plz", "ort", "anrede", "telefonprivat",
              "telefondienstlich", "handy", "email", "soll", "ist", "differenz",
              "stundensatz", "gesamtbetrag" };
          writer.writeHeader(header);
          // set up some data to write
          while (it.hasNext())
          {
            ArbeitseinsatzZeile z = (ArbeitseinsatzZeile) it.next();
            final HashMap<String, ? super Object> data1 = new HashMap<>();
            Mitglied m = (Mitglied) z.getAttribute("mitglied");
            data1.put(header[0], m.getName());
            data1.put(header[1], m.getVorname());
            data1.put(header[2], m.getStrasse());
            data1.put(header[3], m.getAdressierungszusatz());
            data1.put(header[4], m.getPlz());
            data1.put(header[5], m.getOrt());
            data1.put(header[6], m.getAnrede());
            data1.put(header[7], m.getTelefonprivat());
            data1.put(header[8], m.getTelefondienstlich());
            data1.put(header[9], m.getHandy());
            data1.put(header[10], m.getEmail());
            data1.put(header[11], z.getAttribute("soll"));
            data1.put(header[12], z.getAttribute("ist"));
            data1.put(header[13], z.getAttribute("differenz"));
            data1.put(header[14], z.getAttribute("stundensatz"));
            data1.put(header[15], z.getAttribute("gesamtbetrag"));
            writer.write(data1, header);
          }
          writer.close();
          GUI.getStatusBar().setSuccessText("Auswertung gestartet");
          GUI.getCurrentView().reload();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
        }
        FileViewer.show(file);
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

  private void starteArbeitseinsatzGenerierung() throws RemoteException
  {
    final GenericIterator<ArbeitseinsatzZeile> it = getIterator();
    final int jahr = (Integer) getSuchJahr().getValue();

    BackgroundTask t = new BackgroundTask()
    {

      @Override
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        try
        {
          while (it.hasNext())
          {
            ArbeitseinsatzZeile z = (ArbeitseinsatzZeile) it.next();
            Zusatzbetrag zb = (Zusatzbetrag) Einstellungen.getDBService()
                .createObject(Zusatzbetrag.class, null);
            Double betrag = (Double) z.getAttribute("gesamtbetrag");
            betrag = betrag * -1;
            zb.setBetrag(betrag);
            zb.setBuchungstext(String.format("Arbeitseinsatz %d", jahr));
            zb.setFaelligkeit(new Date());
            zb.setStartdatum(new Date());
            zb.setIntervall(IntervallZusatzzahlung.KEIN);
            zb.setMitglied(Integer.valueOf((String) z.getAttribute("mitgliedid")));
            zb.store();
          }
          GUI.getStatusBar().setSuccessText("Liste Arbeitseins�tze gestartet");
          GUI.getCurrentView().reload();
        }
        catch (Exception e)
        {
          Logger.error("Fehler", e);
          GUI.getStatusBar().setErrorText(e.getMessage());
          throw new ApplicationException(e);
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

  private GenericIterator<ArbeitseinsatzZeile> getIterator()
      throws RemoteException
  {
    ArrayList<ArbeitseinsatzZeile> zeile = arbeitseinsatzueberpruefungList
        .getInfo();
    @SuppressWarnings("unchecked")
    GenericIterator<ArbeitseinsatzZeile> gi = PseudoIterator
        .fromArray(zeile.toArray(new GenericObject[zeile.size()]));
    return gi;
  }

  public Part getArbeitseinsatzUeberpruefungList() throws ApplicationException
  {
    try
    {
      settings.setAttribute("jahr", (Integer) getSuchJahr().getValue());
      settings.setAttribute("schluessel",
          (Integer) getAuswertungSchluessel().getValue());

      if (arbeitseinsatzueberpruefungList == null)
      {
        arbeitseinsatzueberpruefungList = new ArbeitseinsatzUeberpruefungList(
            null, (Integer) getSuchJahr().getValue(),
            (Integer) getAuswertungSchluessel().getValue());
      }
      else
      {
        arbeitseinsatzueberpruefungList
            .setJahr((Integer) getSuchJahr().getValue());
        arbeitseinsatzueberpruefungList
            .setSchluessel((Integer) getAuswertungSchluessel().getValue());
        ArrayList<ArbeitseinsatzZeile> zeile = arbeitseinsatzueberpruefungList
            .getInfo();
        arbeitseinsatzueberpruefungList.removeAll();
        for (ArbeitseinsatzZeile az : zeile)
        {
          arbeitseinsatzueberpruefungList.addItem(az);
        }
        arbeitseinsatzueberpruefungList.sort();
      }
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
      throw new ApplicationException("Fehler aufgetreten", e);
    }
    return arbeitseinsatzueberpruefungList.getArbeitseinsatzUeberpruefungList();
  }
  
  private void refreshList()
  {
    try
    {
      getArbeitseinsatzUeberpruefungList();
    }
    catch (ApplicationException e1)
    {
      //
    }
  }
  
  private class FilterListener implements Listener
  {

    @Override
    public void handleEvent(Event event)
    {
      if (event.type != SWT.Selection && event.type != SWT.FocusOut)
      {
        return;
      }
      refreshList();
    }
  }
  
  public Part getArbeitseinsatzTable() throws RemoteException
  {
    if (arbeitseinsatzList != null)
    {
      return arbeitseinsatzList;
    }
    
    DBIterator<Arbeitseinsatz> arbeitseinsaetze = getArbeitseinsaetzeIt();
    arbeitseinsatzList = new TablePart(arbeitseinsaetze,
        new ArbeitseinsatzAction(null));
    arbeitseinsatzList.setRememberColWidths(true);
    arbeitseinsatzList.setRememberOrder(true);
    arbeitseinsatzList.setContextMenu(new ArbeitseinsatzMenu());
    arbeitseinsatzList.addColumn("Name", "mitglied");
    arbeitseinsatzList.addColumn("Datum", "datum",
        new DateFormatter(new JVDateFormatTTMMJJJJ()));
    arbeitseinsatzList.addColumn("Stunden", "stunden",
        new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    arbeitseinsatzList.addColumn("Bemerkung", "bemerkung");
    return arbeitseinsatzList;
  }
  
  
  public void TabRefresh()
  {
    try
    {
      if (arbeitseinsatzList == null)
      {
        return;
      }
      arbeitseinsatzList.removeAll();
      DBIterator<Arbeitseinsatz> arbeitseinsaetze = getArbeitseinsaetzeIt();
      while (arbeitseinsaetze.hasNext())
      {
        arbeitseinsatzList.addItem(arbeitseinsaetze.next());
      }
      arbeitseinsatzList.sort();
    }
    catch (RemoteException e1)
    {
      Logger.error("Fehler", e1);
    }
  }
  
  private DBIterator<Arbeitseinsatz> getArbeitseinsaetzeIt() throws RemoteException
  {
    DBService service = Einstellungen.getDBService();
    DBIterator<Arbeitseinsatz> arbeitseinsaetze = service
        .createList(Arbeitseinsatz.class);
    arbeitseinsaetze.join("mitglied");
    arbeitseinsaetze.addFilter("mitglied.id = arbeitseinsatz.mitglied");
    
    if (isSuchnameAktiv() && getSuchname().getValue() != null)
    {
      String tmpSuchname = (String) getSuchname().getValue();
      if (tmpSuchname.length() > 0)
      {
        String suchName = "%" + tmpSuchname.toLowerCase() + "%";
        arbeitseinsaetze.addFilter("(lower(name) like ? "
            + "or lower(vorname) like ?)" , 
            new Object[] { suchName, suchName });
      }
    }
    if (isDatumvonAktiv() && getDatumvon().getValue() != null)
    {
      arbeitseinsaetze.addFilter("datum >= ?",
          new Object[] { (Date) getDatumvon().getValue() });
    }
    if (isDatumbisAktiv() && getDatumbis().getValue() != null)
    {
      arbeitseinsaetze.addFilter("datum <= ?",
          new Object[] { (Date) getDatumbis().getValue() });
    }
    if (isSuchtextAktiv() && getSuchtext().getValue() != null)
    {
      String tmpSuchtext = (String) getSuchtext().getValue();
      if (tmpSuchtext.length() > 0)
      {
        arbeitseinsaetze.addFilter("(lower(bemerkung) like ?)",
            new Object[] { "%" + tmpSuchtext.toLowerCase() + "%"});
      }
    }
    arbeitseinsaetze.setOrder("ORDER by datum desc");
    return arbeitseinsaetze;
  }
  
}
