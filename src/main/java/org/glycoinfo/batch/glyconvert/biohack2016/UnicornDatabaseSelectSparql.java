package org.glycoinfo.batch.glyconvert.biohack2016;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.rdf.SelectSparqlBean;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * 
 * An abstract class used to retrieve the glycan sequences, based on the
 * conversion format specified in the getFrom() of the configured GlyConvert.
 * The filter removes any existing sequences in the getTo() of the GlyConvert.
 * 
 * For instance: Retrieving of original glycoct by using
 * org.glycoinfo.conversion.wurcs.GlycoctToWurcsConverter.
 * 
 * @author aoki
 *
 */
public class UnicornDatabaseSelectSparql extends SelectSparqlBean implements
		InitializingBean {
	public UnicornDatabaseSelectSparql(String sparql) {
		super(sparql);
	}

	public UnicornDatabaseSelectSparql() {
		super();
	}

	protected Log logger = LogFactory.getLog(getClass());

	String glycanUri;

	@Override
	public String getSelect() {
		return "DISTINCT ?" + Saccharide.PrimaryId + " ?" + GlycoSequence.Sequence + " ";
	}

	@Override
	public String getPrefix() {
		return "PREFIX glycan: <http://purl.jp/bio/12/glyco/glycan#>";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.glycoinfo.rdf.SelectSparqlBean#getWhere()
	 */
	public String getWhere() {
		return "?glycan glycan:has_glycosequence ?gseq .\n" + 
				"?gseq glycan:has_sequence ?" + GlycoSequence.Sequence + " . \n" + 
				"?gseq glycan:in_carbohydrate_format  glycan:carbohydrate_format_glycoct .\n" + 
				"BIND(STR(?glycan) AS ?strGlycan)\n" + 
				"BIND(STRAFTER(?strGlycan,\"http://unicorn.unicarbkb.org/structure/\") AS ?" + Saccharide.PrimaryId + " )"
			+ getFilter();
	}

	/**
	 * 
	 * the filter removes any sequences that already have a sequence in the
	 * GlyConvert.getTo() format.
	 * 
	 * @return
	 */
	public String getFilter() {
		/* FILTER NOT EXISTS {
?SaccharideURI glycan:has_glycosequence ?existingseq .
?existingseq glycan:has_sequence ?someSequence .
?existingseq glycan:in_carbohydrate_format glycan:carbohydrate_format_wurcs .
}
*/
//		return "FILTER NOT EXISTS {\n"
//				+ "?" + SaccharideURI + " glycan:has_glycosequence ?existingseq .\n"
//				+ "?existingseq glycan:has_sequence ?someSequence .\n"
//				+ "?existingseq glycan:in_carbohydrate_format glycan:carbohydrate_format_glycoct\n .\n" + "}";
		return "";
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(getPrefix() != null, "A ident is required");
		Assert.state(getSelect() != null, "A select is required");
	}
	
	@Override
	public String getFrom() {
    return "FROM <http://rdf.glycoinfo.org/unicorn>\n";
	}
}