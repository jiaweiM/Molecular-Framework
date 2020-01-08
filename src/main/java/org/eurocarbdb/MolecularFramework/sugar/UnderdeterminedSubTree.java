package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Can store only simple GlycoNodes (NonMS, MS, Unvalidated, Subst)
 *
 * @author rene
 * @author JiaweiMao
 * @version 1.0.0
 * @date 2016.12.01, 11:00 PM
 */
public class UnderdeterminedSubTree implements GlycoGraph {
    private ArrayList<GlycoNode> m_aParents = new ArrayList<GlycoNode>();
    private GlycoEdge m_objConnection = null;
    private ArrayList<GlycoNode> m_aResidues = new ArrayList<GlycoNode>();
    public static final double UNKNOWN = -1;

    private double m_dAufenthaltswahrscheinlichkeitUpper = 100;
    private double m_dAufenthaltswahrscheinlichkeitLower = 100;

    /**
     * Delivers all Residues that does not have a parent residue.
     *
     * @throws GlycoconjugateException if the structure contain a cyclic part or if the sugar contain no residue with a
     *                                 parent
     * @see GlycoGraph#getRootNodes()
     */
    public ArrayList<GlycoNode> getRootNodes() throws GlycoconjugateException {
        ArrayList<GlycoNode> t_aResult = new ArrayList<GlycoNode>();
        GlycoNode t_objResidue;
        // for all residues of the sugar
        Iterator<GlycoNode> t_iterResidue = this.getNodeIterator();
        while (t_iterResidue.hasNext()) {
            t_objResidue = t_iterResidue.next();
            GlycoEdge t_objParents = t_objResidue.getParentEdge();
            if (t_objParents == null) {
                t_aResult.add(t_objResidue);
            }
        }
        if (t_aResult.size() < 1) {
            throw new GlycoconjugateException("Sugar seems not to have at least one root residue");
        }
        return t_aResult;
    }

    /**
     * @see GlycoGraph#getNodeIterator()
     */
    public Iterator<GlycoNode> getNodeIterator() {
        return this.m_aResidues.iterator();
    }

    /**
     * @see GlycoGraph#isConnected()
     */
    public boolean isConnected() throws GlycoconjugateException {
        ArrayList<GlycoNode> t_objRoots = this.getRootNodes();
        if (t_objRoots.size() > 1) {
            return false;
        }
        return true;
    }

    public boolean removeNode(GlycoNode a_objResidue) throws GlycoconjugateException {
        GlycoEdge t_objLinkage;
        GlycoNode t_objResidue;
        if (a_objResidue == null) {
            throw new GlycoconjugateException("Invalide residue.");
        }
        if (a_objResidue.getClass() != SugarUnitCyclic.class) {
            this.searchCyclicForDeleting(a_objResidue);
        }
        t_objLinkage = a_objResidue.getParentEdge();
        if (t_objLinkage != null) {
            t_objResidue = t_objLinkage.getParent();
            if (t_objResidue == null) {
                throw new GlycoconjugateException("A linkage with a null parent exists.");
            }
            t_objResidue.removeChildEdge(t_objLinkage);
        }
        for (GlycoEdge glycoEdge : a_objResidue.getChildEdges()) {
            t_objLinkage = glycoEdge;
            t_objResidue = t_objLinkage.getChild();
            if (t_objResidue == null) {
                throw new GlycoconjugateException("A linkage with a null child exists.");
            }
            t_objResidue.removeParentEdge(t_objLinkage);
        }
        return this.m_aResidues.remove(a_objResidue);
    }

    public ArrayList<GlycoNode> getNodes() {
        return this.m_aResidues;
    }

    /**
     * @see GlycoGraph#addNode(GlycoNode)
     */
    public boolean addNode(GlycoNode a_objResidue) throws GlycoconjugateException {
        if (a_objResidue == null) {
            throw new GlycoconjugateException("Invalide residue.");
        }
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
        if (!this.m_aResidues.contains(a_objResidue)) {
            try {
                if (t_objNodeType.isSugarUnitCyclic(a_objResidue)) {
                    throw new GlycoconjugateException("Cyclic unit are not allowed in underdetermined subtrees.");
                }
                if (t_objNodeType.isSugarUnitRepeat(a_objResidue)) {
                    throw new GlycoconjugateException("repeat unit are not allowed in underdetermined subtrees.");
                }
                if (t_objNodeType.isSugarUnitAlternative(a_objResidue)) {
                    throw new GlycoconjugateException("Alternative units are not allowed in underdetermined subtrees.");
                }
            } catch (GlycoVisitorException e) {
                throw new GlycoconjugateException(e.getMessage(), e);
            }
            a_objResidue.removeAllEdges();
            return this.m_aResidues.add(a_objResidue);
        }
        return false;
    }

    /**
     * @see GlycoGraph#addNode(GlycoNode, GlycoEdge, GlycoNode)
     */
    public boolean addNode(GlycoNode a_objParent, GlycoEdge a_objLinkage, GlycoNode a_objChild) throws
            GlycoconjugateException {
        if (a_objParent == null || a_objChild == null) {
            throw new GlycoconjugateException("Invalide residue.");
        }
        if (a_objLinkage == null) {
            throw new GlycoconjugateException("Invalide linkage.");
        }
        if (a_objChild.getParentEdge() != null) {
            throw new GlycoconjugateException("The child residue has a parent residue.");
        }
        this.addNode(a_objChild);
        this.addNode(a_objParent);
        if (!this.m_aResidues.contains(a_objChild) || !this.m_aResidues.contains(a_objParent)) {
            throw new GlycoconjugateException("Could not add residue to undetermined subtree.");
        }
        // test for indirect cyclic structures
        if (this.isParent(a_objChild, a_objParent)) {
            throw new GlycoconjugateException("You try to create a cyclic sugar, which are not allowed in " +
                    "underdeterminded trees.");
        }
        a_objChild.setParentEdge(a_objLinkage);
        a_objParent.addChildEdge(a_objLinkage);
        a_objLinkage.setChild(a_objChild);
        a_objLinkage.setParent(a_objParent);
        return true;
    }

    /**
     * @see GlycoGraph#addEdge(GlycoNode, GlycoNode, GlycoEdge)
     */
    public boolean addEdge(GlycoNode a_objParent, GlycoNode a_objChild, GlycoEdge a_objLinkage) throws
            GlycoconjugateException {
        return this.addNode(a_objParent, a_objLinkage, a_objChild);
    }

    /**
     * @see GlycoGraph#containsNode(GlycoNode)
     */
    public boolean containsNode(GlycoNode a_objNode) {
        return this.m_aResidues.contains(a_objNode);
    }

    private void searchCyclicForDeleting(GlycoNode a_objResidue) throws GlycoconjugateException {
        for (GlycoNode t_objElement : this.m_aResidues) {
            if (t_objElement.getClass() == SugarUnitCyclic.class) {
                SugarUnitCyclic t_objCyclic = (SugarUnitCyclic) t_objElement;
                if (t_objCyclic.getCyclicStart() == a_objResidue) {
                    this.removeNode(t_objElement);
                }
            }
        }
    }

    @Override
    public boolean isParent(GlycoNode a_objParent, GlycoNode a_objNode) {
        GlycoNode t_objParent = a_objNode.getParentNode();
        if (t_objParent == null) {
            return false;
        }
        if (t_objParent == a_objParent) {
            return true;
        }
        return this.isParent(a_objParent, t_objParent);
    }

    @Override
    public boolean removeEdge(GlycoEdge a_objEdge) throws GlycoconjugateException {
        if (a_objEdge == null) {
            return false;
        }
        GlycoNode t_objChild = a_objEdge.getChild();
        GlycoNode t_objParent = a_objEdge.getParent();
        if (t_objChild == null || t_objParent == null) {
            throw new GlycoconjugateException("The edge contains null values.");
        }
        if (t_objChild.getParentEdge() != a_objEdge) {
            throw new GlycoconjugateException("The child attachment is not correct");
        }
        ArrayList<GlycoEdge> t_aEdges = t_objParent.getChildEdges();
        if (!t_aEdges.contains(a_objEdge)) {
            throw new GlycoconjugateException("The parent attachment is not correct");
        }
        t_objChild.removeParentEdge(a_objEdge);
        t_objParent.removeChildEdge(a_objEdge);
        return true;
    }

    public void setConnection(GlycoEdge a_objEdge) {
        a_objEdge.setParent(null);
        a_objEdge.setChild(null);
        this.m_objConnection = a_objEdge;
    }

    public GlycoEdge getConnection() {
        return this.m_objConnection;
    }

    public ArrayList<GlycoNode> getParents() {
        return this.m_aParents;
    }

    protected boolean addParent(GlycoNode a_objParent) throws GlycoconjugateException {
        if (a_objParent == null) {
            throw new GlycoconjugateException("null is not a valide parent.");
        }
        if (this.m_aParents.contains(a_objParent)) {
            return false;
        }
        GlycoVisitorNodeType t_objType = new GlycoVisitorNodeType();
        try {
            if (t_objType.isSugarUnitCyclic(a_objParent)) {
                throw new GlycoconjugateException("A cyclic unit can't be a parent of a UnderdeterminedSubTree.");
            }
            if (t_objType.isSugarUnitAlternative(a_objParent)) {
                throw new GlycoconjugateException("A alternative unit can't be a parent of a UnderdeterminedSubTree.");
            }
            if (t_objType.isSugarUnitRepeat(a_objParent)) {
                throw new GlycoconjugateException("A repeat unit can't be a parent of a UnderdeterminedSubTree.");
            }
            this.m_aParents.add(a_objParent);
        } catch (GlycoVisitorException e) {
            throw new GlycoconjugateException(e.getErrorMessage(), e);
        }
        return true;
    }

    public double getProbabilityUpper() {
        return this.m_dAufenthaltswahrscheinlichkeitUpper;
    }

    public double getProbabilityLower() {
        return this.m_dAufenthaltswahrscheinlichkeitLower;
    }

    public void setProbability(double a_dLower, double a_dUpper) throws GlycoconjugateException {
        if (a_dLower > a_dUpper) {
            throw new GlycoconjugateException("The lower border of a probability must be smaller or equal than the " +
                    "upper border.");
        }
        this.m_dAufenthaltswahrscheinlichkeitLower = a_dLower;
        this.m_dAufenthaltswahrscheinlichkeitUpper = a_dUpper;
    }

    public void setProbability(double a_dProb) {
        this.m_dAufenthaltswahrscheinlichkeitLower = a_dProb;
        this.m_dAufenthaltswahrscheinlichkeitUpper = a_dProb;
    }

    /**
     * Does not copy the parent nodes.
     *
     * @return
     * @throws GlycoconjugateException
     */
    public UnderdeterminedSubTree copy() throws GlycoconjugateException {
        HashMap<GlycoNode, GlycoNode> t_hashResidues = new HashMap<GlycoNode, GlycoNode>();
        UnderdeterminedSubTree t_objCopy = new UnderdeterminedSubTree();
        GlycoNode t_objNodeOne;
        GlycoNode t_objNodeTwo;
        GlycoEdge t_objLinkOriginal;
        GlycoEdge t_objLinkCopy;
        ArrayList<GlycoEdge> t_aLinkages;
        // copy all nodes
        for (GlycoNode m_aResidue : this.m_aResidues) {
            t_objNodeOne = m_aResidue;
            t_objNodeTwo = t_objNodeOne.copy();
            t_hashResidues.put(t_objNodeOne, t_objNodeTwo);
            t_objCopy.addNode(t_objNodeTwo);
        }
        // copy linkages
        for (GlycoNode m_aResidue : this.m_aResidues) {
            t_objNodeOne = m_aResidue;
            t_aLinkages = t_objNodeOne.getChildEdges();
            for (GlycoEdge t_aLinkage : t_aLinkages) {
                t_objLinkOriginal = t_aLinkage;
                t_objLinkCopy = t_objLinkOriginal.copy();
                t_objNodeOne = t_hashResidues.get(t_objLinkOriginal.getParent());
                t_objNodeTwo = t_hashResidues.get(t_objLinkOriginal.getChild());
                if (t_objNodeOne == null || t_objNodeTwo == null) {
                    throw new GlycoconjugateException("Impossible to copy underdetermined subtree unit. Null values " +
                            "in copy.");
                }
                t_objCopy.addEdge(t_objNodeOne, t_objNodeTwo, t_objLinkCopy);
            }
        }
        // copy special infos
        t_objCopy.setProbability(this.m_dAufenthaltswahrscheinlichkeitLower, this
                .m_dAufenthaltswahrscheinlichkeitUpper);
        if (this.m_objConnection != null) {
            t_objCopy.setConnection(this.m_objConnection.copy());
        }
        return t_objCopy;
    }
}