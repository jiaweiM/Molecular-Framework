/**
 *
 */
package org.eurocarbdb.MolecularFramework.util.visitor;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserNodes;

/**
 * Takes an unvalidated sugar and replaces all occurence of a pattern in the names of
 * UnvalidatedGlycoNode. Case sensitive.
 *
 * @author rene
 */
public class GlycoVisitorReplaceName implements GlycoVisitor
{

    private String m_strPattern = "";
    private String m_strName = "";


    public GlycoVisitorReplaceName(String a_strPattern, String a_strName)
    {
        this.m_strPattern = a_strPattern;
        this.m_strName = a_strName;
    }

    @Override
    public void visit(Monosaccharide arg0) throws GlycoVisitorException
    {
        // do nothing
    }

    @Override
    public void visit(UnvalidatedGlycoNode a_objNonMonosaccharide) throws GlycoVisitorException
    {
        String t_strName = a_objNonMonosaccharide.getName();
        int t_iPos = t_strName.indexOf(this.m_strPattern);
        if (t_iPos != -1) {
            String t_strNameOld = t_strName.substring(t_iPos, t_iPos + this.m_strPattern.length());
            try {
                a_objNonMonosaccharide.setName(t_strName.replaceAll(t_strNameOld, this.m_strName));
            } catch (GlycoconjugateException e) {
                throw new GlycoVisitorException(e.getMessage(), e);
            }
        }
    }

    /**
     * @see {@link Sugar}
     */
    public void visit(Sugar a_objSugar) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
    }

    @Override
    public void visit(GlycoEdge arg0) throws GlycoVisitorException
    {
        // nothing to do        
    }

    @Override
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objRepeat);
        for (UnderdeterminedSubTree t_oSubtree : a_objRepeat.getUndeterminedSubTrees()) {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_oSubtree);
        }
    }

    /**
     * @see Sugar
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException
    {
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        for (UnderdeterminedSubTree t_oSubtree : a_objSugar.getUndeterminedSubTrees()) {
            t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_oSubtree);
        }
    }

    /**
     * @throws GlycoVisitorException
     * @see GlycoVisitor
     */
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException
    {
        return new GlycoTraverserNodes(a_objVisitor);
    }

    /**
     * @see GlycoVisitor#clear()
     */
    public void clear()
    {
    }

    /**
     * @see GlycoVisitor#visit(NonMonosaccharide)
     */
    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException
    {
        // do nothing
    }

    /**
     * @see GlycoVisitor#visit(Substituent)
     */
    public void visit(Substituent arg0) throws GlycoVisitorException
    {
        // do nothing
    }

    /**
     * @see GlycoVisitor#visit(SugarUnitCyclic)
     */
    public void visit(SugarUnitCyclic arg0) throws GlycoVisitorException
    {
        // do nothing
    }

    /**
     * @see GlycoVisitor#visit(SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException
    {
        for (GlycoGraphAlternative t_objGraph : a_objAlternative.getAlternatives()) {
            GlycoTraverser t_objTraverser = this.getTraverser(this);
            t_objTraverser.traverseGraph(t_objGraph);
        }
    }
}
