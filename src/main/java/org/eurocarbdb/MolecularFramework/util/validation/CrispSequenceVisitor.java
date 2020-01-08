package org.eurocarbdb.MolecularFramework.util.validation;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserSimple;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

public class CrispSequenceVisitor implements GlycoVisitor {

    private boolean AnomerUnc;
    private boolean RingsizeUnc;
    private boolean BasetypeDefinedUnc;
    private boolean SugJokerSet;
    private boolean ModificationUnc;
    private boolean LinkageTypeUnc;
    private boolean LinkagePosUnc;
    private boolean AltUnitFound;
    private boolean UndUnitFound;
    private boolean RepCountUnc;
    private boolean UnconnectedTree;
    private boolean NonSugObject;

    public void clear() {
        this.AnomerUnc = false;
        this.RingsizeUnc = false;
        this.BasetypeDefinedUnc = false;
        this.SugJokerSet = false;
        this.ModificationUnc = false;
        this.LinkageTypeUnc = false;
        this.LinkagePosUnc = false;
        this.AltUnitFound = false;
        this.UndUnitFound = false;
        this.RepCountUnc = false;
        this.UnconnectedTree = false;
        this.NonSugObject = false;

    }

    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return new GlycoTraverserSimple(a_objVisitor);
    }

    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        this.clear();
        try {
            if (a_objSugar.getRootNodes().size() > 1) {
                this.UnconnectedTree = true;
            }
            GlycoTraverser g = this.getTraverser(this);
            g.traverseGraph(a_objSugar);
            for (UnderdeterminedSubTree underdeterminedSubTree : a_objSugar.getUndeterminedSubTrees()) {
                this.UndUnitFound = true;
                g = this.getTraverser(this);
                UnderdeterminedSubTree t = underdeterminedSubTree;
                if (t.getRootNodes().size() > 1) {
                    this.UnconnectedTree = true;
                }
                g.traverseGraph(t);
            }
        } catch (GlycoconjugateException e) {
            throw new GlycoVisitorException(e.getMessage(), e);
        }
    }

    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        if (a_objMonosaccharid.getAnomer() == Anomer.Unknown) {
            this.AnomerUnc = true;
        }

        if (a_objMonosaccharid.getRingEnd() == Monosaccharide.UNKNOWN_RING ||
                a_objMonosaccharid.getRingStart() == Monosaccharide.UNKNOWN_RING) {
            this.RingsizeUnc = true;
        }

        if (a_objMonosaccharid.getSuperclass() == Superclass.SUG) {
            this.SugJokerSet = true;
        }

        for (BaseType b : a_objMonosaccharid.getBaseType()) {
            if (b.absoluteConfigurationUnknown()) {
                this.BasetypeDefinedUnc = true;
            }
        }
        if (a_objMonosaccharid.getBaseType().size() == 0) {
            this.BasetypeDefinedUnc = true;
        }

        for (Modification m : a_objMonosaccharid.getModification()) {

            if ((m.hasPositionOne() && m.getPositionOne() == Modification.UNKNOWN_POSITION) ||
                    (m.hasPositionTwo() && m.getPositionTwo() == Modification.UNKNOWN_POSITION)) {
                this.ModificationUnc = true;
            }

        }
    }

    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        this.NonSugObject = true;
    }

    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
        if (a_objRepeat.getMaxRepeatCount() == SugarUnitRepeat.UNKNOWN
                || a_objRepeat.getMinRepeatCount() == SugarUnitRepeat.UNKNOWN) {
            this.RepCountUnc = true;
        }
        try {
            if (a_objRepeat.getRootNodes().size() > 1) {
                this.UnconnectedTree = true;
            }
            GlycoTraverser g = this.getTraverser(this);
            g.traverseGraph(a_objRepeat);
            for (UnderdeterminedSubTree underdeterminedSubTree : a_objRepeat.getUndeterminedSubTrees()) {
                this.UndUnitFound = true;
                g = this.getTraverser(this);
                UnderdeterminedSubTree t = underdeterminedSubTree;
                if (t.getRootNodes().size() > 1) {
                    this.UnconnectedTree = true;
                }
                g.traverseGraph(t);
            }
        } catch (GlycoconjugateException e) {
            throw new GlycoVisitorException(e.getMessage(), e);
        }
    }

    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {

    }

    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {

    }

    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        this.NonSugObject = true;
    }

    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {

        for (Linkage lin : a_objLinkage.getGlycosidicLinkages()) {

            if (lin.getChildLinkageType() == LinkageType.UNKNOWN
                    || lin.getParentLinkageType() == LinkageType.UNKNOWN) {
                this.LinkageTypeUnc = true;
            }

            if (lin.getParentLinkages().size() > 1 || lin.getChildLinkages().size() > 1) {
                this.LinkagePosUnc = true;
            }

            for (Integer t_i : lin.getParentLinkages()) {
                if (t_i == Linkage.UNKNOWN_POSITION) {
                    this.LinkagePosUnc = true;
                }
            }

            for (Integer t_i : lin.getChildLinkages()) {
                if (t_i == Linkage.UNKNOWN_POSITION) {
                    this.LinkagePosUnc = true;
                }
            }
        }
    }

    public boolean CrispStructure() {
        return !(AltUnitFound ||
                AnomerUnc ||
                BasetypeDefinedUnc ||
                LinkagePosUnc ||
                LinkageTypeUnc ||
                ModificationUnc ||
                NonSugObject ||
                RepCountUnc ||
                RingsizeUnc ||
                SugJokerSet ||
                UnconnectedTree ||
                UndUnitFound);

    }


    public boolean getAltUnitFound() {
        return AltUnitFound;
    }

    public boolean getAnomerUnc() {
        return AnomerUnc;
    }

    public boolean getBasetypeDefinedUnc() {
        return BasetypeDefinedUnc;
    }

    public boolean getLinkagePosUnc() {
        return LinkagePosUnc;
    }

    public boolean getLinkageTypeUnc() {
        return LinkageTypeUnc;
    }

    public boolean getModificationUnc() {
        return ModificationUnc;
    }

    public boolean getNonSugObject() {
        return NonSugObject;
    }

    public boolean getRepCountUnc() {
        return RepCountUnc;
    }

    public boolean getRingsizeUnc() {
        return RingsizeUnc;
    }

    public boolean getSugJokerSet() {
        return SugJokerSet;
    }

    public boolean getUnconnectedTree() {
        return UnconnectedTree;
    }

    public boolean getUndUnitFound() {
        return UndUnitFound;
    }

    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        this.AltUnitFound = true;
        for (GlycoGraphAlternative t_objGraph : a_objAlternative.getAlternatives()) {
            try {
                if (t_objGraph.getRootNodes().size() > 1) {
                    this.UnconnectedTree = true;
                }
                GlycoTraverser g = this.getTraverser(this);
                g.traverseGraph(t_objGraph);
            } catch (GlycoconjugateException e) {
                throw new GlycoVisitorException(e.getMessage(), e);
            }
        }
    }
}
