package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author rene
 */
public class SugarUnitAlternative extends GlycoNode {

    private ArrayList<GlycoGraphAlternative> m_aAlternatives = new ArrayList<GlycoGraphAlternative>();


    @Override
    public void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        a_objVisitor.visit(this);
    }

    public void setAlternatives(ArrayList<GlycoGraphAlternative> a_aList) throws GlycoconjugateException {
        if (a_aList == null) {
            throw new GlycoconjugateException("null is not a valid set of alternatives");
        }
        this.m_aAlternatives.clear();
        for (GlycoGraphAlternative anA_aList : a_aList) {
            this.addAlternative(anA_aList);
        }
    }

    public ArrayList<GlycoGraphAlternative> getAlternatives() {
        return this.m_aAlternatives;
    }

    public void addAlternative(GlycoGraphAlternative a_objAlternative) throws GlycoconjugateException {
        if (a_objAlternative == null) {
            throw new GlycoconjugateException("null is not a valid residue.");
        }
        if (!this.m_aAlternatives.contains(a_objAlternative)) {
            this.m_aAlternatives.add(a_objAlternative);
        }
    }

    public void removeAlternative(GlycoGraphAlternative a_objAlternative) throws GlycoconjugateException {
        if (!this.m_aAlternatives.contains(a_objAlternative)) {
            throw new GlycoconjugateException("Can't remove invalid alternative residue.");
        }
        this.m_aAlternatives.remove(a_objAlternative);
    }

    /**
     * Copies the alternative residue. All alternative graphs are copied. For the child connections the tuple "old egde"
     * "new glyconode" is set.
     *
     * @see org.eurocarbdb.MolecularFramework.sugar.GlycoNode#copy()
     */
    public SugarUnitAlternative copy() throws GlycoconjugateException {
        SugarUnitAlternative t_objCopy = new SugarUnitAlternative();
        for (GlycoGraphAlternative m_aAlternative : this.m_aAlternatives) {
            t_objCopy.addAlternative(m_aAlternative.copy());
        }
        return t_objCopy;
    }

    public void setLeadInNode(GlycoNode a_objParent, GlycoGraphAlternative a_objAlternative) throws
            GlycoconjugateException {
        if (!this.m_aAlternatives.contains(a_objAlternative)) {
            throw new GlycoconjugateException("GlycoGraphAlternative is not part of this sugar unit alternative.");
        }
        if (this.m_objParentLinkage == null) {
            throw new GlycoconjugateException("This sugar unit alternative does not have a parent linkage.");
        }
        a_objAlternative.setLeadInNode(a_objParent);
    }

    public void addLeadOutNodeToNode(GlycoNode a_objParent, GlycoGraphAlternative a_objAlternative, GlycoNode
            a_objChild) throws GlycoconjugateException {
        if (!this.m_aAlternatives.contains(a_objAlternative)) {
            throw new GlycoconjugateException("GlycoGraphAlternative is not part of this sugar unit alternative.");
        }
        boolean t_bFound = false;
        for (GlycoEdge m_aChildLinkage : this.m_aChildLinkages) {
            if (a_objChild == m_aChildLinkage.getChild()) {
                t_bFound = true;
            }
        }
        if (!t_bFound) {
            throw new GlycoconjugateException("This sugar unit alternative does not have this child linkage.");
        }
        a_objAlternative.addLeadOutNodeToNode(a_objChild, a_objParent);
    }


    public void setLeadOutNodeToNode(HashMap<GlycoNode, GlycoNode> a_hNodeToNode, GlycoGraphAlternative
            a_objAltGraph) throws GlycoconjugateException {
        a_objAltGraph.removeAllLeadOutNodes();
        for (GlycoNode t_objKEy : a_hNodeToNode.keySet()) {
            this.addLeadOutNodeToNode(a_hNodeToNode.get(t_objKEy), a_objAltGraph, t_objKEy);
        }
    }
}
