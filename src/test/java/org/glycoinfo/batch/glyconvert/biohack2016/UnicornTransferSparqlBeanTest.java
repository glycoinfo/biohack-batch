package org.glycoinfo.batch.glyconvert.biohack2016;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.batch.glyconvert.GlyConvertSparqlItemConfig;
import org.glycoinfo.batch.glyconvert.biohack2016.UnicornTransferConfig;
import org.glycoinfo.batch.glyconvert.biohack2016.UnicornTransferSparqlBatch;
import org.glycoinfo.convert.util.DetectFormat;
import org.glycoinfo.rdf.InsertSparql;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SelectSparqlBean;
import org.glycoinfo.rdf.dao.SparqlDAO;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.GlycoSequenceInsertSparql;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.glycan.SaccharideInsertSparql;
import org.glycoinfo.rdf.glycan.SaccharideUtil;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glycoinfo.rdf.utils.TripleStoreProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author aoki
 *
 *         Test cases for the wurcs converter. The batch should process all
 *         wurcs glycosequences that do not have an associated iupac version.
 *         The ETL for this batch:<br>
 *         <br>
 *         Extract: Select from GlycoSequence all wurcs strings filtered by
 *         non-existing iupac strings for the same Saccharide (accession
 *         number).<br>
 *         Transform: Execute the WurcsToIupac converter on the wurcs string to
 *         generate the iupac string.<br>
 *         Load: Insert the iupac into the same Saccharide.<br>
 *
 *         This work is licensed under the Creative Commons Attribution 4.0
 *         International License. To view a copy of this license, visit
 *         http://creativecommons.org/licenses/by/4.0/.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { UnicornTransferSparqlBeanTest.class, UnicornTransferConfig.class, VirtSesameTransactionConfig.class, GlyConvertSparqlItemConfig.class })
@Configuration
@EnableAutoConfiguration
@IntegrationTest
public class UnicornTransferSparqlBeanTest {
  protected Log logger = LogFactory.getLog(getClass());

//  @Autowired
//  GlycanProcedure glycanProcedure;

  @Autowired
  SparqlDAO sparqlDAO;

  @Autowired
  @Qualifier("itemReaderSelectSparql")
  SelectSparql itemReaderSelectSparql;

  @Autowired
  @Qualifier("itemWriterInsertSparql")
  InsertSparql itemWriterInsertSparql;

  // using a weird wurcs...
  String wurcs = "WURCS=2.0/8,9,8/[a1122h-1x_1-5][a2122h-1x_1-5][a2112h-1x_1-5][a1221m-1x_1-5][a221h-1x_1-5][a2112h-1x_1-5_2*NCC/3=O][a2122h-1x_1-5_2*NCC/3=O][Aad21122h-2x_2-6_5*NCC/3=O]/1-2-3-4-5-6-7-8-4/a?-b1_b?-c1_c?-d1_d?-e1_e?-f1_f?-g1_g?-h2_h?-i1";

  @Autowired
  @Qualifier("GlycosequenceInsert")
  InsertSparql glycoSequenceInsert;
  
  @Autowired
  @Qualifier("SaccharideInsert")
  InsertSparql saccharideInsertSparql;
  
  @Bean(name="GlycosequenceInsert")
  InsertSparql glycoSequenceInsert() {
    GlycoSequenceInsertSparql gsis = new GlycoSequenceInsertSparql();
    gsis.setSparqlEntity(new SparqlEntity());
    gsis.setGraphBase("http://unicorn.glycoinfo.org/sequence/glycoct");
    return gsis;
  }
  
  @Bean(name="SaccharideInsert")
  InsertSparql saccharideInsertSparql() {
    SaccharideInsertSparql sis = new SaccharideInsertSparql();
    sis.setGraph("http://unicorn.glycoinfo.org/sequence/glycoct");
    return sis;
  }
  
  @Test
  @Transactional
  public void testExtract() throws Exception {
    SparqlEntity glycoSequenceInsertSE = new SparqlEntity();
    String primarykey="TESTCONVERSION12345MO";
    glycoSequenceInsertSE.setValue(Saccharide.PrimaryId, primarykey);
    glycoSequenceInsertSE.setValue(GlycoSequence.Sequence, wurcs);
    glycoSequenceInsertSE.setValue(GlycoSequence.Format, "wurcs");

    glycoSequenceInsert.setSparqlEntity(glycoSequenceInsertSE);

    SaccharideInsertSparql sis = new SaccharideInsertSparql();
    sis.setSparqlEntity(glycoSequenceInsert.getSparqlEntity());

    glycoSequenceInsert.getSparqlEntity().setValue(Saccharide.URI,
        SaccharideUtil.getURI(glycoSequenceInsertSE.getValue(Saccharide.PrimaryId)));
    saccharideInsertSparql.setSparqlEntity(glycoSequenceInsertSE);
    sparqlDAO.insert(saccharideInsertSparql);

    // insert the wurcs which will not have iupac
    sparqlDAO.insert(glycoSequenceInsert);
    

    
//    String accessNumber = glycanProcedure.register(wurcs, "0");

    // SelectSparql select = new SelectSparqlBean("PREFIX glycan:
    // <http://purl.jp/bio/12/glyco/glycan#>\n"
    // + "SELECT count(distinct ?SaccharideURI) as ?test\n" + "FROM
    // <http://rdf.glytoucan.org>\n"
    // + "FROM <http://rdf.glytoucan.org/sequence/wurcs>\n" + "WHERE {\n"
    // + "?SaccharideURI glycan:has_glycosequence ?GlycanSequenceURI .\n"
    // + "?GlycanSequenceURI glycan:has_sequence \"" + wurcs + "\" .\n"
    // + "?GlycanSequenceURI glycan:in_carbohydrate_format
    // glycan:carbohydrate_format_wurcs .\n }\n");
    // List<SparqlEntity> results = sparqlDAO.query(select);
    // SparqlEntity next = results.iterator().next();
    //
    // String count = next.getValue("test");
    //
    // logger.debug("count:>" + count);
    //
    // Assert.assertTrue(Integer.getInteger(count) > 0);
    
    // make sure it is picked up by the select
    SparqlEntity se = new SparqlEntity();

    // retrieve any sequence not converted yet
    List<SparqlEntity> results = sparqlDAO.query(itemReaderSelectSparql);

    boolean wurcsfound = false;
    for (Iterator iterator = results.iterator(); iterator.hasNext();) {
      SparqlEntity sparqlEntity = (SparqlEntity) iterator.next();
      String sequence = sparqlEntity.getValue("PrimaryId");
      
      // make sure the weird wurcs is there.
      if (sequence.contains(primarykey)) {
        wurcsfound = true;
        break;
      }
    }

    if (!wurcsfound)
      Assert.fail();
  }

  @Test
  public void testTransform() {

    // confirm the conversion is ok with processor

  }

  @Test
  @Transactional
  public void testLoad() {
    // confirm insert contains the converted format

  }

  @Bean
  TripleStoreProperties getTripleStoreProperties() {
    return new TripleStoreProperties();
  }
}