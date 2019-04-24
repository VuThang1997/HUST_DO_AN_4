package edu.hust.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.enumData.AccountRole;
import edu.hust.enumData.AccountStatus;
import edu.hust.model.Account;
import edu.hust.model.ReportError;
import edu.hust.model.User;
import edu.hust.service.AccountService;
import edu.hust.utils.JsonMapUtil;
import edu.hust.utils.ValidationData;

@RestController
public class AccountController {

	private AccountService accountService;
	private ValidationData validationData;
	private JsonMapUtil jsonMapUtil;

	@Autowired
	public AccountController(@Qualifier("AccountServiceImpl1") AccountService accountService,
			@Qualifier("ValidationDataImpl1") ValidationData validationData,
			@Qualifier("JsonMapUtilImpl1") JsonMapUtil jsonMapUtil) {
		this.accountService = accountService;
		this.validationData = validationData;
		this.jsonMapUtil = jsonMapUtil;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> checkLogin(@RequestBody String infoLogin) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		String email = null;
		String password = null;
		String errorMessage = null;
		ReportError report = null;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(infoLogin, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "email", "password")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(10, "Login failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			email = jsonMap.get("email").toString();
			password = jsonMap.get("password").toString();
			Account account = this.accountService.findAccountByEmailAndPassword(email, password);
			if (account == null) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			// in the first login, student will be redirect to Update info page
			if (account.getRole() == AccountRole.STUDENT.getValue()) {
				if (account.getImei() == null || account.getImei().isBlank()) {
					account.setImei(null);
				}
			}

			return ResponseEntity.ok(account);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public ResponseEntity<?> registration(@RequestBody String registrationData) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		Account account = null;
		String errorMessage = null;
		String email = null;
		String username = null;
		String password = null;
		int role = -1;
		ReportError report;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(registrationData, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "email", "username", "role", "password")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(12, "Registration failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			email = jsonMap.get("email").toString();
			if (this.accountService.checkEmailIsUsed(email) != null) {
				report = new ReportError(13, "Registrantion failed because this email has already been used");
				return ResponseEntity.badRequest().body(report);
			}

			username = jsonMap.get("username").toString();
			password = jsonMap.get("password").toString();
			role = Integer.parseUnsignedInt(jsonMap.get("role").toString());

			account = new Account(username, password, role, email);
			account.setIsActive(AccountStatus.INACTIVE.getValue());
			account.setImei(null);
			account.setUserInfo(null);
			this.accountService.saveAccount(account);

			return new ResponseEntity<>("Registration success", HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/deactivateAccount", method = RequestMethod.PATCH)
	public ResponseEntity<?> disableAccount(@RequestBody String requestInfo) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		String errorMessage = null;
		String email = null;
		String password = null;
		int role = -1;
		ReportError report;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(requestInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "email", "role", "password")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(14, "Deactive account failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			email = jsonMap.get("email").toString();
			password = jsonMap.get("password").toString();
			role = Integer.parseInt(jsonMap.get("role").toString());
			if (this.accountService.deactivateAccount(email, password, role)) {
				return ResponseEntity.ok("Deactivate account successful!");
			}

			report = new ReportError(11, "Authentication has failed or has not yet been provided!");
			return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/activateAccount", method = RequestMethod.PATCH)
	public ResponseEntity<?> activateAccount(@RequestBody String requestInfo) {
		Map<String, Object> jsonMap = null;
		ObjectMapper objectMapper = null;
		String errorMessage = null;
		String email = null;
		String password = null;
		int role = -1;
		ReportError report;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(requestInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "email", "role", "password")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(15, "Active account failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			email = jsonMap.get("email").toString();
			password = jsonMap.get("password").toString();
			role = Integer.parseInt(jsonMap.get("role").toString());
			if (this.accountService.activateAccount(email, password, role)) {
				return ResponseEntity.ok("Activate account successful!");
			}

			report = new ReportError(11, "Authentication has failed or has not yet been provided!");
			return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/accounts", method = RequestMethod.GET)
	public ResponseEntity<?> getAccountInfo(@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "password", required = true) String password) {
		Map<String, Object> jsonMap = null;
		String errorMessage = null;
		ReportError report;

		try {
			jsonMap = new HashMap<>();
			jsonMap.put("email", email);
			jsonMap.put("password", password);

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(16, "Getting account info failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			Account account = this.accountService.findAccountByEmailAndPassword(email, password);
			if (account != null) {
				return ResponseEntity.ok(account);
			}

			report = new ReportError(11, "Authentication has failed or has not yet been provided!");
			return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/accounts", method = RequestMethod.PATCH)
	public ResponseEntity<?> updateAccountInfo(@RequestHeader(value = "email") String email,
			@RequestHeader(value = "password") String password,
			@RequestParam(value = "updateUser", required = true) boolean updateUser, @RequestBody String accountInfo) {
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Account account = null;
		String errorMessage = null;
		String newEmail = null;
		String newPassword = null;
		String newUsername = null;
		String newImei = null;
		ReportError report;
		User user = null;

		try {
			jsonMap = new HashMap<>();

			// check old info (provided by header) is valid
			jsonMap.put("email", email);
			jsonMap.put("password", password);
			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(17, "Updating account info failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			account = this.accountService.findAccountByEmailAndPassword(jsonMap.get("email").toString(),
					jsonMap.get("password").toString());
			if (account == null) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			// remove all old info.
			jsonMap.clear();

			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(accountInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (updateUser == false) {
				if (!this.jsonMapUtil.checkKeysExist(jsonMap, "email", "password", "username", "imei")) {
					report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
					return ResponseEntity.badRequest().body(report);
				}
			} else {
				if (!this.jsonMapUtil.checkKeysExist(jsonMap, "email", "password", "username", "imei", "birthday",
						"phone", "address", "fullName")) {
					report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
					return ResponseEntity.badRequest().body(report);
				}
			}

			// check new data is valid
			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(17, "Updating account info failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			newEmail = jsonMap.get("email").toString();
			newPassword = jsonMap.get("password").toString();
			newUsername = jsonMap.get("username").toString();
			newImei = jsonMap.get("imei").toString();

			if (updateUser == true) {
				LocalDate birthday = null;
				String address = null;
				String fullName = null;
				String phone = null;
			
				errorMessage = this.validationData.validateUserData(jsonMap);
				if (errorMessage != null) {
					report = new ReportError(20, "Adding user info failed because" + errorMessage);
					return ResponseEntity.badRequest().body(report);
				}
				
				fullName = jsonMap.get("fullName").toString();
				phone = jsonMap.get("phone").toString();
				birthday = LocalDate.parse(jsonMap.get("birthday").toString());
				address = jsonMap.get("address").toString();
				user = new User(address, fullName, birthday, phone);
			}

			account.setEmail(newEmail);
			account.setPassword(newPassword);
			account.setUsername(newUsername);
			account.setImei(newImei);

			this.accountService.updateAccountInfo(account);
			if (user != null) {
				user.setId(account.getId());
				this.accountService.addUserInfo(user);
			}
			return ResponseEntity.ok("Updating account info successes!");

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public ResponseEntity<?> addUserInfo(@RequestBody String userInfo) {
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		int id = -1;
		Account account = null;
		User user = null;
		String errorMessage = null;
		LocalDate birthday = null;
		String address = null;
		String fullName = null;
		String phone = null;
		ReportError report;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(userInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "id", "birthday", "phone", "address", "fullName")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateUserData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(20, "Adding user info failed because" + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			id = Integer.parseInt(jsonMap.get("id").toString());
			account = this.accountService.findAccountByID(id);
			if (account == null) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			String tmpUserInfo = account.getUserInfo();
			if (tmpUserInfo != null && !tmpUserInfo.isEmpty()) {
				report = new ReportError(21, "User's info cannot be overriden by this API !");
				return new ResponseEntity<>(report, HttpStatus.CONFLICT);
			}

			fullName = jsonMap.get("fullName").toString();
			phone = jsonMap.get("phone").toString();
			birthday = LocalDate.parse(jsonMap.get("birthday").toString());
			address = jsonMap.get("address").toString();
			user = new User(id, address, fullName, birthday, phone);

			this.accountService.addUserInfo(user);
			return ResponseEntity.ok("Add user info successful!");

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.PUT)
	public ResponseEntity<?> updateUserInfo(@RequestBody String userInfo) {
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		int id = -1;
		Account account = null;
		User user = null;
		String errorMessage = null;
		LocalDate birthday = null;
		String address = null;
		String fullName = null;
		String phone = null;
		ReportError report;

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(userInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "id", "birthday", "phone", "address", "fullName")) {
				report = new ReportError(1, "Json dynamic map lacks necessary key(s)!");
				return ResponseEntity.badRequest().body(report);
			}

			errorMessage = this.validationData.validateUserData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(22, "Updating user info failed because" + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			id = Integer.parseInt(jsonMap.get("id").toString());
			account = this.accountService.findAccountByID(id);
			if (account == null) {
				report = new ReportError(11, "Authentication has failed or has not yet been provided!");
				return new ResponseEntity<>(report, HttpStatus.UNAUTHORIZED);
			}

			fullName = jsonMap.get("fullName").toString();
			phone = jsonMap.get("phone").toString();
			birthday = LocalDate.parse(jsonMap.get("birthday").toString());
			address = jsonMap.get("address").toString();
			user = new User(id, address, fullName, birthday, phone);

			this.accountService.updateUserInfo(user);
			return ResponseEntity.ok("Update user info successful!");

		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public ResponseEntity<?> findUserInfo(@RequestHeader(value = "email", required = true) String email,
			@RequestHeader(value = "password", required = true) String password) {

		Map<String, Object> jsonMap = null;
		String errorMessage = null;
		ReportError report;

		try {
			jsonMap = new HashMap<>();
			jsonMap.put("email", email);
			jsonMap.put("password", password);

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				report = new ReportError(16, "Getting account info failed because " + errorMessage);
				return ResponseEntity.badRequest().body(report);
			}

			Account account = this.accountService.findAccountByEmailAndPassword(email, password);
			if (account != null) {
				String userInfo = account.getUserInfo();
				if (userInfo == null || userInfo.isBlank()) {
					report = new ReportError(23, "This user info has not existed yet!");
					return new ResponseEntity<>(report, HttpStatus.NOT_FOUND);
				}
			}

			return ResponseEntity.ok(account.getUserInfo());
		} catch (Exception e) {
			e.printStackTrace();
			report = new ReportError(2, "Error happened when jackson deserialization info!");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, report.toString());
		}
	}
}
