package org.eurocarbdb.MolecularFramework.io.GlycoCT;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.jdom.*;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.eurocarbdb.MolecularFramework.io.SugarExporterException;
import org.eurocarbdb.MolecularFramework.sugar.BaseType;
import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraphAlternative;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.Linkage;
import org.eurocarbdb.MolecularFramework.sugar.Modification;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnderdeterminedSubTree;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * @author Logan
 */
public class SugarExporterGlycoCT implements GlycoVisitor {

    private Document m_objDocument;
    private Element m_objRootElement;
    private Element m_objResidues;
    private Element m_objLinkages;
    private Integer m_iResCounter;
    private Integer m_iLinkageCounter;
    private Integer m_iEdgeCounter;
    private ArrayList<SugarUnitRepeat> m_aRepeats = new ArrayList<>();
    private ArrayList<NonMonosaccharide> m_aNonMS = new ArrayList<>();
    private ArrayList<UnderdeterminedSubTree> m_aSpezialTrees = new ArrayList<>();
    private HashMap<GlycoNode, Integer> m_hashResidueID = new HashMap<>();
    private HashMap<GlycoEdge, Integer> m_hashEdgeID = new HashMap<>();
    private HashMap<Linkage, Integer> m_hashLinkageID = new HashMap<>();
    private ArrayList<SugarUnitAlternative> m_aAlternative = new ArrayList<>();

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        return new GlycoCTTraverser(a_objVisitor);
    }

    @Override
    public void clear() {
        this.m_objDocument = null;
        this.m_objRootElement = null;
        this.m_objResidues = null;
        this.m_objLinkages = null;
        this.m_iResCounter = 1;
        this.m_iLinkageCounter = 1;
        this.m_iEdgeCounter = 1;
        this.m_aRepeats.clear();
        this.m_aSpezialTrees.clear();
        this.m_hashEdgeID.clear();
        this.m_hashResidueID.clear();
        this.m_aNonMS.clear();
        this.m_hashLinkageID.clear();
        this.m_aAlternative.clear();
    }

    @Override
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        // <res id="1" type="b" anomer="x" superclass="hex" ringStart="-1" ringEnd="-1" />
        Element t_objResidue = new Element("basetype");
        t_objResidue.setAttribute("id", this.m_iResCounter.toString());
        this.m_hashResidueID.put(a_objMonosaccharid, this.m_iResCounter);
        this.m_iResCounter++;
        t_objResidue.setAttribute("anomer", a_objMonosaccharid.getAnomer().getSymbol());
        t_objResidue.setAttribute("superclass", a_objMonosaccharid.getSuperclass().getName());
        t_objResidue.setAttribute("ringStart", String.valueOf(a_objMonosaccharid.getRingStart()));
        t_objResidue.setAttribute("ringEnd", String.valueOf(a_objMonosaccharid.getRingEnd()));
        t_objResidue.setAttribute("name", a_objMonosaccharid.getGlycoCTName());
        // basetypes
        ArrayList<BaseType> t_aBaseType = a_objMonosaccharid.getBaseType();
        int t_iMax = t_aBaseType.size();
        BaseType t_objBaseType;
        Element t_objSub;
        for (int t_iCounter = 0; t_iCounter < t_iMax; t_iCounter++) {
            t_objBaseType = t_aBaseType.get(t_iCounter);
            t_objSub = new Element("stemtype");
            t_objSub.setAttribute("id", String.valueOf(t_iCounter + 1));
            t_objSub.setAttribute("type", t_objBaseType.getName());
            t_objResidue.addContent(t_objSub);
        }
        //modifications
        for (Modification t_objModi : a_objMonosaccharid.getModification()) {

            t_objSub = new Element("modification");
            t_objSub.setAttribute("type", t_objModi.getName());
            t_objSub.setAttribute("pos_one", String.valueOf(t_objModi.getPositionOne()));
            if (t_objModi.hasPositionTwo()) {
                t_objSub.setAttribute("pos_two", String.valueOf(t_objModi.getPositionTwo()));
            }
            t_objResidue.addContent(t_objSub);
        }
        this.m_objResidues.addContent(t_objResidue);
        GlycoEdge t_objEdge = a_objMonosaccharid.getParentEdge();
        if (t_objEdge != null) {
            this.writeEdge(t_objEdge,
                    this.m_hashResidueID.get(t_objEdge.getParent()),
                    this.m_hashResidueID.get(t_objEdge.getChild()));
        }
    }

    @Override
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        Element t_objResidue = new Element("substituent");
        t_objResidue.setAttribute("id", this.m_iResCounter.toString());
        this.m_hashResidueID.put(a_objSubstituent, this.m_iResCounter);
        this.m_iResCounter++;
        t_objResidue.setAttribute("name", a_objSubstituent.getSubstituentType().getName());
        this.m_objResidues.addContent(t_objResidue);
        GlycoEdge t_objEdge = a_objSubstituent.getParentEdge();
        if (t_objEdge != null) {
            this.writeEdge(t_objEdge,
                    this.m_hashResidueID.get(t_objEdge.getParent()),
                    this.m_hashResidueID.get(t_objEdge.getChild()));
        }
    }

    @Override
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        GlycoEdge t_objEdge = a_objCyclic.getParentEdge();
        if (t_objEdge != null) {
            this.writeEdge(t_objEdge,
                    this.m_hashResidueID.get(t_objEdge.getParent()),
                    this.m_hashResidueID.get(a_objCyclic.getCyclicStart()));
        }
    }

    @Override
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        Element t_objResidue = new Element("alternative");
        t_objResidue.setAttribute("id", this.m_iResCounter.toString());
        this.m_hashResidueID.put(a_objAlternative, this.m_iResCounter);
        this.m_iResCounter++;
        t_objResidue.setAttribute("alternativeId", String.valueOf(this.m_aAlternative.size() + 1));
        this.m_objResidues.addContent(t_objResidue);
        GlycoEdge t_objEdge = a_objAlternative.getParentEdge();
        if (t_objEdge != null) {
            this.writeEdge(t_objEdge,
                    this.m_hashResidueID.get(t_objEdge.getParent()),
                    this.m_hashResidueID.get(t_objEdge.getChild()));
        }
        this.m_aAlternative.add(a_objAlternative);
    }

    @Override
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {
        this.m_aNonMS.add(a_objResidue);
    }

    @Override
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
        Element t_objResidue = new Element("repeat");
        t_objResidue.setAttribute("id", this.m_iResCounter.toString());
        this.m_hashResidueID.put(a_objRepeat, this.m_iResCounter);
        this.m_iResCounter++;
        t_objResidue.setAttribute("repeatId", String.valueOf(this.m_aRepeats.size() + 1));
        this.m_objResidues.addContent(t_objResidue);
        GlycoEdge t_objEdge = a_objRepeat.getParentEdge();
        if (t_objEdge != null) {
            this.writeEdge(t_objEdge,
                    this.m_hashResidueID.get(t_objEdge.getParent()),
                    this.m_hashResidueID.get(t_objEdge.getChild()));
        }
        this.m_aRepeats.add(a_objRepeat);
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objRepeat.getUndeterminedSubTrees()) {
            this.m_aSpezialTrees.add(underdeterminedSubTree);
        }
    }

    @Override
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // nothing to do
    }

    @Override
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        throw new GlycoVisitorException("UnvalidatedGlycoNode are not allowed for GlycoCT.");
    }

    /**
     * Adds the complete sugar information below the given element
     *
     * @param a_objSugar
     * @param t_objRootElement
     * @throws GlycoVisitorException
     */
    public void start(Sugar a_objSugar, Element a_objRootElement) throws GlycoVisitorException {
        this.clear();
        this.m_objRootElement = new Element("sugar");
        this.m_objRootElement.setAttribute("version", "1.0");
        this.m_objResidues = new Element("residues");
        this.m_objRootElement.addContent(this.m_objResidues);
        this.m_objLinkages = new Element("linkages");
        this.m_objRootElement.addContent(this.m_objLinkages);
        a_objRootElement.addContent(this.m_objRootElement);
        this.export(a_objSugar);
    }

    @Override
    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        this.clear();
        this.m_objRootElement = new Element("sugar");
        this.m_objRootElement.setAttribute("version", "1.0");
        this.m_objDocument = new Document(this.m_objRootElement);
        this.m_objResidues = new Element("residues");
        this.m_objRootElement.addContent(this.m_objResidues);
        this.m_objLinkages = new Element("linkages");
        this.m_objRootElement.addContent(this.m_objLinkages);
        this.export(a_objSugar);
    }

    private void export(Sugar a_objSugar) throws GlycoVisitorException {
        // save subtrees
        for (UnderdeterminedSubTree underdeterminedSubTree : a_objSugar.getUndeterminedSubTrees()) {
            this.m_aSpezialTrees.add(underdeterminedSubTree);
        }
        GlycoTraverser t_objTraverser = this.getTraverser(this);
        t_objTraverser.traverseGraph(a_objSugar);
        // repeats
        if (this.m_aRepeats.size() > 0) {
            Element t_objRepeatElement = new Element("repeat");
            this.m_objRootElement.addContent(t_objRepeatElement);
            int t_iCounter = 0;
            Element t_objUnit;
            while (t_iCounter < this.m_aRepeats.size()) {
                SugarUnitRepeat t_objRepeatUnit = this.m_aRepeats.get(t_iCounter);
                t_objUnit = new Element("unit");
                t_objRepeatElement.addContent(t_objUnit);
                t_iCounter++;
                t_objUnit.setAttribute("id", String.valueOf(t_iCounter));
                t_objUnit.setAttribute("minOccur", String.valueOf(t_objRepeatUnit.getMinRepeatCount()));
                t_objUnit.setAttribute("maxOccur", String.valueOf(t_objRepeatUnit.getMaxRepeatCount()));
                // travers tree
                this.m_objResidues = new Element("residues");
                t_objUnit.addContent(this.m_objResidues);
                this.m_objLinkages = new Element("linkages");
                t_objUnit.addContent(this.m_objLinkages);
                t_objTraverser = this.getTraverser(this);
                t_objTraverser.traverseGraph(t_objRepeatUnit);
                // internal linkage				
                GlycoEdge t_objEdge = t_objRepeatUnit.getRepeatLinkage();
                if (t_objEdge != null) {
                    Element t_objInternal = new Element("internalLinkage");
                    t_objUnit.addContent(t_objInternal);
                    Integer t_iParentID = this.m_hashResidueID.get(t_objEdge.getParent());
                    Integer t_iChildID = this.m_hashResidueID.get(t_objEdge.getChild());
                    if (t_iParentID == null || t_iChildID == null) {
                        throw new GlycoVisitorException("Critical error in repeat. Parent or child resiude missing.");
                    }
                    t_objInternal.setAttribute("parent", t_iParentID.toString());
                    t_objInternal.setAttribute("child", t_iChildID.toString());
                    Element t_objCon;
                    Element t_objFrom;
                    Element t_objTo;
                    if (t_objEdge.getGlycosidicLinkages().size() == 0) {
                        throw new GlycoVisitorException("An repeating edge without a linkage is not valid.");
                    }
                    ArrayList<Linkage> t_oLinkages = t_objEdge.getGlycosidicLinkages();
                    GlycoCTLinkageComparator t_linComp = new GlycoCTLinkageComparator();
                    Collections.sort(t_oLinkages, t_linComp);
                    for (Linkage t_objLinkage : t_oLinkages) {
                        t_objCon = new Element("linkage");
                        t_objCon.setAttribute("id", this.m_iLinkageCounter.toString());
                        this.m_iLinkageCounter++;
                        t_objCon.setAttribute("parentType", String.valueOf(t_objLinkage.getParentLinkageType()
                                .getType()));
                        t_objCon.setAttribute("childType", String.valueOf(t_objLinkage.getChildLinkageType().getType
                                ()));
                        ArrayList<Integer> t_aPositions = t_objLinkage.getParentLinkages();
                        Collections.sort(t_aPositions);
                        for (Integer t_aPosition : t_aPositions) {
                            t_objFrom = new Element("parent");
                            t_objFrom.setAttribute("pos", t_aPosition.toString());
                            t_objCon.addContent(t_objFrom);
                        }
                        t_aPositions = t_objLinkage.getChildLinkages();
                        Collections.sort(t_aPositions);
                        for (Integer t_aPosition : t_aPositions) {
                            t_objTo = new Element("child");
                            t_objTo.setAttribute("pos", t_aPosition.toString());
                            t_objCon.addContent(t_objTo);
                        }
                        t_objInternal.addContent(t_objCon);
                    }
                }
            }
        }
        // spezial trees
        if (this.m_aSpezialTrees.size() != 0) {
            GlycoCTUnderdeterminedSubtreeComparator t_oSubTreeComp = new GlycoCTUnderdeterminedSubtreeComparator();
            Collections.sort(this.m_aSpezialTrees, t_oSubTreeComp);
            Integer t_iCounter = 0;
            Element t_objUnder = new Element("underDeterminedSubtrees");
            this.m_objRootElement.addContent(t_objUnder);
            for (UnderdeterminedSubTree t_objSubtree : this.m_aSpezialTrees) {
                Element t_objTree = new Element("tree");
                t_objUnder.addContent(t_objTree);
                t_iCounter++;
                t_objTree.setAttribute("id", t_iCounter.toString());
                t_objTree.setAttribute("probLow", String.valueOf(t_objSubtree.getProbabilityLower()));
                t_objTree.setAttribute("probUp", String.valueOf(t_objSubtree.getProbabilityUpper()));
                // subtree
                this.m_objResidues = new Element("residues");
                t_objTree.addContent(this.m_objResidues);
                this.m_objLinkages = new Element("linkages");
                t_objTree.addContent(this.m_objLinkages);
                t_objTraverser = this.getTraverser(this);
                t_objTraverser.traverseGraph(t_objSubtree);
                // parents
                Element t_objParents = new Element("parents");
                t_objTree.addContent(t_objParents);
                ArrayList<GlycoNode> t_aNodes = t_objSubtree.getParents();

                ArrayList<Integer> t_aParentNodes = new ArrayList<Integer>();
                for (GlycoNode t_objNode : t_aNodes) {
                    Integer t_iParent = this.m_hashResidueID.get(t_objNode);
                    if (t_iParent == null) {
                        throw new GlycoVisitorException("Cricital error: parent residue for subtree not declareted.");
                    }
                    t_aParentNodes.add(t_iParent);
                }
                Collections.sort(t_aParentNodes);
                for (Integer t_iParent : t_aParentNodes) {
                    Element t_objParent = new Element("parent");
                    t_objParents.addContent(t_objParent);
                    t_objParent.setAttribute("res_id", t_iParent.toString());
                }

                // spezial linkage				
                GlycoEdge t_objEdge = t_objSubtree.getConnection();
                if (t_objEdge == null) {
                    if (t_aNodes.size() != 0) {
                        throw new GlycoVisitorException("Subtree linkage is missing.");
                    }
                } else {
                    Element t_objConnection = new Element("connection");
                    t_objTree.addContent(t_objConnection);
                    Element t_objLin;
                    Element t_objFrom;
                    Element t_objTo;
                    if (t_objEdge.getGlycosidicLinkages().size() == 0) {
                        throw new GlycoVisitorException("A subtree connection without a linkage object is not valid.");
                    }
                    ArrayList<Linkage> t_aLinkages = t_objEdge.getGlycosidicLinkages();
                    GlycoCTLinkageComparator t_oLinComp = new GlycoCTLinkageComparator();
                    Collections.sort(t_aLinkages, t_oLinComp);
                    for (Linkage t_objLinkage : t_aLinkages) {
                        t_objLin = new Element("linkage");
                        t_objLin.setAttribute("id", this.m_iLinkageCounter.toString());
                        this.m_iLinkageCounter++;
                        t_objLin.setAttribute("parentType", String.valueOf(t_objLinkage.getParentLinkageType()
                                .getType()));
                        t_objLin.setAttribute("childType", String.valueOf(t_objLinkage.getChildLinkageType().getType
                                ()));
                        ArrayList<Integer> t_aPositions = t_objLinkage.getParentLinkages();
                        Collections.sort(t_aPositions);
                        for (Integer t_aPosition : t_aPositions) {
                            t_objFrom = new Element("parent");
                            t_objFrom.setAttribute("pos", t_aPosition.toString());
                            t_objLin.addContent(t_objFrom);
                        }
                        t_aPositions = t_objLinkage.getChildLinkages();
                        Collections.sort(t_aPositions);
                        for (Integer t_aPosition : t_aPositions) {
                            t_objTo = new Element("child");
                            t_objTo.setAttribute("pos", t_aPosition.toString());
                            t_objLin.addContent(t_objTo);
                        }
                        t_objConnection.addContent(t_objLin);
                    }
                }
            }
        }
        // alternatives
        if (this.m_aAlternative.size() > 0) {
            Element t_objAlternativeElement = new Element("alternative");
            this.m_objRootElement.addContent(t_objAlternativeElement);
            int t_iCounter = 0;
            Element t_objUnit;
            Element t_objTree;
            Element t_objConnection;
            Integer t_iID;
            while (t_iCounter < this.m_aAlternative.size()) {
                SugarUnitAlternative t_objAlternative = this.m_aAlternative.get(t_iCounter);
                t_objUnit = new Element("unit");
                t_objAlternativeElement.addContent(t_objUnit);
                t_iCounter++;
                t_objUnit.setAttribute("id", String.valueOf(t_iCounter));
                // subtrees
                ArrayList<GlycoGraphAlternative> t_aSubtrees = t_objAlternative.getAlternatives();
                GlycoCTGraphAlternativeComparator t_comp = new GlycoCTGraphAlternativeComparator();
                Collections.sort(t_aSubtrees, t_comp);
                for (GlycoGraphAlternative t_objAltGraph : t_aSubtrees) {
                    t_objTree = new Element("substructure");
                    // travers tree
                    this.m_objResidues = new Element("residues");
                    t_objTree.addContent(this.m_objResidues);
                    this.m_objLinkages = new Element("linkages");
                    t_objTree.addContent(this.m_objLinkages);
                    t_objTraverser = this.getTraverser(this);
                    t_objTraverser.traverseGraph(t_objAltGraph);
                    // parent
                    if (t_objAltGraph.getLeadInNode() != null) {
                        t_objConnection = new Element("lead_in");
                        t_iID = this.m_hashResidueID.get(t_objAltGraph.getLeadInNode());
                        if (t_iID == null) {
                            throw new GlycoVisitorException("Cricital error: parent residue for alternative subtree " +
                                    "was not declareted.");
                        }
                        t_objConnection.setAttribute("residue_id", t_iID.toString());
                        t_objTree.addContent(t_objConnection);
                    }
                    // childs
                    if (t_objAltGraph.getLeadOutNodeToNode().size() != 0) {
                        HashMap<GlycoNode, GlycoNode> t_hMapping = t_objAltGraph.getLeadOutNodeToNode();
                        ArrayList<GlycoNode> t_aTempNodes = new ArrayList<GlycoNode>();
                        for (GlycoNode t_node : t_hMapping.keySet()) {
                            t_aTempNodes.add(t_node);
                        }
                        GlycoCTGlycoNodeComparator t_oNodeComparator = new GlycoCTGlycoNodeComparator();
                        Collections.sort(t_aTempNodes, t_oNodeComparator);
                        for (GlycoNode t_aTempNode : t_aTempNodes) {
                            t_objConnection = new Element("lead_out");
                            GlycoNode t_objNodeTo = t_aTempNode;
                            GlycoNode t_objNodeFrom = t_hMapping.get(t_objNodeTo);
                            t_iID = this.m_hashResidueID.get(t_objNodeFrom);
                            if (t_iID == null) {
                                throw new GlycoVisitorException("Cricital error: child residue for alternative " +
                                        "subtree was not declareted.");
                            }
                            t_objConnection.setAttribute("residue_id", t_iID.toString());
                            t_iID = this.m_hashResidueID.get(t_objNodeTo);
                            if (t_iID == null) {
                                throw new GlycoVisitorException("Cricital error: child residue attache point for " +
                                        "alternative subtree was not declareted.");
                            }
                            t_objConnection.setAttribute("connected_to", t_iID.toString());
                            t_objTree.addContent(t_objConnection);
                        }
                    }
                    t_objUnit.addContent(t_objTree);
                }
            }
        }
        // Non MS
        if (this.m_aNonMS.size() > 0) {
            Element t_objElement = new Element("aglyca");
            this.m_objRootElement.addContent(t_objElement);
            Element t_objHistorical = new Element("historicalData");
            t_objElement.addContent(t_objHistorical);
            int t_iCounter = 0;
            while (t_iCounter < this.m_aNonMS.size()) {
                NonMonosaccharide t_objNonMS = this.m_aNonMS.get(t_iCounter);
                this.writeNonMs(t_objNonMS, t_objHistorical, t_iCounter);
                t_iCounter++;
            }
        }
    }

    private void writeNonMs(NonMonosaccharide a_objHistorical, Element a_objHistoricalElement, Integer a_iID) throws
            GlycoVisitorException {
//      <historicalData>
//      <entry id="0" name="something" fromResidue="0">
//      <lin linkageType="o">
//      <from pos="2">
//      <to pos="1">
//      </lin>
//      ...
//      </agl>
//      ...
//      </historicalData>
        Element t_objAGL = new Element("entry");
        a_objHistoricalElement.addContent(t_objAGL);
        t_objAGL.setAttribute("id", a_iID.toString());
        t_objAGL.setAttribute("name", a_objHistorical.getName());
        GlycoEdge t_objEdge;
        Element t_objLin;
        Element t_objFrom;
        Element t_objTo;
        if (a_objHistorical.getParentEdge() != null) {
            if (a_objHistorical.getChildEdges().size() != 0) {
                throw new GlycoVisitorException("NonMonosaccharide can not have child AND parent edges.");
            }
            // at the non resducing end
            t_objEdge = a_objHistorical.getParentEdge();
            Integer t_iID = this.m_hashResidueID.get(t_objEdge.getParent());
            if (t_iID == null) {
                throw new GlycoVisitorException("Attache residue of aglyca " + a_objHistorical.getName() + " not " +
                        "valid.");
            }
            t_objAGL.setAttribute("fromResidue", t_iID.toString());
            if (t_objEdge.getGlycosidicLinkages().size() == 0) {
                throw new GlycoVisitorException("An aglyca edge without an linkage object is not valid.");
            }
            for (Linkage t_objLinkage : t_objEdge.getGlycosidicLinkages()) {
                t_objLin = new Element("linkage");
                t_objLin.setAttribute("parentType", String.valueOf(t_objLinkage.getParentLinkageType().getType()));
                t_objLin.setAttribute("childType", String.valueOf(t_objLinkage.getChildLinkageType().getType()));
                ArrayList<Integer> t_aPositions = t_objLinkage.getParentLinkages();
                Collections.sort(t_aPositions);
                for (Integer t_aPosition : t_aPositions) {
                    t_objFrom = new Element("parent");
                    t_objFrom.setAttribute("pos", t_aPosition.toString());
                    t_objLin.addContent(t_objFrom);
                }
                t_aPositions = t_objLinkage.getChildLinkages();
                Collections.sort(t_aPositions);
                for (Integer t_aPosition : t_aPositions) {
                    t_objTo = new Element("child");
                    t_objTo.setAttribute("pos", t_aPosition.toString());
                    t_objLin.addContent(t_objTo);
                }
                t_objAGL.addContent(t_objLin);
            }
        } else {
            if (a_objHistorical.getChildEdges().size() == 0) {
                throw new GlycoVisitorException("Unconnected aglyca are forbidden.");
            } else if (a_objHistorical.getChildEdges().size() == 1) {
                t_objEdge = a_objHistorical.getChildEdges().get(0);
                Integer t_iID = this.m_hashResidueID.get(t_objEdge.getChild());
                if (t_iID == null) {
                    throw new GlycoVisitorException("Attache residue of aglyca " + a_objHistorical.getName() + " not " +
                            "valid.");
                }
                t_objAGL.setAttribute("toResidue", t_iID.toString());
                for (Linkage t_objLinkage : t_objEdge.getGlycosidicLinkages()) {
                    t_objLin = new Element("linkage");
                    t_objLin.setAttribute("parentType", String.valueOf(t_objLinkage.getParentLinkageType().getType()));
                    t_objLin.setAttribute("childType", String.valueOf(t_objLinkage.getChildLinkageType().getType()));
                    for (Integer integer : t_objLinkage.getParentLinkages()) {
                        t_objFrom = new Element("parent");
                        t_objFrom.setAttribute("pos", integer.toString());
                        t_objLin.addContent(t_objFrom);
                    }
                    for (Integer integer : t_objLinkage.getChildLinkages()) {
                        t_objTo = new Element("child");
                        t_objTo.setAttribute("pos", integer.toString());
                        t_objLin.addContent(t_objTo);
                    }
                    t_objAGL.addContent(t_objLin);
                }
            } else {
                throw new GlycoVisitorException("NonMonosaccharide can not have more then one child edges.");
            }
        }
    }

    public String getXMLCode() throws IOException {
        Format t_objFormat = Format.getPrettyFormat();
        XMLOutputter t_objExportXML = new XMLOutputter(t_objFormat);
        StringWriter t_objWriter = new StringWriter();
        t_objExportXML.output(this.m_objDocument, t_objWriter);
        return t_objWriter.toString();
    }

    public Document getDocument() {
        return this.m_objDocument;
    }

    public HashMap<GlycoNode, Integer> getNodeIdMap() {
        return this.m_hashResidueID;
    }

    public HashMap<GlycoEdge, Integer> getEdgeIdMap() {
        return this.m_hashEdgeID;
    }

    /**
     * @param edge
     * @param integer
     * @param integer2
     * @throws SugarExporterException
     */
    private void writeEdge(GlycoEdge a_objEdge, Integer a_iParentID, Integer a_iChildID) throws GlycoVisitorException {
        if (a_iParentID == null || a_iChildID == null) {
            // connection to aglyca
            return;
        }
        Element t_objLinkageElement = new Element("connection");
        t_objLinkageElement.setAttribute("id", this.m_iEdgeCounter.toString());
        this.m_iEdgeCounter++;
        t_objLinkageElement.setAttribute("parent", a_iParentID.toString());
        t_objLinkageElement.setAttribute("child", a_iChildID.toString());
        Element t_objCon;
        Element t_objFrom;
        Element t_objTo;
        if (a_objEdge.getGlycosidicLinkages().size() == 0) {
            throw new GlycoVisitorException("An edge without an linkage object is not valid.");
        }
        ArrayList<Linkage> t_aLinkages = a_objEdge.getGlycosidicLinkages();
        GlycoCTLinkageComparator t_linComp = new GlycoCTLinkageComparator();
        Collections.sort(t_aLinkages, t_linComp);

        for (Linkage t_objLinkage : t_aLinkages) {
            t_objCon = new Element("linkage");
            t_objCon.setAttribute("id", this.m_iLinkageCounter.toString());
            this.m_iLinkageCounter++;
            t_objCon.setAttribute("parentType", String.valueOf(t_objLinkage.getParentLinkageType().getType()));
            t_objCon.setAttribute("childType", String.valueOf(t_objLinkage.getChildLinkageType().getType()));
            ArrayList<Integer> t_aPositions = t_objLinkage.getParentLinkages();
            Collections.sort(t_aPositions);
            for (Integer t_aPosition : t_aPositions) {
                t_objFrom = new Element("parent");
                t_objFrom.setAttribute("pos", t_aPosition.toString());
                t_objCon.addContent(t_objFrom);
            }
            t_aPositions = t_objLinkage.getChildLinkages();
            Collections.sort(t_aPositions);
            for (Integer t_aPosition : t_aPositions) {
                t_objTo = new Element("child");
                t_objTo.setAttribute("pos", t_aPosition.toString());
                t_objCon.addContent(t_objTo);
            }
            t_objLinkageElement.addContent(t_objCon);
        }
        this.m_objLinkages.addContent(t_objLinkageElement);
    }
}
