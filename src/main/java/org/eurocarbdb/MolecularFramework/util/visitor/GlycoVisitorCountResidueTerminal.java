package org.eurocarbdb.MolecularFramework.util.visitor;


import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;

import java.util.ArrayList;


/**
 * will ignore underdeterminded subtrees
 *
 * @author Logan
 */
public class GlycoVisitorCountResidueTerminal implements GlycoVisitor {

    private int m_iTerminalResidue;
    private int m_iTerminalBasetype;
    private int m_iTerminalMonosaccharide;
    private int m_iTerminalSubstituents;

    @Override
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        ArrayList<GlycoEdge> t_objLinkages = a_objMonosaccharid.getChildEdges();

        if (t_objLinkages.size() == 0) {
            this.m_iTerminalResidue++;
            this.m_iTerminalBasetype++;
            this.m_iTerminalMonosaccharide++;
        } else {
            boolean t_bTerminal = true;
            GlycoVisitorNodeType t_objType = new GlycoVisitorNodeType();
            for (GlycoNode t_objChild : a_objMonosaccharid.getChildNodes()) {
                if (t_objType.isMonosaccharide(t_objChild)) {
                    t_bTerminal = false;
                } else if (t_objType.isSubstituent(t_objChild)) {
                    if (t_objChild.getChildEdges().size() != 0) {
                        t_bTerminal = false;
                    }
                } else if (t_objType.isSugarUnitAlternative(t_objChild)) {
                    t_bTerminal = false;
                } else if (t_objType.isSugarUnitCyclic(t_objChild)) {
                    t_bTerminal = false;
                } else if (t_objType.isSugarUnitRepeat(t_objChild)) {
                    t_bTerminal = false;
                }
            }
            if (t_bTerminal) {
                this.m_iTerminalMonosaccharide++;
                this.m_iTerminalResidue++;
            }
        }
    }

    @Override
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        throw new GlycoVisitorException("NonMonosaccharides are not allowed.");
    }


    @Override
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException {
        GlycoTraverser t_trav = this.getTraverser(this);
        t_trav.traverseGraph(a_objRepeate);
        GlycoNode t_objNode = a_objRepeate.getRepeatLinkage().getParent();
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        if (t_objNode.getChildEdges().size() == 0) {
            if (t_visType.isMonosaccharide(t_objNode)) {
                this.m_iTerminalMonosaccharide--;
                this.m_iTerminalResidue--;
                this.m_iTerminalBasetype--;
            } else {
                this.m_iTerminalSubstituents--;
                this.m_iTerminalResidue--;
            }
        } else {
            if (t_visType.isMonosaccharide(t_objNode)) {
                boolean t_bTerminal = true;
                for (GlycoNode t_objChild : t_objNode.getChildNodes()) {
                    if (t_visType.isMonosaccharide(t_objChild)) {
                        t_bTerminal = false;
                    } else if (t_visType.isSubstituent(t_objChild)) {
                        if (t_objChild.getChildEdges().size() != 0) {
                            t_bTerminal = false;
                        }
                    } else if (t_visType.isSugarUnitAlternative(t_objChild)) {
                        t_bTerminal = false;
                    } else if (t_visType.isSugarUnitCyclic(t_objChild)) {
                        t_bTerminal = false;
                    } else if (t_visType.isSugarUnitRepeat(t_objChild)) {
                        t_bTerminal = false;
                    }
                }
                if (t_bTerminal) {
                    this.m_iTerminalMonosaccharide--;
                    this.m_iTerminalResidue--;
                }
            }
        }
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objRepeate.getUndeterminedSubTrees()) {
            t_trav = this.getTraverser(this);
            t_trav.traverseGraph(underdeterminedSubTree);
        }
    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    @Override
    public void clear() {
        this.m_iTerminalMonosaccharide = 0;
        this.m_iTerminalResidue = 0;
        this.m_iTerminalSubstituents = 0;
        this.m_iTerminalBasetype = 0;
    }

    public int getTerminalCountResidue() {
        return this.m_iTerminalResidue;
    }

    public int getTerminalMonosaccharide() {
        return this.m_iTerminalMonosaccharide;
    }

    public int getTerminalBasetype() {
        return this.m_iTerminalBasetype;
    }

    public int getTerminalSubstituent() {
        return this.m_iTerminalSubstituents;
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objSugar.getUndeterminedSubTrees()) {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(underdeterminedSubTree);
        }
    }

    public void start(GlycoNode a_objResidue) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverse(a_objResidue);
    }

    @Override
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        ArrayList<GlycoEdge> t_objLinkages = a_objSubstituent.getChildEdges();

        if (t_objLinkages.size() == 0) {
            this.m_iTerminalResidue++;
            this.m_iTerminalSubstituents++;
        }
    }

    @Override
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
    }

    @Override
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        throw new GlycoVisitorException("SugarUnitAlternative are not allowed.");
    }

    @Override
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        throw new GlycoVisitorException("UnvalidatedGlycoNode are not allowed.");
    }

    @Override
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // nothing to do
    }

}
