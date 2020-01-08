package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;


public class NonMonosaccharide extends GlycoNode {

    public static final int POSITION_UNKNOWN = -1;
    private String m_strName = "";
    private int m_iAttachPosition = NonMonosaccharide.POSITION_UNKNOWN;

    @Override
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        a_objVisitor.visit(this);
    }

    public NonMonosaccharide(String a_strName) throws GlycoconjugateException {
        this.setName(a_strName);
    }

    public void setName(String a_strName) throws GlycoconjugateException {
        if (a_strName == null) {
            throw new GlycoconjugateException("null is not allowed for a name.");
        }
        this.m_strName = a_strName;
    }

    public String getName() {
        return this.m_strName;
    }

    public void setAttachPosition(int a_iPosition) {
        this.m_iAttachPosition = a_iPosition;
    }

    public int getAttachPosition() {
        return this.m_iAttachPosition;
    }

    @Override
    public NonMonosaccharide copy() throws GlycoconjugateException {
        NonMonosaccharide t_objCopy = new NonMonosaccharide(this.m_strName);
        t_objCopy.setAttachPosition(this.m_iAttachPosition);
        return t_objCopy;
    }

}