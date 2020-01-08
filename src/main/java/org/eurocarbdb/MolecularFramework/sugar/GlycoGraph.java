package org.eurocarbdb.MolecularFramework.sugar;


import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author JiaweiMao
 * @author Logan
 * @version 1.0.0
 * @date 2016.12.01, 10:51 PM
 */
public interface GlycoGraph {

    boolean addEdge(GlycoNode a_objParent, GlycoNode a_objChild, GlycoEdge a_objLinkage) throws
            GlycoconjugateException;

    /**
     * Adds a Residue to the sugar. The linkage and the parent Residue is given.
     *
     * @param a_objParent
     * @param a_objLinkage
     * @param a_objChild
     */
    boolean addNode(GlycoNode a_objParent, GlycoEdge a_objLinkage, GlycoNode a_objChild) throws
            GlycoconjugateException;

    /**
     * Adds a Residue to the sugar.
     *
     * @param a_objResidue
     * @throws GlycoconjugateException
     */
    boolean addNode(GlycoNode a_objResidue) throws GlycoconjugateException;

    /**
     * Delivers all residues that do not have a parent residue.
     *
     * @return Arraylist of all residues
     * @throws GlycoconjugateException if the structure contain a cyclic part or if the sugar contain no residue with a
     *                                 parent
     */
    ArrayList<GlycoNode> getRootNodes() throws GlycoconjugateException;

    /**
     * Delivers an iterator over all RESIDUES of the sugar
     *
     * @return Iterator
     */
    Iterator<GlycoNode> getNodeIterator();

    /**
     * Returns true if all residues of the SugarUnit are connected to one tree
     *
     * @return
     */
    boolean isConnected() throws GlycoconjugateException;

    boolean removeNode(GlycoNode a_objNode) throws GlycoconjugateException;

    boolean removeEdge(GlycoEdge a_objEdge) throws GlycoconjugateException;

    ArrayList<GlycoNode> getNodes();

    boolean containsNode(GlycoNode a_objNode);

    /**
     * Recursive check if query node has specified parent
     *
     * @param a_objParent parent GlycoNode to check.
     * @param a_objNode   child GlycoNode to check.
     * @return true if a a_objNode is the child node of a_objParent.
     */
    boolean isParent(GlycoNode a_objParent, GlycoNode a_objNode);
}
