package com.flowershop.login.controller;

import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.WebUtils;

import com.flowershop.login.domain.UserVo;
import com.flowershop.login.service.impl.LoginServiceImpl;

@Controller
public class LoginController {

	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	@Autowired
	LoginServiceImpl loginService;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginGET(@ModelAttribute("vo") UserVo userVo) throws Exception {
		return "login/login";
	}

	@RequestMapping(value = "/loginPost", method = RequestMethod.POST)
	public String loginPOST(@ModelAttribute("vn") UserVo userVo, HttpSession session, Model model,
			HttpServletResponse response) throws Exception {

		UserVo vo = loginService.login(userVo.getUser_id());

		if ((vo == null) || (vo != null && (passwordEncoder.matches(userVo.getUser_pw(), vo.getUser_pw()) == false))) {
			String msg = "아이디와 비밀번호가 올바르지 않습니다.";
			model.addAttribute("msg", msg);
			return "login/login";

		} else if (vo != null && (passwordEncoder.matches(userVo.getUser_pw(), vo.getUser_pw()) == true)) {
			model.addAttribute("userVo", vo);
			if (userVo.isUseCookie()) {
				int amount = 60 * 60 * 24 * 7;
				Date sessionLimit = new Date(System.currentTimeMillis() + (1000 * amount));
				loginService.keepLogin(vo.getUser_id(), session.getId(), sessionLimit);
			}
		}
		return "login/login";
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		Object obj = session.getAttribute("authUser");
		if (obj != null) {
			UserVo vo = (UserVo) obj;
			session.removeAttribute("authUser");
			session.invalidate();
			Cookie loginCookie = WebUtils.getCookie(request, "loginCookie");
			if (loginCookie != null) {
				loginCookie.setPath("/");
				loginCookie.setMaxAge(0);
				response.addCookie(loginCookie);
				 loginService.keepLogin(vo.getUser_id(), session.getId(), new Date());
			}
		}
		return "main/main";
	}
	
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String adminGET() throws Exception {
		return "login/admin";
	}
	
	@RequestMapping(value="/adminPost", method = RequestMethod.POST)
	public String admin(){
		return "main/main";
	}
	
	// @RequestMapping(value = "/kakaoLogin", produces = "application/json",
	// method = { RequestMethod.GET,
	// RequestMethod.POST })
	// public void kakaoLogin(@RequestParam("access_token") String access_token,
	// HttpServletRequest request, HttpServletResponse response, Model model)
	// throws Exception {
	// model.addAttribute("userVo", access_token);
	// }

}