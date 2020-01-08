package org.eurocarbdb.MolecularFramework.util.analytical.mass;

import org.eurocarbdb.MolecularFramework.sugar.*;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverserTreeSingle;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

import java.util.ArrayList;

/**
 * NOTE: THIS VISITOR HANDLES ONLY FULL CONNECTED MONOSACCHARIDES
 *
 * @author rene
 * @author JiaweiMao
 * @version 1.0.0
 * @date 2016.12.01, 10:45 PM
 */
public class GlycoVisitorMass implements GlycoVisitor {

    public static final int DERIVATISATION_NONE = 0;
    public static final int DERIVATISATION_PME = 1;
    public static final int DERIVATISATION_PDME = 2;
    public static final int DERIVATISATION_PAC = 3;
    public static final int DERIVATISATION_PDAC = 4;

    protected boolean m_bMonoisotopic = true;
    protected double m_dMass = 0;
    protected MassComponents m_objMasses = new MassComponents();
    protected int m_iPerMePos = 0;
    protected int m_iPerAcPos = 0;

    public double getMass(int a_iDerivate) {
        double t_dMassReduction = 0;
        // TODO
//		if ( a_iDerivate == DERIVATISATION_PME || a_iDerivate == DERIVATISATION_PME )
//		{
//			t_dMassReduction = this.m_iPerMePos * this.m_objMasses.getDerivatisation(a_iDerivate);
//		}
//		else if ( a_iDerivate == DERIVATISATION_PAC || a_iDerivate == DERIVATISATION_PDAC )
//		{
//			t_dMassReduction = this.m_iPerAcPos * this.m_objMasses.getDerivatisation(a_iDerivate);
//		} 
        return this.m_dMass - t_dMassReduction;
    }

    // TODO durch obere ersaetzen
    public double getMass() {
        return this.m_dMass;
    }

    public void setMonoisotopic(boolean a_bMonoisotpic) {
        this.m_bMonoisotopic = a_bMonoisotpic;
    }

    public boolean getMonoisotopic() {
        return this.m_bMonoisotopic;
    }

    @Override
    public void clear() {
        this.m_dMass = 0;
    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return new GlycoTraverserTreeSingle(a_objVisitor);
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(Monosaccharide)
     */
    public void visit(Monosaccharide a_objMonosaccharide) throws GlycoVisitorException {
        // mass of the basetype
        double t_dMass = this.m_objMasses.getSuperclassMass(a_objMonosaccharide.getSuperclass(), this.m_bMonoisotopic);
        // mass of the modifications
        int t_iKetoCount = 0;
        for (Modification t_objModi : a_objMonosaccharide.getModification()) {
            if (t_objModi.getModificationType() != ModificationType.KETO) {
                t_dMass += this.m_objMasses.getModificationMass(t_objModi.getModificationType(), this
                        .m_bMonoisotopic, t_objModi.getPositionOne());
            } else {
                t_iKetoCount++;
            }
        }
        if (t_iKetoCount > 1) {
            t_dMass += (t_iKetoCount - 1) * this.m_objMasses.getModificationMass(ModificationType.KETO, this
                    .m_bMonoisotopic, 2);
        }
        // mass of the linkages around
        if (a_objMonosaccharide.getParentEdge() != null) {
            for (Linkage linkage : a_objMonosaccharide.getParentEdge().getGlycosidicLinkages()) {
                LinkageType t_objLinkageType = linkage.getChildLinkageType();
                t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkageType, this.m_bMonoisotopic);
            }
        }
        for (GlycoEdge glycoEdge : a_objMonosaccharide.getChildEdges()) {
            for (Linkage linkage : glycoEdge.getGlycosidicLinkages()) {
                LinkageType t_objLinkageType = linkage.getParentLinkageType();
                t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkageType, this.m_bMonoisotopic);
            }
        }
        this.m_dMass += t_dMass;
    }

    @Override
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        double t_dMass = this.m_objMasses.getSubstitutionsMass(a_objSubstituent.getSubstituentType(), this
                .m_bMonoisotopic);
        int t_iLinkageCount = 0;
        if (a_objSubstituent.getParentEdge() != null) {
            for (Linkage linkage : a_objSubstituent.getParentEdge().getGlycosidicLinkages()) {
                t_iLinkageCount++;
            }
        }
        for (GlycoEdge glycoEdge : a_objSubstituent.getChildEdges()) {
            for (Linkage linkage : glycoEdge.getGlycosidicLinkages()) {
                t_iLinkageCount++;
            }
        }
        SubstituentType t_objSubstType = a_objSubstituent.getSubstituentType();
        if (t_iLinkageCount < t_objSubstType.getMinValence()) {
            throw new GlycoMassException("Error with minimum linkage count of substituent " + a_objSubstituent
                    .getSubstituentType().getName() + ".");
        }
        if (t_iLinkageCount > t_objSubstType.getMinValence()) {
            t_dMass += this.handleMultipleLinkedSubstituents(0, t_iLinkageCount, a_objSubstituent);
        }
        this.m_dMass += t_dMass;
    }

    @Override
    public void visit(UnvalidatedGlycoNode unvalidated) throws GlycoVisitorException {
        throw new GlycoMassException("Mass calculation of Unvalidated residues (UnvalidatedGlycoNode) is not " +
                "supported .");
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(GlycoEdge)
     */
    public void visit(GlycoEdge linkage) throws GlycoVisitorException {
        // nothing to do
    }

    /**
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#visit(SugarUnitAlternative)
     */
    public void visit(SugarUnitAlternative alternative) throws GlycoVisitorException {
        throw new GlycoMassException("Mass calculation of alternative SugarUnits is not supported .");
    }

    @Override
    public void visit(NonMonosaccharide residue) throws GlycoVisitorException {
        throw new GlycoMassException("Mass calculation of NonMonosaccharide " + residue.getName() + " is not " +
                "supported .");
    }

    @Override
    public void visit(SugarUnitRepeat a_objRepeate) throws GlycoVisitorException {
        if (a_objRepeate.getMinRepeatCount() != a_objRepeate.getMaxRepeatCount() || a_objRepeate.getMinRepeatCount()
                == SugarUnitRepeat.UNKNOWN) {
            throw new GlycoMassException("Mass calculation of repeat units with not exactly defined repeat count is " +
                    "not possible.");
        }
        GlycoVisitorMass t_objMass = new GlycoVisitorMass();
        GlycoTraverser t_trav = this.getTraverser(t_objMass);
        t_trav.traverseGraph(a_objRepeate);
        this.m_dMass += t_objMass.getMass() * a_objRepeate.getMinRepeatCount();
        // internal repeat linkage
        double t_dLinkIn = this.specialLinkage(a_objRepeate.getRepeatLinkage(), true, a_objRepeate);
        double t_dLinkOut = this.specialLinkage(a_objRepeate.getRepeatLinkage(), false, a_objRepeate);
        this.m_dMass += (t_dLinkIn + t_dLinkOut) * (a_objRepeate.getMinRepeatCount() - 1);
        // in and out linkages
        if (a_objRepeate.getParentEdge() != null) {
            if (a_objRepeate.getParentEdge().getGlycosidicLinkages().size() != a_objRepeate.getRepeatLinkage()
                    .getGlycosidicLinkages().size()) {
                throw new GlycoMassException("Repeat in linkage and repeat linkage weight does not match.");
            }
            this.m_dMass += t_dLinkIn;
        }
        for (GlycoEdge glycoEdge : a_objRepeate.getChildEdges()) {
            if (glycoEdge.getGlycosidicLinkages().size() != a_objRepeate.getRepeatLinkage()
                    .getGlycosidicLinkages().size()) {
                throw new GlycoMassException("Repeat out linkage and repeat linkage weight does not match.");
            }
            this.m_dMass += t_dLinkOut;
        }
        // underdetermined units
        for (UnderdeterminedSubTree t_objUTree : a_objRepeate.getUndeterminedSubTrees()) {
            if (t_objUTree.getProbabilityLower() < 100) {
                throw new GlycoMassException("Mass calculation for stoichometric distribution is not possible.");
            } else {
                t_objMass = new GlycoVisitorMass();
                t_trav = this.getTraverser(t_objMass);
                t_trav.traverseGraph(t_objUTree);
                this.m_dMass += t_objMass.getMass();
                // incoming linkage
                for (Linkage t_objLinkage : t_objUTree.getConnection().getGlycosidicLinkages()) {
                    if (t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID) {
                        if (this.isHomogenSubst(t_objUTree.getParents())) {
                            this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getParents(), t_objUTree
                                    .getConnection());
                        } else {
                            throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is " +
                                    "not possible.");
                        }
                    } else {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getParentLinkageType(), this
                                .m_bMonoisotopic);
                    }
                    if (t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID) {
                        try {
                            if (this.isHomogenSubst(t_objUTree.getRootNodes())) {
                                this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getRootNodes(),
                                        t_objUTree.getConnection());
                            } else {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit" +
                                        " is not possible.");
                            }
                        } catch (GlycoconjugateException e) {
                            throw new GlycoVisitorException(e.getMessage(), e);
                        }
                    } else {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getChildLinkageType(), this
                                .m_bMonoisotopic);
                    }
                }
            }
        }
    }

    private double specialLinkage(GlycoEdge a_objRepeatEdge, boolean a_bIn, SugarUnitRepeat a_objRepeat) throws
            GlycoVisitorException {
        double t_dMass = 0;
        if (a_bIn) {
            for (Linkage t_objLinkage : a_objRepeatEdge.getGlycosidicLinkages()) {
                if (t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID) {
                    GlycoVisitorRepeatLinkType t_visStart = new GlycoVisitorRepeatLinkType();
                    a_objRepeat.accept(t_visStart);
                    if (t_visStart.getEdge() == null) {
                        throw new GlycoMassException("Mass calculation of repeat is not possible.");
                    } else {
                        if (a_objRepeatEdge.getGlycosidicLinkages().size() != t_visStart.getEdge()
                                .getGlycosidicLinkages().size()) {
                            throw new GlycoMassException("Repeat linkage and inner repeat linkage weight does not " +
                                    "match.");
                        } else {
                            if (this.isHomogenSubst(t_visStart.getStartNodes())) {
                                t_dMass += this.checkAndCalculateSubstituent(t_visStart.getStartNodes(), t_visStart
                                        .getEdge());
                            } else {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit" +
                                        " is not possible.");
                            }
                        }
                    }
                } else {
                    t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getChildLinkageType(), this
                            .m_bMonoisotopic);
                }
            }
        } else {
            for (Linkage t_objLinkage : a_objRepeatEdge.getGlycosidicLinkages()) {
                if (t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID) {
                    GlycoVisitorRepeatLinkType t_visStart = new GlycoVisitorRepeatLinkType();
                    t_visStart.setRepeatIn(false);
                    a_objRepeat.accept(t_visStart);
                    if (t_visStart.getEdge() == null) {
                        throw new GlycoMassException("Mass calculation of repeat is not possible.");
                    } else {
                        if (a_objRepeatEdge.getGlycosidicLinkages().size() != t_visStart.getEdge()
                                .getGlycosidicLinkages().size()) {
                            throw new GlycoMassException("Repeat linkage and inner repeat linkage weight does not " +
                                    "match.");
                        } else {
                            if (this.isHomogenSubst(t_visStart.getStartNodes())) {
                                t_dMass += this.checkAndCalculateSubstituent(t_visStart.getStartNodes(), t_visStart
                                        .getEdge());
                            } else {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit" +
                                        " is not possible.");
                            }
                        }
                    }
                } else {
                    t_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getParentLinkageType(), this
                            .m_bMonoisotopic);
                }
            }
        }
        return t_dMass;
    }


    /*
     * @see org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor#start(org.eurocarbdb.MolecularFramework.sugar
     * .Sugar)
     */
    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        this.clear();
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        // underdetermined units        
        for (UnderdeterminedSubTree t_objUTree : a_objSugar.getUndeterminedSubTrees()) {
            if (t_objUTree.getProbabilityLower() < 100) {
                throw new GlycoMassException("Mass calculation for stoichometric distribution is not possible.");
            } else {
                GlycoVisitorMass t_objMass = new GlycoVisitorMass();
                GlycoTraverser t_trav = this.getTraverser(t_objMass);
                t_trav.traverseGraph(t_objUTree);
                this.m_dMass += t_objMass.getMass();
                // incoming linkage
                for (Linkage t_objLinkage : t_objUTree.getConnection().getGlycosidicLinkages()) {
                    if (t_objLinkage.getParentLinkageType() == LinkageType.NONMONOSACCHARID) {
                        if (this.isHomogenSubst(t_objUTree.getParents())) {
                            this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getParents(), t_objUTree
                                    .getConnection());
                        } else {
                            throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit is " +
                                    "not possible.");
                        }
                    } else {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getParentLinkageType(), this
                                .m_bMonoisotopic);
                    }
                    if (t_objLinkage.getChildLinkageType() == LinkageType.NONMONOSACCHARID) {
                        try {
                            if (this.isHomogenSubst(t_objUTree.getRootNodes())) {
                                this.m_dMass += this.checkAndCalculateSubstituent(t_objUTree.getRootNodes(),
                                        t_objUTree.getConnection());
                            } else {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit" +
                                        " is not possible.");
                            }
                        } catch (GlycoconjugateException e) {
                            throw new GlycoVisitorException(e.getMessage(), e);
                        }
                    } else {
                        this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkage.getChildLinkageType(), this
                                .m_bMonoisotopic);
                    }
                }
            }
        }
        GlycoVisitorPerPosition t_visPositions = new GlycoVisitorPerPosition();
        t_visPositions.start(a_objSugar);
        // TODO
//        this.m_iPerAcPos = t_visPositions.getPerAcPositions();
//        this.m_iPerMePos = t_visPositions.getPerMePositions();
    }

    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        if (a_objCyclic.getParentEdge() != null) {
            for (Linkage linkage : a_objCyclic.getParentEdge().getGlycosidicLinkages()) {
                // for each linkage
                LinkageType t_objLinkageType = linkage.getChildLinkageType();
                if (t_objLinkageType == LinkageType.NONMONOSACCHARID) {
                    GlycoVisitorRepeatLinkType t_visStart = new GlycoVisitorRepeatLinkType();
                    a_objCyclic.getCyclicStart().accept(t_visStart);
                    if (t_visStart.getEdge() == null) {
                        throw new GlycoMassException("Mass calculation of cyclic start is not possible.");
                    } else {
                        if (a_objCyclic.getParentEdge().getGlycosidicLinkages().size() != t_visStart.getEdge()
                                .getGlycosidicLinkages().size()) {
                            throw new GlycoMassException("Cyclic linkage and repeat linkage weight does not match.");
                        } else {
                            if (this.isHomogenSubst(t_visStart.getStartNodes())) {
                                this.m_dMass += this.checkAndCalculateSubstituent(t_visStart.getStartNodes(),
                                        t_visStart.getEdge());
                            } else {
                                throw new GlycoMassException("Mass calculation of (heterogen) composition repeat unit" +
                                        " is not possible.");
                            }
                        }
                    }
                } else {
                    this.m_dMass += this.m_objMasses.getLinkageTypeMass(t_objLinkageType, this.m_bMonoisotopic);
                }
            }
        } else {
            throw new GlycoMassException("Mass calculation of unconnected cylcic unit is not possible.");
        }
    }

    private double checkAndCalculateSubstituent(ArrayList<GlycoNode> a_aStartNodes, GlycoEdge a_objEdge) throws
            GlycoVisitorException {
        double t_dMass = -1;
        double t_dMassTemp = -1;
        int t_iLinkageCount = 0;
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        for (GlycoNode t_objNode : a_aStartNodes) {
            if (t_objNode.getParentEdge() != null) {
                t_iLinkageCount += t_objNode.getParentEdge().getGlycosidicLinkages().size();
            }
            for (GlycoEdge glycoEdge : t_objNode.getChildEdges()) {
                t_iLinkageCount += glycoEdge.getGlycosidicLinkages().size();

            }
            t_dMassTemp = this.handleMultipleLinkedSubstituents(t_iLinkageCount, a_objEdge.getGlycosidicLinkages()
                    .size(), t_visType.getSubstituent(t_objNode));
            if (t_dMass == -1) {
                t_dMass = t_dMassTemp;
            } else {
                if (t_dMass != t_dMassTemp) {
                    throw new GlycoMassException("Mass calculation of heterogen repeat substituents is not possible.");
                }
            }
        }
        return t_dMass;
    }

    private boolean isHomogenSubst(ArrayList<GlycoNode> startNodes) throws GlycoVisitorException {
        boolean t_bOther = false;
        GlycoVisitorNodeType t_visType = new GlycoVisitorNodeType();
        for (GlycoNode startNode : startNodes) {
            if (!t_visType.isSubstituent(startNode)) {
                t_bOther = true;
            }
        }
        return !t_bOther;
    }

    private double handleMultipleLinkedSubstituents(int t_iLinkageCalculated, int t_iLinkageCountNew, Substituent
            a_objSubstituent) throws GlycoMassException {
        SubstituentType t_objSubstType = a_objSubstituent.getSubstituentType();
        if (t_objSubstType == SubstituentType.PHOSPHATE || t_objSubstType == SubstituentType.SULFATE ||
                t_objSubstType == SubstituentType.N_SULFATE) {
            double t_dIncMass = 0;
            if (this.m_bMonoisotopic) {
                t_dIncMass = 17.0027396541;
            } else {
                t_dIncMass = 17.00734568218410;
            }
            if ((t_iLinkageCountNew + t_iLinkageCalculated) > t_objSubstType.getMaxValence()) {
                throw new GlycoMassException("Error with max. linkage count) of substituent " + a_objSubstituent
                        .getSubstituentType().getName() + ".");
            }
            return (1 - (t_iLinkageCalculated + t_iLinkageCountNew)) * t_dIncMass;
        }
        throw new GlycoMassException("Error with linkage count of substituent " + a_objSubstituent.getSubstituentType
                ().getName() + ".");
    }
}
