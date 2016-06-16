package org.glycoinfo.batch.glyconvert.biohack2016;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.glyconvert.ConvertInsertSparql;
import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.convert.error.ConvertException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class UnicornTransferSparqlProcessor implements
		ItemProcessor<SparqlEntity, SparqlEntity> {
	protected Log logger = LogFactory.getLog(getClass());
	
	@Override
	public SparqlEntity process(final SparqlEntity sparqlEntity) throws SparqlException, ConvertException {
		
		// get the sequence
		String sequence = sparqlEntity.getValue(ConvertSelectSparql.Sequence);
		logger.debug("Converting (" + sequence + ")");
		
		String id = sparqlEntity.getValue(Saccharide.PrimaryId);
		logger.debug("id (" + id + ")");
		// convert the sequence
		String convertedSeq = sequence;
		String errorMessage = null;

//		if (null != convertedSeq) {
			logger.debug("Converting (" + sequence + ") into (" + convertedSeq + ")");
			
			// log this action.  Conversion processes expect the wurcs to be there already
			
			
//			String encoded;
//			try {
//				encoded = URLEncoder.encode(convertedSeq, "UTF-8");
//				encoded = convertedSeq;
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//				throw new ConvertException(e);
//			}
		
//			logger.debug("Encoded (" + convertedSeq + ") into (" + encoded + ")");
			sparqlEntity.setValue(ConvertInsertSparql.ConvertedSequence, convertedSeq);
//		}

		if (null != errorMessage) {
			String encodedErrorMessage;
			try {
				encodedErrorMessage = URLEncoder.encode(errorMessage, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new ConvertException(e);
			}

			sparqlEntity.setValue(GlycoSequence.ErrorMessage, encodedErrorMessage);
		}

		return sparqlEntity;
	}
}