package moore.christian.thehub.controllers;

import java.lang.reflect.Field;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import moore.christian.thehub.models.Project;
import moore.christian.thehub.models.User;
import moore.christian.thehub.repositories.ProjectJpaRepository;
import moore.christian.thehub.repositories.UserJpaRepository;

@RestController
@RequestMapping("/project")
public class ProjectRestController {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private ProjectJpaRepository projects;
	
	@Autowired
	private UserRestController users;
	
	@Autowired 
	private UserJpaRepository usersJpa;
	
//	@PostConstruct
//	@Transactional
//	private void init() {
//		User joe = usersJpa.findByUsername("joe");
//		if(joe == null) {
//			joe = new User();
//			joe.setEmail("junk@junkmail.com");
//			joe.setUsername("joe");
//			joe.setPassword(passwordEncoder.encode("1234"));
//			joe.getRoles().addAll(Arrays.asList("USER", "ADMIN"));
//			usersJpa.save(joe);
//		}
//		
//	}
	
	@RequestMapping(path="", method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN') || hasAuthority('USER')")
	@Transactional
	public void create(@RequestBody Project newProject, Principal p) {
		if (newProject.getDateCreated() == 0) {
			newProject.setDateCreated(System.currentTimeMillis());
		}
		if (newProject.getProjectUsers() == null){
			newProject.setProjectUsers(new ArrayList<User>());
		}
		String username = p.getName();
		this.projects.save(newProject);
	}
	
	@RequestMapping(path="/{projectId}", method=RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('ADMIN')")
	@Transactional
	public void delete(@PathVariable(value="projectId") int projectId) {
		this.projects.deleteById(projectId);
	}
	
	@RequestMapping(path="/{projectId}", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public Project retreiveProject(@PathVariable(value="projectId") int projectId) {
		return projects.findById(projectId).orElse(null);
	}
	
	@RequestMapping(path="/retrieveAll", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody List<Project> retrieveAll() {
		return projects.findAll();
	}
	
	@RequestMapping(path="/{projectId}", method=RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('USER')")
	@Transactional
	public void updateProjectPartial(@PathVariable int projectId, @RequestBody Map<String, Object> updates) {
		Project project = projects.getOne(projectId);
		if(project instanceof HibernateProxy) {
		    project = (Project) Hibernate.unproxy(project);
		}
		//updates
		for (String key : updates.keySet()) {

			Field field = null;
			try {
				field = project.getClass().getDeclaredField(key);
				field.setAccessible(true);
				field.set(project, updates.get(key));
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		projects.save(project);
	}
	
	@RequestMapping(path="/{projectId}", method=RequestMethod.PUT)
	@PreAuthorize("hasAuthority('ADMIN')")
	@Transactional
	public void updateProjectFull(@PathVariable int projectId, @RequestBody Project src) {
		Project proj = projects.getOne(projectId);
		proj.copy(src);
		
		projects.save(proj);
		
	}
	
	@RequestMapping(path="/{projectId}/user/{userId}", method=RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('USER')")
	@Transactional
	public void addUserToProject(@PathVariable int projectId, @PathVariable int userId) {
		Project project  = projects.getOne(projectId);
		User user = users.retrieveUser(userId);
		project.getProjectUsers().add(user);
		projects.save(project);
	}

	
	@RequestMapping(path="/searchByProjName/{name}", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public List<Project> findProjectsName(@PathVariable String name){
		return this.projects.findByName(name);
	}
	
	@RequestMapping(path="/searchByUser", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public List<Project> findProjectsByUser(@RequestBody User user){
		return this.projects.queryByUser(user);
	}
	
	
	
}