package com.xxx.b2b;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@ComponentScan(basePackages = {"com.xxx"})
@EnableAutoConfiguration
@RestController
@EnableZuulProxy
public class B2BGatewayServerApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(B2BGatewayServerApplication.class, args);
    }
    
    @RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test(HttpServletRequest request, @RequestParam String token) {
    	return token;
    }
    
    @Configuration
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			//This is to disable csrf validation at server side (csrf value passed on header)
			http.csrf().disable();
			// @formatter:off
			http
				.httpBasic()
			.and()
				.logout()
			.and()
				.authorizeRequests()
					.antMatchers("/**").permitAll()
					.anyRequest().authenticated()
			.and().httpBasic().disable();
			http.headers().addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsMode.SAMEORIGIN));
			// @formatter:on
		}
	}
}
