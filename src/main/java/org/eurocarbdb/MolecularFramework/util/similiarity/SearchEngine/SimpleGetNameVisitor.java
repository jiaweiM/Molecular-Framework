/**
 *
 */
package org.eurocarbdb.MolecularFramework.util.similiarity.SearchEngine;

import org.eurocarbdb.MolecularFramework.sugar.GlycoEdge;
import org.eurocarbdb.MolecularFramework.sugar.Monosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.NonMonosaccharide;
import org.eurocarbdb.MolecularFramework.sugar.Substituent;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitAlternative;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitCyclic;
import org.eurocarbdb.MolecularFramework.sugar.SugarUnitRepeat;
import org.eurocarbdb.MolecularFramework.sugar.UnvalidatedGlycoNode;
import org.eurocarbdb.MolecularFramework.util.traverser.GlycoTraverser;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitor;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;

/**
 * @author sherget
 */
public class SimpleGetNameVisitor implements GlycoVisitor {

    String m_sName;

    @Override
    public void visit(Monosaccharide a_objMonosaccharid) throws GlycoVisitorException {
        this.m_sName = a_objMonosaccharid.getGlycoCTName();

    }

    @Override
    public void visit(NonMonosaccharide a_objResidue) throws GlycoVisitorException {

    }

    @Override
    public void visit(SugarUnitRepeat a_objRepeat) throws GlycoVisitorException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(Substituent a_objSubstituent) throws GlycoVisitorException {
        this.m_sName = a_objSubstituent.getSubstituentType().getName();

    }

    @Override
    public void visit(SugarUnitCyclic a_objCyclic) throws GlycoVisitorException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(SugarUnitAlternative a_objAlternative) throws GlycoVisitorException {
        // TODO Auto-generated method stub

    }

    @Override
    public void visit(UnvalidatedGlycoNode a_objUnvalidated) throws GlycoVisitorException {
        this.m_sName = a_objUnvalidated.getName();

    }

    @Override
    public void visit(GlycoEdge a_objLinkage) throws GlycoVisitorException {
        // TODO Auto-generated method stub

    }

    @Override
    public void start(Sugar a_objSugar) throws GlycoVisitorException {
        // TODO Auto-generated method stub

    }

    @Override
    public GlycoTraverser getTraverser(GlycoVisitor a_objVisitor) throws GlycoVisitorException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        this.m_sName = "";

    }

    public String getName() {
        return this.m_sName;
    }

}
