package org.eurocarbdb.MolecularFramework.util.visitor;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;

/**
 * @author Logan
 */
public class GlycoVisitorResidueName implements GlycoVisitor
{
    private String m_strName = "";
    private boolean m_bReducing = true;
    private boolean m_bMonosaccharide = false;

    /**
     * {@inheritDoc}
     */
    public void visit(Monosaccharide arg0) throws GlycoVisitorException
    {
        this.m_strName = arg0.getGlycoCTName();
        this.m_bMonosaccharide = true;
    }

    @Override
    public void visit(NonMonosaccharide arg0) throws GlycoVisitorException
    {
        this.m_strName = arg0.getName();
    }

    @Override
    public void visit(SugarUnitRepeat arg0) throws GlycoVisitorException
    {
        if (this.m_bReducing) {
            arg0.getRepeatLinkage().getChild().accept(this);
        } else {
            arg0.getRepeatLinkage().getParent().accept(this);
        }
    }

    @Override
    public void visit(Substituent arg0) throws GlycoVisitorException
    {
        this.m_strName = arg0.getSubstituentType().getName();
    }

    @Override
    public void visit(SugarUnitCyclic arg0) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("SugarUnitCyclic is not supported by GlycoVisitorResidueName.");
    }

    @Override
    public void visit(SugarUnitAlternative arg0) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("SugarUnitAlternative is not supported by GlycoVisitorResidueName.");
    }

    @Override
    public void visit(UnvalidatedGlycoNode arg0) throws GlycoVisitorException
    {
        this.m_strName = arg0.getName();
    }

    @Override
    public void visit(GlycoEdge arg0) throws GlycoVisitorException
    {
        // nothing to do
    }

    @Override
    public void start(Sugar arg0) throws GlycoVisitorException
    {
        throw new GlycoVisitorException("GlycoVisitorResidueName works only for GlycoNodes use .start(GlycoNode).");
    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor arg0) throws GlycoVisitorException
    {
        return null;
    }

    @Override
    public void clear()
    {
        this.m_strName = null;
    }

    public String start(GlycoNode a_objNode, boolean a_bReducing) throws GlycoVisitorException
    {
        this.m_bMonosaccharide = false;
        this.m_bReducing = a_bReducing;
        a_objNode.accept(this);
        return this.m_strName;
    }

    /**
     * @return
     */
    public boolean isMonosaccharide()
    {
        return this.m_bMonosaccharide;
    }
}
