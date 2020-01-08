package org.eurocarbdb.MolecularFramework.io;

import org.eurocarbdb.MolecularFramework.sugar.Anomer;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Superclass;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @date 2016.12.01, 11:26 PM
 */
public class MonosaccharideBuilderTest
{

    @Test
    public void fromGlycoCT() throws Exception
    {
        Monosaccharide monosaccharide = MonosaccharideBuilder.fromGlycoCT("b-dgal-HEX-1:5");
        assertEquals(1, monosaccharide.getRingStart());
        assertEquals(5, monosaccharide.getRingEnd());
        assertEquals(Anomer.Beta, monosaccharide.getAnomer());

        ArrayList<BaseType> baseType = monosaccharide.getBaseType();
        assertEquals(1, baseType.size());
        assertEquals(BaseType.DGAL, baseType.get(0));

        assertEquals(Superclass.HEX, monosaccharide.getSuperclass());
    }

}