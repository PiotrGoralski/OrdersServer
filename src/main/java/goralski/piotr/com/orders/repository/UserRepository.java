package goralski.piotr.com.orders.repository;

import goralski.piotr.com.orders.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
}