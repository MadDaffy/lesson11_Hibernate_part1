package com.geekbrains.lesson11;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


//        1. В базе данных необходимо иметь возможность хранить информацию о покупателях (id, имя)
//        и товарах (id, название, стоимость);
//        2. У каждого покупателя свой набор купленных товаров, одна покупка одного товара это
//        отдельная запись в таблице (группировать не надо);
//        3. Написать тестовое консольное приложение (просто Scanner и System.out.println()), которое
//        позволит выполнять команды:
//        /showProductsByPerson имя_покупателя - посмотреть какие товары покупал клиент;
//        /findPersonsByProductTitle название_товара - какие клиенты купили определенный товар;
//        /removePerson(removeProduct) имя_элемента - предоставить возможность удалять из базы
//        товары/покупателей,
//        /buy имя_покупателя название_товара - организовать возможность “покупки товара”.
//        4. * Добавить детализацию по паре покупатель-товар: сколько стоил товар, в момент покупки
//        клиентом;

public class MainApp {
    public static void main(String[] args) {
        SessionFactory factory = null;
        Session session = null;
        try {
            factory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Customer.class)
                    .addAnnotatedClass(Product.class)
                    .addAnnotatedClass(Order.class)
                    .buildSessionFactory();

            String commandSQL = "";

            //Заполняем таблицы тестовыми данными
            Customer customer = new Customer();
            customer.setName("Efanov Vano");
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(customer);
            session.getTransaction().commit();

            Customer customer1 = new Customer();
            customer1.setName("Sysoev Dmit");
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(customer1);
            session.getTransaction().commit();

            Product product = new Product();
            product.setName("PC");
            product.setPrice(99_999);
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(product);
            session.getTransaction().commit();

            Product product1 = new Product();
            product1.setName("IPhone");
            product1.setPrice(66_666);
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(product1);
            session.getTransaction().commit();

            Order order = new Order();
            order.setProduct(product);
            order.setPrice(product.getPrice());
            order.setCustomer(customer);
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(order);
            session.getTransaction().commit();

            Order order1 = new Order();
            order1.setProduct(product);
            order1.setPrice(product.getPrice());
            order1.setCustomer(customer1);
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(order1);
            session.getTransaction().commit();

            Order order2 = new Order();
            order2.setProduct(product1);
            order2.setPrice(product1.getPrice());
            order2.setCustomer(customer);
            session = factory.getCurrentSession();
            session.beginTransaction();
            session.save(order2);
            session.getTransaction().commit();


            // Аля бизнес логика
            // Необходимо вынести большое количество повторяющегося кода в отдельные функции!
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
                while (!(commandSQL = bufferedReader.readLine()).equals("exit")) {
                    if (commandSQL.startsWith("/showProductsByPerson")) {
                        String customerName = commandSQL.substring(commandSQL.indexOf(" ") + 1);
                        session = factory.getCurrentSession();
                        session.beginTransaction();
                        Customer customerEntity = getCustomer(session, customerName);
                        List<Order> orderList = customerEntity.getOrderList();
                        orderList.forEach(System.out::println);
                        session.getTransaction().commit();
                    }
                    if (commandSQL.startsWith("/findPersonsByProductTitle")) {
                        String productName = commandSQL.substring(commandSQL.indexOf(" ") + 1);
                        session = factory.getCurrentSession();
                        session.beginTransaction();
                        Product productEntity = getProduct(session, productName);
                        List<Order> orderList = productEntity.getOrderList();
                        orderList.stream().map(Order::getCustomer).forEach(System.out::println);
                        session.getTransaction().commit();
                    }
                    if (commandSQL.startsWith("/removePerson")) {
                        String personName = commandSQL.substring(commandSQL.indexOf(" ") + 1);
                        session = factory.getCurrentSession();
                        session.beginTransaction();
                        Customer customerEntity = getCustomer(session, personName);
                        session.delete(customerEntity);
                        session.getTransaction().commit();
                    }
                    if (commandSQL.startsWith("/removeProduct")) {
                        String productName = commandSQL.substring(commandSQL.indexOf(" ") + 1);
                        session = factory.getCurrentSession();
                        session.beginTransaction();
                        Product productEntity = getProduct(session, productName);
                        session.delete(productEntity);
                        session.getTransaction().commit();
                    }
                    if (commandSQL.startsWith("/buy")) {
                        String personName = commandSQL.split(" ")[1] + " " + commandSQL.split(" ")[2];
                        String productName = commandSQL.split(" ")[3];
                        session = factory.getCurrentSession();
                        session.beginTransaction();
                        Customer customerEntity = getCustomer(session, personName);
                        Product productEntity = getProduct(session, productName);
                        Order orderEntity = new Order();
                        orderEntity.setCustomer(customerEntity);
                        orderEntity.setProduct(productEntity);
                        orderEntity.setPrice(product.getPrice());
                        customerEntity.getOrderList().add(orderEntity);
                        session.getTransaction().commit();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
//            factory.close();
//            session.close();
        }

    }

    private static Product getProduct(Session session, String productName) {
        Product productEntity = (Product) session.createQuery("FROM Product c WHERE c.name = :name")
                .setParameter("name", productName).getSingleResult();
        return productEntity;
    }

    private static Customer getCustomer(Session session, String personName) {
        Customer customerEntity = (Customer) session.createQuery("FROM Customer c WHERE c.name = :name")
                .setParameter("name", personName).getSingleResult();
        return customerEntity;
    }
}
