package com.ppm.secureapi.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.ppm.secureapi.jwt.JwtUtils;
import com.ppm.secureapi.model.Role;
import com.ppm.secureapi.model.User;
import com.ppm.secureapi.payload.JwtResponse;
import com.ppm.secureapi.payload.LoginRequest;
import com.ppm.secureapi.payload.MessageResponse;
import com.ppm.secureapi.payload.SignupRequest;
import com.ppm.secureapi.repository.RoleRepository;
import com.ppm.secureapi.repository.UserRepository;
import com.ppm.secureapi.service.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	/*
	 * @Autowired private ApplicationEventPublisher eventPublisher;
	 * 
	 * @Autowired private MessageSource messages;
	 * 
	 * @Autowired private VerificationTokenRepository verificationTokenRepository;
	 */

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getFirstName(),
				userDetails.getLastName(), userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest, WebRequest request) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}
		
		if (!signUpRequest.getPassword().equals(signUpRequest.getMatchingPassword())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Password doesn't match!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getFirstName(), signUpRequest.getLastName(), signUpRequest.getUsername(),
				signUpRequest.getEmail(), encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName("ROLE_USER")
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role.toUpperCase()) {
				case "ADMIN":
					Role adminRole = roleRepository.findByName("ROLE_ADMIN")
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				default:
					Role userRole = roleRepository.findByName("ROLE_USER")
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

//		try {
//			String appUrl = request.getContextPath();
//			eventPublisher.publishEvent(new OnRegistrationSuccessEvent(user, request.getLocale(), appUrl));
//		} catch (Exception re) {
//			re.printStackTrace();
//		}

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

//	TODO fix mail service
	/* 
	 * @GetMapping("/confirmRegistration") public String
	 * confirmRegistration(WebRequest request, Model model, @RequestParam("token")
	 * String token) { Locale locale = request.getLocale(); VerificationToken
	 * verificationToken = verificationTokenRepository.findByToken(token); if
	 * (verificationToken == null) { String message =
	 * messages.getMessage("auth.message.invalidToken", null, locale);
	 * model.addAttribute("message", message); return "redirect:access-denied"; }
	 * User user = verificationToken.getUser(); Calendar calendar =
	 * Calendar.getInstance(); if ((verificationToken.getExpiryDate().getTime() -
	 * calendar.getTime().getTime()) <= 0) { String message =
	 * messages.getMessage("auth.message.expired", null, locale);
	 * model.addAttribute("message", message); return "redirect:access-denied"; }
	 * 
	 * user.setVerified(true); userRepository.saveAndFlush(user);
	 * verificationTokenRepository.delete(verificationToken); return "verified"; }
	 */
}
