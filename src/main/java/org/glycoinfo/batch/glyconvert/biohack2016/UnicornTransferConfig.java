package org.glycoinfo.batch.glyconvert.biohack2016;

import org.glycoinfo.batch.SparqlItemReader;
import org.glycoinfo.batch.SparqlItemWriter;
import org.glycoinfo.batch.glyconvert.ConvertInsertSparql;
import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.batch.glyconvert.ConvertSparqlProcessor;
import org.glycoinfo.batch.glyconvert.GlyConvertSparqlItemConfig;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.convert.iupac.IupacCondensedToGlycoctConverter;
import org.glycoinfo.convert.wurcs.WurcsToIupacCondensedConverter;
import org.glycoinfo.rdf.InsertSparql;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({VirtSesameTransactionConfig.class})
public class UnicornTransferConfig {
	
	@Bean
	GlyConvert glyConvert() {
		return new IupacCondensedToGlycoctConverter();
	}

	// graph base to set the graph to insert into. The format type (toFormat())
	// will be added to the end.
	public static String graphbase = "http://unicorn.glycoinfo.org/sequence";

	@Bean(name = "itemReaderSelectSparql")
	SelectSparql itemReaderSelectSparql() {
		SelectSparql select = new UnicornDatabaseSelectSparql();
		select.setFrom("FROM <http://rdf.glytoucan.org> FROM <http://rdf.glytoucan.org/sequence/wurcs> FROM <https://rdf.glytoucan.org/sequence/iupac>");
		return select;
	}

	@Bean(name = "itemWriterInsertSparql")
	InsertSparql getInsertSparql() {
		ConvertInsertSparql convert = new ConvertInsertSparql();
		convert.setGraphBase(graphbase);
		return convert;
	}
}