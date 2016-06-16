package org.glycoinfo.batch.glyconvert.biohack2016;

import org.glycoinfo.batch.glyconvert.GlyConvertSparqlItemConfig;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableBatchProcessing
@Import(GlyConvertSparqlItemConfig.class)
public class UnicornTransferConvertJobConfig {


	// graph base to set the graph to insert into. The format type (toFormat())
	// will be added to the end.
	public static String graphbase = "http://rdf.glytoucan.org/sequence";

	@Bean
	public Job importUserJob(JobBuilderFactory jobs, Step s1) {
	  return jobs.get("UnicornTransfer").incrementer(new RunIdIncrementer()).flow(s1).end().build();
	}

	@Bean
	public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<SparqlEntity> reader,
			ItemWriter<SparqlEntity> writer, ItemProcessor<SparqlEntity, SparqlEntity> processor) {
		return stepBuilderFactory.get("step1").<SparqlEntity, SparqlEntity> chunk(10).reader(reader)
				.processor(processor).writer(writer).build();
	}
	
  @Bean
  public ItemProcessor<SparqlEntity, SparqlEntity> processor() {
    return new UnicornTransferSparqlProcessor();
  }
}