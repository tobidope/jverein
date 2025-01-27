package de.jost_net.JVerein.gui.dialogs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.ImageInput;
import de.willuhn.jameica.gui.parts.ButtonArea;

/**
 * Anzeige eines QR-Codes
 */
public class QRCodeImageDialog extends AbstractDialog<Object>
{
  private ImageInput image;

  /**
   * @param position
   * @throws IOException
   * @throws RemoteException
   */
  public QRCodeImageDialog(int position, BufferedImage qrcode)
      throws IOException
  {
    super(position);
    setTitle("QR-Code");
    setSize(330, SWT.DEFAULT);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(qrcode, "jpg", baos);
    baos.flush();
    byte[] imageInByte = baos.toByteArray();
    baos.close();

    image = new ImageInput(imageInByte, 300, 300);
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    image.paint(parent);
    ButtonArea buttons = new ButtonArea();
    buttons.addButton("Schlie�en", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        close();
      }
    }, null, true, "process-stop.png");
    buttons.paint(parent);
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
}
