package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * @author rene
 * @author JiaweiMao
 * @version 1.0.0
 * @date 2016.12.01, 11:05 PM
 */
public class Substituent extends GlycoNode {

    private SubstituentType m_enumSubstType;

    public Substituent(SubstituentType a_enumType) throws GlycoconjugateException {
        this.setSubstituentType(a_enumType);
    }

    @Override
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        a_objVisitor.visit(this);
    }

    public void setSubstituentType(SubstituentType a_enumType) throws GlycoconjugateException {
        if (a_enumType == null) {
            throw new GlycoconjugateException("Invalide substituent.");
        }
        this.m_enumSubstType = a_enumType;
    }

    public SubstituentType getSubstituentType() {
        return this.m_enumSubstType;
    }

    @Override
    public Substituent copy() throws GlycoconjugateException {
        return new Substituent(this.m_enumSubstType);
    }
}
