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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.hust.enumData.AccountRole;
import edu.hust.enumData.AccountStatus;
import edu.hust.model.Account;
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

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(infoLogin, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "Email", "Password")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01;\n Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 10;\n Content: Login failed because " + errorMessage);
			}

			email = jsonMap.get("Email").toString();
			password = jsonMap.get("Password").toString();
			Account account = this.accountService.findAccountByEmailAndPassword(email, password);
			if (account == null) {
				return new ResponseEntity<>(
						"Error code: 11;\nContent: Authentication has failed or has not yet been provided!",
						HttpStatus.UNAUTHORIZED);
			}

			// in the first login, student will be redirect to Update info page
			if (account.getRole() == AccountRole.STUDENT.getValue()) {
				if (account.getImei() == null || account.getImei().isEmpty()) {
					account.setImei("redirect");
				}
			}

			return ResponseEntity.ok(account);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
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

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(registrationData, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "Email", "Username", "Role", "Password")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 12;\n Content: Registration failed because " + errorMessage);
			}

			email = jsonMap.get("Email").toString();
			if (this.accountService.checkEmailIsUsed(email) != null) {
				return ResponseEntity.badRequest().body(
						"Error code: 13;\n Content: Registrantion failed because this email has already been used");
			}

			username = jsonMap.get("Username").toString();
			password = jsonMap.get("Password").toString();
			role = Integer.parseUnsignedInt(jsonMap.get("Role").toString());

			account = new Account(username, password, role, email);
			account.setIsActive(AccountStatus.INACTIVE.getValue());
			account.setImei(null);
			account.setUserInfo(null);
			this.accountService.saveAccount(account);

			return new ResponseEntity<>("Registration success", HttpStatus.CREATED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02; Content: Error happened when jackson deserialization info !");
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

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(requestInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "Email", "Role", "Password")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01;\n Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 14;\n Content: Deactive account failed because " + errorMessage);
			}

			email = jsonMap.get("Email").toString();
			password = jsonMap.get("Password").toString();
			role = Integer.parseInt(jsonMap.get("Role").toString());
			if (this.accountService.deactivateAccount(email, password, role)) {
				return ResponseEntity.ok("Deactivate account successful!");
			}

			return new ResponseEntity<>(
					"Error code: 11;\n Content: Authentication has failed or has not yet been provided!",
					HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02;\n Content: Error happened when jackson deserialization info !");
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

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(requestInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "Email", "Role", "Password")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01;\n Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 15;\n Content: Active account failed because " + errorMessage);
			}

			email = jsonMap.get("Email").toString();
			password = jsonMap.get("Password").toString();
			role = Integer.parseInt(jsonMap.get("Role").toString());
			if (this.accountService.activateAccount(email, password, role)) {
				return ResponseEntity.ok("Activate account successful!");
			}

			return new ResponseEntity<>(
					"Error code: 11;\n Content: Authentication has failed or has not yet been provided!",
					HttpStatus.UNAUTHORIZED);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02;\n Content: Error happened when jackson deserialization info !");
		}
	}

	@RequestMapping(value = "/accounts", method = RequestMethod.GET)
	public ResponseEntity<?> getAccountInfo(@RequestHeader(value = "Email", required = true) String email,
			@RequestHeader(value = "Password", required = true) String password) {
		Map<String, Object> jsonMap = null;
		String errorMessage = null;
		try {
			jsonMap = new HashMap<>();
			jsonMap.put("Email", email);
			jsonMap.put("Password", password);

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 16;\n Content: Getting account info failed because " + errorMessage);
			}

			Account account = this.accountService.findAccountByEmailAndPassword(email, password);
			if (account != null) {
				return ResponseEntity.ok(account);
			}

			return new ResponseEntity<>(
					"Error code: 11;\n Content: Authentication has failed or has not yet been provided!",
					HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 03;\n Content: Json map get error when putting element(s) !");
		}
	}

	@RequestMapping(value = "/accounts", method = RequestMethod.PATCH)
	public ResponseEntity<?> updateAccountInfo(@RequestHeader(value = "Email") String email,
			@RequestHeader(value = "Password") String password, @RequestBody String accountInfo) {
		ObjectMapper objectMapper = null;
		Map<String, Object> jsonMap = null;
		Account account = null;
		String errorMessage = null;
		String newEmail = null;
		String newPassword = null;
		String newUsername = null;
		String newImei = null;

		try {
			jsonMap = new HashMap<>();

			// check old info (provided by header) is valid
			jsonMap.put("Email", email);
			jsonMap.put("Password", password);
			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 17;\n Content: Updating account info failed because " + errorMessage);
			}

			account = this.accountService.findAccountByEmailAndPassword(jsonMap.get("Email").toString(),
					jsonMap.get("Password").toString());
			if (account == null) {
				return new ResponseEntity<>(
						"Error code: 11;\n Content: Authentication has failed or has not yet been provided!",
						HttpStatus.UNAUTHORIZED);
			}

			// remove all old info.
			jsonMap.clear();

			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(accountInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "Email", "Password", "Username", "Imei")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01;\n Content: Json dynamic map lacks necessary key(s)!");
			}

			// check new data is valid
			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 17;\n Content: Updating account info failed because " + errorMessage);
			}

			newEmail = jsonMap.get("Email").toString();
			newPassword = jsonMap.get("Password").toString();
			newUsername = jsonMap.get("Username").toString();
			newImei = jsonMap.get("Imei").toString();
			account.setEmail(newEmail);
			account.setPassword(newPassword);
			account.setUsername(newUsername);
			account.setImei(newImei);

			this.accountService.updateAccountInfo(account);
			return ResponseEntity.ok(account);

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02;\n Content: Error happened when jackson deserialization info");
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

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(userInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "ID", "Birthday", "Phone", "Address", "FullName")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateUserData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 20; Content: Adding user info failed because " + errorMessage);
			}

			id = Integer.parseInt(jsonMap.get("ID").toString());
			account = this.accountService.findAccountByID(id);
			if (account == null) {
				return new ResponseEntity<>(
						"Error code: 11; Content: Authentication has failed or has not yet been provided!",
						HttpStatus.UNAUTHORIZED);
			}

			String tmpUserInfo = account.getUserInfo();
			if (tmpUserInfo != null && !tmpUserInfo.isEmpty()) {
				return new ResponseEntity<>("Error code: 21; Content: User's info cannot be overriden by this API !",
						HttpStatus.CONFLICT);
			}

			fullName = jsonMap.get("FullName").toString();
			phone = jsonMap.get("Phone").toString();
			birthday = LocalDate.parse(jsonMap.get("Birthday").toString());
			address = jsonMap.get("Address").toString();
			user = new User(id, address, fullName, birthday, phone);

			this.accountService.addUserInfo(user);
			return ResponseEntity.ok("Add user info successful!");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02;\n Content: Error happened when jackson deserialization info");
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

		try {
			objectMapper = new ObjectMapper();
			jsonMap = objectMapper.readValue(userInfo, new TypeReference<Map<String, Object>>() {
			});

			// check request body has enough info in right JSON format
			if (!this.jsonMapUtil.checkKeysExist(jsonMap, "ID", "Birthday", "Phone", "Address", "FullName")) {
				return ResponseEntity.badRequest()
						.body("Error code: 01; Content: Json dynamic map lacks necessary key(s)!");
			}

			errorMessage = this.validationData.validateUserData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 22; Content: Update user info failed because " + errorMessage);
			}

			id = Integer.parseInt(jsonMap.get("ID").toString());
			account = this.accountService.findAccountByID(id);
			if (account == null) {
				return new ResponseEntity<>(
						"Error code: 11; Content: Authentication has failed or has not yet been provided!",
						HttpStatus.UNAUTHORIZED);
			}

			fullName = jsonMap.get("FullName").toString();
			phone = jsonMap.get("Phone").toString();
			birthday = LocalDate.parse(jsonMap.get("Birthday").toString());
			address = jsonMap.get("Address").toString();
			user = new User(id, address, fullName, birthday, phone);

			this.accountService.updateUserInfo(user);
			return ResponseEntity.ok("Update user info successful!");

		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02;\n Content: Error happened when jackson deserialization info");
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public ResponseEntity<?> findUserInfo(@RequestHeader(value = "Email", required = true) String email,
			@RequestHeader(value = "Password", required = true) String password) {

		Map<String, Object> jsonMap = null;
		String errorMessage = null;
		try {
			jsonMap = new HashMap<>();
			jsonMap.put("Email", email);
			jsonMap.put("Password", password);

			errorMessage = this.validationData.validateAccountData(jsonMap);
			if (errorMessage != null) {
				return ResponseEntity.badRequest()
						.body("Error code: 16; Content: Getting account info failed because " + errorMessage);
			}

			Account account = this.accountService.findAccountByEmailAndPassword(email, password);
			if (account != null) {
				String userInfo = account.getUserInfo();
				if (userInfo == null || userInfo.isBlank()) {
					return new ResponseEntity<>("Error code: 23; Content: This info has not existed yet!",
							HttpStatus.NOT_FOUND);
				}
			}

			return ResponseEntity.ok(account.getUserInfo());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Error code: 02;\n Content: Error happened when jackson deserialization info");
		}
	}
}
