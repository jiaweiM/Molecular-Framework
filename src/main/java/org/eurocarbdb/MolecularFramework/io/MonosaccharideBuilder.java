package org.eurocarbdb.MolecularFramework.io;

import org.eurocarbdb.MolecularFramework.sugar.*;

import java.util.ArrayList;

/**
 * @author rene
 */
public class MonosaccharideBuilder
{
    /**
     * Parse the glycoCT string format to Monosaccharide.
     *
     * @param a_strNameGlycoCT a GlycoCT format string.
     * @return a {@link Monosaccharide} object.
     * @throws GlycoconjugateException if parse failed.
     */
    public static Monosaccharide fromGlycoCT(String a_strNameGlycoCT) throws GlycoconjugateException
    {
        try {
            String a_strName = a_strNameGlycoCT + "$";
            int index = 0;
            char t_cToken = a_strName.charAt(index);
            // anomer
            Anomer t_objAnomer;
            t_objAnomer = Anomer.forSymbol(t_cToken);
            if (a_strName.charAt(++index) != '-') {
                return null;
            }
            index++;
            // configuration
            int t_iMaxPos = a_strName.indexOf(":", index) - 7;
            ArrayList<BaseType> t_aConfiguration = new ArrayList<>();
            String t_strInformation = "";
            while (index < t_iMaxPos) {
                t_strInformation = "";
                for (int t_iCounter = 0; t_iCounter < 4; t_iCounter++) {
                    t_strInformation += a_strName.charAt(index++);
                }
                t_aConfiguration.add(BaseType.forName(t_strInformation));
                if (a_strName.charAt(index++) != '-') {
                    return null;
                }
            }
            // superclass
            t_strInformation = "";
            for (int t_iCounter = 0; t_iCounter < 3; t_iCounter++) {
                t_strInformation += a_strName.charAt(index++);
            }
            Superclass t_objSuper;
            t_objSuper = Superclass.forName(t_strInformation.toLowerCase());
            Monosaccharide t_objMS = new Monosaccharide(t_objAnomer, t_objSuper);
            t_objMS.setBaseType(t_aConfiguration);
            if (a_strName.charAt(index++) != '-') {
                return null;
            }
            // ring
            int t_iRingStart;
            if (a_strName.charAt(index) == 'x') {
                t_iRingStart = Monosaccharide.UNKNOWN_RING;
                index++;
            } else {
                if (a_strName.charAt(index) == '0') {
                    index++;
                    t_iRingStart = 0;
                } else {
                    int t_iDigit = (int) a_strName.charAt(index++);
                    if (t_iDigit < 49 || t_iDigit > 57) {
                        return null;
                    }
                    t_iRingStart = t_iDigit - 48;
                    // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                    t_iDigit = (int) a_strName.charAt(index);
                    while (t_iDigit > 47 && t_iDigit < 58) {
                        t_iRingStart = (t_iRingStart * 10) + (t_iDigit - 48);
                        index++;
                        t_iDigit = (int) a_strName.charAt(index);
                    }
                }
            }
            if (a_strName.charAt(index++) != ':') {
                return null;
            }
            if (a_strName.charAt(index) == 'x') {
                t_objMS.setRing(t_iRingStart, Monosaccharide.UNKNOWN_RING);
                index++;
            } else {
                int t_iRingEnd = 0;
                if (a_strName.charAt(index) == '0') {
                    index++;
                    t_iRingEnd = 0;
                } else {
                    int t_iDigit = (int) a_strName.charAt(index++);
                    if (t_iDigit < 49 || t_iDigit > 57) {
                        return null;
                    }
                    t_iRingEnd = t_iDigit - 48;
                    // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                    t_iDigit = (int) a_strName.charAt(index);
                    while (t_iDigit > 47 && t_iDigit < 58) {
                        t_iRingEnd = (t_iRingEnd * 10) + (t_iDigit - 48);
                        index++;
                        t_iDigit = (int) a_strName.charAt(index);
                    }
                }
                t_objMS.setRing(t_iRingStart, t_iRingEnd);
            }
            // modifications
            while (a_strName.charAt(index) == '|') {
                int t_iPosOne;
                Integer t_iPosTwo = null;
                index++;
                if (a_strName.charAt(index) == 'x') {
                    t_iPosOne = Modification.UNKNOWN_POSITION;
                    index++;
                } else {
                    if (a_strName.charAt(index) == '0') {
                        index++;
                        t_iPosOne = 0;
                    } else {
                        int t_iDigit = (int) a_strName.charAt(index++);
                        if (t_iDigit < 49 || t_iDigit > 57) {
                            return null;
                        }
                        t_iPosOne = t_iDigit - 48;
                        // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                        t_iDigit = (int) a_strName.charAt(index);
                        while (t_iDigit > 47 && t_iDigit < 58) {
                            t_iPosOne = (t_iPosOne * 10) + (t_iDigit - 48);
                            index++;
                            t_iDigit = (int) a_strName.charAt(index);
                        }
                    }
                }
                if (a_strName.charAt(index) == ',') {
                    index++;
                    if (a_strName.charAt(index) == '0') {
                        index++;
                        t_iPosTwo = 0;
                    } else {
                        int t_iDigit = (int) a_strName.charAt(index++);
                        if (t_iDigit < 49 || t_iDigit > 57) {
                            return null;
                        }
                        t_iPosTwo = t_iDigit - 48;
                        // ( "1" | ... | "9" ) { "0" | "1" | ... | "9" }
                        t_iDigit = (int) a_strName.charAt(index);
                        while (t_iDigit > 47 && t_iDigit < 58) {
                            t_iPosTwo = (t_iPosTwo * 10) + (t_iDigit - 48);
                            index++;
                            t_iDigit = (int) a_strName.charAt(index);
                        }
                    }
                }
                if (a_strName.charAt(index++) != ':') {
                    return null;
                }
                t_strInformation = "";
                boolean t_bNext = true;
                while (t_bNext) {
                    t_cToken = a_strName.charAt(index);
                    t_bNext = false;
                    if (t_cToken >= 'A' && t_cToken <= 'Z') {
                        index++;
                        t_bNext = true;
                        t_strInformation += t_cToken;
                    } else if (t_cToken >= 'a' && t_cToken <= 'z') {
                        index++;
                        t_bNext = true;
                        t_strInformation += t_cToken;
                    }
                }
                ModificationType t_enumMod;
                t_enumMod = ModificationType.forName(t_strInformation);
                Modification t_objModi = new Modification(t_enumMod, t_iPosOne, t_iPosTwo);
                t_objMS.addModification(t_objModi);
            }
            return t_objMS;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
