package org.geektimes.projects.user.web.controller;

import org.geektimes.projects.user.JNDITest;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.service.UserServiceImpl;
import org.geektimes.web.mvc.annotation.Autowired;
import org.geektimes.web.mvc.annotation.Controller;
import org.geektimes.web.mvc.controller.RestController;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.sql.SQLException;

@Path("")
@Controller
public class UserController implements RestController {
    @Autowired
    UserServiceImpl service;

    @POST
    @Path("/signInSuccess")
    public void signInSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException, NamingException {
        System.out.println("请求来了");
        JNDITest jndiTest = new JNDITest();
        jndiTest.getDatasource();
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");
        User user = new User(System.currentTimeMillis(), name, password, email, phoneNumber);
//        UserService service = new UserServiceImpl();
        //注册
        boolean register = service.register(user);
        response.setCharacterEncoding("UTF-8");
        if (register) {
            response.getWriter().write("register success!!");
        } else {
            response.getWriter().write("register failed!!!");
        }

    }
}
