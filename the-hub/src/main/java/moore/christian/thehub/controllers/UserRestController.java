package moore.christian.thehub.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import java.io.File;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.gridfs.GridFSDBFile;
import com.mysql.cj.xdevapi.Result;

import moore.christian.thehub.MongoConfig;
import moore.christian.thehub.models.Project;
import moore.christian.thehub.models.User;
import moore.christian.thehub.repositories.UserJpaRepository;

@RestController
@RequestMapping("/users")
public class UserRestController {

	@Autowired
	private UserJpaRepository users;

	@Autowired
	GridFsTemplate gridFsTemplate;

	@Autowired
	private ProjectRestController projects;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	 @Autowired
	 private JavaMailSender emailSender;
	 
	 @Autowired
	 private GridFsOperations gridOperations;
	 
	
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

	@RequestMapping(path = "", method = RequestMethod.POST)
	@Transactional
	public void create(@RequestBody User newUser) {
		newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
		if (newUser.getDateCreated() == 0) {
			newUser.setDateCreated(System.currentTimeMillis());
		}
		this.users.save(newUser);
	}
	
	@RequestMapping(path="/register", method=RequestMethod.POST)
	@Transactional
	public void registerUser(@RequestBody Map<String, Object> credentials) {
		User newUser = new User();
		Set<String> keySet = credentials.keySet();
		for(String key : keySet) {
			switch (key) {
			case "password":
				newUser.setPassword(passwordEncoder.encode((String)credentials.get(key)));
				break;
			case "username": 
				newUser.setUsername((String)credentials.get(key));
				break;
			case "email":
				newUser.setEmail((String)credentials.get(key));
			}
		}
		if(newUser.getRoles() == null) {
			newUser.setRoles(new ArrayList<String>());
			
		}
		newUser.getRoles().add("USER");
		newUser.setDateCreated(System.currentTimeMillis());
		this.users.save(newUser);
	}
	
	
	@RequestMapping(path="/getu/{username}/{password}", method=RequestMethod.GET)
	public String getUser(@PathVariable String username, @PathVariable String password) {
		User user = users.findByUsername(username);
		String result = null;
		if(BCrypt.checkpw(password, user.getPassword())) {
			result = user.getUsername() + ":::" + password + ":::" + user.getUserId();
		}
		else
		{
			throw new IllegalArgumentException();
		}
		return result;
		
	} 
	
	@RequestMapping(path="/{userId}/pass", method=RequestMethod.PUT)
	@PreAuthorize("hasAuthority('USER')")
	@Transactional
	public void changeUserPassword(@RequestBody Map<String, Object> credentials, @PathVariable int userId) {
		User user = users.getOne(userId);
		Set<String> keySet = credentials.keySet();
		for(String key : keySet) {
			if(key.equals("newPassword")) {
				user.setPassword(passwordEncoder.encode((String)credentials.get(key)));
			}
		}
		users.save(user);
	} 
	
	@RequestMapping(path="/{userId}/reset", method=RequestMethod.POST)
	@Transactional
	public void resetUserPassword(@PathVariable int userId) {
		User user = users.getOne(userId);
	    SimpleMailMessage message = new SimpleMailMessage(); 
        message.setTo(user.getEmail()); 
        message.setSubject("Password Reset"); 
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) 
              (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String newPass = buffer.toString();
        
        user.setPassword(passwordEncoder.encode(newPass));
        message.setText("New password: " + newPass);
        emailSender.send(message);
        users.save(user);
	}
	
	@RequestMapping(path = "/{userId}", method = RequestMethod.DELETE)
	@Transactional
	public void delete(@PathVariable int userId) {
		users.deleteById(userId);
	}

	@RequestMapping(path = "/{userId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public User retrieveUser(@PathVariable int userId) {
		return users.findById(userId).orElse(null);
	}

	@RequestMapping(path = "/retrieveAll", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody List<User> retrieveAll() {
		return users.findAll();
	}
	
	@RequestMapping(path = "/role/{userId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('ADMIN')")
	@Transactional
	public void updateUserRole(@PathVariable int userId, @RequestBody Map<String, Object> update) {
		User user = users.getOne(userId);
		if(update.size() == 1 && update.containsKey("roles")) {
			Map.Entry<String, Object> entry = update.entrySet().stream().findFirst().get();
//			if(user instanceof HibernateProxy) {
//			    user = (User) Hibernate.unproxy(user);
//			}
			user.setRoles((List<String>)entry.getValue());
			users.save(user);
		}
	}

	@RequestMapping(path = "/{userId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('ADMIN') || hasAuthority('User')")
	@Transactional
	public void updateUserPartial(@PathVariable int userId, @RequestBody Map<String, Object> updates) {
		User user =  users.getOne(userId);
		if(user instanceof HibernateProxy) {
		    user = (User) Hibernate.unproxy(user);
		}

		// updates
		for (String key : updates.keySet()) {

			Field field = null;
			try {
				field = user.getClass().getDeclaredField(key);
				field.setAccessible(true);
				field.set(user, updates.get(key));
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
		users.save(user);
	}

	@RequestMapping(path = "/{userId}", method = RequestMethod.PUT)
	@Transactional
	public void updateUserFull(@PathVariable int userId, @RequestBody User src) {
		User user = users.getOne(userId);
		user.copy(src);
		users.save(user);
	}

	@RequestMapping(path = "/{userId}/project/{project}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('USER')")
	@Transactional
	public void addUserToProject(@PathVariable Integer userId, @PathVariable Integer projectId) {
		User user = users.getOne(userId);
		Project proj = projects.retreiveProject(projectId);
		user.getProjects().add(proj);
		users.save(user);
	}

	
	@RequestMapping(path="/image/{userId}", method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('User')")
	public void addUserImage(@PathVariable int userId, HttpServletRequest req) {
		
		User user = users.getOne(userId);
		String imageName = user.getUsername();
		try {
			Part part = req.getPart("file");
			
			InputStream in = part.getInputStream();
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			IOUtils.copy(in, baos);
//			byte[] result =  baos.toByteArray();
//			int length = result.length;
//			Files.copy(in, new File("C:\\Users\\Christian Moore\\Desktop\\test.png").toPath());
			DBObject metaData = new BasicDBObject();
			metaData.put("userId", userId);
			
			gridFsTemplate.store(in, imageName, "image/png", metaData).toString();
			
		} catch (IOException | ServletException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(path="/getImage/{userId}", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('User')")
	public byte[] getUserImage(@PathVariable int userId) {
		User user = users.getOne(userId);
		String imageName = user.getUsername();
		GridFSFile gridFsdbFile = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(user.getUsername())));
		MongoConfig c = new MongoConfig();
		GridFsResource gridFsResource = new GridFsResource(gridFsdbFile, c.getGridFs().openDownloadStream(gridFsdbFile.getObjectId()) );
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtils.copy(gridFsResource.getInputStream(), baos);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] result =  baos.toByteArray();
		int length = result.length;
		return result;
		
	}

	@RequestMapping(path = "/searchByDateCreated/{timeStamp}", method = RequestMethod.GET)
	public List<User> findUsersByDateCreated(@PathVariable Long timeStamp) {
		return this.users.findByDateCreated(timeStamp);
	}

	@RequestMapping(path = "/searchByEmail/{email}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('ADMIN')")
	public List<User> findUsersByProjectName(@PathVariable String email) {
		return this.users.queryByProjectName(email);
	}
	
	

}
