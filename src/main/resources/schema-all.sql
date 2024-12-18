drop table orders if exists;

CREATE TABLE orders(
                        order_id INT PRIMARY KEY,
                        customer_name VARCHAR(255),
                        amount DOUBLE
);
