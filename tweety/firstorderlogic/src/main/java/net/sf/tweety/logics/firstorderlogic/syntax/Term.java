package net.sf.tweety.logics.firstorderlogic.syntax;

import net.sf.tweety.logics.CommonTerm;

/**
 * A term of first-order logic, i.e. an object of a specific sort.
 * @author Matthias Thimm
 */
public abstract class Term extends LogicStructure implements CommonTerm {
		
	private Sort sort;
		
	public Term(Sort sort){
		this.sort = sort;
	}
	
	/**
	 * Substitutes all occurences of term "v" in this term
	 * by term "t" and returns the new term.
	 * @param v the term to be substituted.
	 * @param t the term to substitute.
	 * @return a term where every occurenIf you use logical programs but you have to parse FOL formulas this interface
 * helps by translating between the two different signatures.ce of "v" is replaced
	 * 		by "t".
	 * @throws IllegalArgumentException if "v" and "t" are of different sorts
	 *    (NOTE: this exception is only thrown when "v" actually appears in this
	 *    formula)
	 */
	public abstract Term substitute(Term v, Term t) throws IllegalArgumentException;
	
	/**
	 * Returns the sort of this term.
	 * @return the sort of this term.
	 */
	public Sort getSort(){
		return this.sort;
	}	
	
	@Override
	public String getSortName() {
		return getSort().getName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Term other = (Term) obj;
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
			return false;
		return true;
	}
}