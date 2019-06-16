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

public interface ProjectJpaRepository extends JpaRepository<Project, Integer>{

	List<Project> findByName(String name);
	
	@Query("SELECT p "+
			"FROM Project p "+
			"WHERE :user IN p.projectUsers ")
	List<Project> queryByUser(@Param(value="user") User user);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
    Project getOne(Integer id);
}
