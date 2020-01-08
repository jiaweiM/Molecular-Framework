package org.eurocarbdb.MolecularFramework.util.visitor;


import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;

import java.util.ArrayList;

public class GlycoVisitorCountBranchingPoints implements GlycoVisitor {

    private int m_iBranchingPointsAllResidues;
    private int m_iBranchingPointsOnlyMonosaccharide;
    private ArrayList<GlycoNode> m_aBranchingPointResidue = new ArrayList<>();
    private ArrayList<GlycoNode> m_aBranchingPointMonosaccharide = new ArrayList<>();

    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {

        ArrayList<GlycoEdge> t_objLinkages = a_objMonosaccharid.getChildEdges();

        if (t_objLinkages.size() > 1) {
            this.m_iBranchingPointsAllResidues++;
            this.m_aBranchingPointResidue.add(a_objMonosaccharid);
        }
        // monosaccharide branching count
        Integer t_iMonosaccharideCount = 0;
        for (GlycoEdge t_edge : t_objLinkages) {
            GlycoVisitorNodeType o_tVisitor = new GlycoVisitorNodeType();
            if (o_tVisitor.isMonosaccharide(t_edge.getChild())) {
                t_iMonosaccharideCount++;
            } else if (o_tVisitor.isSugarUnitRepeat(t_edge.getChild())) {
                t_iMonosaccharideCount++;
            }
        }
        if (t_iMonosaccharideCount > 1) {
            this.m_iBranchingPointsOnlyMonosaccharide++;
            this.m_aBranchingPointMonosaccharide.add(a_objMonosaccharid);
        }
    }


    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        throw new GlycoVisitorException("NonMonosaccharides are not allowed.");
    }

    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    public void clear() {
        this.m_iBranchingPointsAllResidues = 0;
        this.m_iBranchingPointsOnlyMonosaccharide = 0;
        this.m_aBranchingPointMonosaccharide.clear();
        this.m_aBranchingPointResidue.clear();
    }

    public int getBranchingPointsCountResidue() {
        return this.m_iBranchingPointsAllResidues;
    }

    public int getBranchingPointsCountMonosaccharide() {
        return this.m_iBranchingPointsOnlyMonosaccharide;
    }

    public void start(GlycoNode a_objResidue) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverse(a_objResidue);
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (UnderdeterminedSubTree t_objSubTree : a_objSugar.getUndeterminedSubTrees()) {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_objSubTree);
            boolean t_bNewResidue = false;
            boolean t_bNewMonosaccharide = false;
            for (GlycoNode t_objNode : t_objSubTree.getParents()) {
                GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
                if (t_visType.isMonosaccharide(t_objNode)) {
                    if (t_objNode.getChildEdges().size() == 1) {
                        if (!this.m_aBranchingPointResidue.contains(t_objNode)) {
                            this.m_aBranchingPointResidue.add(t_objNode);
                            t_bNewResidue = true;
                        }
                    }
                    int t_iMonosaccharideCount = 0;
                    for (GlycoEdge t_edge : t_objNode.getChildEdges()) {
                        GlycoVisitorNodeType o_tVisitor = new GlycoVisitorNodeType();
                        if (o_tVisitor.isMonosaccharide(t_edge.getChild())) {
                            t_iMonosaccharideCount++;
                        } else if (o_tVisitor.isSugarUnitRepeat(t_edge.getChild())) {
                            t_iMonosaccharideCount++;
                        }
                    }
                    if (t_iMonosaccharideCount == 1) {
                        if (!this.m_aBranchingPointMonosaccharide.contains(t_objNode)) {
                            this.m_aBranchingPointMonosaccharide.add(t_objNode);
                            t_bNewMonosaccharide = true;
                        }
                    }
                } else {
                    if (t_objNode.getChildEdges().size() == 1) {
                        if (!this.m_aBranchingPointResidue.contains(t_objNode)) {
                            this.m_aBranchingPointResidue.add(t_objNode);
                            t_bNewResidue = true;
                        }
                    }
                }
            }
            if (t_bNewResidue) {
                this.m_iBranchingPointsAllResidues++;
            }
            if (t_bNewMonosaccharide) {
                this.m_iBranchingPointsOnlyMonosaccharide++;
            }
        }
    }


    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        ArrayList<GlycoEdge> t_objLinkages = a_objSubstituent.getChildEdges();

        if (t_objLinkages.size() > 1) {
            this.m_iBranchingPointsAllResidues++;
            this.m_iBranchingPointsOnlyMonosaccharide++;
            this.m_aBranchingPointResidue.add(a_objSubstituent);
        }
    }


    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        // nothing to do, cyclic can not have childs
    }


    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        throw new GlycoVisitorException("SugarUnitAlternative are not allowed.");
    }

    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        throw new GlycoVisitorException("UnvalidatedGlycoNodes are not allowed.");
    }

    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // nothing to do
    }

    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeate);
        GlycoNode t_objNode = a_objRepeate.getRepeatLinkage().getParent();
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        if (t_visType.isMonosaccharide(t_objNode)) {
            if (t_objNode.getChildEdges().size() == 1) {
                this.m_iBranchingPointsAllResidues++;
                this.m_aBranchingPointResidue.add(t_objNode);
            }
            Integer t_iMonosaccharideCount = 0;
            for (GlycoEdge t_edge : t_objNode.getChildEdges()) {
                if (t_visType.isMonosaccharide(t_edge.getChild())) {
                    t_iMonosaccharideCount++;
                }
            }
            if (t_iMonosaccharideCount == 1) {
                this.m_iBranchingPointsOnlyMonosaccharide++;
                this.m_aBranchingPointMonosaccharide.add(t_objNode);
            }
        } else {
            if (t_objNode.getChildEdges().size() == 1) {
                this.m_iBranchingPointsAllResidues++;
                this.m_iBranchingPointsOnlyMonosaccharide++;
                this.m_aBranchingPointResidue.add(t_objNode);
            }
        }
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objRepeate.getUndeterminedSubTrees()) {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(underdeterminedSubTree);
        }
    }
}