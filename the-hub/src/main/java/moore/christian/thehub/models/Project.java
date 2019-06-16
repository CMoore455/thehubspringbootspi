package moore.christian.thehub.models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="projects")
public class Project {
	
	@Id
	@Min(0)
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@Size(max=250)
	private String name;
	
	private long dateCreated;
		
	@ManyToMany
	@JsonIgnore(value=true)
	private List<User> projectUsers;
	
	public Project() {}
	
	public Project(String name, List<User> projectUsers) {
		this.setName(name);
		this.setProjectUsers(projectUsers);
		this.setDateCreated(System.currentTimeMillis());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<User> getProjectUsers() {
		return projectUsers;
	}
	public void setProjectUsers(List<User> projectUsers) {
		this.projectUsers = projectUsers;
	}
	
	public long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Override
	public boolean equals(Object obj) {
		Project other = (Project) obj;
		return this.getName().equals(other.getName());
	}
	
	public void copy(Project src) {
		this.setId(src.getId());
		this.setName(src.getName());
		this.setProjectUsers(src.getProjectUsers());
	}
	
	
}

