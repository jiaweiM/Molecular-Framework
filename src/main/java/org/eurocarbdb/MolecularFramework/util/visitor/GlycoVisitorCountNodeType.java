package org.eurocarbdb.MolecularFramework.util.visitor;


import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserSimple;


/**
 * @author logan
 */
public class GlycoVisitorCountNodeType implements GlycoVisitor {

    private int m_iSubstituentCount;
    private int m_iNonMonosaccharideCount;
    private int m_iCyclicCount;
    private int m_iMonosaccharideCount;
    private int m_iRepeatCount;
    private int m_iUnderdetermindedCount;
    private int m_iAlternativeCount;
    private int m_iUnvalidatedGlycoNode;

    @Override
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        this.m_iMonosaccharideCount++;
    }

    @Override
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        this.m_iNonMonosaccharideCount++;
    }

    @Override
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // Nothing to do        
    }

    @Override
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException {
        GlycoTraverser t_trav = this.getTraverser(this);
        t_trav.traverseGraph(a_objRepeate);
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objRepeate.getUndeterminedSubTrees()) {
            t_trav = this.getTraverser(this);
            t_trav.traverseGraph(underdeterminedSubTree);
            this.m_iUnderdetermindedCount++;
        }
        this.m_iRepeatCount++;
    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return new GlycoTraverserSimple(a_objVisitor);
    }

    @Override
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        this.m_iSubstituentCount++;
    }

    @Override
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        this.m_iCyclicCount++;
    }

    @Override
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        throw new GlycoVisitorException("SugarUnitAlternative are not allowed.");
    }

    @Override
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        this.m_iUnvalidatedGlycoNode++;
    }


    @Override
    public void clear() {
        this.m_iRepeatCount = 0;
        this.m_iMonosaccharideCount = 0;
        this.m_iNonMonosaccharideCount = 0;
        this.m_iCyclicCount = 0;
        this.m_iSubstituentCount = 0;
        this.m_iAlternativeCount = 0;
        this.m_iUnvalidatedGlycoNode = 0;
        this.m_iUnderdetermindedCount = 0;
    }

    public int getNonMonosaccharideCount() {
        return this.m_iNonMonosaccharideCount;
    }

    public int getMonosaccharideCount() {
        return this.m_iMonosaccharideCount;
    }

    public int getSubstituentCount() {
        return this.m_iSubstituentCount;
    }


    public int getRepeatCount() {
        return this.m_iRepeatCount;
    }

    public int getAlternativeNodeCount() {
        return this.m_iAlternativeCount;
    }

    public int getCyclicCount() {
        return this.m_iCyclicCount;
    }

    public int getUnvalidatedNodeCount() {
        return this.m_iUnvalidatedGlycoNode;
    }

    public int getUnderdetermindedCount() {
        return this.m_iUnderdetermindedCount;
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objSugar.getUndeterminedSubTrees()) {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(underdeterminedSubTree);
            this.m_iUnderdetermindedCount++;
        }
    }

    public void start(GlycoNode a_objResidue) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverse(a_objResidue);
    }
}