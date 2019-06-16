package moore.christian.thehub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import moore.christian.thehub.repositories.UserJpaRepository;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{

	@Autowired
	private UserJpaRepository users;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
//	
//	@Bean
//	PasswordEncoder getEncoder() {
//	    return new BCryptPasswordEncoder();
//	}
	
	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {
			
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				UserDetails details = users.findByUsername(username);
				if(details == null) {
					throw new UsernameNotFoundException(username);
				}
//				((User)details).setPassword("{noop}" + details.getPassword());
				return details;
			}
		};
	}
	
	@Override
	protected void configure(HttpSecurity http) {
		try {
			http
				.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/users/getu/**").permitAll()
				.antMatchers(HttpMethod.POST, "/users/register").permitAll()
				.antMatchers(HttpMethod.POST, "/users/**/reset").permitAll()
				.antMatchers(HttpMethod.PATCH, "/project/**").permitAll()
				.antMatchers(HttpMethod.GET, "/project/**").permitAll()
				.antMatchers(HttpMethod.POST,"/users/").permitAll()
				.antMatchers(HttpMethod.POST, "/project").hasAuthority("ADMIN")
				.antMatchers(HttpMethod.POST, "/project").hasAuthority("USER")
				.antMatchers(HttpMethod.GET, "/users/searchByProjectName/**").hasAuthority("ADMIN")
				.antMatchers(HttpMethod.PUT, "/users/{userId}/pass").hasAuthority("USER")
				.antMatchers(HttpMethod.PUT, "/projects/{projectId}/user/{userId}").hasAuthority("USER")
				.anyRequest().authenticated()
				.and()
				.httpBasic()
				.and()
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}
		catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
