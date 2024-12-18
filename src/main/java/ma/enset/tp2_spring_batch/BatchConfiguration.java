package ma.enset.tp2_spring_batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {

    @Bean
    public FlatFileItemReader<Order> reader() {
        return new FlatFileItemReaderBuilder<Order>()
                .name("orderItemReader")
                .resource(new ClassPathResource("orders.csv"))
                .delimited()
                .names("orderId", "customerName", "amount")
                .linesToSkip(1) // Pour ignorer la première ligne (en-têtes)
                .targetType(Order.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Order> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Order>()
                .sql("INSERT INTO orders(order_id, customer_name, amount) VALUES (:orderId, :customerName, :amount)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    @Bean
    public OrderItemProcessor processor() {
        return new OrderItemProcessor();
    }

    // Configuration du step
    @Bean
    public Step step1(JobRepository jobRepository,
                      DataSourceTransactionManager transactionManager,
                      FlatFileItemReader<Order> reader,
                      OrderItemProcessor processor,
                      JdbcBatchItemWriter<Order> writer) {
        return new StepBuilder("step1", jobRepository)
                .<Order, Order>chunk(3, transactionManager) // Type générique
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // Configuration du job
    @Bean
    public Job importOrderJob(JobRepository jobRepository, Step step1,
                              JobCompletionNotificationListener notificationListener) {
        return new JobBuilder("importOrderJob", jobRepository)
                .listener(notificationListener)
                .start(step1)
                .build();
    }
}
