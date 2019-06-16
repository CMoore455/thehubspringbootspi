package moore.christian.thehub.repositories;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import moore.christian.thehub.models.Project;
import moore.christian.thehub.models.User;

public interface UserJpaRepository extends JpaRepository<User, Integer>{
	List<User> findByDateCreated(long timeStamp);
	
	@Query("SELECT u "+
			"FROM User u "+
			"WHERE :email = u.email")
	List<User> queryByProjectName(@Param(value="email") String email);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
    User getOne(Integer id);
	
	User findByUsername(String username);
	
	User findByEmail(String email);
}
