package org.eurocarbdb.MolecularFramework.util.visitor;


/**
 * @author rene
 */
public interface Visitable {

    void accept(GlycoVisitor a_objVisitor) throws GlycoVisitorException;
}
