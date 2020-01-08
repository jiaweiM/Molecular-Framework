package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.Visitable;

import java.util.ArrayList;

/**
 * @author rene
 * @version 1.0.0
 * @author  JiaweiMao
 * @date	2016.12.01, 11:12 PM
 */
public class GlycoEdge implements Visitable {

    private GlycoNode m_objParent = null;
    private GlycoNode m_objChild = null;
    private ArrayList<Linkage> m_aLinkages = new ArrayList<>();

    public void setParent(GlycoNode a_objParent) {
        this.m_objParent = a_objParent;
    }

    public void setChild(GlycoNode a_objChild) {
        this.m_objChild = a_objChild;
    }

    public GlycoNode getChild() {
        return this.m_objChild;
    }

    public GlycoNode getParent() {
        return this.m_objParent;
    }

    public void setGlycosidicLinkages(ArrayList<Linkage> a_aLinkages) throws GlycoconjugateException {
        if (a_aLinkages == null) {
            throw new GlycoconjugateException("null is not a valide set of linkages.");
        }
        this.m_aLinkages.clear();
        for (Linkage a_aLinkage : a_aLinkages) {
            this.addGlycosidicLinkage(a_aLinkage);
        }
        this.m_aLinkages = a_aLinkages;
    }

    public ArrayList<Linkage> getGlycosidicLinkages() {
        return this.m_aLinkages;
    }

    public boolean addGlycosidicLinkage(Linkage a_objLinkage) throws GlycoconjugateException {
        if (a_objLinkage == null) {
            throw new GlycoconjugateException("null linkage is not allowed");
        }
        return !this.m_aLinkages.contains(a_objLinkage) && this.m_aLinkages.add(a_objLinkage);
    }

    public boolean removeGlycosidicLinkage(Linkage a_objLinkage) {
        return this.m_aLinkages.contains(a_objLinkage) && this.m_aLinkages.remove(a_objLinkage);
    }

    @Override
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        a_objVisitor.visit(this);
    }

    /**
     * copy without parent and childs
     *
     * @return copy of this GlycoEdge.
     * @throws GlycoconjugateException
     */
    public GlycoEdge copy() throws GlycoconjugateException {
        GlycoEdge t_objCopy = new GlycoEdge();
        for (Linkage m_aLinkage : this.m_aLinkages) {
            t_objCopy.addGlycosidicLinkage(m_aLinkage.copy());
        }

        return t_objCopy;
    }
}