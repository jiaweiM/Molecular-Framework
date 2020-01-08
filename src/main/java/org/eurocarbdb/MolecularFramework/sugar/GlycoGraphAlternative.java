package org.eurocarbdb.MolecularFramework.sugar;

import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorNodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class GlycoGraphAlternative implements GlycoGraph
{

    private ArrayList<GlycoNode> m_aResidues = new ArrayList<>();
    private GlycoNode m_objLeadInNode = null;
    private HashMap<GlycoNode, GlycoNode> m_hLeadOutNodeToNode = new HashMap<>();

    @Override
    public ArrayList<GlycoNode> getRootNodes() throws GlycoconjugateException
    {
        ArrayList<GlycoNode> t_aResult = new ArrayList<>();

        // for all residues of the sugar
        for (GlycoNode glycoNode : m_aResidues) {
            GlycoEdge parentEdge = glycoNode.getParentEdge();
            if (parentEdge == null) {
                t_aResult.add(glycoNode);
            }
        }

        if (t_aResult.size() < 1) {
            throw new GlycoconjugateException("Sugar seems not to have at least one root residue");
        }
        return t_aResult;
    }

    @Override
    public Iterator<GlycoNode> getNodeIterator()
    {
        return this.m_aResidues.iterator();
    }

    @Override
    public boolean isConnected() throws GlycoconjugateException
    {
        ArrayList<GlycoNode> t_objRoots = this.getRootNodes();
        return t_objRoots.size() <= 1;
    }

    public boolean removeNode(GlycoNode a_objResidue) throws GlycoconjugateException
    {
        GlycoEdge t_objLinkage;
        GlycoNode t_objResidue;
        if (a_objResidue == null) {
            throw new GlycoconjugateException("Invalide residue.");
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

    public ArrayList<GlycoNode> getNodes()
    {
        return this.m_aResidues;
    }

    @Override
    public boolean addNode(GlycoNode a_objResidue) throws GlycoconjugateException
    {
        if (a_objResidue == null) {
            throw new GlycoconjugateException("Invalide residue.");
        }
        GlycoVisitorNodeType t_objNodeType = new GlycoVisitorNodeType();
        if (!this.m_aResidues.contains(a_objResidue)) {
            try {
                if (t_objNodeType.isSugarUnitCyclic(a_objResidue)) {
                    throw new GlycoconjugateException("Cyclic unit are not allowed in alternative graphs.");
                }
                if (t_objNodeType.isSugarUnitRepeat(a_objResidue)) {
                    throw new GlycoconjugateException("repeat unit are not allowed in alternative graphs.");
                }
                if (t_objNodeType.isSugarUnitAlternative(a_objResidue)) {
                    throw new GlycoconjugateException("Alternative units are not allowed in alternative graphs.");
                }
            } catch (GlycoVisitorException e) {
                throw new GlycoconjugateException(e.getMessage(), e);
            }
            a_objResidue.removeAllEdges();
            return this.m_aResidues.add(a_objResidue);
        }
        return false;
    }

    @Override
    public boolean addNode(GlycoNode a_objParent, GlycoEdge a_objLinkage, GlycoNode a_objChild) throws
            GlycoconjugateException
    {
        if (a_objParent == null || a_objChild == null) {
            throw new GlycoconjugateException("Invalide residue.");
        }
        if (a_objLinkage == null) {
            throw new GlycoconjugateException("Invalide linkage.");
        }
        if (a_objChild.getParentEdge() != null) {
            throw new GlycoconjugateException("The child residue has a parent residue.");
        }
        this.addNode(a_objParent);
        this.addNode(a_objChild);
        if (this.m_aResidues.contains(a_objChild) && this.m_aResidues.contains(a_objParent)) {
            // test for indirect cyclic structures
            if (this.isParent(a_objChild, a_objParent) || a_objChild == a_objParent) {
                throw new GlycoconjugateException("Cyclic structures are not allowed in alternative glyco graphs.");
            }
        } else {
            throw new GlycoconjugateException("Critical error imposible to add residue.");
        }
        a_objChild.setParentEdge(a_objLinkage);
        a_objParent.addChildEdge(a_objLinkage);
        a_objLinkage.setChild(a_objChild);
        a_objLinkage.setParent(a_objParent);
        return true;
    }

    @Override
    public boolean addEdge(GlycoNode a_objParent, GlycoNode a_objChild, GlycoEdge a_objLinkage) throws
            GlycoconjugateException
    {
        return this.addNode(a_objParent, a_objLinkage, a_objChild);
    }

    @Override
    public boolean containsNode(GlycoNode a_objNode)
    {
        return this.m_aResidues.contains(a_objNode);
    }

    @Override
    public boolean isParent(GlycoNode a_objParent, GlycoNode a_objNode)
    {
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
    public boolean removeEdge(GlycoEdge a_objEdge) throws GlycoconjugateException
    {
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

    public GlycoGraphAlternative copy() throws GlycoconjugateException
    {
        HashMap<GlycoNode, GlycoNode> t_hashResidues = new HashMap<GlycoNode, GlycoNode>();
        GlycoGraphAlternative t_objCopy = new GlycoGraphAlternative();
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
                    throw new GlycoconjugateException("Impossible to copy alternative glyco graph. Null values in " +
                            "copy.");
                }
                t_objCopy.addEdge(t_objNodeOne, t_objNodeTwo, t_objLinkCopy);
            }
        }
        // copy parent connection information
        if (this.m_objLeadInNode != null) {
            t_objCopy.setLeadInNode(t_hashResidues.get(this.m_objLeadInNode));
        }
        // copy child connection information
        for (GlycoNode t_objChild : this.m_hLeadOutNodeToNode.keySet()) {
            t_objNodeOne = t_hashResidues.get(this.m_hLeadOutNodeToNode.get(t_objChild));
            if (t_objNodeOne == null) {
                throw new GlycoconjugateException("Impossible to copy child connection. Null values in copy.");
            }
            t_objCopy.addLeadOutNodeToNode(t_objChild, t_objNodeOne);
        }
        return t_objCopy;
    }

    protected void setLeadInNode(GlycoNode a_objNode) throws GlycoconjugateException
    {
        if (!this.m_aResidues.contains(a_objNode)) {
            throw new GlycoconjugateException("Parent residue is not part of the glyco graph.");
        }
        this.m_objLeadInNode = a_objNode;
    }

    public GlycoNode getLeadInNode()
    {
        return this.m_objLeadInNode;
    }

    protected void addLeadOutNodeToNode(GlycoNode a_objChildResidue, GlycoNode a_objNode) throws
            GlycoconjugateException
    {
        if (!this.m_aResidues.contains(a_objNode)) {
            throw new GlycoconjugateException("Residue is not part of the glyco graph.");
        }
        this.m_hLeadOutNodeToNode.put(a_objChildResidue, a_objNode);
    }

    public HashMap<GlycoNode, GlycoNode> getLeadOutNodeToNode()
    {
        return this.m_hLeadOutNodeToNode;
    }

    public boolean removeLeadOutNodeToNode(GlycoNode a_objChild, GlycoNode a_objNode)
    {
        if (!this.m_hLeadOutNodeToNode.containsKey(a_objChild)) {
            return false;
        }
        if (this.m_hLeadOutNodeToNode.get(a_objChild) != a_objNode) {
            return false;
        }
        this.m_hLeadOutNodeToNode.remove(a_objChild);
        return true;
    }

    /**
     *
     */
    public void removeAllLeadOutNodes()
    {
        this.m_hLeadOutNodeToNode.clear();
    }
}