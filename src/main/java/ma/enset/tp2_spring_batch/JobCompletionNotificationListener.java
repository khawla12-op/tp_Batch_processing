package ma.enset.tp2_spring_batch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final JdbcTemplate jdbcTemplate;

    public JobCompletionNotificationListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LOGGER.info("Job terminé avec succès. Commandes insérées :");

            // Récupérer les données depuis la base
            List<Order> orders = jdbcTemplate.query(
                    "SELECT * FROM orders",
                    new DataClassRowMapper<>(Order.class)
            );

            if (orders.isEmpty()) {
                LOGGER.info("Aucune commande insérée dans la base de données.");
            } else {
                orders.forEach(order -> LOGGER.info(order.toString()));
            }
        } else {
            LOGGER.error("Le job n'a pas terminé correctement. Statut : " + jobExecution.getStatus());
        }
    }
}
