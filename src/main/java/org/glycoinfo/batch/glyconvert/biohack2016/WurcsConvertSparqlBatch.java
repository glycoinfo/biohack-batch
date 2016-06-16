package org.glycoinfo.batch.glyconvert.biohack2016;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.batch.SparqlItemReader;
import org.glycoinfo.batch.SparqlItemWriter;
import org.glycoinfo.batch.glyconvert.ConvertInsertSparql;
import org.glycoinfo.batch.glyconvert.ConvertSelectSparql;
import org.glycoinfo.batch.glyconvert.ConvertSparqlProcessor;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.convert.glycoct.GlycoctToWurcsConverter;
import org.glycoinfo.rdf.InsertSparql;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.dao.SparqlDAO;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.virt.SparqlDAOVirtSesameImpl;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.utils.TripleStoreProperties;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
// @ComponentScan(basePackages = ("org.glycoinfo.batch"))
@EnableBatchProcessing
@SpringBootApplication
@Import(VirtSesameTransactionConfig.class)
public class WurcsConvertSparqlBatch {

	private static final Log logger = LogFactory.getLog(WurcsConvertSparqlBatch.class);

	// graph base to set the graph to insert into. The format type (toFormat())
	// will be added to the end.
	// example: http://glytoucan.org/rdf/demo/0.7/wurcs
	public static String graphbase = "http://rdf.glytoucan.org/sequence";
	private int pageSize = 10000;

	@Autowired
	TripleStoreProperties tripleStoreProperties;
	
	@Autowired
	SparqlItemReader<SparqlEntity> sparqlItemReader;

	@Autowired
	SparqlItemWriter<SparqlEntity> sparqlItemWriter;

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ApplicationContext ctx = SpringApplication.run(WurcsConvertSparqlBatch.class, args);
	}

	@Bean
	GlyConvert getGlyConvert() {
		return new GlycoctToWurcsConverter();
	}

	@Bean(name = "itemReaderSelectSparql")
	SelectSparql getSelectSparql() {
		SelectSparql select = new UnicornDatabaseSelectSparql();
//		select.setFrom("FROM <http://rdf.glytoucan.org> FROM <http://rdf.glytoucan.org/sequence/wurcs>");
		return select;
	}

	@Bean(name = "itemWriterInsertSparql")
	InsertSparql getInsertSparql() {
		ConvertInsertSparql convert = new ConvertInsertSparql();
		convert.setGraphBase(graphbase);
		return convert;
	}

	@Bean
	SparqlDAO getSparqlDAO() {
		return new SparqlDAOVirtSesameImpl();
	}

//	@Bean
//	TripleStoreProperties getTripleStoreProperties() {
//		return new TripleStoreProperties();
//	}

	@Bean
	SparqlItemReader<SparqlEntity> sparqlItemReader() {
		SparqlItemReader<SparqlEntity> reader = new SparqlItemReader<SparqlEntity>();
		reader.setPageSize(pageSize);
		return reader;
	}

	@Bean
	public ItemReader<SparqlEntity> reader() {
		// logger.debug(sparqlItemReader.toString());
		return sparqlItemReader;
	}

	@Bean
	public SparqlItemWriter<SparqlEntity> sparqlItemWriter() {
		SparqlItemWriter<SparqlEntity> reader = new SparqlItemWriter<SparqlEntity>();
		return reader;
	}

	@Bean
	public ItemWriter<SparqlEntity> writer() {
		return sparqlItemWriter;
	}

	@Bean
	public Job importUserJob(JobBuilderFactory jobs, Step s1) {
		return jobs.get("ConvertWurcs").incrementer(new RunIdIncrementer()).flow(s1).end().build();
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<SparqlEntity> reader,
			ItemWriter<SparqlEntity> writer, ItemProcessor<SparqlEntity, SparqlEntity> processor) {
		return stepBuilderFactory.get("step1").<SparqlEntity, SparqlEntity> chunk(10).reader(reader)
				.processor(processor).writer(writer).build();
	}

	@Bean
	public ItemProcessor<SparqlEntity, SparqlEntity> processor() {
		ConvertSparqlProcessor process = new ConvertSparqlProcessor();
		process.setGlyConvert(getGlyConvert());

		return process;
	}
}