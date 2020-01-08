package org.eurocarbdb.MolecularFramework.io;

import org.eurocarbdb.MolecularFramework.sugar.Sugar;


/**
 * @author rene
 */
public abstract class SugarExporter {

    // sugar object that is build
    protected Sugar m_objSugar = null;

    public abstract String export(Sugar a_objSugar) throws SugarExporterException;

}
