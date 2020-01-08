package org.eurocarbdb.MolecularFramework.util.visitor;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;

/**
 * @author rene
 */
public class GlycoVisitorNodeType implements GlycoVisitor {

    public static final int MONOSACCHARIDE = 0;
    public static final int NONMONOSACCHARIDE = 1;
    public static final int REPEAT = 2;
    public static final int CYCLIC = 3;
    public static final int SUBSTITUENT = 4;
    public static final int ALTERNATIVE = 5;
    public static final int UNVALIDATED = 6;

    private Monosaccharide m_objMS = null;
    private NonMonosaccharide m_objNonMS = null;
    private SugarUnitAlternative m_objAlternative = null;
    private SugarUnitCyclic m_objCyclic = null;
    private SugarUnitRepeat m_objRepeat = null;
    private UnvalidatedGlycoNode m_objUnvalidated = null;
    private Substituent m_objSubstitutent = null;

    private int m_iResidueType = -1;

    @Override
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.MONOSACCHARIDE;
        this.m_objMS = a_objMonosaccharid;
    }

    @Override
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.NONMONOSACCHARIDE;
        this.m_objNonMS = a_objResidue;
    }

    @Override
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // not a residue, nothing to do
    }

    @Override
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.REPEAT;
        this.m_objRepeat = a_objRepeate;
    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return null;
    }

    @Override
    public void clear() {
        this.m_objMS = null;
        this.m_objNonMS = null;
        this.m_objAlternative = null;
        this.m_objCyclic = null;
        this.m_objRepeat = null;
        this.m_objUnvalidated = null;
        this.m_objSubstitutent = null;
    }

    @Override
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.SUBSTITUENT;
        this.m_objSubstitutent = a_objSubstituent;
    }

    @Override
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.CYCLIC;
        this.m_objCyclic = a_objCyclic;
    }

    @Override
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.ALTERNATIVE;
        this.m_objAlternative = a_objAlternative;
    }

    @Override
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        this.m_iResidueType = GlycoVisitorNodeType.UNVALIDATED;
        this.m_objUnvalidated = a_objUnvalidated;
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        throw new GlycoVisitorException("Cant be used with a sugar.");
    }

    public int getNodeType(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType;
    }

    public boolean isMonosaccharide(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.MONOSACCHARIDE;
    }

    public boolean isNonMonosaccharide(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.NONMONOSACCHARIDE;
    }

    public boolean isSugarUnitRepeat(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.REPEAT;
    }

    public boolean isSugarUnitCyclic(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.CYCLIC;
    }

    public boolean isSubstituent(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.SUBSTITUENT;
    }

    public boolean isSugarUnitAlternative(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.ALTERNATIVE;
    }

    public boolean isUnvalidatedNode(GlycoNode a_objNode) throws GlycoVisitorException {
        a_objNode.accept(this);
        return this.m_iResidueType == GlycoVisitorNodeType.UNVALIDATED;
    }

    public Monosaccharide getMonosaccharide(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objMS;
    }

    public NonMonosaccharide getNonMonosaccharide(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objNonMS;
    }

    public SugarUnitRepeat getSugarUnitRepeat(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objRepeat;
    }

    public SugarUnitCyclic getSugarUnitCyclic(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objCyclic;
    }

    public Substituent getSubstituent(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objSubstitutent;
    }

    public SugarUnitAlternative getSugarUnitAlternative(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objAlternative;
    }

    public UnvalidatedGlycoNode getUnvalidatedNode(GlycoNode a_objNode) throws GlycoVisitorException {
        this.clear();
        a_objNode.accept(this);
        return this.m_objUnvalidated;
    }
}