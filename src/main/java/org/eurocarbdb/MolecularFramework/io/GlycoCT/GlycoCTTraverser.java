package org.eurocarbdb.MolecularFramework.io.GlycoCT;


import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.GlycoGraph;
import org.eurocarbdb.MolecularFramework.sugar.GlycoNode;
import org.eurocarbdb.MolecularFramework.sugar.GlycoconjugateException;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

import java.util.ArrayList;
import java.util.Collections;

public class GlycoCTTraverser extends GlycoTraverser {

    public GlycoCTTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        super(a_objVisitor);
    }

    public void traverse(GlycoEdge a_objEdge) throws GlycoVisitorException {
        // callback of the function before subtree 
        this.m_iState = GlycoTraverser.ENTER;
        a_objEdge.accept(this.m_objVisitor);
        // traverse subtree
        this.traverse(a_objEdge.getChild());
    }

    @Override
    public void traverse(GlycoNode a_objNode) throws GlycoVisitorException {
        // callback before subtree
        this.m_iState = GlycoTraverser.ENTER;
        a_objNode.accept(this.m_objVisitor);
        // traverse subtree in GlycoCT order
        ArrayList<GlycoEdge> t_aEdge = a_objNode.getChildEdges();
        GlycoCTGlycoEdgeComparator t_oNodeComparator = new GlycoCTGlycoEdgeComparator();
        Collections.sort(t_aEdge, t_oNodeComparator);
        for (GlycoEdge t_oEdge : t_aEdge) {
            this.traverse(t_oEdge);
        }
    }

    @Override
    public void traverseGraph(GlycoGraph a_objSugar) throws GlycoVisitorException {
        ArrayList<GlycoNode> t_aRoot;
        try {
            // get root nodes of forest of graphs
            t_aRoot = a_objSugar.getRootNodes();
            //priorize according to GlycoCT all isolated subgraphs and process consecutivly
            GlycoCTGlycoNodeComparator t_oNodeComparator = new GlycoCTGlycoNodeComparator();
            Collections.sort(t_aRoot, t_oNodeComparator);

            for (GlycoNode aT_aRoot : t_aRoot) {
                this.traverse(aT_aRoot);
            }
        } catch (GlycoconjugateException e) {
            throw new GlycoVisitorException(e.getMessage(), e);
        }
    }
}

