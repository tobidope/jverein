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
package de.jost_net.JVerein.keys;

/**
 * Zahlungstermine
 */

public enum Zahlungstermin
{

  MONATLICH(1, "Monatlich", new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }), //
  VIERTELJAEHRLICH1(31, "Viertelj�hrlich (Jan./Apr./Juli/Okt)", //
      new int[] { 1, 4, 7, 10 }), //
  VIERTELJAEHRLICH2(32, "Viertelj�hrlich (Feb./Mai /Aug./Nov.)", //
      new int[] { 2, 5, 8, 11 }), //
  VIERTELJAEHRLICH3(33, "Viertelj�hrlich (M�rz/Juni/Sep./Dez.)", //
      new int[] { 3, 6, 9, 12 }), //
  HALBJAEHRLICH1(61, "Halbj�hrlich (Jan./Juli)", new int[] { 1, 7 }), //
  HALBJAEHRLICH2(62, "Halbj�hrlich (Feb./Aug.)", new int[] { 2, 8 }), //
  HALBJAEHRLICH3(63, "Halbj�hrlich (M�rz/Sep.)", new int[] { 3, 9 }), //
  HALBJAEHRLICH4(64, "Halbj�hrlich (Apr./Okt.)", new int[] { 4, 10 }), //
  HALBJAEHRLICH5(65, "Halbj�hrlich (Mai /Nov.)", new int[] { 5, 11 }), //
  HALBJAEHRLICH6(66, "Halbj�hrlich (Juni/Dez.)", new int[] { 6, 12 }), //
  JAERHLICH01(1201, "J�hrlich (Jan.)", new int[] { 1 }), //
  JAERHLICH02(1202, "J�hrlich (Feb.)", new int[] { 2 }), //
  JAERHLICH03(1203, "J�hrlich (M�rz)", new int[] { 3 }), //
  JAERHLICH04(1204, "J�hrlich (Apr.)", new int[] { 4 }), //
  JAERHLICH05(1205, "J�hrlich (Mai )", new int[] { 5 }), //
  JAERHLICH06(1206, "J�hrlich (Juni)", new int[] { 6 }), //
  JAERHLICH07(1207, "J�hrlich (Juli)", new int[] { 7 }), //
  JAERHLICH08(1208, "J�hrlich (Aug.)", new int[] { 8 }), //
  JAERHLICH09(1209, "J�hrlich (Sep.)", new int[] { 9 }), //
  JAERHLICH10(1210, "J�hrlich (Okt.)", new int[] { 10 }), //
  JAERHLICH11(1211, "J�hrlich (Nov.)", new int[] { 11 }), //
  JAERHLICH12(1212, "J�hrlich (Dez.)", new int[] { 12 });

  private final String text;

  private final int key;

  private int[] monate;

  Zahlungstermin(int key, String text, int[] monate)
  {
    this.key = key;
    this.text = text;
    this.monate = monate;
  }

  public int getKey()
  {
    return key;
  }

  public String getText()
  {
    return text;
  }

  public static Zahlungstermin getByKey(int key)
  {
    for (Zahlungstermin zt : Zahlungstermin.values())
    {
      if (zt.getKey() == key)
      {
        return zt;
      }
    }
    return null;
  }

  // public static ArrayList<Zahlungstermin> getZahlungstermin(int monat)
  // {
  // ArrayList<Zahlungstermin> ret = new ArrayList<Zahlungstermin>();
  // for (Zahlungstermin t : Zahlungstermin.values())
  // {
  // for (int m : t.monate)
  // {
  // if (m == monat)
  // {
  // ret.add(t);
  // }
  // }
  // }
  // return ret;
  // }
  public boolean isAbzurechnen(int monat)
  {
    for (int m : monate)
    {
      if (monat == m)
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString()
  {
    return getText();
  }
}
